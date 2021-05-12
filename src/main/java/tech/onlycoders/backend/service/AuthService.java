package tech.onlycoders.backend.service;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.util.HashMap;
import javax.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import tech.onlycoders.backend.bean.FirebaseService;
import tech.onlycoders.backend.bean.auth.UserDetails;
import tech.onlycoders.backend.dto.ApiErrorResponse;
import tech.onlycoders.backend.dto.auth.request.AuthRequestDto;
import tech.onlycoders.backend.dto.auth.response.AuthResponseDto;
import tech.onlycoders.backend.dto.user.request.CreateUserDto;
import tech.onlycoders.backend.dto.user.response.ReadUserDto;
import tech.onlycoders.backend.exception.ApiException;
import tech.onlycoders.backend.model.User;
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
      claims.put("canonicalName", person.getCanonicalName());
      claims.put("complete", true);
    } else {
      claims.put("complete", false);
    }
    var newToken = this.jwtService.createToken(claims, pairEmailIAT.getFirst());
    return AuthResponseDto.builder().token(newToken).build();
  }

  public AuthResponseDto postCreateUser(User person) {
    var claims = new HashMap<String, Object>();
    claims.put("roles", person.getRole());
    claims.put("canonicalName", person.getCanonicalName());
    claims.put("complete", true);
    var newToken = this.jwtService.createToken(claims, person.getEmail());
    return AuthResponseDto.builder().token(newToken).build();
  }
}
