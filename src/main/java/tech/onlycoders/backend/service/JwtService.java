package tech.onlycoders.backend.service;

import io.jsonwebtoken.*;
import java.util.Date;
import java.util.HashMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import tech.onlycoders.backend.bean.auth.UserDetails;
import tech.onlycoders.backend.exception.ApiException;

@Service
public class JwtService {

  @Value("${only-coders.jwt.secret:DEFAULT_KEY_VALUE}")
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
      .setSubject(subject)
      .setIssuedAt(new Date(System.currentTimeMillis()))
      .setExpiration(new Date(System.currentTimeMillis() + 1000L * ACCESS_EXPIRATION))
      .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
      .compact();
  }

  public UserDetails getUserDetails(String token) throws ApiException {
    try {
      var claims = Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(token).getBody();
      var email = claims.getSubject();
      var canonicalName = (String) claims.get("canonicalName");
      var roles = (String) claims.get("roles");
      var id = (String) claims.get("id");
      return UserDetails.builder().id(id).email(email).canonicalName(canonicalName).roles(roles).build();
    } catch (SignatureException | ExpiredJwtException | MalformedJwtException e) {
      throw new ApiException(HttpStatus.UNAUTHORIZED, "Unauthorized");
    }
  }

  public Pair<String, Date> verifyTTL(String token) throws ApiException {
    try {
      var claims = Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(token).getBody();
      return Pair.of(claims.getSubject(), claims.getIssuedAt());
    } catch (ExpiredJwtException e) {
      var ttl = e.getClaims().get("ttl");
      var expirationDate = new Date((Long) ttl);
      var today = new Date();
      if (expirationDate.after(today)) {
        return Pair.of(e.getClaims().getSubject(), e.getClaims().getIssuedAt());
      } else {
        throw new ApiException(HttpStatus.UNAUTHORIZED, "Unauthorized");
      }
    } catch (SignatureException | MalformedJwtException e) {
      throw new ApiException(HttpStatus.UNAUTHORIZED, "Unauthorized");
    }
  }
}
