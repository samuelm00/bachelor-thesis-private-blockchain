package controller;

import com.auth0.jwt.algorithms.Algorithm;
import config.runner.PrimaryNodeSelectionRunner;
import model.Tweet;
import model.User;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import p2pServer.P2PServer;
import repository.UserRepository;
import restPayload.AuthRequestPayload;
import util.key.RSAUtils;
import utils.TestKeyPairGenerator;

import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.InvalidKeySpecException;


@Testcontainers
@SpringBootTest(classes = {main.CentralAuthorityNodeApplication.class},
  properties = {"jwt.secret=secret", "spring.jpa.hibernate.ddl-auto=update"},
  webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AddTweetTests {
  private final String loginEndpoint = "/api/login";
  private final String registerEndpoint = "/api/register";
  private final String addTweetEndpoint = "/api/tweet";
  private String bearerToken;
  private User user;
  private final TestKeyPairGenerator keyPairGenerator = new TestKeyPairGenerator();

  @MockBean
  private PrimaryNodeSelectionRunner primaryNodeSelectionRunner;

  @Container
  public static PostgreSQLContainer<?> dbContainer = new PostgreSQLContainer<>(DockerImageName.parse("postgres"))
    .withDatabaseName("ledger")
    .withUsername("username")
    .withPassword("password");

  @Autowired
  private UserRepository repository;

  @Autowired
  private Algorithm algorithm;

  @Autowired
  private TestRestTemplate restTemplate;

  @MockBean
  private P2PServer p2PServer;

  @DynamicPropertySource
  static void postgresProps(DynamicPropertyRegistry registry) {
    dbContainer.start();
    registry.add("spring.datasource.url", dbContainer::getJdbcUrl);
    registry.add("spring.datasource.username", dbContainer::getUsername);
    registry.add("spring.datasource.password", dbContainer::getPassword);
    System.out.println(dbContainer.getJdbcUrl());
  }

  @BeforeEach
  public void registerAndLogIn() {
    if (bearerToken != null && keyPairGenerator.getPrivateKey() != null && keyPairGenerator.getPublicKey() != null) {
      return;
    }
    keyPairGenerator.initKeys();
    user = new User(keyPairGenerator.getPublicKey(), "password");

    AuthRequestPayload payload = new AuthRequestPayload(user.getPublicKey(), user.getPassword());
    restTemplate.postForObject(registerEndpoint, payload, AuthRequestPayload.class);

    var response = restTemplate.exchange(loginEndpoint, HttpMethod.POST, new HttpEntity<>(payload), AuthRequestPayload.class);
    String token = response.getHeaders().get("Authorization").get(0);
    bearerToken = "Bearer " + token;
  }

  private Tweet createTweet() throws NoSuchAlgorithmException, InvalidKeySpecException {
    String tweetContent = "Test tweet";
    RSAPrivateKey privateKey = RSAUtils.importPrivateKey(keyPairGenerator.getPrivateKey());
    String signature = RSAUtils.sign(privateKey, tweetContent);
    return new Tweet(tweetContent, keyPairGenerator.getPublicKey(), signature, System.currentTimeMillis());
  }

  @Test
  public void testSetup() {
    Assertions.assertNotNull(repository);
    Assertions.assertNotNull(algorithm);
    Assertions.assertNotNull(restTemplate);
    Assertions.assertNotNull(keyPairGenerator.getPrivateKey());
    Assertions.assertNotNull(keyPairGenerator.getPublicKey());
    Assertions.assertNotNull(user);
  }

  @Test
  public void testAddTweet() throws NoSuchAlgorithmException, InvalidKeySpecException {
    HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", bearerToken);

    Tweet tweet = createTweet();
    HttpEntity<Tweet> entity = new HttpEntity<>(tweet, headers);

    var response = restTemplate.exchange(addTweetEndpoint, HttpMethod.POST, entity, Tweet.class);

    // 400 because there are no validation nodes registered yet
    Assertions.assertEquals(400, response.getStatusCodeValue());
  }
}
