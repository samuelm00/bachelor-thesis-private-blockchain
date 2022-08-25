package security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import security.filter.AuthenticationFilter;
import security.filter.JwtTokenFilter;
import security.util.AuthPath;
import security.util.JwtAuthorizer;
import security.util.JwtGenerator;
import service.UserService;

import javax.servlet.http.HttpServletResponse;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig extends WebSecurityConfigurerAdapter {
  private final UserService userService;
  private final JwtGenerator jwtGenerator;
  private final PasswordEncoder bCryptPasswordEncoder;

  /**
   * Configures the authentication manager such that it uses the {@link UserService} to retrieve users.
   *
   * @param auth the {@link AuthenticationManagerBuilder} to use
   * @throws Exception if the UserService can't find a user
   */
  @Override
  protected void configure(AuthenticationManagerBuilder auth) throws Exception {
    auth.userDetailsService(publicKey ->
        userService
          .findByPublicKey(publicKey)
          .orElseThrow(() -> new RuntimeException("User not found")))
      .passwordEncoder(bCryptPasswordEncoder);
  }

  @Bean
  public AuthenticationManager authenticationManagerBean() throws Exception {
    return super.authenticationManager();
  }

  /**
   * Configures the {@link HttpSecurity} to use the {@link AuthenticationFilter} and the {@link JwtTokenFilter}
   *
   * @param http the {@link HttpSecurity} to use
   */
  @Override
  protected void configure(HttpSecurity http) throws Exception {
    AuthenticationFilter authenticationFilter = new AuthenticationFilter(authenticationManagerBean(), jwtGenerator);

    authenticationFilter.setFilterProcessesUrl(AuthPath.LOGIN.getPathWithBase() + "/**");

    // Enable CORS and disable CSRF
    http = http.cors().and().csrf().disable();

    // Set session management to stateless
    http = http
      .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
      .and();

    // Set unauthorized requests exception handler
    http = http
      .exceptionHandling()
      .authenticationEntryPoint(
        (request, response, ex) -> response.sendError(
          HttpServletResponse.SC_UNAUTHORIZED,
          ex.getMessage()
        )
      )
      .and();

    // Set permissions on endpoints
    http.authorizeRequests()
      .antMatchers(AuthPath.LOGIN.getPathWithBase() + "/**").permitAll()
      .antMatchers(AuthPath.REGISTER.getPathWithBase() + "/**").permitAll();

    // Add Authentication Filter
    http.addFilter(authenticationFilter);
    // Add JWT Filter
    http.addFilterBefore(new JwtTokenFilter(new JwtAuthorizer(jwtGenerator.getAlgorithm())), UsernamePasswordAuthenticationFilter.class);
  }
}
