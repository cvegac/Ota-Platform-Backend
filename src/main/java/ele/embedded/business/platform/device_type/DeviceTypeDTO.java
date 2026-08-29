package ele.embedded.business.platform.device_type;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class DeviceTypeDTO{
  private UUID id;
  private String description;
  @NotNull
  private String name;
  @NotNull
  private UUID projectId;
}
