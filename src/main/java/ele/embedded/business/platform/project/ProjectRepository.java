package ele.embedded.business.platform.project;

import ele.embedded.core.BaseRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface ProjectRepository extends BaseRepository<ProjectEntity> {

    @Query("SELECT up.projectEntity FROM UserProjectEntity up WHERE up.userEntity.id = :clientId")
    List<ProjectEntity> findByClientId(UUID clientId);
}
