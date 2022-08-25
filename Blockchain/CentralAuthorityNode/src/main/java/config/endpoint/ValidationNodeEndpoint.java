package config.endpoint;


import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Enum that contains all the endpoints + queryParam-strings of the ValidationNodes
 */
public enum ValidationNodeEndpoint {
  GET_TWEETS_OF_USER("/tweets", "?publicKey="), GET_TWEET("/tweet", "?hash=");

  private final String base = "/api";
  private final String endpoint;
  private final String queryParam;

  ValidationNodeEndpoint(String endpoint, String queryParam) {
    this.endpoint = endpoint;
    this.queryParam = queryParam;
  }

  /**
   * Get the endpoint with or without encoding
   *
   * @param queryParamValue the value for the query parameter
   * @param encode          if true the query param value will be encoded otherwise not {@link URLEncoder#encode(String, String)}
   * @return the endpoint
   */
  public String getEndpoint(String queryParamValue, Boolean encode) {
    StringBuilder res = new StringBuilder(base + endpoint + queryParam);
    if (encode) {
      res.append(URLEncoder.encode(queryParamValue, StandardCharsets.UTF_8));
    } else {
      res.append(queryParamValue);
    }
    return res.toString();
  }
}


