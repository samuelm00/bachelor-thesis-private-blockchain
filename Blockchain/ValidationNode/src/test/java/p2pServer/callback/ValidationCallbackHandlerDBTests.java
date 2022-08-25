package p2pServer.callback;

import blockBuilder.BlockBuilder;
import callback.P2PCallback;
import connection.ConnectionDetails;
import envelope.BlockWithValidation;
import envelope.Envelope;
import envelope.EnvelopeType;
import model.Block;
import model.Tweet;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import p2pServer.P2PServer;
import repository.BlockRepository;
import repository.MerkleNodeRepository;
import repository.TweetRepository;
import util.key.RSAUtils;
import utils.TestKeyPairGenerator;

import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Testcontainers
@SpringBootTest(classes = {main.ValidationNodeApplication.class},
  properties = {"jwt.secret=secret", "spring.jpa.hibernate.ddl-auto=update"})
public class ValidationCallbackHandlerDBTests {
  private final List<Envelope<Tweet>> tweets = new ArrayList<>();
  private final TestKeyPairGenerator keyPairGenerator = new TestKeyPairGenerator();
  private P2PCallback callbackHandler;

  @Autowired
  private BlockRepository blockRepository;
  @Autowired
  private MerkleNodeRepository merkleNodeRepository;
  @Autowired
  private TweetRepository tweetRepository;

  @MockBean
  private P2PServer p2pServer;

  @Container
  public static PostgreSQLContainer<?> dbContainer = new PostgreSQLContainer<>(DockerImageName.parse("postgres"))
    .withDatabaseName("ledger")
    .withUsername("username")
    .withPassword("password");

  @DynamicPropertySource
  static void postgresProps(DynamicPropertyRegistry registry) {
    dbContainer.start();
    registry.add("spring.datasource.url", dbContainer::getJdbcUrl);
    registry.add("spring.datasource.username", dbContainer::getUsername);
    registry.add("spring.datasource.password", dbContainer::getPassword);
    System.out.println(dbContainer.getJdbcUrl());
  }

  private Envelope<BlockWithValidation> envelopeGenerator(EnvelopeType type, List<Tweet> tweets, String key) {
    BlockBuilder blockBuilder = new BlockBuilder(blockRepository);
    Block block = blockBuilder.build(tweets);
    BlockWithValidation blockWithValidation = new BlockWithValidation(block, true, key, tweets);
    return new Envelope<>(blockWithValidation, type);
  }

  private Tweet createTweet() throws NoSuchAlgorithmException, InvalidKeySpecException {
    String tweetContent = "Test tweet";
    RSAPrivateKey privateKey = RSAUtils.importPrivateKey(this.keyPairGenerator.getPrivateKey());
    String signature = RSAUtils.sign(privateKey, tweetContent);
    return new Tweet(tweetContent, this.keyPairGenerator.getPublicKey(), signature, System.currentTimeMillis());
  }

  @BeforeEach
  void initServices() {
    merkleNodeRepository.deleteAll();
    blockRepository.deleteAll();
    blockRepository.save(new Block(null, "merkleRoot", System.currentTimeMillis()));
    this.keyPairGenerator.initKeys();
    this.tweets.clear();
    ConnectionDetails connectionDetails = new ConnectionDetails(10, 12, "test", keyPairGenerator.getPublicKey());
    this.callbackHandler = new ValidationCallbackHandler(tweets, 3, connectionDetails, blockRepository, merkleNodeRepository, tweetRepository);
  }

  @Test
  public void testSetup() {
    Assertions.assertTrue(dbContainer.isRunning());
    Assertions.assertNotNull(blockRepository);
    Assertions.assertNotNull(merkleNodeRepository);
    Assertions.assertNotNull(keyPairGenerator.getPrivateKey());
    Assertions.assertNotNull(keyPairGenerator.getPublicKey());
  }

  @Test
  public void testOnBlockIsValidReceiveWithInvalidTweets() {
    List<Tweet> tweets = new ArrayList<>();
    String firstValidatorKey = "firstValidatorKey";
    String secondValidatorKey = "secondValidatorKey";
    tweets.add(new Tweet("testMessage", "publicKeyCreator", "signature", System.currentTimeMillis()));

    Envelope<BlockWithValidation> envelope = envelopeGenerator(EnvelopeType.BLOCK_RECEIVE, tweets, firstValidatorKey);
    callbackHandler.onBlockReceived(envelope);

    Envelope<BlockWithValidation> envelope2 = envelopeGenerator(EnvelopeType.BLOCK_RECEIVE, tweets, secondValidatorKey);
    callbackHandler.onBlockReceived(envelope2);

    // Only the genesis block should be in the database because the block created from the above tweets is invalid
    Assertions.assertEquals(1, blockRepository.findAll().size());
  }

  @Test
  public void testOnBlockIsValidReceiveWithValidTweets() throws NoSuchAlgorithmException, InvalidKeySpecException {
    List<Tweet> tweets = new ArrayList<>();
    keyPairGenerator.initKeys();
    String firstValidatorKey = keyPairGenerator.getPublicKey();
    tweets.add(createTweet());

    Envelope<BlockWithValidation> blockValidationEnvelope = envelopeGenerator(EnvelopeType.BLOCK_RECEIVE, tweets, firstValidatorKey);

    // send tweets
    tweets.forEach(tweet -> callbackHandler.onTweetReceived(new Envelope<>(tweet, EnvelopeType.TWEET_RECEIVE)));

    // select primary node
    var primaryNodeSelectionEnvelope = new Envelope<>(new ConnectionDetails(10, 12, "test",
      keyPairGenerator.getPublicKey()), EnvelopeType.PRIMARY_NODE_SELECTION);
    callbackHandler.onPrimaryNodeSelected(primaryNodeSelectionEnvelope);

    // simulate blockValidation
    callbackHandler.onBlockReceived(blockValidationEnvelope);

    // Block should be correctly mined and stored in the database
    Assertions.assertEquals(2, blockRepository.findAll().size());
    // The merkle tree should also be correctly stored in the database
    Assertions.assertEquals(1, merkleNodeRepository.findAll().size());
    // The queue should not contain any Tweets that were included in the last one
    Assertions.assertTrue(this.tweets.stream().noneMatch(tweetEnvelope ->
      tweets.stream().anyMatch(tweet2 -> tweet2.equals(tweetEnvelope.getData()))));
  }

  @Test
  public void testOnPrimaryNodeSelected() throws NoSuchAlgorithmException, InvalidKeySpecException {
    this.tweets.add(new Envelope<>(createTweet(), EnvelopeType.TWEET_RECEIVE));
    this.tweets.add(new Envelope<>(createTweet(), EnvelopeType.TWEET_RECEIVE));

    Envelope<ConnectionDetails> envelope = new Envelope<>(new ConnectionDetails(10, 11, "test",
      keyPairGenerator.getPublicKey()), EnvelopeType.PRIMARY_NODE_SELECTION);

    Optional<Envelope<BlockWithValidation>> response = callbackHandler.onPrimaryNodeSelected(envelope);

    Assertions.assertTrue(response.isPresent());
    Assertions.assertTrue(response.get().getData().isValid());
    Assertions.assertTrue(!this.tweets.isEmpty());
  }
}
