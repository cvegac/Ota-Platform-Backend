package ele.embedded.business.platform.project.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class ProjectRequest {
        private UUID id;
        @NotNull
        private String name;
        private String description;
        private UUID userId;
}
