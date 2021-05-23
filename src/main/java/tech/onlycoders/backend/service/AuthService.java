package tech.onlycoders.backend.service;

import java.util.HashMap;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.onlycoders.backend.bean.FirebaseService;
import tech.onlycoders.backend.dto.auth.request.AuthRequestDto;
import tech.onlycoders.backend.dto.auth.response.AuthResponseDto;
import tech.onlycoders.backend.exception.ApiException;
import tech.onlycoders.backend.model.Person;
import tech.onlycoders.backend.model.User;
import tech.onlycoders.backend.repository.PersonRepository;

@Service
@Transactional
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
      extendClaims(claims, person);
    } else {
      claims.put("complete", false);
    }
    var token = this.jwtService.createToken(claims, email);
    return AuthResponseDto.builder().token(token).build();
  }

  private void extendClaims(HashMap<String, Object> claims, Person person) {
    claims.put("id", person.getId());
    claims.put("roles", person.getRole().getName());
    claims.put("canonicalName", person.getCanonicalName());
    claims.put("complete", true);
    claims.put("imageURI", person.getImageURI());
    claims.put("fullName", person.getFirstName() + " " + person.getLastName());
  }

  public AuthResponseDto refreshToken(String token) throws ApiException {
    var pairEmailIAT = this.jwtService.verifyTTL(token);
    var optionalPerson = this.personRepository.findByEmail(pairEmailIAT.getFirst());
    var claims = new HashMap<String, Object>();
    if (optionalPerson.isPresent()) {
      var person = optionalPerson.get();
      if (person.getSecurityUpdate() != null && person.getSecurityUpdate().after(pairEmailIAT.getSecond())) {
        throw new ApiException(HttpStatus.UNAUTHORIZED, "error.not-authorized");
      }
      extendClaims(claims, person);
    } else {
      claims.put("complete", false);
    }
    var newToken = this.jwtService.createToken(claims, pairEmailIAT.getFirst());
    return AuthResponseDto.builder().token(newToken).build();
  }

  public AuthResponseDto postCreateUser(User person) {
    var claims = new HashMap<String, Object>();
    claims.put("id", person.getId());
    claims.put("roles", person.getRole().getName());
    claims.put("canonicalName", person.getCanonicalName());
    claims.put("complete", true);
    var newToken = this.jwtService.createToken(claims, person.getEmail());
    return AuthResponseDto.builder().token(newToken).build();
  }
}
