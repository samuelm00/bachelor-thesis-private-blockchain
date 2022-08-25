package security.filter;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.filter.OncePerRequestFilter;
import security.util.AuthPath;
import security.util.JwtAuthorizer;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;

/**
 * Intercepts every single request and checks if the request has a valid JWT token.
 */
@Slf4j
@RequiredArgsConstructor
public class JwtTokenFilter extends OncePerRequestFilter {
  private final AuthPath[] publicAuthPaths = {AuthPath.LOGIN, AuthPath.REGISTER};
  private final JwtAuthorizer jwtAuthorizer;

  /**
   * Is called for every request and checks if the request has a valid JWT token.
   *
   * @param request     the request
   * @param response    the response
   * @param filterChain the filter chain
   */
  @Override
  protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
                                  @NonNull FilterChain filterChain) throws ServletException, IOException {
    if (isPublicRequest(request)) {
      log.info("No Auth needed for {}", request.getRequestURI());
      filterChain.doFilter(request, response);
    } else {
      String authHeader = request.getHeader("Authorization");

      // check if the auth header is valid
      if (isAuthHeaderValid(authHeader)) {
        String token = getTokenFromAuthHeader(authHeader);
        try {
          jwtAuthorizer.verifyToken(token);
          filterChain.doFilter(request, response);
        } catch (Exception e) {
          log.error("Error while checking token", e);
          response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
        }
      } else {
        log.error("No valid auth header found");
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
      }
    }
  }

  /**
   * Checks if the request is a public request.
   *
   * @param request the request
   * @return true if the request is a public request
   */
  private boolean isPublicRequest(HttpServletRequest request) {
    return Arrays.stream(publicAuthPaths).anyMatch(authPath -> request.getRequestURI().contains(authPath.getPathWithBase()));
  }

  /**
   * Checks if the auth header is valid.
   *
   * @param authHeader the auth header
   * @return true if the auth header is valid
   */
  private boolean isAuthHeaderValid(String authHeader) {
    return authHeader != null && authHeader.startsWith("Bearer ");
  }

  /**
   * Gets the token from the auth header.
   *
   * @param authHeader the auth header containing the token
   * @return the token
   */
  private String getTokenFromAuthHeader(String authHeader) {
    return authHeader.substring("Bearer ".length());
  }
}
