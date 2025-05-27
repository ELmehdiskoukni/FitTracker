package com.emsi.fittracker.utils;

import android.util.Patterns;
import java.util.regex.Pattern;

public class ValidationUtils {

    // Email validation pattern
    private static final Pattern EMAIL_PATTERN = Patterns.EMAIL_ADDRESS;

    // Strong password pattern: at least 1 uppercase, 1 lowercase, 1 digit, minimum 6 characters
    private static final Pattern STRONG_PASSWORD_PATTERN =
            Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d@$!%*?&]{6,}$");

    // Name validation pattern: only letters, spaces, hyphens, and apostrophes
    private static final Pattern NAME_PATTERN =
            Pattern.compile("^[a-zA-ZÀ-ÿ\\s'-]+$");

    /**
     * Validates email format
     * @param email The email to validate
     * @return true if email is valid, false otherwise
     */
    public static boolean isValidEmail(String email) {
        return email != null && !email.trim().isEmpty() && EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    /**
     * Validates password strength
     * @param password The password to validate
     * @return true if password is strong, false otherwise
     */
    public static boolean isStrongPassword(String password) {
        return password != null && STRONG_PASSWORD_PATTERN.matcher(password).matches();
    }

    /**
     * Validates name format
     * @param name The name to validate
     * @return true if name is valid, false otherwise
     */
    public static boolean isValidName(String name) {
        return name != null && !name.trim().isEmpty() &&
                name.trim().length() >= 2 &&
                NAME_PATTERN.matcher(name.trim()).matches();
    }

    /**
     * Validates password length
     * @param password The password to validate
     * @return true if password meets minimum length requirement, false otherwise
     */
    public static boolean isValidPasswordLength(String password) {
        return password != null && password.length() >= 6;
    }

    /**
     * Validates if passwords match
     * @param password The original password
     * @param confirmPassword The confirmation password
     * @return true if passwords match, false otherwise
     */
    public static boolean doPasswordsMatch(String password, String confirmPassword) {
        return password != null && confirmPassword != null && password.equals(confirmPassword);
    }

    /**
     * Sanitizes input by trimming whitespace
     * @param input The input to sanitize
     * @return Sanitized input or empty string if input is null
     */
    public static String sanitizeInput(String input) {
        return input != null ? input.trim() : "";
    }

    /**
     * Validates if a string is not empty after trimming
     * @param input The input to validate
     * @return true if input is not empty, false otherwise
     */
    public static boolean isNotEmpty(String input) {
        return input != null && !input.trim().isEmpty();
    }

    /**
     * Validates phone number format (basic validation)
     * @param phoneNumber The phone number to validate
     * @return true if phone number is valid, false otherwise
     */
    public static boolean isValidPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return false;
        }

        // Remove all non-digit characters
        String digitsOnly = phoneNumber.replaceAll("[^\\d]", "");

        // Check if it has 10 digits (for most countries) or starts with country code
        return digitsOnly.length() >= 10 && digitsOnly.length() <= 15;
    }

    /**
     * Validates age range
     * @param age The age to validate
     * @return true if age is within valid range (13-120), false otherwise
     */
    public static boolean isValidAge(int age) {
        return age >= 13 && age <= 120;
    }

    /**
     * Validates weight input (in kg)
     * @param weight The weight to validate
     * @return true if weight is valid (20-300 kg), false otherwise
     */
    public static boolean isValidWeight(double weight) {
        return weight >= 20.0 && weight <= 300.0;
    }

    /**
     * Validates height input (in cm)
     * @param height The height to validate
     * @return true if height is valid (100-250 cm), false otherwise
     */
    public static boolean isValidHeight(double height) {
        return height >= 100.0 && height <= 250.0;
    }
}
