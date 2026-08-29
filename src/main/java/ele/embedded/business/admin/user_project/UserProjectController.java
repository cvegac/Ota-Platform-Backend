package ele.embedded.business.admin.user_project;

import ele.embedded.core.BaseController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/user-projects")
public class UserProjectController extends BaseController<
        UserProjectEntity,
        UserProjectRepository,
        UserProjectDTO,
        UserProjectDTO,
        UserProjectService
        > {

}
