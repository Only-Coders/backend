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
    FirebaseToken token;
    try {
      token = firebaseAuth.verifyIdToken(firebaseToken);
    } catch (FirebaseAuthException e) {
      throw new ApiException(HttpStatus.FORBIDDEN, " invalid Token");
    }
    var name = token.getName();
    System.out.println(" Id: " + name);
    return name;
  }
}
