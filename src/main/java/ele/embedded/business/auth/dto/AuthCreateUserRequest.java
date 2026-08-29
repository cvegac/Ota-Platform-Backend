package ele.embedded.business.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record AuthCreateUserRequest(
        @NotBlank
        String name,

        @NotBlank
        String email,

        @NotBlank
        String username,

        @NotBlank
        String password) {
}