package ele.embedded.business.admin.user_project;

import lombok.Data;

import java.util.UUID;

@Data
public class UserProjectDTO {
        private UUID id;
        private UUID userEntityId;
        private UUID projectEntityId;
        private UUID userLevelEntityId;
}
