package ele.embedded.business.aws.iot;

import ele.embedded.business.aws.iot.dto.JobExecutionDTO;
import ele.embedded.business.aws.iot.dto.JobHistoryResponse;
import ele.embedded.business.aws.iot.dto.Thing;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.iot.IotClient;
import software.amazon.awssdk.services.iot.model.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.ZipOutputStream;

import static ele.embedded.util.ZipUtils.addFileToZip;

@Service
public class IotService {

  private final IotClient iotClient;
  private final Map<String, CreateKeysAndCertificateResponse> certificateCache = new ConcurrentHashMap<>();

  @Autowired
  public IotService(IotClient iotClient) {
    this.iotClient = iotClient;
  }

  public String createThingGroup(String groupName) {
    CreateThingGroupRequest request = CreateThingGroupRequest.builder().thingGroupName(groupName).build();
    CreateThingGroupResponse response = iotClient.createThingGroup(request);
    return response.thingGroupName();
  }

  public byte[] createThingWithCertificateAndGetZip(String thingName, String groupName) throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();

    try (ZipOutputStream zipOut = new ZipOutputStream(baos)) {
      // 1. Crear Thing
      CreateThingRequest thingRequest = CreateThingRequest.builder()
              .thingName(thingName)
              .build();
      iotClient.createThing(thingRequest);
      // 2. Generar certificado y claves
      CreateKeysAndCertificateResponse certResponse = iotClient.createKeysAndCertificate();
      String certificatePem = certResponse.certificatePem();
      String privateKey = certResponse.keyPair().privateKey();
      String certificateId = certResponse.certificateId();

      // 3. Asociar certificado al Thing y política
      attachCertificateToThing(thingName, certResponse.certificateArn());
      attachPolicyToCertificate("DevicePolicy", certResponse.certificateArn());
      activateCertificate(certificateId);
      addThingToGroup(thingName, groupName);

      // 4. Agregar archivos al ZIP
      addFileToZip(zipOut, certificatePem.getBytes(), thingName + ".cert.pem.crt");
      addFileToZip(zipOut, privateKey.getBytes(), thingName + ".private.pem.key");

    } catch (IotException | IOException e) {
      throw new RuntimeException("Error en AWS IoT: " + e.getMessage());
    }

    return baos.toByteArray();
  }

  private void attachCertificateToThing(String thingName, String certificateArn) {
    AttachThingPrincipalRequest attachRequest = AttachThingPrincipalRequest.builder()
            .thingName(thingName)
            .principal(certificateArn)
            .build();

    iotClient.attachThingPrincipal(attachRequest);
  }

  private void attachPolicyToCertificate(String policyName, String certificateArn) {
    AttachPolicyRequest policyRequest = AttachPolicyRequest.builder()
            .policyName(policyName)
            .target(certificateArn)
            .build();

    iotClient.attachPolicy(policyRequest);
  }

  private void activateCertificate(String certificateId) {
    UpdateCertificateRequest updateRequest = UpdateCertificateRequest.builder()
            .certificateId(certificateId)
            .newStatus(CertificateStatus.ACTIVE)
            .build();

    iotClient.updateCertificate(updateRequest);
  }

  private void addThingToGroup(String thingName, String groupName) {
    AddThingToThingGroupRequest groupRequest = AddThingToThingGroupRequest.builder()
            .thingName(thingName)
            .thingGroupName(groupName)
            .build();
    iotClient.addThingToThingGroup(groupRequest);
  }

 public List<Thing> getThingsByGroup(String groupName){
    ListThingsInThingGroupRequest request = ListThingsInThingGroupRequest.builder()
            .thingGroupName(groupName)
            .build();
    return iotClient.listThingsInThingGroup(request)
            .things()
            .stream()
            .map(Thing::new)
            .toList();
  }

  public String findGroupForThing(String thingName) {
    ListThingGroupsForThingRequest request = ListThingGroupsForThingRequest.builder()
            .thingName(thingName)
            .build();
    return iotClient
            .listThingGroupsForThing(request)
            .thingGroups()
            .stream()
            .findFirst()
            .map(GroupNameAndArn::groupName)
            .orElseThrow(() -> new RuntimeException("El dispositivo no pertenece a ningún grupo"));
  }

  public JobHistoryResponse getJobExecutionsForThing(String thingName, Integer maxResults, String nextToken) {
    ListJobExecutionsForThingRequest request = ListJobExecutionsForThingRequest.builder()
            .thingName(thingName)
            .maxResults(maxResults != null ? maxResults : 10)
            .nextToken(nextToken)
            .build();

    ListJobExecutionsForThingResponse response = iotClient.listJobExecutionsForThing(request);
    List<JobExecutionDTO> executions = response.executionSummaries()
            .stream()
            .map(JobExecutionDTO::new)
            .toList();

    return JobHistoryResponse.builder()
            .executions(executions)
            .nextToken(response.nextToken())
            .build();
  }

}
