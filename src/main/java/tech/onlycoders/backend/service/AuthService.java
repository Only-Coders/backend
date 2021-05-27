package tech.onlycoders.backend.service;

import java.util.HashMap;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.onlycoders.backend.bean.FirebaseService;
import tech.onlycoders.backend.dto.auth.request.AuthRequestDto;
import tech.onlycoders.backend.dto.auth.response.AuthResponseDto;
import tech.onlycoders.backend.exception.ApiException;
import tech.onlycoders.backend.model.Person;
import tech.onlycoders.backend.model.User;
import tech.onlycoders.backend.repository.AdminRepository;
import tech.onlycoders.backend.repository.PersonRepository;
import tech.onlycoders.backend.repository.UserRepository;

@Service
@Transactional
public class AuthService {

  private final UserRepository userRepository;
  private final JwtService jwtService;
  private final FirebaseService firebaseService;
  private final AdminRepository adminRepository;

  public AuthService(
    PersonRepository personRepository,
    UserRepository userRepository,
    JwtService jwtService,
    FirebaseService firebaseService,
    AdminRepository adminRepository
  ) {
    this.userRepository = userRepository;
    this.jwtService = jwtService;
    this.firebaseService = firebaseService;
    this.adminRepository = adminRepository;
  }

  public AuthResponseDto authenticate(AuthRequestDto authRequestDto) throws ApiException {
    var email = this.firebaseService.verifyFirebaseToken(authRequestDto.getFirebaseToken());
    HashMap<String, Object> claims;
    if (email.endsWith("onlycoders.tech")) {
      claims = this.adminAuthentication(email);
    } else {
      claims = this.userAuthentication(email);
    }
    var token = this.jwtService.createToken(claims, email);
    return AuthResponseDto.builder().token(token).build();
  }

  private HashMap<String, Object> adminAuthentication(String email) {
    var optionalAdmin = this.adminRepository.findByEmail(email);
    HashMap<String, Object> claims = new HashMap<>();
    if (optionalAdmin.isPresent()) {
      var admin = optionalAdmin.get();
      claims = extendClaims(admin);
    }
    return claims;
  }

  private HashMap<String, Object> userAuthentication(String email) {
    var optionalUser = this.userRepository.findByEmail(email);
    HashMap<String, Object> claims = new HashMap<>();
    if (optionalUser.isPresent()) {
      var user = optionalUser.get();
      claims = extendClaims(user);
      claims.put("defaultPrivacy", user.getDefaultPrivacyIsPublic());
    }
    return claims;
  }

  private HashMap<String, Object> extendClaims(Person person) {
    var claims = new HashMap<String, Object>();
    claims.put("id", person.getId());
    claims.put("roles", person.getRole().getName());
    claims.put("canonicalName", person.getCanonicalName());
    claims.put("complete", true);
    claims.put("imageURI", person.getImageURI());
    claims.put("fullName", person.getFirstName() + " " + person.getLastName());
    return claims;
  }

  public AuthResponseDto refreshToken(String token) throws ApiException {
    var pairEmailIAT = this.jwtService.verifyTTL(token);
    var email = pairEmailIAT.getFirst();
    var claims = new HashMap<String, Object>();
    if (email.endsWith("onlycoders.tech")) {
      claims = this.adminAuthentication(email);
    } else {
      claims = this.userAuthentication(email);
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
