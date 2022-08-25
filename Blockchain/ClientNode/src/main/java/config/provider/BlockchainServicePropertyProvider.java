package config.provider;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BlockchainServicePropertyProvider {
  @Getter
  @Setter
  @Value("${blockchain.service.url}")
  private String blockchainUrl;

  @Getter
  @Setter
  private String token;
}
