package tech.onlycoders.backend.service;

import java.util.HashMap;
import org.springframework.stereotype.Service;
import tech.onlycoders.backend.bean.FirebaseService;
import tech.onlycoders.backend.dto.auth.request.AuthRequestDto;
import tech.onlycoders.backend.dto.auth.response.AuthResponseDto;
import tech.onlycoders.backend.exception.ApiException;
import tech.onlycoders.backend.repository.PersonRepository;

@Service
public class AuthService {

  private final PersonRepository personRepository;
  private final JwtService jwtService;
  private final FirebaseService firebaseService;

  public AuthService(PersonRepository personRepository, JwtService jwtService, FirebaseService firebaseService) {
    this.personRepository = personRepository;
    this.jwtService = jwtService;
    this.firebaseService = firebaseService;
  }

  public AuthResponseDto authenticate(AuthRequestDto authRequestDto) throws ApiException {
    var email = this.firebaseService.verifyFirebaseToken(authRequestDto.getFirebaseToken());
    return getAuthResponseDto(email);
  }

  public AuthResponseDto refreshToken(String token) throws ApiException {
    var email = this.jwtService.verifyTTL(token);
    return getAuthResponseDto(email);
  }

  private AuthResponseDto getAuthResponseDto(String email) {
    var optionalPerson = this.personRepository.findByEmail(email);
    var claims = new HashMap<String, Object>();
    if (optionalPerson.isPresent()) {
      var person = optionalPerson.get();
      claims.put("roles", person.getRole());
      claims.put("complete", false);
    } else {
      claims.put("complete", true);
    }
    var token = this.jwtService.createToken(claims, email);
    return AuthResponseDto.builder().token(token).build();
  }
}
