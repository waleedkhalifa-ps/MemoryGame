package com.localpulse.data;

import android.content.Context;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import java.util.Map;

public class AuthRepository {
  public interface Result {
    void done(String error);
  }

  public static boolean configured(Context context) {
    return !FirebaseApp.getApps(context).isEmpty();
  }

  public void login(String email, String password, Result result) {
    FirebaseAuth.getInstance()
        .signInWithEmailAndPassword(email, password)
        .addOnCompleteListener(
            t ->
                result.done(
                    t.isSuccessful()
                        ? null
                        : t.getException() == null
                            ? "Authentication failed"
                            : t.getException().getLocalizedMessage()));
  }

  public void register(String name, String email, String password, Result result) {
    FirebaseAuth.getInstance()
        .createUserWithEmailAndPassword(email, password)
        .addOnCompleteListener(
            t -> {
              if (!t.isSuccessful()) {
                result.done(
                    t.getException() == null
                        ? "Registration failed"
                        : t.getException().getLocalizedMessage());
                return;
              }
              String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
              FirebaseDatabase.getInstance()
                  .getReference("users")
                  .child(uid)
                  .setValue(
                      Map.of(
                          "displayName",
                          name,
                          "createdAt",
                          com.google.firebase.database.ServerValue.TIMESTAMP))
                  .addOnCompleteListener(
                      profile ->
                          result.done(
                              profile.isSuccessful()
                                  ? null
                                  : "Account created; profile could not be saved. Please sign in"
                                        + " and retry."));
            });
  }

  public void reset(String email, Result result) {
    FirebaseAuth.getInstance()
        .sendPasswordResetEmail(email)
        .addOnCompleteListener(
            t ->
                result.done(
                    t.isSuccessful()
                        ? null
                        : t.getException() == null
                            ? "Reset failed"
                            : t.getException().getLocalizedMessage()));
  }

  public void logout() {
    FirebaseAuth.getInstance().signOut();
  }
}
