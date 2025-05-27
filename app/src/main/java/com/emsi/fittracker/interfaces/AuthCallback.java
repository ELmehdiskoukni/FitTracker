package com.emsi.fittracker.interfaces;

import com.google.firebase.auth.FirebaseUser;

public interface AuthCallback {
    void onSuccess(FirebaseUser user);
    void onFailure(String error);
}