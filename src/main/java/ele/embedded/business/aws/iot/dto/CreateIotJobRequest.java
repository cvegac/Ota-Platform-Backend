package ele.embedded.business.aws.iot.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateIotJobRequest {
  private String jobId;
  private String thingName;
  private String codeSigningProfileName;
}
