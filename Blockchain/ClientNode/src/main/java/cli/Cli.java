package cli;

import lombok.RequiredArgsConstructor;
import model.MerkleNode;
import model.Tweet;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import service.BlockchainService;
import util.encoder.Encoder;
import util.key.RSAUtils;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.util.List;

@ShellComponent
@RequiredArgsConstructor
public class Cli {
  private final BlockchainService blockchainService;
  private String publicKey;
  private String privateKey;
  private String password;

  @ShellMethod("Generates private and public keys")
  public String generateKeys() throws NoSuchAlgorithmException {
    KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(RSAUtils.ALGORITHM);
    keyPairGenerator.initialize(RSAUtils.KEY_SIZE);
    KeyPair keyPair = keyPairGenerator.generateKeyPair();
    this.privateKey = Encoder.encodeBytes(keyPair.getPrivate().getEncoded());
    this.publicKey = Encoder.encodeBytes(keyPair.getPublic().getEncoded());
    return "Keys generated: \n publicKey: " + this.publicKey +
      "\n\n privateKey: " + this.privateKey;
  }

  @ShellMethod("Login to the blockchain. No args required. Simply uses the password that was used in register command")
  public String login() {
    return blockchainService.login(this.publicKey, this.password) ? "Login successful" : "Login failed";
  }

  @ShellMethod("Register to the blockchain. Example of how to use the command: register --password Password")
  public String register(String password) {
    this.password = password;
    return blockchainService.register(this.publicKey, this.password) ? "Registration successful" : "Registration failed";
  }

  @ShellMethod("Post a Tweet to the blockchain. Example of how to use the command: post-tweet --tweet \"This is a tweet\" ")
  public void postTweet(String tweet) throws NoSuchAlgorithmException, InvalidKeySpecException {
    RSAPrivateKey key = RSAUtils.importPrivateKey(privateKey);
    String signature = RSAUtils.sign(key, tweet);
    Tweet tweetModel = new Tweet(tweet, this.publicKey, signature, System.currentTimeMillis());
    blockchainService.postTweet(tweetModel);
  }

  @ShellMethod("Get all of my tweets")
  public String getMyTweets() {
    List<MerkleNode> merkleNodes = blockchainService.getMyTweets();
    StringBuilder stringBuilder = new StringBuilder();
    for (int i = 0; i < merkleNodes.size(); i++) {
      stringBuilder
        .append(i + 1)
        .append(" ")
        .append(merkleNodes.get(i).getTweet().getContent())
        .append(", hash: ")
        .append(merkleNodes.get(i).getHash())
        .append("\n");
    }
    return stringBuilder.toString();
  }

  @ShellMethod("Get tweet with a specific hash. Example of how to use the command: get-tweet --hash someHashValue")
  public String getTweet(String hash) {
    Tweet tweet = blockchainService.getTweet(hash);

    if (tweet == null) {
      return "Tweet not found";
    }

    return tweet.getContent() + "; From: " + tweet.getPublicKeyOfCreator();
  }

  @ShellMethod("Send a tweet that is not valid. No args required.")
  public String sendInvalidTweet() {
    Tweet tweet = new Tweet("Invalid", this.publicKey, "TestSignature", System.currentTimeMillis());
    System.out.println(tweet);
    blockchainService.postTweet(tweet);
    return "Tweet sent";
  }
}
