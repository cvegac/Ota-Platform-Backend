package ele.embedded.business.platform.device;

import lombok.Data;

import java.util.UUID;

@Data
public class DeviceDTO {
  private UUID id;
  private String name;
  private String description;
  private UUID groupId;
}
