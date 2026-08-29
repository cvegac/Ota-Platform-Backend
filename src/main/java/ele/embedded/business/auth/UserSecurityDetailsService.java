package ele.embedded.business.auth;

import com.auth0.jwt.interfaces.DecodedJWT;
import ele.embedded.business.admin.user.RoleEnum;
import ele.embedded.business.admin.user.UserEntity;
import ele.embedded.business.admin.user.UserService;
import ele.embedded.business.auth.dto.AuthCreateUserRequest;
import ele.embedded.business.auth.dto.AuthLoginRequest;
import ele.embedded.business.admin.user_project.UserProjectEntity;
import ele.embedded.business.admin.user_project.UserProjectService;
import ele.embedded.business.auth.dto.*;
import ele.embedded.util.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserSecurityDetailsService implements UserDetailsService {

  @Autowired
  private UserService userService;

  @Autowired
  private PasswordEncoder passwordEncoder;

  @Autowired
  private JwtUtils jwtUtils;

  @Autowired
  private UserProjectService userProjectService;



  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

    UserEntity userEntity = userService.getUserByUsername(username);

    List<SimpleGrantedAuthority> authorityList = new ArrayList<>();
    authorityList.add(new SimpleGrantedAuthority("ROLE_" + userEntity.getRole().name()));

    return new User(
            userEntity.getUsername(),
            userEntity.getPassword(),
            userEntity.isEnabled(),
            userEntity.isAccountNoExpired(),
            userEntity.isCredentialNoExpired(),
            userEntity.isAccountNoLocked(),
            authorityList
    );
  }

  public AuthResponse createUser(AuthCreateUserRequest createRoleRequest) {

    String name = createRoleRequest.name();
    String email = createRoleRequest.email();
    String username = createRoleRequest.username();
    String password = createRoleRequest.password();

    userService.validateUserUniqueFields(username, email);

    UserEntity userEntity = UserEntity.builder()
            .name(name)
            .username(username)
            .email(email)
            .password(passwordEncoder.encode(password))
            .role(RoleEnum.ADMIN)
            .isEnabled(true)
            .accountNoExpired(true)
            .accountNoLocked(true)
            .credentialNoExpired(true)
            .build();

    userService.add(userEntity);


    List<SimpleGrantedAuthority> authorities = new ArrayList<>();
    authorities.add(new SimpleGrantedAuthority("ROLE_" + userEntity.getRole().name()));

    Authentication authentication = new UsernamePasswordAuthenticationToken(userEntity, null, authorities);

    String accessToken = jwtUtils.createToken(authentication);

    userEntity = userService.getUserByUsername(username);

    UserAuthResponse userAuthResponse = new UserAuthResponse(
            userEntity.getId(),
            userEntity.getName(),
            userEntity.getEmail(),
            userEntity.getUsername(),
            userEntity.getRole());

    return new AuthResponse(userAuthResponse, "User created successfully", accessToken, true);
  }

  public AuthResponse createCollaborator(AuthAdminCreateUserRequest createRequest) {
    String name = createRequest.name();
    String email = createRequest.email();
    String username = createRequest.username();
    String password = createRequest.password();
    java.util.UUID projectId = createRequest.projectId();

    userService.validateUserUniqueFields(username, email);

    UserEntity userEntity = UserEntity.builder()
            .name(name)
            .username(username)
            .email(email)
            .password(passwordEncoder.encode(password))
            .role(RoleEnum.USER)
            .isEnabled(true)
            .accountNoExpired(true)
            .accountNoLocked(true)
            .credentialNoExpired(true)
            .build();

    userService.add(userEntity);
    
    ele.embedded.business.platform.project.ProjectEntity project = new ele.embedded.business.platform.project.ProjectEntity();
    project.setId(projectId);
    
    UserProjectEntity userProjectEntity = new UserProjectEntity(userEntity, project);
    userProjectService.add(userProjectEntity);

    UserAuthResponse userAuthResponse = new UserAuthResponse(
            userEntity.getId(),
            userEntity.getName(),
            userEntity.getEmail(),
            userEntity.getUsername(),
            userEntity.getRole());

    return new AuthResponse(userAuthResponse, "Collaborator created and assigned to project", null, true);
  }


  public AuthResponse loginUser(AuthLoginRequest authLoginRequest) {

    String username = authLoginRequest.username();
    String password = authLoginRequest.password();

    Authentication authentication = this.authenticate(username, password);
    SecurityContextHolder.getContext().setAuthentication(authentication);

    String accessToken = jwtUtils.createToken(authentication);

    UserEntity userEntity = userService.getUserByUsername(username);

    UserAuthResponse userAuthResponse = new UserAuthResponse(
            userEntity.getId(),
            userEntity.getName(),
            userEntity.getEmail(),
            userEntity.getUsername(),
            userEntity.getRole());

    return new AuthResponse(userAuthResponse, "User logged successfully", accessToken, true);
  }


  public AuthResponse checkTokenUser(HttpServletRequest request) {
    String jwtToken = request.getHeader(HttpHeaders.AUTHORIZATION);
    String username = "";

    if (jwtToken != null){
      jwtToken = jwtToken.substring(7);

      DecodedJWT decodedJWT = jwtUtils.validateToken(jwtToken);

      username = jwtUtils.extractUsername(decodedJWT);

      UserEntity userEntity = userService.getUserByUsername(username);

      UserAuthResponse userAuthResponse = new UserAuthResponse(
              userEntity.getId(),
              userEntity.getName(),
              userEntity.getEmail(),
              userEntity.getUsername(),
              userEntity.getRole());

      return new AuthResponse(userAuthResponse, "User validate successfully", jwtToken, true);
    }

    return new AuthResponse(null, "Invalid token", null, false);
  }


  public Authentication authenticate(String username, String password) {
    UserDetails userDetails = this.loadUserByUsername(username);

    if (userDetails == null) {
      throw new BadCredentialsException("Invalid username or password");
    }

    if (!passwordEncoder.matches(password, userDetails.getPassword())) {
      throw new BadCredentialsException("Incorrect Password");
    }

    return new UsernamePasswordAuthenticationToken(username, password, userDetails.getAuthorities());
  }
}
