package ele.embedded.business.platform;

import ele.embedded.business.aws.iot.dto.GroupVersionsResponse;
import ele.embedded.business.aws.iot.dto.JobHistoryResponse;
import ele.embedded.business.platform.device.DeviceDTO;
import ele.embedded.business.platform.device_type.DeviceTypeDTO;
import ele.embedded.business.platform.device_type.DeviceTypeEntity;
import ele.embedded.business.platform.download.DownloadDTO;
import ele.embedded.business.platform.project.ProjectEntity;
import ele.embedded.business.platform.project.dto.ProjectRequest;
import ele.embedded.business.platform.project.dto.ProjectResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("platform")
@CrossOrigin(originPatterns = "*")
public class PlatformController {

    private final PlatformService platformService;

    PlatformController(PlatformService platformService){
        this.platformService = platformService;
    }

    @GetMapping("project/{projectId}/group/{groupId}/devices")
    public List<DeviceDTO> getAllDevices(@PathVariable UUID groupId){
        return this.platformService.getDevices(groupId);
    }

    @PostMapping("project/{projectId}/group/{groupId}/device")
    public ResponseEntity<byte[]> createDevice(@RequestBody DeviceDTO deviceDTO) {
        try {
            byte[] zipBytes = this.platformService.createDevice(deviceDTO);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", deviceDTO.getName() + "_certificates.zip");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(zipBytes);

        } catch (IOException e) {
            return ResponseEntity.status(500).body("Error generando ZIP".getBytes());
        }
    }

    @GetMapping("projects/groups")
    public List<DeviceTypeEntity> getAllGroups(){
        return this.platformService.getAllDeviceTypes();
    }

    @GetMapping("project/{projectId}/groups")
    public List<DeviceTypeEntity> getGroups(@PathVariable UUID projectId){
        return this.platformService.getDeviceTypesByProject(projectId);
    }
    @PostMapping("project/{projectId}/group")
    public DeviceTypeDTO createGroup(@RequestBody DeviceTypeDTO deviceType){
        return this.platformService.createGroup(deviceType);
    }
    @GetMapping("/projects")
    public List<ProjectEntity> getProjects(){
        return this.platformService.getAllProjects();
    }

    @PostMapping("/project")
    public ProjectResponse createProject(@RequestBody ProjectRequest projectRequest){
        return this.platformService.createProject(projectRequest);
    }

    public record GeneratePresignedUrlResponse(String presignedUrl){}
    @PostMapping("/project/{projectId}/group/{groupId}/firmware/presigned-url")
    public GeneratePresignedUrlResponse generatePresignedUrl(
            @PathVariable UUID projectId,
            @PathVariable UUID groupId
    ){
      return new GeneratePresignedUrlResponse(this.platformService.generatePresignedUr(projectId, groupId));
    }

    @PostMapping("/project/{projectId}/group/{groupId}/device/{deviceId}/firmware/presigned-url")
    public GeneratePresignedUrlResponse generatePresignedUrl(
            @PathVariable UUID projectId,
            @PathVariable UUID groupId,
            @PathVariable UUID deviceId
    ){
        return new GeneratePresignedUrlResponse(this.platformService.generatePresignedUr(projectId, groupId, deviceId));
    }

    public record UpdateJobRequest(String versionId){

    }

    @PostMapping("/project/{projectId}/group/{groupId}/firmware/job/update")
    public void lauchUpdateJob(
            @PathVariable UUID projectId,
            @PathVariable UUID groupId,
            @RequestBody UpdateJobRequest updateJobRequest
    ){
        this.platformService.lauchUpdateJob(projectId, groupId, updateJobRequest.versionId);
    }

    @PostMapping("/project/{projectId}/group/{groupId}/device/{deviceId}/firmware/job/update")
    public void lauchUpdateJob(
            @PathVariable UUID projectId,
            @PathVariable UUID groupId,
            @PathVariable UUID deviceId,
            @RequestBody UpdateJobRequest updateJobRequest
    ){
        this.platformService.lauchUpdateJob(projectId, groupId, deviceId, updateJobRequest.versionId);
    }

    @GetMapping("/project/{projectId}/group/{groupId}/firmware/versions")
    public GroupVersionsResponse getFirmwareVersions(
            @PathVariable UUID projectId,
            @PathVariable UUID groupId
    ){
        return this.platformService.getFirmwareVersions(projectId, groupId);
    }

    @GetMapping("/project/{projectId}/group/{groupId}/device/{deviceId}/firmware/versions")
    public GroupVersionsResponse getFirmwareVersions(
            @PathVariable UUID projectId,
            @PathVariable UUID groupId,
            @PathVariable UUID deviceId
    ){
        return this.platformService.getFirmwareVersions(projectId, groupId, deviceId);
    }
    @GetMapping("/project/{projectId}/group/{groupId}/device/{deviceId}/downloads")
    public List<DownloadDTO> getDownloads(
            @PathVariable UUID projectId,
            @PathVariable UUID groupId,
            @PathVariable UUID deviceId
    ){
        return this.platformService.getDownloads(projectId, groupId, deviceId);
    }

    @GetMapping("/project/{projectId}/group/{groupId}/device/{deviceId}/jobs")
    public JobHistoryResponse getJobHistory(
            @PathVariable UUID projectId,
            @PathVariable UUID groupId,
            @PathVariable UUID deviceId,
            @RequestParam(required = false) Integer maxResults,
            @RequestParam(required = false) String nextToken
    ){
        return this.platformService.getJobHistory(projectId, groupId, deviceId, maxResults, nextToken);
    }

}
