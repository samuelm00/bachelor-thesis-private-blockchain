package security.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthorizer {
  @Getter
  private final Algorithm algorithm;

  /**
   * Verifies the JWT token and sets the user as the principal in the security context.
   *
   * @param token the JWT token
   * @return returns the public key of the user
   */
  public String verifyToken(String token) {
    if (token == null) {
      throw new IllegalArgumentException("Token cannot be null");
    }

    // verify token
    JWTVerifier verifier = JWT.require(algorithm).build();
    DecodedJWT decodedJWT = verifier.verify(token);
    String publicKey = decodedJWT.getSubject();

    log.info("Token for following public Key is valid {}", publicKey);
    UsernamePasswordAuthenticationToken authenticationToken =
      new UsernamePasswordAuthenticationToken(publicKey, null, null);
    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
    return publicKey;
  }
}
