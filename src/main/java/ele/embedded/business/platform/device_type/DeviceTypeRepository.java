package ele.embedded.business.platform.device_type;

import ele.embedded.core.BaseRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface DeviceTypeRepository extends BaseRepository<DeviceTypeEntity> {

    @Query("SELECT dt FROM DeviceTypeEntity dt WHERE dt.projectEntity.id = :projectId")
    List<DeviceTypeEntity> findDeviceTypesByProjectId(UUID projectId);

}
