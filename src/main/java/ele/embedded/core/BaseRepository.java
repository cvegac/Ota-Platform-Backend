package ele.embedded.core;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.UUID;

@NoRepositoryBean
public interface BaseRepository<Entity> extends JpaRepository<Entity, UUID> {
  // You can add custom methods here if needed
}
