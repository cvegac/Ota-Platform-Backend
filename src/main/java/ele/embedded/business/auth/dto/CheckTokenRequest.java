package ele.embedded.business.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record CheckTokenRequest(@NotBlank String username) {

}
