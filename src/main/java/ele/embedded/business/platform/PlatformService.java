package ele.embedded.business.platform;

import ele.embedded.business.admin.user.RoleEnum;
import ele.embedded.business.admin.user.UserEntity;
import ele.embedded.business.admin.user.UserService;
import ele.embedded.business.aws.iot.IotService;
import ele.embedded.business.aws.iot.dto.GroupVersionsResponse;
import ele.embedded.business.aws.iot.dto.JobHistoryResponse;
import ele.embedded.business.aws.lambda.LambdaController;
import ele.embedded.business.aws.lambda.LambdaService;
import ele.embedded.business.aws.lambda.TargetType;
import ele.embedded.business.aws.s3.S3Service;
import ele.embedded.business.platform.device.DeviceDTO;
import ele.embedded.business.platform.device.DeviceService;
import ele.embedded.business.platform.device_type.DeviceTypeDTO;
import ele.embedded.business.platform.device_type.DeviceTypeEntity;
import ele.embedded.business.platform.device_type.DeviceTypeService;
import ele.embedded.business.platform.download.DownloadDTO;
import ele.embedded.business.platform.download.DownloadService;
import ele.embedded.business.platform.project.ProjectEntity;
import ele.embedded.business.platform.project.ProjectService;
import ele.embedded.business.platform.project.dto.ProjectRequest;
import ele.embedded.business.platform.project.dto.ProjectResponse;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class PlatformService {

    private final ProjectService projectService;
    private final DeviceTypeService deviceTypeService;
    private final DeviceService deviceService;
    private final UserService userService;
    private final IotService iotService;
    private final S3Service s3Service;
    private final LambdaService lambdaService;
    private final DownloadService downloadService;

    @Autowired
    public PlatformService(ProjectService projectService, DeviceTypeService deviceTypeService, DeviceService deviceService, UserService userService, IotService iotService, S3Service s3Service, LambdaService lambdaService, DownloadService downloadService){
        this.projectService = projectService;
        this.deviceTypeService = deviceTypeService;
        this.deviceService = deviceService;
        this.userService = userService;
        this.iotService = iotService;
        this.s3Service = s3Service;
        this.lambdaService = lambdaService;
        this.downloadService = downloadService;
    }

    private UserEntity  getUser(){
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userService.getUserByUsername(((String) principal));
    }

    // AWS IoT and S3 only allow [a-zA-Z0-9:_-]+ in resource names
    private String sanitizeForIot(String name) {
        return name.replaceAll("[^a-zA-Z0-9:_\\-]", "_");
    }
    public ProjectResponse createProject(ProjectRequest projectRequest){
        UserEntity user = this.getUser();
        if (user.getRole() != RoleEnum.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only administrators can create projects");
        }
        projectRequest.setUserId(user.getId());
        return this.projectService.create(projectRequest);
    }
    @Transactional
    public DeviceTypeDTO createGroup(DeviceTypeDTO deviceTypeDTO){
        UserEntity user = this.getUser();
        if (user.getRole() != RoleEnum.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only administrators can create groups");
        }
        boolean projectExist = this.getAllProjects().stream().anyMatch(project -> deviceTypeDTO.getProjectId().equals(project.getId()));
        if(!projectExist) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This project don't exist or you don't have access");
        DeviceTypeDTO response = this.deviceTypeService.create(deviceTypeDTO);
        // AWS IoT only allows [a-zA-Z0-9:_-]+ — replace invalid characters (e.g. spaces) with underscores
        this.iotService.createThingGroup(sanitizeForIot(response.getName()));
        return response;
    }

    public List<ProjectEntity> getAllProjects(){

        return this.projectService.getProjects(getUser().getId());
    }
    public List<DeviceTypeEntity> getAllDeviceTypes(){

        List<ProjectEntity> projects = this.projectService.getProjects(this.getUser().getId());
        return projects.stream()
                        .flatMap(project ->
                                this.deviceTypeService.getDeviceTypes(project.getId()).stream())
                        .toList();
    }

    public List<DeviceTypeEntity> getDeviceTypesByProject(UUID projectId){
        List<ProjectEntity> projects = this.projectService.getProjects(this.getUser().getId());
        if(projects.stream().noneMatch(project -> project.getId().equals(projectId)))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This project don't exist");
        return deviceTypeService.getDeviceTypes(projectId);
    }
    @Transactional
    public  byte[] createDevice(DeviceDTO deviceDTO) throws IOException {
        UserEntity user = this.getUser();
        if (user.getRole() != RoleEnum.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only administrators can create devices");
        }
        List<ProjectEntity> projects = this.projectService.getProjects(user.getId());
        DeviceTypeDTO deviceType = this.deviceTypeService.getById(deviceDTO.getGroupId());
        if(projects.stream().noneMatch(project -> project.getId().equals(deviceType.getProjectId())))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This group don't exist or you don't have access");
        this.deviceService.create(deviceDTO);
        return iotService.createThingWithCertificateAndGetZip(
                sanitizeForIot(deviceDTO.getName()),
                sanitizeForIot(deviceType.getName()));
    }

    public List<DeviceDTO> getDevices(UUID groupId) {
        List<ProjectEntity> projects = this.projectService.getProjects(this.getUser().getId());
        DeviceTypeDTO deviceType = this.deviceTypeService.getById(groupId);
        if (projects.stream().noneMatch(project -> project.getId().equals(deviceType.getProjectId())))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This group don't exist");
        return this.deviceService.getDevices(groupId);
    }

    public String generatePresignedUr(UUID projectId, UUID groupId){
        DeviceTypeDTO deviceTypeDTO = deviceTypeService.getById(groupId);
        String filename = String.format("group_%s_SigningProfile.bin", sanitizeForIot(deviceTypeDTO.getName()));
        return this.s3Service.generatePresignedUrl(filename);
    }

    public String generatePresignedUr(UUID projectId, UUID groupId, UUID deviceId){
        DeviceTypeDTO deviceTypeDTO = deviceTypeService.getById(groupId);
        DeviceDTO device = deviceService.getById(deviceId);
        String filename = String.format("%s_SigningProfile.bin",device.getName());
        return this.s3Service.generatePresignedUrl(filename);
    }
    public void lauchUpdateJob(UUID projectId, UUID groupId, String versionId){
        DeviceTypeDTO deviceTypeDTO = deviceTypeService.getById(groupId);
        LambdaController.JobRequest jobRequest = new LambdaController.JobRequest(
                sanitizeForIot(deviceTypeDTO.getName()), versionId, 1000000, TargetType.GROUP);
        if(this.lambdaService.newJobControlled(jobRequest)){
            DownloadDTO download = new DownloadDTO();
            this.downloadService.create(download);
        }
    }
    public void lauchUpdateJob(UUID projectId, UUID groupId, UUID deviceId,  String versionId){
        DeviceDTO device = deviceService.getById(deviceId);
        LambdaController.JobRequest jobRequest = new LambdaController.JobRequest(device.getName(), versionId, 1000000, TargetType.THING);
        if(this.lambdaService.newJobControlled(jobRequest)){
            DownloadDTO download = new DownloadDTO();
            this.downloadService.create(download);
        }
    }
    public GroupVersionsResponse getFirmwareVersions(UUID projectId, UUID groupId){
        DeviceTypeDTO deviceTypeDTO = deviceTypeService.getById(groupId);
        return this.s3Service.getGroupVersions(sanitizeForIot(deviceTypeDTO.getName()));
    }
    public GroupVersionsResponse getFirmwareVersions(UUID projectId, UUID groupId, UUID deviceId){
        DeviceDTO device = deviceService.getById(deviceId);
        return this.s3Service.getDeviceVersions(device.getName());
    }
    public List<DownloadDTO> getDownloads(UUID projectId, UUID groupId, UUID deviceId){
        return downloadService.getAll().stream().filter(download -> download.getDeviceId().equals(deviceId)).toList();
    }

    public JobHistoryResponse getJobHistory(UUID projectId, UUID groupId, UUID deviceId, Integer maxResults, String nextToken) {
        DeviceDTO device = deviceService.getById(deviceId);
        return iotService.getJobExecutionsForThing(device.getName(), maxResults, nextToken);
    }
}
