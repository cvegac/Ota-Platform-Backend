package ele.embedded.business.platform.download;

import ele.embedded.business.platform.device_type.DeviceTypeDTO;
import ele.embedded.business.platform.device_type.DeviceTypeEntity;
import ele.embedded.business.platform.project.ProjectEntity;
import ele.embedded.business.platform.project.dto.ProjectResponse;
import ele.embedded.core.BaseService;
import org.springframework.stereotype.Service;

@Service
public class DownloadService extends BaseService<DownloadEntity, DownloadDTO, DownloadDTO, DownloadRepository> {
    @Override
    protected DownloadDTO convertToDTO(DownloadEntity downloadEntity) {
        return modelMapper.map(downloadEntity, DownloadDTO.class);
    }

    @Override
    protected DownloadEntity convertToEntity(DownloadDTO dto) {
        DownloadEntity downloadEntity = modelMapper.map(dto, DownloadEntity.class);
        return downloadEntity;
    }
}
