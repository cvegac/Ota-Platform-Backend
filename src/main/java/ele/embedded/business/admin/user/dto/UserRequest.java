package ele.embedded.business.admin.user.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.util.UUID;

@Data
public class UserRequest{
        private UUID id;
        private String name;
        private String email;
        private String username;
        private String password;

        @JsonIgnore
        private boolean isEnabled;

        @JsonIgnore
        private boolean accountNoExpired;

        @JsonIgnore
        private boolean accountNoLocked;

        @JsonIgnore
        private boolean credentialNoExpired;
}
