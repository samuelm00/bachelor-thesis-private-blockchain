package controller;

import com.auth0.jwt.algorithms.Algorithm;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
import security.util.JwtAuthorizer;

@Testcontainers
@SpringBootTest(classes = {main.CentralAuthorityNodeApplication.class},
  properties = {"jwt.secret=secret", "spring.jpa.hibernate.ddl-auto=update"},
  webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AuthTests {
  private final String loginEndpoint = "/api/login";
  private final String registerEndpoint = "/api/register";
  private final String getTweetEndpoint = "/api/tweets";
  private JwtAuthorizer jwtAuthorizer;

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
  void initServices() {
    repository.deleteAll();
    if (jwtAuthorizer == null) {
      jwtAuthorizer = new JwtAuthorizer(algorithm);
    }
  }

  @Test
  public void testSetup() {
    Assertions.assertNotNull(repository);
    Assertions.assertNotNull(algorithm);
    Assertions.assertNotNull(restTemplate);
    Assertions.assertNotNull(jwtAuthorizer);
  }

  @Test
  public void testRegister() {
    restTemplate.postForObject(registerEndpoint, new AuthRequestPayload("test", "password"), AuthRequestPayload.class);
    Assertions.assertTrue(repository.findByPublicKey("test").isPresent());
  }

  @Test
  public void testLogin() {
    AuthRequestPayload payload = new AuthRequestPayload("test", "password");
    restTemplate.postForObject(registerEndpoint, payload, AuthRequestPayload.class);

    var response = restTemplate.exchange(loginEndpoint, HttpMethod.POST, new HttpEntity<>(payload), AuthRequestPayload.class);
    Assertions.assertTrue(response.getHeaders().containsKey("Authorization"));
    String token = response.getHeaders().get("Authorization").get(0);
    Assertions.assertEquals(jwtAuthorizer.verifyToken(token), payload.getPublicKey());
  }


  @Test
  public void testGetTweetRejection() {
    var response = restTemplate.exchange(getTweetEndpoint, HttpMethod.GET, null, String.class);
    Assertions.assertEquals(response.getStatusCodeValue(), 401);
  }

  @Test
  public void testJwtValidation() {
    AuthRequestPayload payload = new AuthRequestPayload("test", "password");
    restTemplate.postForObject(registerEndpoint, payload, AuthRequestPayload.class);

    // login to receive the token
    var response = restTemplate.exchange(loginEndpoint, HttpMethod.POST, new HttpEntity<>(payload), AuthRequestPayload.class);
    Assertions.assertTrue(response.getHeaders().containsKey("Authorization"));
    String token = response.getHeaders().get("Authorization").get(0);

    // set the token in the header
    HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", "Bearer " + token);

    HttpEntity<String> entity = new HttpEntity<>(null, headers);

    var tweetResponse = restTemplate.exchange(getTweetEndpoint, HttpMethod.GET, entity, String.class);
    Assertions.assertEquals(400, tweetResponse.getStatusCodeValue());
  }
}

