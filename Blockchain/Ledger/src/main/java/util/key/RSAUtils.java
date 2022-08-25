package util.key;

import util.encoder.Encoder;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.security.SignatureException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;

public class RSAUtils {
  public static final String ALGORITHM = "RSA";
  public static final String SIGNATURE_INSTANCE = "SHA256withRSA";
  public static final int KEY_SIZE = 2048;

  /**
   * imports a public key from a string
   *
   * @param publicKey the public key as a Base64 encoded string
   * @return the public key
   * @throws NoSuchAlgorithmException if the algorithm is not supported
   * @throws InvalidKeySpecException  if the key is invalid
   */
  public static RSAPublicKey importPublicKey(String publicKey) throws NoSuchAlgorithmException, InvalidKeySpecException {
    return RSAKeyImporter.importPublicKey(Encoder.decodeString(publicKey), ALGORITHM);
  }

  /**
   * imports a public key from a string
   *
   * @param privateKey the private key as a Base64 encoded string
   * @return the private key
   * @throws NoSuchAlgorithmException if the algorithm is not supported
   * @throws InvalidKeySpecException  if the key is invalid
   */
  public static RSAPrivateKey importPrivateKey(String privateKey) throws NoSuchAlgorithmException, InvalidKeySpecException {
    return RSAKeyImporter.importPrivateKey(Encoder.decodeString(privateKey), ALGORITHM);
  }

  /**
   * Signs string with provided private key
   *
   * @param privateKey the private key
   * @param data       the data to sign
   * @return the signature or null if the signature could not be created
   */
  public static String sign(RSAPrivateKey privateKey, String data) {
    try {
      Signature signatureInstance = Signature.getInstance(SIGNATURE_INSTANCE);
      signatureInstance.initSign(privateKey);
      signatureInstance.update(data.getBytes());
      byte[] signatureBytes = signatureInstance.sign();
      return Encoder.encodeBytes(signatureBytes);
    } catch (NoSuchAlgorithmException | SignatureException | InvalidKeyException e) {
      System.err.println("Error signing data: " + e.getMessage());
      return null;
    }
  }

  /**
   * Verifies the signature of the data with the provided public key.
   *
   * @param publicKey the public key
   * @param data      the data to verify
   * @param signature the signature
   * @return true if the signature is valid, false otherwise
   */
  public static boolean verifySignature(RSAPublicKey publicKey, String data, String signature) {
    try {
      Signature signatureInstance = Signature.getInstance(SIGNATURE_INSTANCE);
      signatureInstance.initVerify(publicKey);
      signatureInstance.update(data.getBytes());
      byte[] signatureBytes = Encoder.decodeString(signature);
      return signatureInstance.verify(signatureBytes);
    } catch (NoSuchAlgorithmException | SignatureException | InvalidKeyException e) {
      System.err.println("Error verifying signature: " + e.getMessage());
      return false;
    }
  }
}
