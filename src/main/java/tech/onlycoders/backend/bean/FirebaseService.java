package tech.onlycoders.backend.bean;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import tech.onlycoders.backend.exception.ApiException;

@Service
public class FirebaseService {

  private final FirebaseAuth firebaseAuth;

  public FirebaseService(FirebaseAuth firebaseAuth) {
    this.firebaseAuth = firebaseAuth;
  }

  public String verifyFirebaseToken(String firebaseToken) throws ApiException {
    try {
      FirebaseToken token = firebaseAuth.verifyIdToken(firebaseToken);
      if (!token.isEmailVerified()) {
        throw new ApiException(HttpStatus.FORBIDDEN, "Email not verified");
      }
      return token.getEmail();
    } catch (FirebaseAuthException e) {
      throw new ApiException(HttpStatus.FORBIDDEN, "Invalid firebase token");
    }
  }
}
