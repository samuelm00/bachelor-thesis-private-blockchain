package config.provider;

import config.endpoint.BlockchainEndpoint;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;

@Configuration
@RequiredArgsConstructor
public class BlockchainEndpointProvider {
  private final BlockchainServicePropertyProvider props;

  public HttpHeaders getHttpHeaders() {
    var headers = new HttpHeaders();
    headers.add("Authorization", props.getToken());
    return headers;
  }

  public String getEndpoint(BlockchainEndpoint endpoint, String queryParamValue) {
    return props.getBlockchainUrl() + endpoint.getEndpoint(queryParamValue, true);
  }

  public void setToken(String jwtToken) {
    props.setToken(jwtToken);
  }

  @Bean
  public BlockchainEndpointProvider endpointProvider() {
    return new BlockchainEndpointProvider(props);
  }
}
