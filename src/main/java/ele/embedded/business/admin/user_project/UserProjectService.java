package ele.embedded.business.admin.user_project;

import ele.embedded.business.admin.user.UserService;
import ele.embedded.business.platform.project.ProjectService;

import ele.embedded.core.BaseService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UserProjectService extends BaseService<UserProjectEntity, UserProjectDTO, UserProjectDTO, UserProjectRepository> {

  @Autowired
  private ModelMapper modelMapper;

  @Autowired
  private UserService userService;

  @Autowired
  private ProjectService projectService;

  @Override
  protected UserProjectDTO convertToDTO(UserProjectEntity userProjectEntity) {
    return modelMapper.map(userProjectEntity, UserProjectDTO.class);
  }

  @Override
  protected UserProjectEntity convertToEntity(UserProjectDTO dto) {
    return modelMapper.map(dto, UserProjectEntity.class);
  }

  @Override
  public UserProjectDTO create(UserProjectDTO userProjectDTO) {
    validateEntitiesKeys(userProjectDTO);

    return super.create(userProjectDTO);
  }

  @Override
  public UserProjectDTO update(UUID id,UserProjectDTO userProjectDTO) {
    getIfEntityExistById(id, "UserProject");

    validateEntitiesKeys(userProjectDTO);

    return super.update(id, userProjectDTO);
  }

  private void validateEntitiesKeys(UserProjectDTO userProjectDTO) {
    UUID userId = userProjectDTO.getUserEntityId();
    userService.getIfEntityExistById(userId, "User");

    UUID projectId = userProjectDTO.getProjectEntityId();
    projectService.getIfEntityExistById(projectId, "Project");

  }
}
