package ele.embedded.business.admin.user;

import ele.embedded.business.admin.user.dto.UserRequest;
import ele.embedded.business.admin.user.dto.UserResponse;
import ele.embedded.core.BaseService;
import ele.embedded.exceptions.InconsistentDataException;
import ele.embedded.exceptions.ResourceNotFoundException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserService extends BaseService<UserEntity, UserRequest, UserResponse, UserRepository> {

  @Autowired
  private ModelMapper modelMapper;

  @Override
  @jakarta.transaction.Transactional
  public UserResponse create(UserRequest requestDto) {
    validateUserUniqueFields(requestDto.getUsername(), requestDto.getEmail());
    UserEntity user = convertToEntity(requestDto);
    this.add(user);

    return convertToDTO(user);
  }

  public void validateUserUniqueFields(String username, String email) {
    if (repository.findUserEntityByUsername(username).isPresent()) {
      throw new InconsistentDataException("Username already exists: " + username);
    }
    // Use findAllByEmail to avoid JPA exception when duplicate rows exist in the DB
    if (!repository.findAllByEmail(email).isEmpty()) {
      throw new InconsistentDataException("Email already exists: " + email);
    }
  }

  @Override
  protected UserResponse convertToDTO(UserEntity userEntity) {
    modelMapper.getConfiguration().setAmbiguityIgnored(true);

    return modelMapper.map(userEntity, UserResponse.class);
  }

  @Override
  protected UserEntity convertToEntity(UserRequest dto) {
    UserEntity userMapped = modelMapper.map(dto, UserEntity.class);
    userMapped.setAccountNoExpired(true);
    userMapped.setEnabled(true);
    userMapped.setAccountNoLocked(true);
    userMapped.setCredentialNoExpired(true);

    return userMapped;
  }

  @Override
  public UserResponse update(UUID id, UserRequest requestDto) {
    verifyIdentityAndExist(id);
    return super.update(id, requestDto);
  }


  @Transactional(readOnly = true)
  public void verifyIdentity(UserEntity user){
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    if(!user.getUsername().equals(authentication.getName())){
      throw new InconsistentDataException("Inconsistent Data");
    }
  }

  @Transactional(readOnly = true)
  public void verifyIdentityAndExist(UUID userId){
    UserEntity user = getIfEntityExistById(userId, "User");
    verifyIdentity(user);
  }

  @Transactional(readOnly = true)
  public UserEntity getUserByUsername(String username){
    return repository.findUserEntityByUsername(username)
            .orElseThrow(() -> new ResourceNotFoundException("Entity not found with username: " + username));
  }

  @Transactional(readOnly = true)
  public UserEntity getUserByEmail(String email){
    List<UserEntity> results = repository.findAllByEmail(email);
    if (results.isEmpty()) {
      throw new ResourceNotFoundException("Entity not found with email: " + email);
    }
    if (results.size() > 1) {
      throw new InconsistentDataException("Multiple accounts found with email: " + email + ". Please contact support.");
    }
    return results.get(0);
  }
}
