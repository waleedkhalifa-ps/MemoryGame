package com.localpulse.ui;

import android.widget.*;
import com.google.firebase.auth.FirebaseAuth;
import com.localpulse.R;
import com.localpulse.data.AuthRepository;

public class AuthFragment extends BaseFragment {
  private boolean registration, busy;

  @Override
  protected void render() {
    title(registration ? R.string.register : R.string.login);
    if (!AuthRepository.configured(context)) {
      note(R.string.firebase_needed);
      note(R.string.local_only);
      return;
    }
    if (FirebaseAuth.getInstance().getCurrentUser() != null) {
      Ui.note(page, FirebaseAuth.getInstance().getCurrentUser().getEmail());
      note(R.string.local_only);
      button(
          R.string.logout,
          () -> {
            new AuthRepository().logout();
            redraw();
          });
      return;
    }
    EditText name = registration ? Ui.field(page, getString(R.string.full_name), "", false) : null;
    EditText email = Ui.field(page, getString(R.string.email), "", false);
    email.setInputType(
        android.text.InputType.TYPE_CLASS_TEXT
            | android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
    EditText pass = Ui.field(page, getString(R.string.password), "", false);
    pass.setInputType(
        android.text.InputType.TYPE_CLASS_TEXT
            | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
    CheckBox consent = new CheckBox(context);
    consent.setText(R.string.responsibility);
    if (registration) Ui.add(page, consent);
    com.google.android.material.button.MaterialButton submit =
        Ui.button(
            context, getString(registration ? R.string.register : R.string.login), false, () -> {});
    Ui.add(page, submit);
    submit.setOnClickListener(
        v -> {
          if (busy) return;
          String address = email.getText().toString().trim(), password = pass.getText().toString();
          if (!android.util.Patterns.EMAIL_ADDRESS.matcher(address).matches()) {
            email.setError(getString(R.string.required));
            return;
          }
          if (password.length() < 6) {
            pass.setError("6+ characters");
            return;
          }
          if (registration
              && (name.getText().toString().trim().isEmpty() || !consent.isChecked())) {
            toast(R.string.review_required);
            return;
          }
          busy = true;
          submit.setEnabled(false);
          AuthRepository auth = new AuthRepository();
          AuthRepository.Result callback =
              error -> {
                busy = false;
                if (!isAdded()) return;
                if (error != null) {
                  Toast.makeText(context, error, Toast.LENGTH_LONG).show();
                  submit.setEnabled(true);
                } else redraw();
              };
          if (registration)
            auth.register(name.getText().toString().trim(), address, password, callback);
          else auth.login(address, password, callback);
        });
    outline(
        registration ? R.string.login : R.string.register,
        () -> {
          registration = !registration;
          redraw();
        });
    outline(
        R.string.reset_password,
        () -> {
          String address = email.getText().toString().trim();
          if (!android.util.Patterns.EMAIL_ADDRESS.matcher(address).matches()) {
            email.setError(getString(R.string.required));
            return;
          }
          new AuthRepository()
              .reset(
                  address,
                  error -> {
                    if (isAdded())
                      Toast.makeText(
                              context,
                              error == null ? getString(R.string.saved) : error,
                              Toast.LENGTH_LONG)
                          .show();
                  });
        });
  }
}
