package validation;

import util.key.RSAUtils;

import java.security.interfaces.RSAPublicKey;

public class SignatureValidator {
  /**
   * Verifies the signature of a given message using the public key of the sender.
   *
   * @param signature The signature to verify.
   * @param publicKey The public key of the sender (in hex format).
   * @param message   The message to verify the signature of.
   * @return True if the signature is valid, false otherwise.
   */
  public static boolean verifySignature(String signature, String publicKey, String message) {
    try {
      RSAPublicKey key = RSAUtils.importPublicKey(publicKey);
      return RSAUtils.verifySignature(key, message, signature);
    } catch (Exception e) {
      return false;
    }
  }
}
