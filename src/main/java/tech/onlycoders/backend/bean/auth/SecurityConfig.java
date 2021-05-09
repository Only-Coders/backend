package tech.onlycoders.backend.bean.auth;

import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

  private final TokenAuthenticationProvider tokenAuthenticationProvider;
  private final HandlerExceptionResolver resolver;

  @Value("${only-coders.cookie.name}")
  private String COOKIE_NAME;

  public SecurityConfig(
    TokenAuthenticationProvider tokenAuthenticationProvider,
    @Qualifier("handlerExceptionResolver") HandlerExceptionResolver resolver
  ) {
    this.tokenAuthenticationProvider = tokenAuthenticationProvider;
    this.resolver = resolver;
  }

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http
      .csrf()
      .disable()
      .sessionManagement()
      .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
      .and()
      .authorizeRequests()
      .anyRequest() //            antMatchers(actuatorEndpoints()).hasRole(backendAdminRole).
      .authenticated()
      .and()
      .anonymous()
      .disable()
      .exceptionHandling()
      .authenticationEntryPoint(unauthorizedEntryPoint());
    http.cors();
    http.addFilterBefore(
      new AuthenticationFilter(authenticationManager(), resolver, COOKIE_NAME),
      BasicAuthenticationFilter.class
    );
  }

  @Override
  public void configure(WebSecurity web) {
    web.ignoring().antMatchers("/api/auth/login", "/swagger-ui/**", "/api-docs/**");
  }

  @Override
  protected void configure(AuthenticationManagerBuilder auth) {
    auth.authenticationProvider(tokenAuthenticationProvider);
  }

  @Bean
  public AuthenticationEntryPoint unauthorizedEntryPoint() {
    return (request, response, authException) -> response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
  }
}
