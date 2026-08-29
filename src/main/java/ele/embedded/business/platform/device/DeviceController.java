package ele.embedded.business.platform.device;

import ele.embedded.core.BaseController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/devices")
public class DeviceController extends BaseController<DeviceEntity, DeviceRepository, DeviceDTO, DeviceDTO, DeviceService> {
}
