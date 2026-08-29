package ele.embedded.business.aws.iot.dto;

import lombok.Getter;
import lombok.Setter;
import software.amazon.awssdk.services.iot.model.CreateKeysAndCertificateResponse;

@Getter
@Setter
public class CertificateResponseDTO {
  private String certificateArn;
  private String certificateId;
  private String certificatePem;
  private String keyPairPublicKey;
  private String keyPairPrivateKey;

  public CertificateResponseDTO(CreateKeysAndCertificateResponse response) {
    this.certificateArn = response.certificateArn();
    this.certificateId = response.certificateId();
    this.certificatePem = response.certificatePem();
    this.keyPairPublicKey = response.keyPair().publicKey();
    this.keyPairPrivateKey = response.keyPair().privateKey();
  }

}

