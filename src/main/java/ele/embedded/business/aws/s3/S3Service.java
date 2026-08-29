package ele.embedded.business.aws.s3;

import ele.embedded.business.aws.iot.dto.GroupVersionsResponse;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListObjectVersionsRequest;
import software.amazon.awssdk.services.s3.model.ListObjectVersionsResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class S3Service {

  private final S3Client s3;
  private final S3Presigner s3Presigner;
  private static final String BUCKET_NAME = "ota-binaries-bucket";

  public S3Service(S3Client s3Client, S3Presigner s3Presigner) {
    this.s3 = s3Client;
    this.s3Presigner = s3Presigner;
  }

  public String generatePresignedUrl(String fileName) {
    PutObjectRequest putObjectRequest = PutObjectRequest.builder()
            .bucket(BUCKET_NAME)
            .key(fileName)
            .contentType("application/octet-stream")
            .build();

    PresignedPutObjectRequest presignedRequest = s3Presigner
            .presignPutObject(r -> r
            .signatureDuration(Duration.ofHours(24))
            .putObjectRequest(putObjectRequest)
    );

    return presignedRequest.url().toString();
  }

  public GroupVersionsResponse getGroupVersions(String groupName) {
    String objectKey = "group_" + groupName + "_SigningProfile.bin";

    ListObjectVersionsRequest request = ListObjectVersionsRequest.builder()
            .bucket(BUCKET_NAME)
            .prefix(objectKey)
            .build();

    ListObjectVersionsResponse result = s3.listObjectVersions(request);

    List<Map<String, Object>> versions = result.versions().stream()
            .map(v -> {
              Map<String, Object> map = new HashMap<>();
              map.put("versionId", v.versionId());
              map.put("latest", v.isLatest());
              map.put("lastModified", v.lastModified().toString());
              map.put("filesize", v.size());
              return map;
            }).toList();

    return new GroupVersionsResponse(
            versions,
            result.prefix(),
            result.maxKeys()
    );
  }
    public GroupVersionsResponse getDeviceVersions(String deviceName) {
        String objectKey = deviceName + "_SigningProfile.bin";

        ListObjectVersionsRequest request = ListObjectVersionsRequest.builder()
                .bucket(BUCKET_NAME)
                .prefix(objectKey)
                .build();

        ListObjectVersionsResponse result = s3.listObjectVersions(request);

        List<Map<String, Object>> versions = result.versions().stream()
                .map(v -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("versionId", v.versionId());
                    map.put("latest", v.isLatest());
                    map.put("lastModified", v.lastModified().toString());
                    map.put("filesize", v.size());
                    return map;
                }).toList();

        return new GroupVersionsResponse(
                versions,
                result.prefix(),
                result.maxKeys()
        );
    }
}
