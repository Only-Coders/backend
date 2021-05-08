package tech.onlycoders.backend.service;

import io.jsonwebtoken.*;
import java.util.Date;
import java.util.HashMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import tech.onlycoders.backend.bean.auth.UserDetails;
import tech.onlycoders.backend.exception.ApiException;

@Service
public class JwtService {

  @Value("${jwt.secret:DEFAULT_KEY_VALUE}")
  private String SECRET_KEY;

  @Value("${only-coders.jwt.access-expires:500}")
  private Integer ACCESS_EXPIRATION;

  @Value("${only-coders.jwt.refresh-expires:500}")
  private Integer REFRESH_EXPIRATION;

  public String createToken(HashMap<String, Object> claims, String subject) {
    claims.put("ttl", new Date(System.currentTimeMillis() + 1000L * this.REFRESH_EXPIRATION));
    return Jwts
      .builder()
      .setClaims((claims))
      .setId("asd")
      .setSubject(subject)
      .setIssuedAt(new Date(System.currentTimeMillis()))
      .setExpiration(new Date(System.currentTimeMillis() + 1000L * ACCESS_EXPIRATION))
      .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
      .compact();
  }

  public UserDetails getUserDetails(String token) throws ApiException {
    try {
      var claims = Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(token).getBody();
      var userName = claims.getSubject();
      var canonicalName = (String) claims.get("canonicalName");
      var roles = (String) claims.get("roles");
      return UserDetails.builder().firstName(userName).canonicalName(canonicalName).roles(roles).build();
    } catch (SignatureException | ExpiredJwtException | MalformedJwtException e) {
      throw new ApiException(HttpStatus.UNAUTHORIZED, "Unauthorized");
    }
  }
}
