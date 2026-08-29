package ele.embedded.business.aws.iot.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class GroupVersionsResponse {
  private List<Map<String, Object>> versions;
  private String prefix;
  private Integer maxKeys;

  public GroupVersionsResponse(List<Map<String, Object>> versions, String prefix, Integer maxKeys) {
    this.versions = versions;
    this.prefix = prefix;
    this.maxKeys = maxKeys;
  }
}