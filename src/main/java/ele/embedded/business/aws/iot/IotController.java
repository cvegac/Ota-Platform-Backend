package ele.embedded.business.aws.iot;

import ele.embedded.business.aws.iot.dto.JobHistoryResponse;
import ele.embedded.business.aws.iot.dto.Thing;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import software.amazon.awssdk.services.iot.IotClient;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("iot")
public class IotController {

  @Autowired
  private IotService iotService;

  @Autowired
  protected CertificateService certificateService;

  public record CreateThingRequest(String name, String groupName) {
  }

  public IotController(IotClient iotClient) {
  }

  @GetMapping("/thing/{groupName}")
  public List<Thing> getThingsByGroup(@PathVariable  String groupName){
    return iotService.getThingsByGroup(groupName);
  }

  @PostMapping("/group/{groupName}")
  public String createThingGroup(@PathVariable String groupName) {
    return iotService.createThingGroup(groupName);
  }

  @PostMapping(value = "/thing", produces = "application/zip")
  public ResponseEntity<byte[]> createThing(
          @RequestBody CreateThingRequest request) throws IOException {

    try {
      byte[] zipBytes = iotService.createThingWithCertificateAndGetZip(
              request.name(),
              request.groupName()
      );

      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
      headers.setContentDispositionFormData("attachment", request.name() + "_certificates.zip");

      return ResponseEntity.ok()
              .headers(headers)
              .body(zipBytes);

    } catch (IOException e) {
      return ResponseEntity.status(500).body("Error generando ZIP".getBytes());
    }
  }

  @GetMapping("/code-signing/{deviceName}/{email}/{profileName}")
  public ResponseEntity<byte[]> downloadCertificates(@PathVariable String deviceName, @PathVariable String email, @PathVariable String profileName) throws Exception {
    byte[] zipContent = certificateService.generateZipWithKeyAndCertificate(deviceName, email, profileName);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
    headers.setContentDispositionFormData("attachment", deviceName + "_certificates.zip");

    return ResponseEntity.ok()
            .headers(headers)
            .body(zipContent);
  }

  @GetMapping("/acm/certificates")
  public List<String> getCertificates() {
    return certificateService.listAcmCertificates();
  }

  @GetMapping("/thing/{thingName}/jobs")
  public JobHistoryResponse getJobHistory(
          @PathVariable String thingName,
          @RequestParam(required = false) Integer maxResults,
          @RequestParam(required = false) String nextToken) {
    return iotService.getJobExecutionsForThing(thingName, maxResults, nextToken);
  }
}
