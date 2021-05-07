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
public class JwtUtil {

  private final String SECRET_KEY;
  private final Integer ACCESS_EXPIRATION;
  private final Integer REFRESH_EXPIRATION;

  public JwtUtil(
    @Value("${jwt.secret:DEFAULT_KEY_VALUE}") String secretKey,
    @Value("${only-coders.jwt.access-expires}") int accessExpiration,
    @Value("${only-coders.jwt.refresh-expires}") int refreshExpiration
  ) {
    this.ACCESS_EXPIRATION = accessExpiration;
    this.SECRET_KEY = secretKey;
    this.REFRESH_EXPIRATION = refreshExpiration;
  }

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
      return UserDetails.builder().firstName(userName).roles((String) claims.get("roles")).build();
    } catch (SignatureException | ExpiredJwtException | MalformedJwtException e) {
      throw new ApiException(HttpStatus.UNAUTHORIZED, "Unauthorized");
    }
  }
}
