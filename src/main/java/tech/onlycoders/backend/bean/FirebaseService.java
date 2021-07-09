package tech.onlycoders.backend.bean;

import static com.google.firebase.auth.AuthErrorCode.EMAIL_ALREADY_EXISTS;

import com.google.firebase.auth.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import tech.onlycoders.backend.exception.ApiException;
import tech.onlycoders.backend.service.NotificatorService;

@Service
public class FirebaseService {

  private final FirebaseAuth firebaseAuth;

  public FirebaseService(FirebaseAuth firebaseAuth) {
    this.firebaseAuth = firebaseAuth;
  }

  public void deleteAccount(String email) {
    try {
      var userRecord = firebaseAuth.getUserByEmail(email);
      firebaseAuth.deleteUser(userRecord.getUid());
    } catch (FirebaseAuthException e) {
      System.out.println("[E] Error removing user " + email + " from firebase auth");
    }
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

  public String createUser(String email) throws ApiException {
    var firebaseUser = new UserRecord.CreateRequest();
    firebaseUser.setEmail(email);
    firebaseUser.setEmailVerified(false);
    try {
      var resetPasswordAction = ActionCodeSettings.builder().setUrl("https://onlycoders.tech").build();
      this.firebaseAuth.createUser(firebaseUser);
      var passwordLink = this.firebaseAuth.generatePasswordResetLink(email, resetPasswordAction);
      var activateAccountAction = ActionCodeSettings.builder().setUrl(passwordLink).build();
      return this.firebaseAuth.generateEmailVerificationLink(email, activateAccountAction);
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
