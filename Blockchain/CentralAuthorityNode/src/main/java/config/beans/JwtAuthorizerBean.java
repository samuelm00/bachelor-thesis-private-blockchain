package config.beans;

import com.auth0.jwt.algorithms.Algorithm;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import security.util.JwtAuthorizer;

@Configuration
@RequiredArgsConstructor
public class JwtAuthorizerBean {
  private final Algorithm algorithm;

  @Bean
  public JwtAuthorizer jwtAuthorizer() {
    return new JwtAuthorizer(algorithm);
  }
}
