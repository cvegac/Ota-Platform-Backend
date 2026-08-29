package ele.embedded.business.auth;

import ele.embedded.business.auth.dto.AuthAdminCreateUserRequest;
import ele.embedded.business.auth.dto.AuthCreateUserRequest;
import ele.embedded.business.auth.dto.AuthLoginRequest;
import ele.embedded.business.auth.dto.AuthResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*")
public class AuthenticationController {

  @Autowired
  private UserSecurityDetailsService userSecurityDetailsService;

  @PostMapping("/sign-up")
  @ResponseStatus(HttpStatus.CREATED)
  public AuthResponse register(@RequestBody @Valid AuthCreateUserRequest userRequest){
    return this.userSecurityDetailsService.createUser(userRequest);
  }

  @PostMapping("/create-collaborator")
  @ResponseStatus(HttpStatus.CREATED)
  @PreAuthorize("hasRole('ADMIN')")
  public AuthResponse createCollaborator(@RequestBody @Valid AuthAdminCreateUserRequest userRequest){
    return this.userSecurityDetailsService.createCollaborator(userRequest);
  }

  @PostMapping("/log-in")
  @ResponseStatus(HttpStatus.OK)
  public AuthResponse login(@RequestBody @Valid AuthLoginRequest userRequest){
    return this.userSecurityDetailsService.loginUser(userRequest);
  }

  @GetMapping("/check-token")
  @ResponseStatus(HttpStatus.OK)
  public AuthResponse checkTokenUser(HttpServletRequest request){
    return this.userSecurityDetailsService.checkTokenUser(request);
  }
}