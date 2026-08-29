package ele.embedded.business.auth.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import ele.embedded.business.admin.user.RoleEnum;

import java.util.UUID;

@JsonPropertyOrder({"id", "name", "email", "username", "role"})
public record UserAuthResponse(
        UUID id,
        String name,
        String email,
        String username,
        RoleEnum role) {
}
