package ele.embedded.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.acm.AcmClient;
import software.amazon.awssdk.services.iot.IotClient;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.signer.SignerClient;

/**
 * Clientes AWS.
 *
 * Credenciales: se usa la DefaultCredentialsProvider chain. En AWS las provee el
 * IAM Instance Role (sin llaves en disco). En local, ~/.aws/credentials o las
 * variables AWS_ACCESS_KEY_ID / AWS_SECRET_ACCESS_KEY del entorno.
 *
 * NUNCA volver a hardcodear access-key/secret-key en application.properties.
 */
@Configuration
public class AwsConfig {

  @Value("${aws.region:us-east-2}")
  private String awsRegion;

  private DefaultCredentialsProvider credentialsProvider() {
    return DefaultCredentialsProvider.create();
  }

  private Region region() {
    return Region.of(awsRegion);
  }

  @Bean
  public AcmClient acmClient() {
    return AcmClient.builder()
            .region(region())
            .credentialsProvider(credentialsProvider())
            .build();
  }

  @Bean
  public IotClient iotClient() {
    return IotClient.builder()
            .region(region())
            .credentialsProvider(credentialsProvider())
            .build();
  }

  @Bean
  public S3Client s3Client() {
    return S3Client.builder()
            .region(region())
            .credentialsProvider(credentialsProvider())
            .build();
  }

  @Bean
  public LambdaClient lambdaClient() {
    return LambdaClient.builder()
            .region(region())
            .credentialsProvider(credentialsProvider())
            .build();
  }

  @Bean
  public SignerClient signerClient() {
    return SignerClient.builder()
            .region(region())
            .credentialsProvider(credentialsProvider())
            .build();
  }

  @Bean
  public S3Presigner s3Presigner() {
    return S3Presigner.builder()
            .region(region())
            .credentialsProvider(credentialsProvider())
            .build();
  }
}
