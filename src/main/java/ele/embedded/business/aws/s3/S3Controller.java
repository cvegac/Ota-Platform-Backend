package ele.embedded.business.aws.s3;

import ele.embedded.business.aws.iot.dto.GroupVersionsResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("s3")
public class S3Controller {

    @Autowired
    protected S3Service s3Service;

    public record GenPresignedRequest(String fileName) {
    }

    @GetMapping("/group/versions/{groupName}")
    public GroupVersionsResponse getGroupVersions(@PathVariable String groupName) {
        return s3Service.getGroupVersions(groupName);
    }

    @PostMapping("/presigned-url")
    public ResponseEntity<Map<String, String>> generatePresignedUrl(@RequestBody GenPresignedRequest request) {
        String url = s3Service.generatePresignedUrl(request.fileName());

        return ResponseEntity.ok(Map.of(
                "presignedUrl", url
        ));
    }
}
