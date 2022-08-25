package validation;

import model.Tweet;

public class TweetValidator {
  /**
   * Validates signature of message
   *
   * @param signature signature that was signed by sender with sender's private key
   * @param publicKey sender's public key
   * @param message   message that was signed
   * @return true if signature is valid, false otherwise
   */
  private static boolean isSignatureValid(String signature, String publicKey, String message) {
    return SignatureValidator.verifySignature(signature, publicKey, message);
  }

  /**
   * Validates message by checking if the signature is valid and message is not empty
   *
   * @param tweet message that should be validated
   * @return true if message is valid, false otherwise
   */
  public static boolean isTweetValid(Tweet tweet) {
    return TweetValidator.isSignatureValid(tweet.getSignature(), tweet.getPublicKeyOfCreator(),
      tweet.getContent());
  }
}
