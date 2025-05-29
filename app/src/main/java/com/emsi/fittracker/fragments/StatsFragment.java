package com.emsi.fittracker.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.emsi.fittracker.R;
import com.emsi.fittracker.interfaces.DataCallback;
import com.emsi.fittracker.models.Exercise;
import com.emsi.fittracker.models.Workout;
import com.emsi.fittracker.models.WorkoutSession;
import com.emsi.fittracker.utils.FirebaseHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class StatsFragment extends Fragment {

    private static final String TAG = "StatsFragment";

    // UI Components
    private TextView tvTotalWorkouts, tvTotalDuration, tvAverageDuration;
    private TextView tvCaloriesBurned, tvThisMonth, tvLastMonth;
    private TextView tvMostActiveDay, tvPreferredTime;
    private TextView tvTotalExercises, tvAvgExercisesPerWorkout;
    private TextView tvMostFrequentExercise, tvTotalVolume;
    private ProgressBar progressLoading;
    private View statsContent;
    private CardView cardOverview, cardTimeStats, cardExerciseStats, cardMonthlyStats;

    // Progress bars
    private ProgressBar progressThisMonth, progressLastMonth;
    private TextView tvThisMonthProgress, tvLastMonthProgress;

    // Data
    private FirebaseHelper firebaseHelper;
    private String currentUserId;
    private List<WorkoutSession> allSessions;
    private List<Workout> allWorkouts;

    // Constants for calorie calculation
    private static final double CALORIES_PER_MINUTE_MODERATE = 7.5;
    private static final double CALORIES_PER_MINUTE_INTENSE = 10.0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_stats, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);

        firebaseHelper = FirebaseHelper.getInstance();
        allSessions = new ArrayList<>();
        allWorkouts = new ArrayList<>();

        // Check if user is authenticated
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            currentUserId = currentUser.getUid();
            loadStatistics();
        } else {
            Toast.makeText(getContext(), "Utilisateur non connecté", Toast.LENGTH_SHORT).show();
            showEmptyState();
        }
    }

    private void initViews(View view) {
        // Overview card
        tvTotalWorkouts = view.findViewById(R.id.tvTotalWorkouts);
        tvTotalDuration = view.findViewById(R.id.tvTotalDuration);
        tvAverageDuration = view.findViewById(R.id.tvAverageDuration);
        tvCaloriesBurned = view.findViewById(R.id.tvCaloriesBurned);

        // Time stats card
        tvMostActiveDay = view.findViewById(R.id.tvMostActiveDay);
        tvPreferredTime = view.findViewById(R.id.tvPreferredTime);

        // Exercise stats card
        tvTotalExercises = view.findViewById(R.id.tvTotalExercises);
        tvAvgExercisesPerWorkout = view.findViewById(R.id.tvAvgExercisesPerWorkout);
        tvMostFrequentExercise = view.findViewById(R.id.tvMostFrequentExercise);
        tvTotalVolume = view.findViewById(R.id.tvTotalVolume);

        // Monthly comparison card
        tvThisMonth = view.findViewById(R.id.tvThisMonth);
        tvLastMonth = view.findViewById(R.id.tvLastMonth);
        progressThisMonth = view.findViewById(R.id.progressThisMonth);
        progressLastMonth = view.findViewById(R.id.progressLastMonth);
        tvThisMonthProgress = view.findViewById(R.id.tvThisMonthProgress);
        tvLastMonthProgress = view.findViewById(R.id.tvLastMonthProgress);

        // Loading and content views
        progressLoading = view.findViewById(R.id.progressLoading);
        statsContent = view.findViewById(R.id.statsContent);

        // Cards
        cardOverview = view.findViewById(R.id.cardOverview);
        cardTimeStats = view.findViewById(R.id.cardTimeStats);
        cardExerciseStats = view.findViewById(R.id.cardExerciseStats);
        cardMonthlyStats = view.findViewById(R.id.cardMonthlyStats);
    }

    private void loadStatistics() {
        showLoading();

        // Load workout sessions first
        firebaseHelper.getUserWorkoutSessions(currentUserId, new DataCallback<List<WorkoutSession>>() {
            @Override
            public void onSuccess(List<WorkoutSession> sessions) {
                allSessions.clear();
                if (sessions != null) {
                    allSessions.addAll(sessions);
                }

                // Then load workouts
                loadWorkouts();
            }

            @Override
            public void onFailure(String error) {
                Log.e(TAG, "Error loading sessions: " + error);
                if (getActivity() != null && isAdded()) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Erreur de chargement des statistiques", Toast.LENGTH_SHORT).show();
                        showEmptyState();
                    });
                }
            }
        });
    }

    private void loadWorkouts() {
        firebaseHelper.getUserWorkouts(currentUserId, new DataCallback<List<Workout>>() {
            @Override
            public void onSuccess(List<Workout> workouts) {
                allWorkouts.clear();
                if (workouts != null) {
                    allWorkouts.addAll(workouts);
                }

                if (getActivity() != null && isAdded()) {
                    getActivity().runOnUiThread(() -> {
                        calculateAndDisplayStatistics();
                        showContent();
                    });
                }
            }

            @Override
            public void onFailure(String error) {
                Log.e(TAG, "Error loading workouts: " + error);
                if (getActivity() != null && isAdded()) {
                    getActivity().runOnUiThread(() -> {
                        // Still calculate stats with sessions only
                        calculateAndDisplayStatistics();
                        showContent();
                    });
                }
            }
        });
    }

    private void calculateAndDisplayStatistics() {
        // Overview statistics
        int totalWorkouts = allSessions.size();
        tvTotalWorkouts.setText(String.valueOf(totalWorkouts));

        // Total duration
        long totalMinutes = 0;
        for (WorkoutSession session : allSessions) {
            totalMinutes += session.getDuration();
        }

        String totalDurationText = formatDuration(totalMinutes);
        tvTotalDuration.setText(totalDurationText);

        // Average duration
        long averageMinutes = totalWorkouts > 0 ? totalMinutes / totalWorkouts : 0;
        tvAverageDuration.setText(averageMinutes + " min");

        // Estimated calories burned (using moderate intensity as default)
        double totalCalories = totalMinutes * CALORIES_PER_MINUTE_MODERATE;
        tvCaloriesBurned.setText(String.format(Locale.getDefault(), "%.0f", totalCalories));

        // Time statistics
        calculateTimeStatistics();

        // Exercise statistics
        calculateExerciseStatistics();

        // Monthly comparison
        calculateMonthlyComparison();
    }

    private void calculateTimeStatistics() {
        if (allSessions.isEmpty()) {
            tvMostActiveDay.setText("-");
            tvPreferredTime.setText("-");
            return;
        }

        // Most active day of week
        Map<Integer, Integer> dayFrequency = new HashMap<>();
        Map<Integer, Integer> hourFrequency = new HashMap<>();

        Calendar calendar = Calendar.getInstance();
        for (WorkoutSession session : allSessions) {
            calendar.setTime(session.getDate());
            int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
            int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);

            dayFrequency.put(dayOfWeek, dayFrequency.getOrDefault(dayOfWeek, 0) + 1);
            hourFrequency.put(hourOfDay, hourFrequency.getOrDefault(hourOfDay, 0) + 1);
        }

        // Find most active day
        int mostActiveDay = -1;
        int maxDayCount = 0;
        for (Map.Entry<Integer, Integer> entry : dayFrequency.entrySet()) {
            if (entry.getValue() > maxDayCount) {
                maxDayCount = entry.getValue();
                mostActiveDay = entry.getKey();
            }
        }

        String[] daysOfWeek = {"", "Dimanche", "Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi", "Samedi"};
        tvMostActiveDay.setText(mostActiveDay >= 1 && mostActiveDay <= 7 ? daysOfWeek[mostActiveDay] : "-");

        // Find preferred time
        int preferredHour = -1;
        int maxHourCount = 0;
        for (Map.Entry<Integer, Integer> entry : hourFrequency.entrySet()) {
            if (entry.getValue() > maxHourCount) {
                maxHourCount = entry.getValue();
                preferredHour = entry.getKey();
            }
        }

        if (preferredHour >= 0) {
            String timeRange;
            if (preferredHour < 6) {
                timeRange = "Nuit (00h-06h)";
            } else if (preferredHour < 12) {
                timeRange = "Matin (06h-12h)";
            } else if (preferredHour < 18) {
                timeRange = "Après-midi (12h-18h)";
            } else {
                timeRange = "Soir (18h-00h)";
            }
            tvPreferredTime.setText(timeRange);
        } else {
            tvPreferredTime.setText("-");
        }
    }

    private void calculateExerciseStatistics() {
        if (allWorkouts.isEmpty()) {
            tvTotalExercises.setText("0");
            tvAvgExercisesPerWorkout.setText("0");
            tvMostFrequentExercise.setText("-");
            tvTotalVolume.setText("0 kg");
            return;
        }

        // Count total exercises and calculate volume
        int totalExercises = 0;
        double totalVolume = 0;
        Map<String, Integer> exerciseFrequency = new HashMap<>();

        for (Workout workout : allWorkouts) {
            if (workout.getExercises() != null) {
                for (Exercise exercise : workout.getExercises()) {
                    totalExercises++;
                    totalVolume += exercise.getTotalVolume();

                    String exerciseName = exercise.getName().toLowerCase();
                    exerciseFrequency.put(exerciseName,
                            exerciseFrequency.getOrDefault(exerciseName, 0) + 1);
                }
            }
        }

        tvTotalExercises.setText(String.valueOf(totalExercises));

        // Average exercises per workout
        double avgExercises = allWorkouts.size() > 0 ?
                (double) totalExercises / allWorkouts.size() : 0;
        tvAvgExercisesPerWorkout.setText(String.format(Locale.getDefault(), "%.1f", avgExercises));

        // Most frequent exercise
        String mostFrequent = "-";
        int maxFrequency = 0;
        for (Map.Entry<String, Integer> entry : exerciseFrequency.entrySet()) {
            if (entry.getValue() > maxFrequency) {
                maxFrequency = entry.getValue();
                mostFrequent = entry.getKey();
                // Capitalize first letter
                mostFrequent = mostFrequent.substring(0, 1).toUpperCase() +
                        mostFrequent.substring(1);
            }
        }
        tvMostFrequentExercise.setText(mostFrequent);

        // Total volume
        if (totalVolume > 1000) {
            tvTotalVolume.setText(String.format(Locale.getDefault(), "%.1f tonnes", totalVolume / 1000));
        } else {
            tvTotalVolume.setText(String.format(Locale.getDefault(), "%.0f kg", totalVolume));
        }
    }

    private void calculateMonthlyComparison() {
        Calendar calendar = Calendar.getInstance();

        // This month
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        Date thisMonthStart = calendar.getTime();

        // Last month
        calendar.add(Calendar.MONTH, -1);
        Date lastMonthStart = calendar.getTime();
        calendar.add(Calendar.MONTH, 1);
        Date lastMonthEnd = calendar.getTime();

        int thisMonthCount = 0;
        int lastMonthCount = 0;

        for (WorkoutSession session : allSessions) {
            Date sessionDate = session.getDate();
            if (sessionDate.after(thisMonthStart) || sessionDate.equals(thisMonthStart)) {
                thisMonthCount++;
            } else if ((sessionDate.after(lastMonthStart) || sessionDate.equals(lastMonthStart))
                    && sessionDate.before(lastMonthEnd)) {
                lastMonthCount++;
            }
        }

        tvThisMonth.setText(String.valueOf(thisMonthCount));
        tvLastMonth.setText(String.valueOf(lastMonthCount));

        // Set progress bars (assuming goal of 20 workouts per month)
        int monthlyGoal = 20;
        int thisMonthProgress = (int) ((thisMonthCount / (float) monthlyGoal) * 100);
        int lastMonthProgress = (int) ((lastMonthCount / (float) monthlyGoal) * 100);

        progressThisMonth.setProgress(Math.min(thisMonthProgress, 100));
        progressLastMonth.setProgress(Math.min(lastMonthProgress, 100));

        tvThisMonthProgress.setText(thisMonthProgress + "%");
        tvLastMonthProgress.setText(lastMonthProgress + "%");
    }

    private String formatDuration(long totalMinutes) {
        if (totalMinutes < 60) {
            return totalMinutes + " min";
        } else if (totalMinutes < 1440) { // Less than 24 hours
            long hours = totalMinutes / 60;
            long minutes = totalMinutes % 60;
            return String.format("%dh %dmin", hours, minutes);
        } else {
            long days = totalMinutes / 1440;
            long remainingMinutes = totalMinutes % 1440;
            long hours = remainingMinutes / 60;
            return String.format("%dj %dh", days, hours);
        }
    }

    private void showLoading() {
        progressLoading.setVisibility(View.VISIBLE);
        statsContent.setVisibility(View.GONE);
    }

    private void showContent() {
        progressLoading.setVisibility(View.GONE);
        statsContent.setVisibility(View.VISIBLE);
    }

    private void showEmptyState() {
        progressLoading.setVisibility(View.GONE);
        statsContent.setVisibility(View.VISIBLE);

        // Set all values to default/empty
        tvTotalWorkouts.setText("0");
        tvTotalDuration.setText("0 min");
        tvAverageDuration.setText("0 min");
        tvCaloriesBurned.setText("0");
        tvMostActiveDay.setText("-");
        tvPreferredTime.setText("-");
        tvTotalExercises.setText("0");
        tvAvgExercisesPerWorkout.setText("0");
        tvMostFrequentExercise.setText("-");
        tvTotalVolume.setText("0 kg");
        tvThisMonth.setText("0");
        tvLastMonth.setText("0");
        progressThisMonth.setProgress(0);
        progressLastMonth.setProgress(0);
        tvThisMonthProgress.setText("0%");
        tvLastMonthProgress.setText("0%");
    }

    @Override
    public void onResume() {
        super.onResume();
        if (currentUserId != null) {
            loadStatistics();
        }
    }
}