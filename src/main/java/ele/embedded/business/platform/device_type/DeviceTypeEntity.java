package ele.embedded.business.platform.device_type;

import ele.embedded.business.platform.project.ProjectEntity;
import ele.embedded.core.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.validator.constraints.Length;

@Entity
@Data
@EqualsAndHashCode(callSuper = true)
@Table(name = "device_type", schema = "platform_project")
public class DeviceTypeEntity extends BaseEntity {

  @Length(max = 255)
  private String description;

  @NotNull
  @ManyToOne(fetch = FetchType.EAGER, optional = false)
  @JoinColumn(name = "project_id", nullable = false)
  @OnDelete(action = OnDeleteAction.CASCADE)
  private ProjectEntity projectEntity;

  @NotNull
  @Length(max = 100)
  private String name;
}
