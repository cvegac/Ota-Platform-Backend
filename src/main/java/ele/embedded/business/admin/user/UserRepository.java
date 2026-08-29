package ele.embedded.business.admin.user;

import ele.embedded.core.BaseRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends BaseRepository<UserEntity> {

  Optional<UserEntity> findUserEntityByUsername(String username);

  // Returns a list to safely handle cases with duplicate emails in the DB
  List<UserEntity> findAllByEmail(String email);

}
