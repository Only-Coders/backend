package tech.onlycoders.backend.service;

import java.util.HashMap;
import org.springframework.http.HttpStatus;
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
    var optionalPerson = this.personRepository.findByEmail(email);
    var claims = new HashMap<String, Object>();
    if (optionalPerson.isPresent()) {
      var person = optionalPerson.get();
      claims.put("roles", person.getRole().getName());
      claims.put("canonicalName", person.getCanonicalName());
      claims.put("complete", true);
    } else {
      claims.put("complete", false);
    }
    var token = this.jwtService.createToken(claims, email);
    return AuthResponseDto.builder().token(token).build();
  }

  public AuthResponseDto refreshToken(String token) throws ApiException {
    var pairEmailIAT = this.jwtService.verifyTTL(token);
    var optionalPerson = this.personRepository.findByEmail(pairEmailIAT.getFirst());
    var claims = new HashMap<String, Object>();
    if (optionalPerson.isPresent()) {
      var person = optionalPerson.get();
      if (person.getSecurityUpdate() != null && person.getSecurityUpdate().after(pairEmailIAT.getSecond())) {
        throw new ApiException(HttpStatus.UNAUTHORIZED, "Unauthorized");
      }
      claims.put("roles", person.getRole());
      claims.put("complete", true);
    } else {
      claims.put("complete", false);
    }
    var newToken = this.jwtService.createToken(claims, pairEmailIAT.getFirst());
    return AuthResponseDto.builder().token(newToken).build();
  }
}
