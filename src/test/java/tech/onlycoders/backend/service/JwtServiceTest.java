package tech.onlycoders.backend.service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import tech.onlycoders.backend.exception.ApiException;

@ExtendWith(MockitoExtension.class)
public class JwtServiceTest {

  @InjectMocks
  private JwtService jwtService;

  @BeforeEach
  public void setUp() {
    ReflectionTestUtils.setField(jwtService, "SECRET_KEY", "asdasd");
    ReflectionTestUtils.setField(jwtService, "ACCESS_EXPIRATION", 5000);
    ReflectionTestUtils.setField(jwtService, "REFRESH_EXPIRATION", 5000);
  }

  @Test
  public void ShouldSignToken() throws ApiException {
    var token = this.jwtService.createToken(new HashMap<>(), "subject");
    var details = this.jwtService.getUserDetails(token);
    assertEquals("subject", details.getEmail());
    assertNotNull(token);
  }

  @Test
  public void ShouldRejectTokenWithExpiredTTL() {
    ReflectionTestUtils.setField(jwtService, "REFRESH_EXPIRATION", -100);
    ReflectionTestUtils.setField(jwtService, "ACCESS_EXPIRATION", -100);
    var token = this.jwtService.createToken(new HashMap<>(), "subject");
    assertThrows(ApiException.class, () -> this.jwtService.verifyTTL(token));
    ReflectionTestUtils.setField(jwtService, "REFRESH_EXPIRATION", 5000);
  }

  @Test
  public void ShouldReturnIssuerWhenValidTTL() throws ApiException {
    ReflectionTestUtils.setField(jwtService, "ACCESS_EXPIRATION", -100);
    var token = this.jwtService.createToken(new HashMap<>(), "subject");
    var pair = this.jwtService.verifyTTL(token);
    assertEquals("subject", pair.getFirst());
  }

  @Test
  public void ShouldFailWhenMalFormedToken() {
    assertThrows(ApiException.class, () -> this.jwtService.verifyTTL("invalidToken"));
    assertThrows(ApiException.class, () -> this.jwtService.getUserDetails("invalidToken"));
  }
}
