package tech.onlycoders.backend;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;
import tech.onlycoders.backend.exception.ApiException;
import tech.onlycoders.backend.service.JwtService;

@RunWith(MockitoJUnitRunner.class)
public class JwtServiceTest {

  @InjectMocks
  private JwtService jwtService;

  @Before
  public void setUp() {
    ReflectionTestUtils.setField(jwtService, "SECRET_KEY", "asdasd");
    ReflectionTestUtils.setField(jwtService, "ACCESS_EXPIRATION", 5000);
    ReflectionTestUtils.setField(jwtService, "REFRESH_EXPIRATION", 5000);
  }

  @Test
  public void ShouldSignToken() throws ApiException {
    var token = this.jwtService.createToken(new HashMap<>(), "subject");
    var details = this.jwtService.getUserDetails(token);
    assertEquals("subject", details.getFirstName());
    assertNotNull(token);
  }
}
