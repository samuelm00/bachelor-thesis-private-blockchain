package security.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JwtGenerator {
  @Getter
  private final Algorithm algorithm;

  /**
   * Generates a JWT token from the user details.
   *
   * @param request The request.
   * @param user    The authenticated user.
   * @return The JWT token.
   */
  public String generateToken(HttpServletRequest request, User user) {
    return JWT
      .create()
      // username contains the users - public key
      .withSubject(user.getUsername())
      .withIssuer(request.getRequestURL().toString())
      .withClaim("roles", user.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList()))
      .sign(algorithm);
  }
}
