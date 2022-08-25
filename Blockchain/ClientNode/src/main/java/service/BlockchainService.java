package service;

import config.endpoint.BlockchainEndpoint;
import config.provider.BlockchainEndpointProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.MerkleNode;
import model.Tweet;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import restPayload.AuthRequestPayload;


import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
@RequiredArgsConstructor
public class BlockchainService {
  private final BlockchainEndpointProvider endpointProvider;
  private final RestTemplate restTemplate;


  /**
   * @param publicKey The public key of the user
   * @param password  The password the user wants to use
   * @return True if registration was successful false otherwise
   */
  public boolean login(String publicKey, String password) {
    String endpoint = endpointProvider.getEndpoint(BlockchainEndpoint.LOGIN, null);
    var response = restTemplate.exchange(endpoint,
      HttpMethod.POST, new HttpEntity<>(new AuthRequestPayload(publicKey, password)), ResponseEntity.class);

    endpointProvider.setToken("Bearer " + Objects.requireNonNull(response.getHeaders().get("Authorization")).get(0));

    return !response.getStatusCode().isError();
  }

  /**
   * @param publicKey The public key of the user
   * @param password  The password the user wants to login with
   * @return True if login was successful false otherwise
   */
  public boolean register(String publicKey, String password) {
    String endpoint = endpointProvider.getEndpoint(BlockchainEndpoint.REGISTER, null);
    var response = restTemplate.exchange(endpoint,
      HttpMethod.POST, new HttpEntity<>(new AuthRequestPayload(publicKey, password)), ResponseEntity.class);

    return !response.getStatusCode().isError();
  }

  /**
   * @param tweet The tweet to be added
   * @return True if the tweet was added successfully false otherwise
   */
  public boolean postTweet(Tweet tweet) {
    String endpoint = endpointProvider.getEndpoint(BlockchainEndpoint.POST_TWEET, null);
    var response = restTemplate.exchange(endpoint,
      HttpMethod.POST, new HttpEntity<>(tweet, endpointProvider.getHttpHeaders()), ResponseEntity.class);
    return !response.getStatusCode().isError();
  }

  /**
   * @return The list of tweets of the user
   */
  public List<MerkleNode> getMyTweets() {
    String endpoint = endpointProvider.getEndpoint(BlockchainEndpoint.GET_TWEETS_OF_USER, null);
    var response = restTemplate.exchange(endpoint,
      HttpMethod.GET, new HttpEntity<>(null, endpointProvider.getHttpHeaders()), MerkleNode[].class);

    if (response.getStatusCode().isError() || response.getBody() == null) {
      return new ArrayList<>();
    }

    return List.of(response.getBody());
  }

  /**
   * @param hash The hash of the tweet
   * @return The tweet with the given hash
   */
  public Tweet getTweet(String hash) {
    String endpoint = endpointProvider.getEndpoint(BlockchainEndpoint.GET_TWEET, hash);
    var response = restTemplate.exchange(endpoint, HttpMethod.GET,
      new HttpEntity<>(null, endpointProvider.getHttpHeaders()), MerkleNode.class);

    if (response.getStatusCode().isError() || response.getBody() == null) {
      return null;
    }

    return response.getBody().getTweet();
  }
}
