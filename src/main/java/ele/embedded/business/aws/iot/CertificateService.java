package ele.embedded.business.aws.iot;

import ele.embedded.util.BouncyCastleCertificate;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.acm.AcmClient;
import software.amazon.awssdk.services.acm.model.*;
import software.amazon.awssdk.services.signer.SignerClient;
import software.amazon.awssdk.services.signer.model.PutSigningProfileRequest;
import software.amazon.awssdk.services.signer.model.SigningMaterial;

import java.io.ByteArrayOutputStream;
import java.security.KeyPair;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.zip.ZipOutputStream;

import static ele.embedded.util.BouncyCastleCertificate.convertCertificateToPEM;
import static ele.embedded.util.BouncyCastleCertificate.convertPrivateKeyToPEM;
import static ele.embedded.util.ZipUtils.addFileToZip;

@Service
public class CertificateService {

  private final AcmClient acmClient;

  public CertificateService(AcmClient acmClient) {
    this.acmClient = acmClient;
  }

  public byte[] generateZipWithKeyAndCertificate(String deviceName, String email, String profileName) throws Exception {
    KeyPair keyPair = BouncyCastleCertificate.generateKeyPair();
    X509Certificate certificate = BouncyCastleCertificate.generateCertificate(keyPair, email);

    String certificateArn = uploadCertificateToAcm(keyPair, certificate);

    createCodeSigningProfile(profileName, certificateArn);

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try (ZipOutputStream zipOut = new ZipOutputStream(baos)) {
      addFileToZip(zipOut, certificateArn, profileName + ".certificateArn.txt");
      addFileToZip(zipOut, convertPrivateKeyToPEM(keyPair.getPrivate()).getBytes(), deviceName + ".private.key.pem");
      addFileToZip(zipOut, convertCertificateToPEM(certificate).getBytes(), deviceName + ".certificate.crt");
    }
    return baos.toByteArray();
  }

  public String uploadCertificateToAcm(KeyPair keyPair, X509Certificate certificate) throws Exception {
    String pemCertificate = convertCertificateToPEM(certificate);

    String pemPrivateKey = convertPrivateKeyToPEM(keyPair.getPrivate());

    ImportCertificateRequest request = ImportCertificateRequest.builder()
            .certificate(SdkBytes.fromUtf8String(pemCertificate))
            .privateKey(SdkBytes.fromUtf8String(pemPrivateKey))
            .build();

    ImportCertificateResponse response = acmClient.importCertificate(request);

   return response.certificateArn();
  }

  public List<String> listAcmCertificates() {
    ListCertificatesRequest request = ListCertificatesRequest.builder()
            .includes(builder -> builder.keyTypes(KeyAlgorithm.fromValue("EC_prime256v1")))
            .build();
    ListCertificatesResponse response = acmClient.listCertificates(request);

    return response.certificateSummaryList().stream()
            .map(CertificateSummary::certificateArn)
            .collect(Collectors.toList());
  }

  private void createCodeSigningProfile(String profileName, String certificateArn) {
    try (SignerClient signerClient = SignerClient.create()) {

      Map<String, String> signingParameters = new HashMap<>();
      signingParameters.put("certname", "/");

      SigningMaterial signingMaterial = SigningMaterial.builder()
              .certificateArn(certificateArn)
              .build();

      PutSigningProfileRequest request = PutSigningProfileRequest.builder()
              .profileName(profileName)
              .signingMaterial(signingMaterial)
              .platformId("AmazonFreeRTOS-Default")
              .signingParameters(signingParameters)
              .build();

      signerClient.putSigningProfile(request);
    }
  }
}
