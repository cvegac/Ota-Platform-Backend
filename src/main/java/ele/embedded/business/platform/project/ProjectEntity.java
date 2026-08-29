package ele.embedded.business.platform.project;

import ele.embedded.core.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import java.util.UUID;

@Entity
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@Table(name = "project", schema = "platform_project")
public class ProjectEntity extends BaseEntity {

  @NotNull
  @Length(max = 100)
  private String name;
  @Length(max = 255)
  private String description;

  public ProjectEntity(UUID id, String name, String description){
    super.setId(id);
    this.name = name;
    this.description = description;
  }
}
