package ele.embedded.business.admin.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import ele.embedded.core.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import org.hibernate.validator.constraints.Length;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Table(name = "user", schema = "admin_users")
public class UserEntity extends BaseEntity {
  @NotNull
  @Length(max = 40)
  private String name;

  @NotNull
  @Length(max = 40)
  @Column(unique = true)
  private String username;

  @NotNull(message = "Missing email field")
  @Length(max = 40)
  @Column(unique = true)
  private String email;

  @JsonIgnore
  @NotNull
  private String password;

  @JsonIgnore
  @Column(name = "is_enabled")
  private boolean isEnabled;

  @JsonIgnore
  @Column(name = "account_No_Expired")
  private boolean accountNoExpired;

  @JsonIgnore
  @Column(name = "account_No_Locked")
  private boolean accountNoLocked;

  @JsonIgnore
  @Column(name = "credential_No_Expired")
  private boolean credentialNoExpired;

  @Enumerated(EnumType.STRING)
  @Column(nullable = true)
  private RoleEnum role;

  public void setPassword(String password) {
    this.password = new BCryptPasswordEncoder().encode(password);
  }
}
