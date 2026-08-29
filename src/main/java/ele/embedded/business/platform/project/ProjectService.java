package ele.embedded.business.platform.project;

import ele.embedded.business.admin.user.UserEntity;
import ele.embedded.business.admin.user.UserService;
import ele.embedded.business.admin.user.dto.UserResponse;
import ele.embedded.business.admin.user_project.UserProjectEntity;
import ele.embedded.business.admin.user_project.UserProjectService;
import ele.embedded.business.platform.project.dto.ProjectRequest;
import ele.embedded.business.platform.project.dto.ProjectResponse;
import ele.embedded.core.BaseService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ProjectService extends BaseService<ProjectEntity, ProjectRequest, ProjectResponse, ProjectRepository> {

  @Autowired
  private ModelMapper modelMapper;

  @Autowired
  private UserService userService;

  @Autowired
  @Lazy
  private UserProjectService userProjectService;

  @Override
  @jakarta.transaction.Transactional
  public ProjectResponse create(ProjectRequest requestDto) {
    ProjectEntity project = convertToEntity(requestDto);
    this.add(project);
    UserEntity userEntity = userService.getIfEntityExistById(requestDto.getUserId(), "user");

    UserProjectEntity userProjectEntity = new UserProjectEntity(userEntity, project);
    userProjectService.add(userProjectEntity);
    userService.verifyIdentityAndExist(requestDto.getUserId());
    return convertToDTO(project);
  }

  @Override
  protected ProjectResponse convertToDTO(ProjectEntity projectEntity) {
    return modelMapper.map(projectEntity, ProjectResponse.class);
  }

  @Override
  protected ProjectEntity convertToEntity(ProjectRequest dto) {
    return modelMapper.map(dto, ProjectEntity.class);
  }

  public List<ProjectEntity> getProjects(UUID idClient){
    return super.repository.findByClientId(idClient);
  }
}
