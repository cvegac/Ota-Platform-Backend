package ele.embedded.business.aws.lambda;

import ele.embedded.business.aws.iot.IotService;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.model.InvokeRequest;
import software.amazon.awssdk.services.lambda.model.InvokeResponse;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Service
public class LambdaService {

  private final LambdaClient lambdaClient;
  private final IotService iotService;

  private static final String BUCKET_NAME = "ota-binaries-bucket";

  public LambdaService(LambdaClient lambdaClient, IotService iotService) {
    this.lambdaClient = lambdaClient;
    this.iotService = iotService;
  }

  public String newJob(LambdaController.JobRequest jobRequest) {
    try {
      Map<String, Object> payloadMap = getEventRequest(jobRequest);

      String payloadJson = new com.fasterxml.jackson.databind.ObjectMapper()
              .writeValueAsString(payloadMap);

      InvokeRequest request = InvokeRequest.builder()
              .functionName("CreateIoTJobFunction")
              .payload(SdkBytes.fromString(payloadJson, StandardCharsets.UTF_8))
              .build();

      InvokeResponse response = lambdaClient.invoke(request);

      response.payload().asUtf8String();
      return "Lambda ejecutada con éxito! ";

    } catch (Exception e) {
      e.printStackTrace();
      return "Error al ejecutar Lambda: " + e.getMessage();
    }
  }

  public boolean newJobControlled(LambdaController.JobRequest jobRequest) {
    try {
      Map<String, Object> payloadMap = getEventRequest(jobRequest);

      String payloadJson = new com.fasterxml.jackson.databind.ObjectMapper()
              .writeValueAsString(payloadMap);

      InvokeRequest request = InvokeRequest.builder()
              .functionName("CreateIoTJobFunction")
              .payload(SdkBytes.fromString(payloadJson, StandardCharsets.UTF_8))
              .build();

      InvokeResponse response = lambdaClient.invoke(request);

      response.payload().asUtf8String();
      return true;

    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
  }

  private Map<String, Object> getEventRequest(LambdaController.JobRequest jobRequest) {
    Map<String, Object> payloadMap = new HashMap<>();

    TargetType targetType = jobRequest.targetType() == null ? TargetType.THING : jobRequest.targetType();
    String name = jobRequest.name();

    String groupName = (targetType == TargetType.THING)
            ? iotService.findGroupForThing(name)
            : name;

    String sourceKey = TargetType.GROUP.toLower() + "_" + groupName + ".bin";

    payloadMap.put("nombre", name);
    payloadMap.put("tipo", targetType.toLower());
    payloadMap.put("source_bucket", BUCKET_NAME);
    payloadMap.put("source_key", sourceKey);
    payloadMap.put("source_versionId", jobRequest.versionId());
    payloadMap.put("filesize", jobRequest.filesize());

    return payloadMap;
  }
}
