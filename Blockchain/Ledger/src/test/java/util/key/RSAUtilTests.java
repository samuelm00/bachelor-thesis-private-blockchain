package util.key;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import util.encoder.Encoder;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

public class RSAUtilTests {
  private String privateKey;
  private String publicKey;

  @BeforeEach
  public void initKeys() {
    try {
      KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(RSAUtils.ALGORITHM);
      keyPairGenerator.initialize(2048);
      KeyPair keyPair = keyPairGenerator.generateKeyPair();

      privateKey = Encoder.encodeBytes(keyPair.getPrivate().getEncoded());
      publicKey = Encoder.encodeBytes(keyPair.getPublic().getEncoded());
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Test
  public void testPublicKeyImport() {
    try {
      RSAPublicKey publicKey = RSAUtils.importPublicKey(this.publicKey);
      Assertions.assertTrue(publicKey.getEncoded().length > 0);
      Assertions.assertEquals(this.publicKey, Encoder.encodeBytes(publicKey.getEncoded()));
    } catch (Exception e) {
      e.printStackTrace();
      Assertions.fail();
    }
  }

  @Test
  public void testPrivateKeyImport() {
    try {
      RSAPrivateKey privateKey = RSAUtils.importPrivateKey(this.privateKey);
      Assertions.assertTrue(privateKey.getEncoded().length > 0);
      Assertions.assertEquals(this.privateKey, Encoder.encodeBytes(privateKey.getEncoded()));
    } catch (Exception e) {
      e.printStackTrace();
      Assertions.fail();
    }
  }

  @Test
  public void testSignature() {
    try {
      String stringToSign = "Hello World!";
      RSAPrivateKey privateKey = RSAUtils.importPrivateKey(this.privateKey);
      RSAPublicKey publicKey = RSAUtils.importPublicKey(this.publicKey);

      String signature = RSAUtils.sign(privateKey, stringToSign);

      Assertions.assertTrue(signature != null && signature.length() > 0);
      Assertions.assertTrue(RSAUtils.verifySignature(publicKey, stringToSign, signature));
    } catch (Exception e) {
      e.printStackTrace();
      Assertions.fail();
    }
  }
}
