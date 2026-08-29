package ele.embedded.business.platform.download;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;
@Data
public class DownloadDTO {
    private UUID id;
    private LocalDateTime date;
    @NotNull
    private String version;
    @NotNull
    private UUID deviceId;
}
