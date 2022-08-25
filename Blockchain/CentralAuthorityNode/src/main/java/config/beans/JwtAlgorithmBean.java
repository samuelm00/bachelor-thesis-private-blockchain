package config.beans;

import com.auth0.jwt.algorithms.Algorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JwtAlgorithmBean {
  @Bean
  public Algorithm algorithm(@Value("${jwt.secret}") String secret) {
    return Algorithm.HMAC256(secret);
  }
}
