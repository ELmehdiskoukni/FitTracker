package com.emsi.fittracker.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.emsi.fittracker.models.User;
import org.json.JSONException;
import org.json.JSONObject;

public class SharedPrefsManager {
    private static final String TAG = "SharedPrefsManager";
    private static final String PREF_NAME = "FitTrackerPrefs";
    private static final String KEY_USER_DATA = "user_data";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_LAST_SYNC = "last_sync";
    private static final String KEY_APP_VERSION = "app_version";
    private static final String KEY_THEME_MODE = "theme_mode";
    private static final String KEY_UNIT_SYSTEM = "unit_system"; // metric or imperial

    private static SharedPrefsManager instance;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    private SharedPrefsManager(Context context) {
        sharedPreferences = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public static SharedPrefsManager getInstance(Context context) {
        if (instance == null) {
            synchronized (SharedPrefsManager.class) {
                if (instance == null) {
                    instance = new SharedPrefsManager(context);
                }
            }
        }
        return instance;
    }

    // User Data Methods
    public void cacheUser(User user) {
        if (user == null) {
            Log.w(TAG, "Attempted to cache null user");
            return;
        }

        try {
            // Convert User to JSON manually
            JSONObject userJson = new JSONObject();
            userJson.put("uid", user.getUid() != null ? user.getUid() : "");
            userJson.put("name", user.getName() != null ? user.getName() : "");
            userJson.put("email", user.getEmail() != null ? user.getEmail() : "");
            userJson.put("height", user.getHeight());
            userJson.put("weight", user.getWeight());
            userJson.put("age", user.getAge());
            userJson.put("gender", user.getGender() != null ? user.getGender() : "");
            userJson.put("activityLevel", user.getActivityLevel() != null ? user.getActivityLevel() : "");
            userJson.put("createdAt", user.getCreatedAt());
            userJson.put("updatedAt", user.getUpdatedAt());

            editor.putString(KEY_USER_DATA, userJson.toString());
            editor.putString(KEY_USER_NAME, user.getName() != null ? user.getName() : "");
            editor.putString(KEY_USER_EMAIL, user.getEmail() != null ? user.getEmail() : "");
            editor.putString(KEY_USER_ID, user.getUid() != null ? user.getUid() : "");
            editor.putBoolean(KEY_IS_LOGGED_IN, true);
            editor.putLong(KEY_LAST_SYNC, System.currentTimeMillis());
            editor.apply();

            Log.d(TAG, "User cached successfully: " + (user.getEmail() != null ? user.getEmail() : "unknown"));
        } catch (JSONException e) {
            Log.e(TAG, "Error creating JSON for user data", e);
        } catch (Exception e) {
            Log.e(TAG, "Error caching user data", e);
        }
    }

    public User getCachedUser() {
        try {
            String userJsonString = sharedPreferences.getString(KEY_USER_DATA, null);
            if (userJsonString != null && !userJsonString.isEmpty()) {
                JSONObject userJson = new JSONObject(userJsonString);

                User user = new User();
                user.setUid(userJson.optString("uid", ""));
                user.setName(userJson.optString("name", ""));
                user.setEmail(userJson.optString("email", ""));
                user.setHeight(userJson.optDouble("height", 0.0));
                user.setWeight(userJson.optDouble("weight", 0.0));
                user.setAge(userJson.optInt("age", 0));
                user.setGender(userJson.optString("gender", ""));
                user.setActivityLevel(userJson.optString("activityLevel", ""));
                user.setCreatedAt(userJson.optLong("createdAt", 0));
                user.setUpdatedAt(userJson.optLong("updatedAt", 0));

                Log.d(TAG, "Retrieved cached user: " + user.getEmail());
                return user;
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error parsing cached user data", e);
            clearUserCache(); // Clear corrupted data
        } catch (Exception e) {
            Log.e(TAG, "Error retrieving cached user", e);
        }
        return null;
    }

    public void updateCachedUserName(String name) {
        User cachedUser = getCachedUser();
        if (cachedUser != null) {
            cachedUser.setName(name);
            cacheUser(cachedUser);
        } else {
            editor.putString(KEY_USER_NAME, name);
            editor.apply();
        }
        Log.d(TAG, "Updated cached user name: " + name);
    }

    public void updateCachedUserEmail(String email) {
        User cachedUser = getCachedUser();
        if (cachedUser != null) {
            cachedUser.setEmail(email);
            cacheUser(cachedUser);
        } else {
            editor.putString(KEY_USER_EMAIL, email);
            editor.apply();
        }
        Log.d(TAG, "Updated cached user email: " + email);
    }

    public String getCachedUserName() {
        return sharedPreferences.getString(KEY_USER_NAME, "");
    }

    public String getCachedUserEmail() {
        return sharedPreferences.getString(KEY_USER_EMAIL, "");
    }

    public String getCachedUserId() {
        return sharedPreferences.getString(KEY_USER_ID, "");
    }

    public boolean isUserLoggedIn() {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public void setUserLoggedIn(boolean isLoggedIn) {
        editor.putBoolean(KEY_IS_LOGGED_IN, isLoggedIn);
        editor.apply();
        Log.d(TAG, "User login status updated: " + isLoggedIn);
    }

    public void clearUserCache() {
        editor.remove(KEY_USER_DATA);
        editor.remove(KEY_USER_NAME);
        editor.remove(KEY_USER_EMAIL);
        editor.remove(KEY_USER_ID);
        editor.remove(KEY_IS_LOGGED_IN);
        editor.remove(KEY_LAST_SYNC);
        editor.apply();
        Log.d(TAG, "User cache cleared");
    }

    // App Settings Methods
    public void setAppVersion(String version) {
        editor.putString(KEY_APP_VERSION, version);
        editor.apply();
    }

    public String getAppVersion() {
        return sharedPreferences.getString(KEY_APP_VERSION, "1.0.0");
    }

    public void setThemeMode(String themeMode) {
        editor.putString(KEY_THEME_MODE, themeMode);
        editor.apply();
        Log.d(TAG, "Theme mode updated: " + themeMode);
    }

    public String getThemeMode() {
        return sharedPreferences.getString(KEY_THEME_MODE, "system"); // system, light, dark
    }

    // Unit System (for BMI calculator and other measurements)
    public void setUnitSystem(String unitSystem) {
        editor.putString(KEY_UNIT_SYSTEM, unitSystem);
        editor.apply();
        Log.d(TAG, "Unit system updated: " + unitSystem);
    }

    public String getUnitSystem() {
        return sharedPreferences.getString(KEY_UNIT_SYSTEM, "metric"); // metric or imperial
    }

    public boolean isMetricSystem() {
        return "metric".equals(getUnitSystem());
    }

    // Sync Methods
    public void updateLastSyncTime() {
        editor.putLong(KEY_LAST_SYNC, System.currentTimeMillis());
        editor.apply();
    }

    public long getLastSyncTime() {
        return sharedPreferences.getLong(KEY_LAST_SYNC, 0);
    }

    public boolean needsSync(long maxAgeMs) {
        long lastSync = getLastSyncTime();
        long currentTime = System.currentTimeMillis();
        return (currentTime - lastSync) > maxAgeMs;
    }

    // Data Management Methods
    public void clearAllData() {
        editor.clear();
        editor.apply();
        Log.d(TAG, "All shared preferences cleared");
    }

    public boolean hasValidUserData() {
        User cachedUser = getCachedUser();
        return cachedUser != null && cachedUser.getUid() != null && !cachedUser.getUid().isEmpty();
    }

    // Backup and restore methods (for app data export/import)
    public String exportUserPreferences() {
        try {
            User user = getCachedUser();
            if (user != null) {
                JSONObject userJson = new JSONObject();
                userJson.put("uid", user.getUid());
                userJson.put("name", user.getName());
                userJson.put("email", user.getEmail());
                userJson.put("height", user.getHeight());
                userJson.put("weight", user.getWeight());
                userJson.put("age", user.getAge());
                userJson.put("gender", user.getGender());
                userJson.put("activityLevel", user.getActivityLevel());
                userJson.put("createdAt", user.getCreatedAt());
                userJson.put("updatedAt", user.getUpdatedAt());
                return userJson.toString();
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error creating JSON for export", e);
        } catch (Exception e) {
            Log.e(TAG, "Error exporting user preferences", e);
        }
        return null;
    }

    public boolean importUserPreferences(String json) {
        try {
            JSONObject userJson = new JSONObject(json);

            User user = new User();
            user.setUid(userJson.optString("uid", ""));
            user.setName(userJson.optString("name", ""));
            user.setEmail(userJson.optString("email", ""));
            user.setHeight(userJson.optDouble("height", 0.0));
            user.setWeight(userJson.optDouble("weight", 0.0));
            user.setAge(userJson.optInt("age", 0));
            user.setGender(userJson.optString("gender", ""));
            user.setActivityLevel(userJson.optString("activityLevel", ""));
            user.setCreatedAt(userJson.optLong("createdAt", 0));
            user.setUpdatedAt(userJson.optLong("updatedAt", 0));

            if (user.getUid() != null && !user.getUid().isEmpty()) {
                cacheUser(user);
                Log.d(TAG, "User preferences imported successfully");
                return true;
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error parsing imported user preferences", e);
        } catch (Exception e) {
            Log.e(TAG, "Error importing user preferences", e);
        }
        return false;
    }

    // Debug methods
    public void logAllPreferences() {
        if (Log.isLoggable(TAG, Log.DEBUG)) {
            Log.d(TAG, "=== Shared Preferences Debug ===");
            Log.d(TAG, "User logged in: " + isUserLoggedIn());
            Log.d(TAG, "User ID: " + getCachedUserId());
            Log.d(TAG, "User name: " + getCachedUserName());
            Log.d(TAG, "User email: " + getCachedUserEmail());
            Log.d(TAG, "Theme mode: " + getThemeMode());
            Log.d(TAG, "Unit system: " + getUnitSystem());
            Log.d(TAG, "App version: " + getAppVersion());
            Log.d(TAG, "Last sync: " + getLastSyncTime());
            Log.d(TAG, "Has valid user data: " + hasValidUserData());
            Log.d(TAG, "================================");
        }
    }
}