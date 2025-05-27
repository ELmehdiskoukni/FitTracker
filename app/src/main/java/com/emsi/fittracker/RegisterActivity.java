package com.emsi.fittracker;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.emsi.fittracker.R;
import com.emsi.fittracker.utils.ValidationUtils;

public class RegisterActivity extends AppCompatActivity {

    private EditText etFullName, etEmail, etPassword, etConfirmPassword;
    private CheckBox cbTerms;
    private Button btnRegister;
    private TextView tvLoginLink;
    private TextView tvNameError, tvEmailError, tvPasswordError, tvConfirmPasswordError, tvTermsError;
    private ProgressBar progressBar;

    private FirebaseAuth mAuth;
    private boolean isNameValid = false;
    private boolean isEmailValid = false;
    private boolean isPasswordValid = false;
    private boolean isConfirmPasswordValid = false;
    private boolean areTermsAccepted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Initialize views
        initViews();

        // Set up listeners
        setupListeners();

        // Set up real-time validation
        setupValidation();
    }

    private void initViews() {
        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        cbTerms = findViewById(R.id.cbTerms);
        btnRegister = findViewById(R.id.btnRegister);
        tvLoginLink = findViewById(R.id.tvLoginLink);

        tvNameError = findViewById(R.id.tvNameError);
        tvEmailError = findViewById(R.id.tvEmailError);
        tvPasswordError = findViewById(R.id.tvPasswordError);
        tvConfirmPasswordError = findViewById(R.id.tvConfirmPasswordError);
        tvTermsError = findViewById(R.id.tvTermsError);
        progressBar = findViewById(R.id.progressBar);

        // Initially disable register button
        btnRegister.setEnabled(false);
    }

    private void setupListeners() {
        btnRegister.setOnClickListener(v -> registerUser());

        tvLoginLink.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish();
        });

        cbTerms.setOnCheckedChangeListener((buttonView, isChecked) -> {
            areTermsAccepted = isChecked;
            if (isChecked) {
                hideTermsError();
            } else {
                showTermsError(getString(R.string.error_terms_not_accepted));
            }
            updateRegisterButtonState();
        });
    }

    private void setupValidation() {
        // Full name validation
        etFullName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateFullName(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Email validation
        etEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateEmail(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Password validation
        etPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validatePassword(s.toString());
                // Re-validate confirm password if it's not empty
                String confirmPassword = etConfirmPassword.getText().toString();
                if (!confirmPassword.isEmpty()) {
                    validateConfirmPassword(confirmPassword);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Confirm password validation
        etConfirmPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateConfirmPassword(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void validateFullName(String name) {
        if (name.trim().isEmpty()) {
            showNameError(getString(R.string.error_empty_name));
            isNameValid = false;
        } else if (name.trim().length() < 2) {
            showNameError("Le nom doit contenir au moins 2 caractères");
            isNameValid = false;
        } else {
            hideNameError();
            isNameValid = true;
        }
        updateRegisterButtonState();
    }

    private void validateEmail(String email) {
        if (email.isEmpty()) {
            showEmailError(getString(R.string.error_empty_email));
            isEmailValid = false;
        } else if (!ValidationUtils.isValidEmail(email)) {
            showEmailError(getString(R.string.error_invalid_email));
            isEmailValid = false;
        } else {
            hideEmailError();
            isEmailValid = true;
        }
        updateRegisterButtonState();
    }

    private void validatePassword(String password) {
        if (password.isEmpty()) {
            showPasswordError(getString(R.string.error_empty_password));
            isPasswordValid = false;
        } else if (password.length() < 6) {
            showPasswordError(getString(R.string.error_short_password));
            isPasswordValid = false;
        } else if (!ValidationUtils.isStrongPassword(password)) {
            showPasswordError("Le mot de passe doit contenir au moins une majuscule, une minuscule et un chiffre");
            isPasswordValid = false;
        } else {
            hidePasswordError();
            isPasswordValid = true;
        }
        updateRegisterButtonState();
    }

    private void validateConfirmPassword(String confirmPassword) {
        String password = etPassword.getText().toString();

        if (confirmPassword.isEmpty()) {
            showConfirmPasswordError("Veuillez confirmer votre mot de passe");
            isConfirmPasswordValid = false;
        } else if (!confirmPassword.equals(password)) {
            showConfirmPasswordError(getString(R.string.error_password_mismatch));
            isConfirmPasswordValid = false;
        } else {
            hideConfirmPasswordError();
            isConfirmPasswordValid = true;
        }
        updateRegisterButtonState();
    }

    private void showNameError(String error) {
        tvNameError.setText(error);
        tvNameError.setVisibility(View.VISIBLE);
    }

    private void hideNameError() {
        tvNameError.setVisibility(View.GONE);
    }

    private void showEmailError(String error) {
        tvEmailError.setText(error);
        tvEmailError.setVisibility(View.VISIBLE);
    }

    private void hideEmailError() {
        tvEmailError.setVisibility(View.GONE);
    }

    private void showPasswordError(String error) {
        tvPasswordError.setText(error);
        tvPasswordError.setVisibility(View.VISIBLE);
    }

    private void hidePasswordError() {
        tvPasswordError.setVisibility(View.GONE);
    }

    private void showConfirmPasswordError(String error) {
        tvConfirmPasswordError.setText(error);
        tvConfirmPasswordError.setVisibility(View.VISIBLE);
    }

    private void hideConfirmPasswordError() {
        tvConfirmPasswordError.setVisibility(View.GONE);
    }

    private void showTermsError(String error) {
        tvTermsError.setText(error);
        tvTermsError.setVisibility(View.VISIBLE);
    }

    private void hideTermsError() {
        tvTermsError.setVisibility(View.GONE);
    }

    private void updateRegisterButtonState() {
        btnRegister.setEnabled(isNameValid && isEmailValid && isPasswordValid &&
                isConfirmPasswordValid && areTermsAccepted);
    }

    private void registerUser() {
        String fullName = etFullName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Show loading
        setLoadingState(true);

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Registration successful, update user profile
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            updateUserProfile(user, fullName);
                        }
                    } else {
                        setLoadingState(false);
                        handleRegistrationError(task.getException());
                    }
                });
    }

    private void updateUserProfile(FirebaseUser user, String fullName) {
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(fullName)
                .build();

        user.updateProfile(profileUpdates)
                .addOnCompleteListener(task -> {
                    setLoadingState(false);

                    if (task.isSuccessful()) {
                        Toast.makeText(RegisterActivity.this,
                                getString(R.string.success_registration),
                                Toast.LENGTH_SHORT).show();

                        // Navigate to MainActivity
                        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();

                    } else {
                        Toast.makeText(RegisterActivity.this,
                                "Erreur lors de la mise à jour du profil",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void handleRegistrationError(Exception exception) {
        String errorMessage;

        if (exception instanceof FirebaseAuthWeakPasswordException) {
            errorMessage = getString(R.string.error_weak_password);
        } else if (exception instanceof FirebaseAuthUserCollisionException) {
            errorMessage = getString(R.string.error_user_exists);
        } else {
            errorMessage = getString(R.string.error_network);
        }

        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
    }

    private void setLoadingState(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        btnRegister.setEnabled(!isLoading && isNameValid && isEmailValid &&
                isPasswordValid && isConfirmPasswordValid && areTermsAccepted);
        etFullName.setEnabled(!isLoading);
        etEmail.setEnabled(!isLoading);
        etPassword.setEnabled(!isLoading);
        etConfirmPassword.setEnabled(!isLoading);
        cbTerms.setEnabled(!isLoading);
    }
}