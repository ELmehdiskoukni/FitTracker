package com.emsi.fittracker.utils;

import android.text.TextUtils;
import android.util.Patterns;

import java.util.regex.Pattern;

public class ValidationUtils {

    // Email validation
    private static final Pattern EMAIL_PATTERN = Patterns.EMAIL_ADDRESS;

    // Name validation - allows letters, spaces, hyphens, and apostrophes
    private static final Pattern NAME_PATTERN = Pattern.compile("^[a-zA-ZÀ-ÿ\\s\\-']{2,50}$");

    // Password validation - at least 6 characters
    private static final int MIN_PASSWORD_LENGTH = 6;

    // Phone number validation (basic international format)
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[+]?[0-9\\s\\-()]{8,15}$");

    // Height validation (in cm) - reasonable range
    private static final double MIN_HEIGHT = 50.0;  // 50 cm
    private static final double MAX_HEIGHT = 300.0; // 300 cm

    // Weight validation (in kg) - reasonable range
    private static final double MIN_WEIGHT = 10.0;  // 10 kg
    private static final double MAX_WEIGHT = 500.0; // 500 kg

    // Age validation - reasonable range
    private static final int MIN_AGE = 1;
    private static final int MAX_AGE = 150;

    /**
     * Validates email address format
     */
    public static boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email) && EMAIL_PATTERN.matcher(email).matches();
    }

    /**
     * Validates name format - allows letters, spaces, hyphens, apostrophes
     * Must be between 2-50 characters
     */
    public static boolean isValidName(String name) {
        return !TextUtils.isEmpty(name) && NAME_PATTERN.matcher(name.trim()).matches();
    }

    /**
     * Validates password strength
     * Must be at least 6 characters long
     */
    public static boolean isValidPassword(String password) {
        return !TextUtils.isEmpty(password) && password.length() >= MIN_PASSWORD_LENGTH;
    }

    /**
     * Validates password with custom minimum length
     */
    public static boolean isValidPassword(String password, int minLength) {
        return !TextUtils.isEmpty(password) && password.length() >= minLength;
    }

    /**
     * Validates strong password (contains uppercase, lowercase, number, special char)
     */
    public static boolean isStrongPassword(String password) {
        if (!isValidPassword(password)) return false;

        boolean hasUpper = password.matches(".*[A-Z].*");
        boolean hasLower = password.matches(".*[a-z].*");
        boolean hasNumber = password.matches(".*\\d.*");
        boolean hasSpecial = password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*");

        return hasUpper && hasLower && hasNumber && hasSpecial;
    }

    /**
     * Validates phone number format
     */
    public static boolean isValidPhone(String phone) {
        return !TextUtils.isEmpty(phone) && PHONE_PATTERN.matcher(phone.trim()).matches();
    }

    /**
     * Validates height in centimeters
     */
    public static boolean isValidHeight(double height) {
        return height >= MIN_HEIGHT && height <= MAX_HEIGHT;
    }

    /**
     * Validates height from string input
     */
    public static boolean isValidHeight(String heightStr) {
        try {
            double height = Double.parseDouble(heightStr);
            return isValidHeight(height);
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Validates weight in kilograms
     */
    public static boolean isValidWeight(double weight) {
        return weight >= MIN_WEIGHT && weight <= MAX_WEIGHT;
    }

    /**
     * Validates weight from string input
     */
    public static boolean isValidWeight(String weightStr) {
        try {
            double weight = Double.parseDouble(weightStr);
            return isValidWeight(weight);
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Validates age
     */
    public static boolean isValidAge(int age) {
        return age >= MIN_AGE && age <= MAX_AGE;
    }

    /**
     * Validates age from string input
     */
    public static boolean isValidAge(String ageStr) {
        try {
            int age = Integer.parseInt(ageStr);
            return isValidAge(age);
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Validates that two passwords match
     */
    public static boolean doPasswordsMatch(String password, String confirmPassword) {
        return !TextUtils.isEmpty(password) &&
                !TextUtils.isEmpty(confirmPassword) &&
                password.equals(confirmPassword);
    }

    /**
     * Validates gender selection
     */
    public static boolean isValidGender(String gender) {
        return !TextUtils.isEmpty(gender) &&
                (gender.equals("male") || gender.equals("female") || gender.equals("other"));
    }

    /**
     * Validates activity level selection
     */
    public static boolean isValidActivityLevel(String activityLevel) {
        return !TextUtils.isEmpty(activityLevel) &&
                (activityLevel.equals("sedentary") ||
                        activityLevel.equals("light") ||
                        activityLevel.equals("moderate") ||
                        activityLevel.equals("active") ||
                        activityLevel.equals("very_active"));
    }

    /**
     * Validates BMI calculation inputs
     */
    public static boolean canCalculateBMI(double height, double weight) {
        return isValidHeight(height) && isValidWeight(weight);
    }

    /**
     * Validates that a string is not empty after trimming
     */
    public static boolean isNotEmpty(String text) {
        return !TextUtils.isEmpty(text) && !text.trim().isEmpty();
    }

    /**
     * Validates string length within range
     */
    public static boolean isValidLength(String text, int minLength, int maxLength) {
        if (TextUtils.isEmpty(text)) return false;
        int length = text.trim().length();
        return length >= minLength && length <= maxLength;
    }

    /**
     * Validates numeric input within range
     */
    public static boolean isNumericInRange(String text, double min, double max) {
        try {
            double value = Double.parseDouble(text);
            return value >= min && value <= max;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Validates integer input within range
     */
    public static boolean isIntegerInRange(String text, int min, int max) {
        try {
            int value = Integer.parseInt(text);
            return value >= min && value <= max;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Sanitizes input by trimming whitespace
     */
    public static String sanitizeInput(String input) {
        return input != null ? input.trim() : "";
    }

    /**
     * Gets validation error message for email
     */
    public static String getEmailErrorMessage(String email) {
        if (TextUtils.isEmpty(email)) {
            return "L'email est requis";
        }
        if (!isValidEmail(email)) {
            return "Format d'email invalide";
        }
        return null;
    }

    /**
     * Gets validation error message for name
     */
    public static String getNameErrorMessage(String name) {
        if (TextUtils.isEmpty(name)) {
            return "Le nom est requis";
        }
        if (name.trim().length() < 2) {
            return "Le nom doit contenir au moins 2 caractères";
        }
        if (name.trim().length() > 50) {
            return "Le nom ne peut pas dépasser 50 caractères";
        }
        if (!isValidName(name)) {
            return "Le nom contient des caractères invalides";
        }
        return null;
    }

    /**
     * Gets validation error message for password
     */
    public static String getPasswordErrorMessage(String password) {
        if (TextUtils.isEmpty(password)) {
            return "Le mot de passe est requis";
        }
        if (password.length() < MIN_PASSWORD_LENGTH) {
            return "Le mot de passe doit contenir au moins " + MIN_PASSWORD_LENGTH + " caractères";
        }
        return null;
    }

    /**
     * Gets validation error message for height
     */
    public static String getHeightErrorMessage(String height) {
        if (TextUtils.isEmpty(height)) {
            return "La taille est requise";
        }
        try {
            double h = Double.parseDouble(height);
            if (h < MIN_HEIGHT) {
                return "La taille doit être supérieure à " + MIN_HEIGHT + " cm";
            }
            if (h > MAX_HEIGHT) {
                return "La taille doit être inférieure à " + MAX_HEIGHT + " cm";
            }
        } catch (NumberFormatException e) {
            return "Format de taille invalide";
        }
        return null;
    }

    /**
     * Gets validation error message for weight
     */
    public static String getWeightErrorMessage(String weight) {
        if (TextUtils.isEmpty(weight)) {
            return "Le poids est requis";
        }
        try {
            double w = Double.parseDouble(weight);
            if (w < MIN_WEIGHT) {
                return "Le poids doit être supérieur à " + MIN_WEIGHT + " kg";
            }
            if (w > MAX_WEIGHT) {
                return "Le poids doit être inférieur à " + MAX_WEIGHT + " kg";
            }
        } catch (NumberFormatException e) {
            return "Format de poids invalide";
        }
        return null;
    }

    /**
     * Gets validation error message for age
     */
    public static String getAgeErrorMessage(String age) {
        if (TextUtils.isEmpty(age)) {
            return "L'âge est requis";
        }
        try {
            int a = Integer.parseInt(age);
            if (a < MIN_AGE) {
                return "L'âge doit être supérieur à " + MIN_AGE;
            }
            if (a > MAX_AGE) {
                return "L'âge doit être inférieur à " + MAX_AGE;
            }
        } catch (NumberFormatException e) {
            return "Format d'âge invalide";
        }
        return null;
    }
}