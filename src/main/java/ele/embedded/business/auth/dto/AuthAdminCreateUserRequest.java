package ele.embedded.business.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record AuthAdminCreateUserRequest(
        @NotBlank
        String name,

        @NotBlank
        String email,

        @NotBlank
        String username,

        @NotBlank
        String password,

        @NotNull
        UUID projectId) {
}
