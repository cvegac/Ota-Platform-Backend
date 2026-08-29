package ele.embedded.business.auth.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({"username", "message", "status", "jwt"})
public record AuthResponse(
        UserAuthResponse user,
        String message,
        String jwt,
        Boolean status) {
}
