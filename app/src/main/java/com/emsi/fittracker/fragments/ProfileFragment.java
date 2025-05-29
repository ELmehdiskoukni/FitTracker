package com.emsi.fittracker.fragments;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.emsi.fittracker.R;
import com.emsi.fittracker.LoginActivity;
import com.emsi.fittracker.interfaces.DataCallback;
import com.emsi.fittracker.models.User;
import com.emsi.fittracker.utils.FirebaseHelper;
import com.emsi.fittracker.utils.SharedPrefsManager;
import com.emsi.fittracker.utils.ValidationUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ProfileFragment extends Fragment {

    private static final String TAG = "ProfileFragment";

    // UI Components
    private CardView cvProfileInfo;
    private ImageView ivProfileAvatar;
    private TextView tvUserName, tvUserEmail, tvMemberSince;
    private Button btnEditName, btnEditEmail, btnSignOut;
    private ProgressBar progressBar;
    private View loadingOverlay;

    // Firebase & Utils
    private FirebaseHelper firebaseHelper;
    private SharedPrefsManager sharedPrefsManager;
    private FirebaseAuth mAuth;
    private String currentUserId;

    // State
    private boolean isLoading = false;
    private User currentUser;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        initFirebase();
        setupListeners();
        loadUserProfile();
    }

    private void initViews(View view) {
        cvProfileInfo = view.findViewById(R.id.cvProfileInfo);
        ivProfileAvatar = view.findViewById(R.id.ivProfileAvatar);
        tvUserName = view.findViewById(R.id.tvUserName);
        tvUserEmail = view.findViewById(R.id.tvUserEmail);
        tvMemberSince = view.findViewById(R.id.tvMemberSince);
        btnEditName = view.findViewById(R.id.btnEditName);
        btnEditEmail = view.findViewById(R.id.btnEditEmail);
        btnSignOut = view.findViewById(R.id.btnSignOut);
        progressBar = view.findViewById(R.id.progressBar);
        loadingOverlay = view.findViewById(R.id.loadingOverlay);
    }

    private void initFirebase() {
        mAuth = FirebaseAuth.getInstance();
        firebaseHelper = FirebaseHelper.getInstance();
        sharedPrefsManager = SharedPrefsManager.getInstance(getContext());

        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser != null) {
            currentUserId = firebaseUser.getUid();
        }
    }

    private void setupListeners() {
        btnEditName.setOnClickListener(v -> showEditNameDialog());
        btnEditEmail.setOnClickListener(v -> showEditEmailDialog());
        btnSignOut.setOnClickListener(v -> showSignOutDialog());
    }

    private void loadUserProfile() {
        if (currentUserId == null) {
            showError("Utilisateur non connecté");
            return;
        }

        // First try to load from cache
        User cachedUser = sharedPrefsManager.getCachedUser();
        if (cachedUser != null) {
            displayUserInfo(cachedUser);
        }

        // Then load from Firebase
        setLoadingState(true);
        firebaseHelper.getUser(currentUserId, new DataCallback<User>() {
            @Override
            public void onSuccess(User user) {
                if (getContext() != null && isAdded()) {
                    setLoadingState(false);
                    currentUser = user;
                    displayUserInfo(user);

                    // Cache the user data
                    sharedPrefsManager.cacheUser(user);
                }
            }

            @Override
            public void onFailure(String error) {
                if (getContext() != null && isAdded()) {
                    setLoadingState(false);

                    // If we have cached data, use it, otherwise show error
                    if (cachedUser == null) {
                        showError("Erreur de chargement: " + error);
                        // Try to get basic info from FirebaseAuth
                        loadBasicUserInfo();
                    }
                }
            }
        });
    }

    private void loadBasicUserInfo() {
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser != null) {
            String name = firebaseUser.getDisplayName();
            String email = firebaseUser.getEmail();

            if (name != null || email != null) {
                User basicUser = new User(firebaseUser.getUid(),
                        name != null ? name : "Utilisateur",
                        email != null ? email : "");
                displayUserInfo(basicUser);
                currentUser = basicUser;
            }
        }
    }

    private void displayUserInfo(User user) {
        if (user == null) return;

        // Display name
        String displayName = user.getName();
        if (displayName != null && !displayName.isEmpty()) {
            tvUserName.setText(displayName);
        } else {
            tvUserName.setText("Utilisateur");
        }

        // Display email
        String email = user.getEmail();
        if (email != null && !email.isEmpty()) {
            tvUserEmail.setText(email);
        } else {
            tvUserEmail.setText("Email non défini");
        }

        // Display member since date (using Firebase user creation time if available)
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser != null && firebaseUser.getMetadata() != null) {
            long creationTime = firebaseUser.getMetadata().getCreationTimestamp();
            Date creationDate = new Date(creationTime);
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
            tvMemberSince.setText("Membre depuis " + dateFormat.format(creationDate));
        } else {
            tvMemberSince.setText("Membre depuis récemment");
        }

        // Set profile avatar (first letter of name)
        String firstLetter = displayName != null && !displayName.isEmpty()
                ? displayName.substring(0, 1).toUpperCase()
                : "U";

        // You can set a background drawable with the letter or use a default avatar
        // For now, we'll just ensure the ImageView is visible
        ivProfileAvatar.setVisibility(View.VISIBLE);
    }

    private void showEditNameDialog() {
        if (getContext() == null) return;

        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_edit_text, null);
        EditText etNewValue = dialogView.findViewById(R.id.etNewValue);
        TextView tvError = dialogView.findViewById(R.id.tvError);

        // Pre-fill with current name
        if (currentUser != null && currentUser.getName() != null) {
            etNewValue.setText(currentUser.getName());
            etNewValue.setSelection(etNewValue.getText().length());
        }

        etNewValue.setHint("Nom complet");

        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setTitle("Modifier le nom")
                .setView(dialogView)
                .setPositiveButton("Modifier", null)
                .setNegativeButton("Annuler", null)
                .create();

        dialog.setOnShowListener(dialogInterface -> {
            Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);

            // Real-time validation
            etNewValue.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    String newName = s.toString().trim();
                    if (newName.isEmpty()) {
                        tvError.setText("Le nom est requis");
                        tvError.setVisibility(View.VISIBLE);
                        positiveButton.setEnabled(false);
                    } else if (!ValidationUtils.isValidName(newName)) {
                        tvError.setText("Format de nom invalide");
                        tvError.setVisibility(View.VISIBLE);
                        positiveButton.setEnabled(false);
                    } else {
                        tvError.setVisibility(View.GONE);
                        positiveButton.setEnabled(true);
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });

            positiveButton.setOnClickListener(v -> {
                String newName = etNewValue.getText().toString().trim();
                if (validateAndUpdateName(newName)) {
                    dialog.dismiss();
                }
            });
        });

        dialog.show();
    }

    private void showEditEmailDialog() {
        if (getContext() == null) return;

        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_edit_text, null);
        EditText etNewValue = dialogView.findViewById(R.id.etNewValue);
        TextView tvError = dialogView.findViewById(R.id.tvError);

        // Pre-fill with current email
        if (currentUser != null && currentUser.getEmail() != null) {
            etNewValue.setText(currentUser.getEmail());
            etNewValue.setSelection(etNewValue.getText().length());
        }

        etNewValue.setHint("Adresse email");

        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setTitle("Modifier l'email")
                .setView(dialogView)
                .setPositiveButton("Modifier", null)
                .setNegativeButton("Annuler", null)
                .create();

        dialog.setOnShowListener(dialogInterface -> {
            Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);

            // Real-time validation
            etNewValue.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    String newEmail = s.toString().trim();
                    if (newEmail.isEmpty()) {
                        tvError.setText("L'email est requis");
                        tvError.setVisibility(View.VISIBLE);
                        positiveButton.setEnabled(false);
                    } else if (!ValidationUtils.isValidEmail(newEmail)) {
                        tvError.setText("Format d'email invalide");
                        tvError.setVisibility(View.VISIBLE);
                        positiveButton.setEnabled(false);
                    } else {
                        tvError.setVisibility(View.GONE);
                        positiveButton.setEnabled(true);
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });

            positiveButton.setOnClickListener(v -> {
                String newEmail = etNewValue.getText().toString().trim();
                if (validateAndUpdateEmail(newEmail)) {
                    dialog.dismiss();
                }
            });
        });

        dialog.show();
    }

    private boolean validateAndUpdateName(String newName) {
        if (!ValidationUtils.isValidName(newName)) {
            showError("Format de nom invalide");
            return false;
        }

        if (currentUser != null && newName.equals(currentUser.getName())) {
            showError("Le nouveau nom est identique à l'ancien");
            return false;
        }

        updateUserName(newName);
        return true;
    }

    private boolean validateAndUpdateEmail(String newEmail) {
        if (!ValidationUtils.isValidEmail(newEmail)) {
            showError("Format d'email invalide");
            return false;
        }

        if (currentUser != null && newEmail.equals(currentUser.getEmail())) {
            showError("Le nouvel email est identique à l'ancien");
            return false;
        }

        updateUserEmail(newEmail);
        return true;
    }

    private void updateUserName(String newName) {
        if (currentUserId == null) {
            showError("Utilisateur non connecté");
            return;
        }

        setLoadingState(true);

        // Update Firebase Auth profile
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser != null) {
            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                    .setDisplayName(newName)
                    .build();

            firebaseUser.updateProfile(profileUpdates)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // Update Firestore user document
                            if (currentUser != null) {
                                currentUser.setName(newName);
                                updateUserInFirestore(currentUser, "Nom mis à jour avec succès");
                            }
                        } else {
                            setLoadingState(false);
                            showError("Erreur lors de la mise à jour du profil");
                        }
                    });
        }
    }

    private void updateUserEmail(String newEmail) {
        if (currentUserId == null) {
            showError("Utilisateur non connecté");
            return;
        }

        setLoadingState(true);

        // Update Firebase Auth email
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser != null) {
            firebaseUser.updateEmail(newEmail)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // Update Firestore user document
                            if (currentUser != null) {
                                currentUser.setEmail(newEmail);
                                updateUserInFirestore(currentUser, "Email mis à jour avec succès");
                            }
                        } else {
                            setLoadingState(false);
                            String errorMessage = task.getException() != null
                                    ? task.getException().getMessage()
                                    : "Erreur lors de la mise à jour de l'email";
                            showError(errorMessage);
                        }
                    });
        }
    }

    private void updateUserInFirestore(User user, String successMessage) {
        firebaseHelper.saveUser(user, new DataCallback<User>() {
            @Override
            public void onSuccess(User result) {
                if (getContext() != null && isAdded()) {
                    setLoadingState(false);
                    currentUser = result;
                    displayUserInfo(result);

                    // Update cache
                    sharedPrefsManager.cacheUser(result);

                    showSuccess(successMessage);
                }
            }

            @Override
            public void onFailure(String error) {
                if (getContext() != null && isAdded()) {
                    setLoadingState(false);
                    showError("Erreur lors de la sauvegarde: " + error);
                }
            }
        });
    }

    private void showSignOutDialog() {
        if (getContext() == null) return;

        new AlertDialog.Builder(getContext())
                .setTitle("Se déconnecter")
                .setMessage("Êtes-vous sûr de vouloir vous déconnecter ?")
                .setPositiveButton("Se déconnecter", (dialog, which) -> signOut())
                .setNegativeButton("Annuler", null)
                .show();
    }

    private void signOut() {
        setLoadingState(true);

        // Clear cached data
        sharedPrefsManager.clearUserCache();

        // Sign out from Firebase
        firebaseHelper.signOut();

        // Navigate to login screen
        Intent intent = new Intent(getContext(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);

        if (getActivity() != null) {
            getActivity().finish();
        }
    }

    private void setLoadingState(boolean loading) {
        isLoading = loading;
        if (loadingOverlay != null) {
            loadingOverlay.setVisibility(loading ? View.VISIBLE : View.GONE);
        }
        if (progressBar != null) {
            progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        }

        // Disable buttons during loading
        if (btnEditName != null) btnEditName.setEnabled(!loading);
        if (btnEditEmail != null) btnEditEmail.setEnabled(!loading);
        if (btnSignOut != null) btnSignOut.setEnabled(!loading);
    }

    private void showError(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    private void showSuccess(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh profile data when fragment becomes visible
        if (!isLoading && currentUserId != null) {
            loadUserProfile();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Clean up references
        currentUser = null;
        isLoading = false;
    }
}