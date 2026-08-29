package ele.embedded.business.platform.project.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class ProjectResponse {
  private UUID id;
  private String name;
  private String description;
}
