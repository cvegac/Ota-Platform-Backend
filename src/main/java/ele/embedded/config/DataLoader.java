package ele.embedded.config;

import ele.embedded.business.admin.user.UserEntity;
import ele.embedded.business.admin.user.RoleEnum;
import ele.embedded.business.admin.user.UserRepository;
import ele.embedded.business.admin.user_project.UserProjectEntity;
import ele.embedded.business.admin.user_project.UserProjectRepository;
import ele.embedded.business.platform.project.ProjectEntity;
import ele.embedded.business.platform.project.ProjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Responsible for loading initial data into the database.
 * Runs automatically on application startup.
 * Only inserts data if it doesn't already exist (idempotent).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataLoader implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final UserProjectRepository userProjectRepository;

    private static final String DEFAULT_PASSWORD = "U2t4n6810";

    @Override
    @Transactional
    public void run(String... args) {
        log.info("=== Starting DataLoader ===");

        // Create users if they don't exist
        UserEntity adminUser = createUserIfNotExists(
                "Admin Degree",
                "admin",
                "admin@degree.com"
        );

        UserEntity baseUser = createUserIfNotExists(
                "Base User",
                "user",
                "user@degree.com"
        );

        // Create project if it doesn't exist
        ProjectEntity project = createProjectIfNotExists(
                "Degree",
                "IoT Platform for embedded device management"
        );

        // Associate users with the project
        if (adminUser != null && project != null) {
            createUserProjectIfNotExists(adminUser, project);
        }
        if (baseUser != null && project != null) {
            createUserProjectIfNotExists(baseUser, project);
        }

        log.info("=== DataLoader completed ===");
    }

    private UserEntity createUserIfNotExists(String name, String username, String email) {
        String pass = new BCryptPasswordEncoder().encode(DEFAULT_PASSWORD);
        return userRepository.findUserEntityByUsername(username)
                .map(user -> {
                    if (user.getRole() == null) {
                        user.setRole(RoleEnum.ADMIN);
                        return userRepository.save(user);
                    }
                    return user;
                })
                .orElseGet(() -> {
                    log.info("Creating user: {}", username);
                    UserEntity user = UserEntity.builder()
                            .name(name)
                            .username(username)
                            .email(email)
                            .password(pass)
                            .role(RoleEnum.ADMIN)
                            .isEnabled(true)
                            .accountNoExpired(true)
                            .accountNoLocked(true)
                            .credentialNoExpired(true)
                            .build();
                    return userRepository.save(user);
                });
    }

    private ProjectEntity createProjectIfNotExists(String name, String description) {
        return projectRepository.findAll().stream()
                .filter(p -> p.getName().equals(name))
                .findFirst()
                .orElseGet(() -> {
                    log.info("Creating project: {}", name);
                    ProjectEntity project = new ProjectEntity();
                    project.setName(name);
                    project.setDescription(description);
                    return projectRepository.save(project);
                });
    }

    private void createUserProjectIfNotExists(UserEntity user, ProjectEntity project) {
        boolean exists = userProjectRepository.findAll().stream()
                .anyMatch(up -> up.getUserEntity().getId().equals(user.getId())
                        && up.getProjectEntity().getId().equals(project.getId()));

        if (!exists) {
            log.info("Associating user '{}' with project '{}'", user.getUsername(), project.getName());
            UserProjectEntity userProject = new UserProjectEntity();
            userProject.setUserEntity(user);
            userProject.setProjectEntity(project);
            userProjectRepository.save(userProject);
        }
    }
}
