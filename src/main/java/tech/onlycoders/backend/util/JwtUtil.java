package tech.onlycoders.backend.util;

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

  public JwtUtil(@Value("${jwt.secret:DEFAULT_KEY_VALUE}") String secret_key) {
    this.SECRET_KEY = secret_key;
  }

  public String createToken(HashMap<String, Object> claims, String subject) {
    claims.put("ttl", new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24 * 5));
    return Jwts
      .builder()
      .setClaims((claims))
      .setId("asd")
      .setSubject(subject)
      .setIssuedAt(new Date(System.currentTimeMillis()))
      .setExpiration(new Date(System.currentTimeMillis() + 1000 * 5)) //TODO: 5 Horas
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

  public String refreshToken(String token) throws ApiException {
    try {
      var claims = Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(token).getBody();
      return token;
    } catch (ExpiredJwtException e) {
      HashMap<String, Object> expectedMap = new HashMap<>(e.getClaims());
      return this.createToken(expectedMap, e.getClaims().getSubject());
    } catch (SignatureException | MalformedJwtException e) {
      throw new ApiException(HttpStatus.UNAUTHORIZED, "Unauthorized");
    }
  }
}
