package config.beans;

import config.provider.BlockchainEndpointProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import service.BlockchainService;

@Configuration
@RequiredArgsConstructor
public class BlockchainServiceBean {
  private final BlockchainEndpointProvider endpointProvider;

  @Bean
  public BlockchainService blockchainService() {
    return new BlockchainService(endpointProvider, new RestTemplate());
  }
}
