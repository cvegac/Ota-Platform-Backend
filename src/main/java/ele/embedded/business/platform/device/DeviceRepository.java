package ele.embedded.business.platform.device;

import ele.embedded.core.BaseRepository;

import java.util.List;
import java.util.UUID;

public interface DeviceRepository extends BaseRepository<DeviceEntity> {
    List<DeviceEntity> getDeviceByDeviceTypeEntityId(UUID deviceTypeId);
}
