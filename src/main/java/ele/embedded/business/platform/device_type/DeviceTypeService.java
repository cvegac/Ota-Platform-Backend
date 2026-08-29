package ele.embedded.business.platform.device_type;

import ele.embedded.business.platform.project.ProjectEntity;
import ele.embedded.business.platform.project.ProjectService;
import ele.embedded.business.platform.project.dto.ProjectResponse;
import ele.embedded.core.BaseService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class DeviceTypeService extends BaseService<DeviceTypeEntity, DeviceTypeDTO, DeviceTypeDTO, DeviceTypeRepository> {

  @Autowired
  private ModelMapper modelMapper;

  @Autowired
  private ProjectService projectService;

  @Override
  public DeviceTypeDTO create( DeviceTypeDTO deviceTypeDTO ) {
    validateEntitiesKeys(deviceTypeDTO);

    return super.create(deviceTypeDTO);
  }

  @Override
  public DeviceTypeDTO update( UUID id, DeviceTypeDTO deviceTypeDTO ) {
    this.getIfEntityExistById( id, "DeviceType");

    validateEntitiesKeys(deviceTypeDTO);

    return super.update(id, deviceTypeDTO);
  }

  @Override
  protected DeviceTypeDTO convertToDTO(DeviceTypeEntity deviceTypeEntity) {
    return modelMapper.map(deviceTypeEntity, DeviceTypeDTO.class);
  }

  @Override
  protected DeviceTypeEntity convertToEntity(DeviceTypeDTO dto) {
    DeviceTypeEntity deviceTypeEntity = modelMapper.map(dto, DeviceTypeEntity.class);
    ProjectResponse project = projectService.getById(dto.getProjectId());
    ProjectEntity projectEntity = new ProjectEntity(project.getId(), project.getName(), project.getDescription());
    deviceTypeEntity.setProjectEntity(projectEntity);
    return deviceTypeEntity;
  }

  private void validateEntitiesKeys(DeviceTypeDTO deviceTypeDTO) {
    UUID projectId = deviceTypeDTO.getProjectId();
    projectService.getIfEntityExistById( projectId, "Project");
  }
  public List<DeviceTypeEntity> getDeviceTypes(UUID projectId){
    return super.repository.findDeviceTypesByProjectId(projectId);
  }
}
