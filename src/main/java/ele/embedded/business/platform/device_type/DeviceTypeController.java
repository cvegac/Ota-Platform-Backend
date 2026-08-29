package ele.embedded.business.platform.device_type;

import ele.embedded.core.BaseController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/device-types")
public class DeviceTypeController extends BaseController<
        DeviceTypeEntity,
        DeviceTypeRepository,
        DeviceTypeDTO,
        DeviceTypeDTO,
        DeviceTypeService
        > {
}
