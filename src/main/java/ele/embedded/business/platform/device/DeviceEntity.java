package ele.embedded.business.platform.device;

import ele.embedded.business.platform.device_type.DeviceTypeEntity;
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
@Table(name = "device", schema = "platform_project")
public class DeviceEntity extends BaseEntity {

  @NotNull
  @Length(max = 100)
  private String name;
  @Length(max = 255)
  private String description;

  @ManyToOne(fetch = FetchType.EAGER, optional = false)
  @JoinColumn(name = "device_type_id", nullable = false)
  @OnDelete(action = OnDeleteAction.CASCADE)
  private DeviceTypeEntity deviceTypeEntity;

}
