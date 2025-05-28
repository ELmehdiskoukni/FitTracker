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

    // BMI validation constants
    private static final double MIN_WEIGHT_KG = 20.0;
    private static final double MAX_WEIGHT_KG = 300.0;
    private static final double MIN_HEIGHT_CM = 100.0;
    private static final double MAX_HEIGHT_CM = 250.0;

    // Imperial weight and height ranges
    private static final double MIN_WEIGHT_LBS = 44.0; // ~20 kg
    private static final double MAX_WEIGHT_LBS = 661.0; // ~300 kg
    private static final double MIN_HEIGHT_INCHES = 39.0; // ~100 cm
    private static final double MAX_HEIGHT_INCHES = 98.0; // ~250 cm

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
        return weight >= MIN_WEIGHT_KG && weight <= MAX_WEIGHT_KG;
    }

    /**
     * Validates height input (in cm)
     * @param height The height to validate
     * @return true if height is valid (100-250 cm), false otherwise
     */
    public static boolean isValidHeight(double height) {
        return height >= MIN_HEIGHT_CM && height <= MAX_HEIGHT_CM;
    }

    /**
     * Validates weight input in pounds
     * @param weight The weight in pounds to validate
     * @return true if weight is valid (44-661 lbs), false otherwise
     */
    public static boolean isValidWeightLbs(double weight) {
        return weight >= MIN_WEIGHT_LBS && weight <= MAX_WEIGHT_LBS;
    }

    /**
     * Validates height input in inches
     * @param height The height in inches to validate
     * @return true if height is valid (39-98 inches), false otherwise
     */
    public static boolean isValidHeightInches(double height) {
        return height >= MIN_HEIGHT_INCHES && height <= MAX_HEIGHT_INCHES;
    }

    /**
     * Validates numeric input string
     * @param input The string to validate as a number
     * @return true if input can be parsed as a valid double, false otherwise
     */
    public static boolean isValidNumericInput(String input) {
        if (input == null || input.trim().isEmpty()) {
            return false;
        }

        try {
            double value = Double.parseDouble(input.trim());
            return !Double.isNaN(value) && !Double.isInfinite(value) && value > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Validates BMI calculation inputs
     * @param weight Weight value
     * @param height Height value
     * @param isMetric True if using metric system, false for imperial
     * @return ValidationResult object with validation status and message
     */
    public static ValidationResult validateBmiInputs(double weight, double height, boolean isMetric) {
        if (isMetric) {
            if (!isValidWeight(weight)) {
                return new ValidationResult(false,
                        "Weight must be between " + MIN_WEIGHT_KG + " and " + MAX_WEIGHT_KG + " kg");
            }
            if (!isValidHeight(height)) {
                return new ValidationResult(false,
                        "Height must be between " + MIN_HEIGHT_CM + " and " + MAX_HEIGHT_CM + " cm");
            }
        } else {
            if (!isValidWeightLbs(weight)) {
                return new ValidationResult(false,
                        "Weight must be between " + MIN_WEIGHT_LBS + " and " + MAX_WEIGHT_LBS + " lbs");
            }
            if (!isValidHeightInches(height)) {
                return new ValidationResult(false,
                        "Height must be between " + MIN_HEIGHT_INCHES + " and " + MAX_HEIGHT_INCHES + " inches");
            }
        }

        return new ValidationResult(true, "Valid inputs");
    }

    /**
     * Validates BMI range (general health check)
     * @param bmi The calculated BMI value
     * @return true if BMI is within reasonable range (10-60), false otherwise
     */
    public static boolean isValidBmiRange(double bmi) {
        return bmi >= 10.0 && bmi <= 60.0;
    }

    /**
     * Gets BMI category based on WHO standards
     * @param bmi The BMI value
     * @return String representing the BMI category
     */
    public static String getBmiCategory(double bmi) {
        if (bmi < 18.5) {
            return "Underweight";
        } else if (bmi <= 24.9) {
            return "Normal Weight";
        } else if (bmi <= 29.9) {
            return "Overweight";
        } else {
            return "Obese";
        }
    }

    /**
     * Gets health advice based on BMI category
     * @param bmi The BMI value
     * @return String with health advice
     */
    public static String getBmiHealthAdvice(double bmi) {
        if (bmi < 18.5) {
            return "Consider consulting a healthcare provider about healthy weight gain strategies.";
        } else if (bmi <= 24.9) {
            return "Great! You're maintaining a healthy weight. Keep up the good work!";
        } else if (bmi <= 29.9) {
            return "Consider lifestyle changes including balanced diet and regular exercise.";
        } else {
            return "Please consult a healthcare provider for personalized weight management guidance.";
        }
    }

    /**
     * Unit conversion: pounds to kilograms
     * @param pounds Weight in pounds
     * @return Weight in kilograms
     */
    public static double convertLbsToKg(double pounds) {
        return pounds * 0.453592;
    }

    /**
     * Unit conversion: inches to centimeters
     * @param inches Height in inches
     * @return Height in centimeters
     */
    public static double convertInchesToCm(double inches) {
        return inches * 2.54;
    }

    /**
     * Unit conversion: kilograms to pounds
     * @param kilograms Weight in kilograms
     * @return Weight in pounds
     */
    public static double convertKgToLbs(double kilograms) {
        return kilograms / 0.453592;
    }

    /**
     * Unit conversion: centimeters to inches
     * @param centimeters Height in centimeters
     * @return Height in inches
     */
    public static double convertCmToInches(double centimeters) {
        return centimeters / 2.54;
    }

    /**
     * Validation result class to hold validation status and message
     */
    public static class ValidationResult {
        private final boolean isValid;
        private final String message;

        public ValidationResult(boolean isValid, String message) {
            this.isValid = isValid;
            this.message = message;
        }

        public boolean isValid() {
            return isValid;
        }

        public String getMessage() {
            return message;
        }
    }
}