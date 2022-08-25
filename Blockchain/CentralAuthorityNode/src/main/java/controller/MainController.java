package controller;

import config.endpoint.ValidationNodeEndpoint;
import config.provider.ValidationNodeEndpointProvider;
import envelope.Envelope;
import envelope.EnvelopeType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.MerkleNode;
import model.Tweet;
import model.User;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import p2pServer.P2PServer;
import restPayload.AuthRequestPayload;
import security.util.JwtAuthorizer;
import service.UserService;

import java.util.List;


@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class MainController {
  private final P2PServer p2pServer;
  private final UserService userService;
  private final RestTemplate restTemplate;
  private final JwtAuthorizer jwtAuthorizer;

  /**
   * @param tweet tweet to be added to the ledger {@link model.Tweet}
   * @return {@link ResponseEntity} with an empty body
   */
  @PostMapping(value = "/tweet")
  public ResponseEntity<?> addTweetToBlockchain(@RequestBody Tweet tweet) {
    Envelope<Tweet> envelope = new Envelope<>(tweet, EnvelopeType.TWEET_RECEIVE);
    boolean success = p2pServer.sendTweetToOtherPeers(envelope);
    return success ? ResponseEntity.ok(null) : ResponseEntity.badRequest().body(null);
  }

  /**
   * @param authorization header with the JWT token {@link security.util.JwtAuthorizer}
   * @return {@link ResponseEntity} with the list of {@link model.MerkleNode}
   */
  @GetMapping(value = "/tweets")
  public ResponseEntity<?> getTweets(@RequestHeader("Authorization") String authorization) {
    String publicKey = jwtAuthorizer.verifyToken(authorization.substring("Bearer ".length()));

    log.info("Fetching all tweets of user with public-key: {}", publicKey);

    String endpoint;

    try {
      endpoint = ValidationNodeEndpointProvider.getEndpoint(
        ValidationNodeEndpoint.GET_TWEETS_OF_USER.getEndpoint(publicKey, true), p2pServer.getConnections());
    } catch (Exception e) {
      log.error("Failed to get endpoint for user with public-key: {}", publicKey);
      return ResponseEntity.badRequest().body(null);
    }

    return restTemplate.exchange(endpoint, HttpMethod.GET, null, List.class);
  }

  /**
   * @param hash hash of the {@link model.MerkleNode} that contains the tweet that the user is trying to query.
   * @return {@link ResponseEntity} with the {@link model.MerkleNode}
   */
  @GetMapping(value = "/tweet")
  public ResponseEntity<?> getTweet(@RequestParam(name = "hash") String hash) {
    log.info("Fetching tweet that is inside the merkle-node with hash: {}", hash);

    String endpoint = ValidationNodeEndpointProvider.getEndpoint(
      ValidationNodeEndpoint.GET_TWEET.getEndpoint(hash, false), p2pServer.getConnections());

    return restTemplate.exchange(endpoint, HttpMethod.GET, null, MerkleNode.class);
  }

  /**
   * @param loginRequestPayload payload with the user's login credentials {@link restPayload.AuthRequestPayload}
   * @return {@link ResponseEntity} with an empty body
   */
  @PostMapping(value = "/register")
  public ResponseEntity<?> register(@RequestBody AuthRequestPayload loginRequestPayload) {
    User user = new User(loginRequestPayload.getPublicKey(), loginRequestPayload.getPassword());
    try {
      userService.save(user);
    } catch (Exception e) {
      log.error(e.getMessage());
      e.printStackTrace();
      return ResponseEntity.badRequest().body("User already exists");
    }
    return ResponseEntity.ok(null);
  }
}
