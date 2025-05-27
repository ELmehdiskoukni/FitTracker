package com.emsi.fittracker;



import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.emsi.fittracker.R;
import com.emsi.fittracker.utils.ValidationUtils;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvRegisterLink, tvForgotPassword;
    private TextView tvEmailError, tvPasswordError;
    private ProgressBar progressBar;

    private FirebaseAuth mAuth;
    private boolean isEmailValid = false;
    private boolean isPasswordValid = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

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
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegisterLink = findViewById(R.id.tvRegisterLink);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        tvEmailError = findViewById(R.id.tvEmailError);
        tvPasswordError = findViewById(R.id.tvPasswordError);
        progressBar = findViewById(R.id.progressBar);

        // Initially disable login button
        btnLogin.setEnabled(false);
    }

    private void setupListeners() {
        btnLogin.setOnClickListener(v -> loginUser());

        tvRegisterLink.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });

        tvForgotPassword.setOnClickListener(v -> handleForgotPassword());
    }

    private void setupValidation() {
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
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
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
        updateLoginButtonState();
    }

    private void validatePassword(String password) {
        if (password.isEmpty()) {
            showPasswordError(getString(R.string.error_empty_password));
            isPasswordValid = false;
        } else if (password.length() < 6) {
            showPasswordError(getString(R.string.error_short_password));
            isPasswordValid = false;
        } else {
            hidePasswordError();
            isPasswordValid = true;
        }
        updateLoginButtonState();
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

    private void updateLoginButtonState() {
        btnLogin.setEnabled(isEmailValid && isPasswordValid);
    }

    private void loginUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Show loading
        setLoadingState(true);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    setLoadingState(false);

                    if (task.isSuccessful()) {
                        // Sign in success
                        Toast.makeText(LoginActivity.this,
                                getString(R.string.success_login),
                                Toast.LENGTH_SHORT).show();

                        // Navigate to MainActivity
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();

                    } else {
                        // Sign in failed
                        handleLoginError(task.getException());
                    }
                });
    }

    private void handleLoginError(Exception exception) {
        String errorMessage;

        if (exception instanceof FirebaseAuthInvalidUserException) {
            errorMessage = getString(R.string.error_user_not_found);
        } else if (exception instanceof FirebaseAuthInvalidCredentialsException) {
            errorMessage = getString(R.string.error_wrong_password);
        } else {
            errorMessage = getString(R.string.error_network);
        }

        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
    }

    private void handleForgotPassword() {
        String email = etEmail.getText().toString().trim();

        if (email.isEmpty()) {
            Toast.makeText(this, getString(R.string.error_empty_email), Toast.LENGTH_SHORT).show();
            etEmail.requestFocus();
            return;
        }

        if (!ValidationUtils.isValidEmail(email)) {
            Toast.makeText(this, getString(R.string.error_invalid_email), Toast.LENGTH_SHORT).show();
            etEmail.requestFocus();
            return;
        }

        setLoadingState(true);

        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    setLoadingState(false);

                    if (task.isSuccessful()) {
                        Toast.makeText(LoginActivity.this,
                                "Email de récupération envoyé",
                                Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(LoginActivity.this,
                                "Erreur lors de l'envoi de l'email",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void setLoadingState(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        btnLogin.setEnabled(!isLoading && isEmailValid && isPasswordValid);
        etEmail.setEnabled(!isLoading);
        etPassword.setEnabled(!isLoading);
    }
}