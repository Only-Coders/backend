package tech.onlycoders.backend.bean;

import static com.google.firebase.auth.AuthErrorCode.EMAIL_ALREADY_EXISTS;

import com.google.firebase.auth.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import tech.onlycoders.backend.exception.ApiException;
import tech.onlycoders.backend.service.NotificatorService;
import tech.onlycoders.notificator.dto.EventType;
import tech.onlycoders.notificator.dto.MessageDTO;

@Service
public class FirebaseService {

  private final FirebaseAuth firebaseAuth;
  private final NotificatorService notificatorService;

  public FirebaseService(FirebaseAuth firebaseAuth, NotificatorService notificatorService) {
    this.firebaseAuth = firebaseAuth;
    this.notificatorService = notificatorService;
  }

  public String verifyFirebaseToken(String firebaseToken) throws ApiException {
    try {
      FirebaseToken token = firebaseAuth.verifyIdToken(firebaseToken);
      if (!token.isEmailVerified()) {
        throw new ApiException(HttpStatus.FORBIDDEN, "error.email-not-verified");
      }
      return token.getEmail();
    } catch (FirebaseAuthException e) {
      throw new ApiException(HttpStatus.FORBIDDEN, "error.invalid-firebase-token");
    }
  }

  public void createUser(String email) throws ApiException {
    var admin = new UserRecord.CreateRequest();
    admin.setEmail(email);
    admin.setEmailVerified(false);
    try {
      var resetPasswordAction = ActionCodeSettings.builder().setUrl("https://onlycoders.tech").build();
      this.firebaseAuth.createUser(admin);
      var passwordLink = this.firebaseAuth.generatePasswordResetLink(email, resetPasswordAction);
      var activateAccountAction = ActionCodeSettings.builder().setUrl(passwordLink).build();
      var activateLink = this.firebaseAuth.generateEmailVerificationLink(email, activateAccountAction);

      this.notificatorService.send(
          MessageDTO.builder().eventType(EventType.NEW_ADMIN_ACCOUNT).to(email).message(activateLink).build()
        );
    } catch (FirebaseAuthException e) {
      var code = e.getAuthErrorCode();
      if (code.equals(EMAIL_ALREADY_EXISTS)) {
        throw new ApiException(HttpStatus.CONFLICT, "error.email-taken");
      }
      e.printStackTrace();
      throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "error.500");
    }
  }
}
