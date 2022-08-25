package utils;

import lombok.Data;
import util.encoder.Encoder;
import util.key.RSAUtils;

import java.security.KeyPair;

@Data
public class TestKeyPairGenerator {
  String publicKey;
  String privateKey;

  public void initKeys() {
    try {
      java.security.KeyPairGenerator keyPairGenerator = java.security.KeyPairGenerator.getInstance(RSAUtils.ALGORITHM);
      keyPairGenerator.initialize(2048);
      KeyPair keyPair = keyPairGenerator.generateKeyPair();

      privateKey = Encoder.encodeBytes(keyPair.getPrivate().getEncoded());
      publicKey = Encoder.encodeBytes(keyPair.getPublic().getEncoded());
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}