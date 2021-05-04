package tech.onlycoders.backend.bean.auth;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.web.filter.GenericFilterBean;
import org.springframework.web.servlet.HandlerExceptionResolver;
import tech.onlycoders.backend.exception.ApiException;

public class AuthenticationFilter extends GenericFilterBean {

  private static final Logger logger = LoggerFactory.getLogger(AuthenticationFilter.class);
  public static final String TOKEN_SESSION_KEY = "token";
  public static final String USER_SESSION_KEY = "user";
  private final AuthenticationManager authenticationManager;

  private final HandlerExceptionResolver resolver;

  public AuthenticationFilter(
    AuthenticationManager authenticationManager,
    @Qualifier("handlerExceptionResolver") HandlerExceptionResolver resolver
  ) {
    this.authenticationManager = authenticationManager;
    this.resolver = resolver;
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
    throws IOException, ServletException {
    HttpServletRequest httpRequest = asHttp(request);
    HttpServletResponse httpResponse = asHttp(response);

    var authorizationHeader = httpRequest.getHeader("Authorization");

    if (authorizationHeader == null) {
      resolver.resolveException(
        asHttp(request),
        asHttp(response),
        null,
        new ApiException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED")
      );
      return;
    }

    var bearerToken = authorizationHeader.substring(7);
    var cookies = Optional.ofNullable(httpRequest.getCookies());
    if (cookies.isPresent()) {
      var signature = Arrays.stream(cookies.get()).filter(cookie -> cookie.getName().equals("SESSION")).findFirst();
      if (signature.isPresent()) {
        bearerToken = bearerToken.concat(".").concat(signature.get().getValue());
      }
    }

    try {
      logger.debug("Trying to authenticate user by Authorization method. Token: {}", bearerToken);
      processTokenAuthentication(bearerToken);
      logger.debug("AuthenticationFilter is passing request down the filter chain");
      chain.doFilter(request, response);
    } catch (InternalAuthenticationServiceException internalAuthenticationServiceException) {
      SecurityContextHolder.clearContext();
      logger.error("Internal authentication service exception", internalAuthenticationServiceException);
      httpResponse.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    } catch (AuthenticationException authenticationException) {
      resolver.resolveException(
        asHttp(request),
        asHttp(response),
        null,
        new ApiException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED")
      );
      SecurityContextHolder.clearContext();
    } finally {
      MDC.remove(TOKEN_SESSION_KEY);
      MDC.remove(USER_SESSION_KEY);
    }
  }

  private HttpServletRequest asHttp(ServletRequest request) {
    return (HttpServletRequest) request;
  }

  private HttpServletResponse asHttp(ServletResponse response) {
    return (HttpServletResponse) response;
  }

  private void processTokenAuthentication(String token) {
    Authentication resultOfAuthentication = tryToAuthenticateWithToken(token);
    SecurityContextHolder.getContext().setAuthentication(resultOfAuthentication);
  }

  private Authentication tryToAuthenticateWithToken(String token) {
    PreAuthenticatedAuthenticationToken requestAuthentication = new PreAuthenticatedAuthenticationToken(token, null);
    return tryToAuthenticate(requestAuthentication);
  }

  private Authentication tryToAuthenticate(Authentication requestAuthentication) {
    Authentication responseAuthentication = authenticationManager.authenticate(requestAuthentication);
    if (responseAuthentication == null || !responseAuthentication.isAuthenticated()) {
      throw new InternalAuthenticationServiceException("Unable to authenticate Domain User for provided credentials");
    }
    logger.debug("User successfully authenticated");
    return responseAuthentication;
  }
}
