package config.endpoint;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Contains all endpoints and query-params for the Blockchain
 */
public enum BlockchainEndpoint {
  LOGIN("/login", null), REGISTER("/register", null), POST_TWEET("/tweet", null),
  GET_TWEETS_OF_USER("/tweets", null), GET_TWEET("/tweet", "?hash=");

  private final String endpoint;
  private final String queryParam;

  BlockchainEndpoint(String endpoint, String queryParam) {
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
    StringBuilder res = new StringBuilder(endpoint);

    if (queryParam == null || queryParamValue == null) {
      return res.toString();
    }

    res.append(queryParam);

    if (encode) {
      res.append(URLEncoder.encode(queryParamValue, StandardCharsets.UTF_8));
    } else {
      res.append(queryParamValue);
    }
    return res.toString();
  }
}
