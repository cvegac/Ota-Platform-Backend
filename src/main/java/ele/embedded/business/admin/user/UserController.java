package ele.embedded.business.admin.user;

import ele.embedded.business.admin.user.dto.UserRequest;
import ele.embedded.business.admin.user.dto.UserResponse;
import ele.embedded.core.BaseController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/v1/users")
public class UserController extends BaseController<UserEntity, UserRepository, UserRequest, UserResponse, UserService> {


}
