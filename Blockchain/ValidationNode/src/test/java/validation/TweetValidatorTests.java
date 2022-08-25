package validation;

import model.Tweet;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import util.key.RSAUtils;
import utils.TestKeyPairGenerator;

import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.InvalidKeySpecException;

public class TweetValidatorTests {
  private String privateKey;
  private String publicKey;

  @BeforeEach
  public void initKeys() {
    try {
      TestKeyPairGenerator keyGen = new TestKeyPairGenerator();
      keyGen.initKeys();
      this.privateKey = keyGen.getPrivateKey();
      this.publicKey = keyGen.getPublicKey();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private Tweet createTweet() throws NoSuchAlgorithmException, InvalidKeySpecException {
    String tweetContent = "Test tweet";
    RSAPrivateKey privateKey = RSAUtils.importPrivateKey(this.privateKey);
    String signature = RSAUtils.sign(privateKey, tweetContent);
    return new Tweet(tweetContent, this.publicKey, signature, System.currentTimeMillis());
  }

  @Test
  public void testTweetValidation() {
    try {
      Tweet tweet = createTweet();
      Assertions.assertTrue(TweetValidator.isTweetValid(tweet));
    } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
      e.printStackTrace();
      Assertions.fail();
    }
  }

  @Test
  public void testTweetValidationWrongPublicKey() {
    try {
      Tweet tweet = createTweet();
      tweet.setPublicKeyOfCreator(this.publicKey + "H");
      Assertions.assertFalse(TweetValidator.isTweetValid(tweet));
    } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
      e.printStackTrace();
      Assertions.fail();
    }
  }

  @Test
  public void testTweetValidationWrongContent() {
    try {
      Tweet tweet = createTweet();
      tweet.setContent("Wrong content");
      Assertions.assertFalse(TweetValidator.isTweetValid(tweet));
    } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
      e.printStackTrace();
      Assertions.fail();
    }
  }
}
