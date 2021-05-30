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
import tech.onlycoders.backend.repository.WorkPositionRepository;

@Service
@Transactional
public class AuthService {

  private final UserRepository userRepository;
  private final JwtService jwtService;
  private final FirebaseService firebaseService;
  private final AdminRepository adminRepository;
  private final WorkPositionRepository workPositionRepository;

  public AuthService(
    PersonRepository personRepository,
    UserRepository userRepository,
    JwtService jwtService,
    FirebaseService firebaseService,
    AdminRepository adminRepository,
    WorkPositionRepository workPositionRepository
  ) {
    this.userRepository = userRepository;
    this.jwtService = jwtService;
    this.firebaseService = firebaseService;
    this.adminRepository = adminRepository;
    this.workPositionRepository = workPositionRepository;
  }

  public AuthResponseDto authenticate(AuthRequestDto authRequestDto) throws ApiException {
    var email = this.firebaseService.verifyFirebaseToken(authRequestDto.getFirebaseToken());
    HashMap<String, Object> claims;
    if (email.endsWith("onlycoders.tech") || email.equals("onlycoders.tech@gmail.com")) {
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
      extendClaims(admin, claims);
    }
    return claims;
  }

  private HashMap<String, Object> userAuthentication(String email) {
    var optionalUser = this.userRepository.findByEmail(email);
    final HashMap<String, Object> claims = new HashMap<>();
    if (optionalUser.isPresent()) {
      var user = optionalUser.get();
      extendClaims(user, claims);
      this.workPositionRepository.getUserCurrentPosition(user.getCanonicalName())
        .ifPresentOrElse(
          workPosition ->
            claims.put("currentPosition", workPosition.getPosition() + " - " + workPosition.getWorkplace().getName()),
          () -> claims.put("currentPosition", "")
        );
      claims.put("defaultPrivacy", user.getDefaultPrivacyIsPublic());
    }
    return claims;
  }

  private void extendClaims(Person person, HashMap<String, Object> claims) {
    claims.put("id", person.getId());
    claims.put("roles", person.getRole().getName());
    claims.put("canonicalName", person.getCanonicalName());
    claims.put("complete", true);
    claims.put("imageURI", person.getImageURI());
    claims.put("fullName", person.getFirstName() + " " + person.getLastName());
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
