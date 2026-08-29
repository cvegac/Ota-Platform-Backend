package ele.embedded.business.platform.device;

import ele.embedded.business.platform.device_type.DeviceTypeEntity;
import ele.embedded.business.platform.device_type.DeviceTypeRepository;
import ele.embedded.core.BaseService;
import ele.embedded.exceptions.InconsistentDataException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;
@Service
public class DeviceService extends BaseService<DeviceEntity, DeviceDTO, DeviceDTO, DeviceRepository> {

  @Autowired
  private ModelMapper modelMapper;

  @Autowired
  private DeviceTypeRepository deviceTypeRepository;

  @Override
  public DeviceDTO create(DeviceDTO deviceDTO) {

    return super.create(deviceDTO);
  }

  @Override
  public DeviceDTO update(UUID id, DeviceDTO deviceDTO) {
    DeviceEntity device = this.getIfEntityExistById(id, "Device");

    if (!deviceDTO.getGroupId().equals(device.getDeviceTypeEntity().getId())){
      throw new InconsistentDataException("Inconsistent DeviceType");
    }

    return super.update(id, deviceDTO);
  }

  @Override
  protected DeviceDTO convertToDTO(DeviceEntity deviceEntity) {
    return modelMapper.map(deviceEntity, DeviceDTO.class);
  }

  @Override
  protected DeviceEntity convertToEntity(DeviceDTO dto) {
    DeviceEntity entity = modelMapper.map(dto, DeviceEntity.class);
    DeviceTypeEntity deviceTypeEntity = deviceTypeRepository.findById(dto.getGroupId()).orElseThrow( () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "This group dont exist"));
    entity.setDeviceTypeEntity(deviceTypeEntity);
    return entity;
  }

  public List<DeviceDTO> getDevices(UUID deviceTypeId){
    return this.repository.getDeviceByDeviceTypeEntityId(deviceTypeId).stream().map(this::convertToDTO).toList();
  }

}
