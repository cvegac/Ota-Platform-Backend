package ele.embedded.business.platform.project;

import ele.embedded.business.platform.project.dto.ProjectRequest;
import ele.embedded.business.platform.project.dto.ProjectResponse;
import ele.embedded.core.BaseController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/projects")
public class ProjectController extends BaseController<ProjectEntity, ProjectRepository, ProjectRequest, ProjectResponse, ProjectService> {

}