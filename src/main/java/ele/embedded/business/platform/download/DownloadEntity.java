package ele.embedded.business.platform.download;

import ele.embedded.business.platform.device.DeviceEntity;
import ele.embedded.business.platform.project.ProjectEntity;
import ele.embedded.core.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDateTime;

@Entity
@Data
@EqualsAndHashCode(callSuper = true)
@Table(name = "download", schema = "platform_project")
public class DownloadEntity extends BaseEntity {

    @NotNull
    @Length(max = 40)
    private String version;

    @NotNull
    private LocalDateTime date;

    @NotNull
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "device_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private DeviceEntity deviceEntity;


}
