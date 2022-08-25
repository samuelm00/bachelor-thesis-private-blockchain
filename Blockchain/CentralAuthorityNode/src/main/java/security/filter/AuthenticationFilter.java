package security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import restPayload.AuthRequestPayload;
import security.util.JwtGenerator;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * This Class is used to authenticate the user and handles everything related to authentication.
 */
@Slf4j
public class AuthenticationFilter extends UsernamePasswordAuthenticationFilter {
  private final AuthenticationManager authenticationManager;
  private final JwtGenerator jwtGenerator;

  public AuthenticationFilter(AuthenticationManager authenticationManager, JwtGenerator jwtGenerator) {
    this.authenticationManager = authenticationManager;
    this.jwtGenerator = jwtGenerator;
  }

  /**
   * The function that is called when a user tries to log in.
   *
   * @param request  the request
   * @param response the response
   * @return the authentication
   */
  @Override
  public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
    try {
      AuthRequestPayload loginRequestPayload = new ObjectMapper().readValue(request.getInputStream(), AuthRequestPayload.class);
      log.info("Attempting to login: {}", loginRequestPayload.getPublicKey());
      UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(loginRequestPayload.getPublicKey(), loginRequestPayload.getPassword());
      return authenticationManager.authenticate(token);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * The function that is called when the login was successful.
   *
   * @param request    the request
   * @param response   the response
   * @param chain      the chain
   * @param authResult the auth result
   */
  @Override
  protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) {
    // The user that was successfully authenticated
    User user = (User) authResult.getPrincipal();
    String jwtToken = jwtGenerator.generateToken(request, user);

    // Add the token to the response
    response.addHeader("Authorization", jwtToken);
  }
}
