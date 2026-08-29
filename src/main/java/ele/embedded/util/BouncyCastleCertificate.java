package ele.embedded.util;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.ExtendedKeyUsage;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.KeyPurposeId;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509ExtensionUtils;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

import java.math.BigInteger;
import java.security.*;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.security.spec.ECGenParameterSpec;
import java.util.Base64;
import java.util.Date;

public class BouncyCastleCertificate {

  static {
    Security.addProvider(new BouncyCastleProvider());
  }

  public static KeyPair generateKeyPair() throws NoSuchAlgorithmException, InvalidAlgorithmParameterException {
    KeyPairGenerator keyGen = KeyPairGenerator.getInstance("EC");
    keyGen.initialize(new ECGenParameterSpec("secp256r1"));
    return keyGen.generateKeyPair();
  }

  public static X509Certificate generateCertificate(KeyPair keyPair, String email) throws Exception {
    X500Name subjectName = new X500Name("CN=" + email);
    X500Name issuerName = new X500Name("CN=" + email);
    BigInteger serialNumber = new BigInteger(64, new SecureRandom());
    Date notBefore = new Date();
    Date notAfter = new Date(System.currentTimeMillis() + (365 * 24 * 60 * 60 * 1000L));

    X509v3CertificateBuilder certBuilder = new JcaX509v3CertificateBuilder(
            issuerName,
            serialNumber,
            notBefore,
            notAfter,
            subjectName,
            keyPair.getPublic()
    );

    JcaX509ExtensionUtils extUtils = new JcaX509ExtensionUtils();

    certBuilder.addExtension(Extension.keyUsage, false, new KeyUsage(KeyUsage.digitalSignature));

    certBuilder.addExtension(Extension.extendedKeyUsage, false,
            new ExtendedKeyUsage(KeyPurposeId.id_kp_codeSigning));

    certBuilder.addExtension(Extension.subjectKeyIdentifier, false,
            extUtils.createSubjectKeyIdentifier(keyPair.getPublic()));

    // Firmar el certificado
    ContentSigner signer = new JcaContentSignerBuilder("SHA256withECDSA").build(keyPair.getPrivate());
    return new JcaX509CertificateConverter().getCertificate(certBuilder.build(signer));
  }

  public static String convertPrivateKeyToPEM(PrivateKey privateKey) {
    return "-----BEGIN PRIVATE KEY-----\n" +
            Base64.getMimeEncoder(64, "\n".getBytes()).encodeToString(privateKey.getEncoded()) +
            "\n-----END PRIVATE KEY-----\n";
  }

  public static String convertCertificateToPEM(X509Certificate certificate) throws CertificateEncodingException {
    return "-----BEGIN CERTIFICATE-----\n" +
            Base64.getMimeEncoder(64, "\n".getBytes()).encodeToString(certificate.getEncoded()) +
            "\n-----END CERTIFICATE-----\n";
  }

}
