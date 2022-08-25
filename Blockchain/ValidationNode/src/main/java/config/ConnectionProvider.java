package config;

import connection.ConnectionDetails;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import propertyFactory.YamlPropertiesFactory;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "validation-nodes")
@PropertySource(value = "classpath:validation-nodes.yml", factory = YamlPropertiesFactory.class)
public class ConnectionProvider {
  @Getter
  @Setter
  private List<ConnectionDetails> connections;
}