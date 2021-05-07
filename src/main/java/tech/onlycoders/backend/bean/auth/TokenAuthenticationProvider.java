package tech.onlycoders.backend.bean.auth;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.stereotype.Component;
import tech.onlycoders.backend.exception.ApiException;
import tech.onlycoders.backend.service.JwtService;

@Component
public class TokenAuthenticationProvider implements AuthenticationProvider {

  private final JwtService jwtService;

  public TokenAuthenticationProvider(JwtService jwtService) {
    this.jwtService = jwtService;
  }

  @Override
  public Authentication authenticate(Authentication authentication) throws AuthenticationException {
    var token = (String) authentication.getPrincipal();
    if (token == null) {
      throw new BadCredentialsException("Invalid token");
    }
    try {
      var userDetails = jwtService.getUserDetails(token);
      var roles = AuthorityUtils.commaSeparatedStringToAuthorityList(userDetails.getRoles());
      return new UsernamePasswordAuthenticationToken(userDetails, token, roles);
    } catch (ApiException e) {
      return null;
    }
  }

  @Override
  public boolean supports(Class<?> authentication) {
    return authentication.equals(PreAuthenticatedAuthenticationToken.class);
  }
}
