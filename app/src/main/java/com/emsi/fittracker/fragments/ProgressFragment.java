package com.emsi.fittracker.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.emsi.fittracker.R;
import com.emsi.fittracker.adapters.WorkoutSessionAdapter;
import com.emsi.fittracker.interfaces.DataCallback;
import com.emsi.fittracker.models.WorkoutSession;
import com.emsi.fittracker.utils.FirebaseHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ProgressFragment extends Fragment implements WorkoutSessionAdapter.OnSessionClickListener {

    private static final String TAG = "ProgressFragment";

    // UI Components
    private RecyclerView recyclerView;
    private WorkoutSessionAdapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private View emptyStateView;
    private TextView tvTotalWorkouts;
    private TextView tvCurrentStreak;
    private TextView tvTotalDuration;
    private TextView tvThisWeek;

    // Data
    private List<WorkoutSession> sessionList;
    private FirebaseHelper firebaseHelper;
    private String currentUserId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_progress, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupRecyclerView();
        setupListeners();

        firebaseHelper = FirebaseHelper.getInstance();

        // Check if user is authenticated
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            currentUserId = currentUser.getUid();
            loadWorkoutSessions();
        } else {
            Toast.makeText(getContext(), "Utilisateur non connecté", Toast.LENGTH_SHORT).show();
        }
    }

    private void initViews(View view) {
        recyclerView = view.findViewById(R.id.recyclerViewSessions);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        emptyStateView = view.findViewById(R.id.emptyStateView);
        tvTotalWorkouts = view.findViewById(R.id.tvTotalWorkouts);
        tvCurrentStreak = view.findViewById(R.id.tvCurrentStreak);
        tvTotalDuration = view.findViewById(R.id.tvTotalDuration);
        tvThisWeek = view.findViewById(R.id.tvThisWeek);
    }

    private void setupRecyclerView() {
        sessionList = new ArrayList<>();
        adapter = new WorkoutSessionAdapter(sessionList, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
    }

    private void setupListeners() {
        swipeRefreshLayout.setOnRefreshListener(this::loadWorkoutSessions);
    }

    private void loadWorkoutSessions() {
        if (currentUserId == null) {
            swipeRefreshLayout.setRefreshing(false);
            return;
        }

        swipeRefreshLayout.setRefreshing(true);

        firebaseHelper.getUserWorkoutSessions(currentUserId, new DataCallback<List<WorkoutSession>>() {
            @Override
            public void onSuccess(List<WorkoutSession> sessions) {
                if (getActivity() != null && isAdded()) {
                    getActivity().runOnUiThread(() -> {
                        sessionList.clear();
                        if (sessions != null) {
                            sessionList.addAll(sessions);
                        }
                        adapter.notifyDataSetChanged();
                        swipeRefreshLayout.setRefreshing(false);
                        updateEmptyState();
                        updateStatistics();

                        Log.d(TAG, "Sessions loaded successfully: " + sessionList.size());
                    });
                }
            }

            @Override
            public void onFailure(String error) {
                if (getActivity() != null && isAdded()) {
                    getActivity().runOnUiThread(() -> {
                        swipeRefreshLayout.setRefreshing(false);
                        Toast.makeText(getContext(), "Erreur de chargement: " + error, Toast.LENGTH_SHORT).show();
                        updateEmptyState();

                        Log.e(TAG, "Error loading sessions: " + error);
                    });
                }
            }
        });
    }

    private void updateStatistics() {
        if (sessionList.isEmpty()) {
            tvTotalWorkouts.setText("0");
            tvCurrentStreak.setText("0 jours");
            tvTotalDuration.setText("0 min");
            tvThisWeek.setText("0");
            return;
        }

        // Total workouts
        tvTotalWorkouts.setText(String.valueOf(sessionList.size()));

        // Calculate current streak
        int streak = calculateCurrentStreak();
        tvCurrentStreak.setText(streak + (streak == 1 ? " jour" : " jours"));

        // Total duration
        long totalMinutes = 0;
        for (WorkoutSession session : sessionList) {
            totalMinutes += session.getDuration();
        }

        if (totalMinutes < 60) {
            tvTotalDuration.setText(totalMinutes + " min");
        } else {
            long hours = totalMinutes / 60;
            long minutes = totalMinutes % 60;
            tvTotalDuration.setText(String.format("%dh %dmin", hours, minutes));
        }

        // This week's workouts
        int thisWeekCount = calculateThisWeekWorkouts();
        tvThisWeek.setText(String.valueOf(thisWeekCount));
    }

    private int calculateCurrentStreak() {
        if (sessionList.isEmpty()) return 0;

        // Sort sessions by date (newest first)
        List<WorkoutSession> sortedSessions = new ArrayList<>(sessionList);
        sortedSessions.sort((a, b) -> b.getDate().compareTo(a.getDate()));

        int streak = 0;
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        Date today = calendar.getTime();
        Date currentDate = today;

        for (WorkoutSession session : sortedSessions) {
            Calendar sessionCal = Calendar.getInstance();
            sessionCal.setTime(session.getDate());
            sessionCal.set(Calendar.HOUR_OF_DAY, 0);
            sessionCal.set(Calendar.MINUTE, 0);
            sessionCal.set(Calendar.SECOND, 0);
            sessionCal.set(Calendar.MILLISECOND, 0);

            Date sessionDate = sessionCal.getTime();

            long daysBetween = TimeUnit.DAYS.convert(
                    currentDate.getTime() - sessionDate.getTime(),
                    TimeUnit.MILLISECONDS
            );

            if (daysBetween == 0) {
                // Same day
                if (streak == 0) streak = 1;
            } else if (daysBetween == 1) {
                // Consecutive day
                streak++;
                currentDate = sessionDate;
            } else {
                // Streak broken
                break;
            }
        }

        return streak;
    }

    private int calculateThisWeekWorkouts() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        Date weekStart = calendar.getTime();

        int count = 0;
        for (WorkoutSession session : sessionList) {
            if (session.getDate().after(weekStart) || session.getDate().equals(weekStart)) {
                count++;
            }
        }

        return count;
    }

    private void updateEmptyState() {
        if (sessionList.isEmpty()) {
            emptyStateView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyStateView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onSessionClick(WorkoutSession session) {
        // Show session details dialog
        showSessionDetailsDialog(session);
    }

    @Override
    public void onSessionDelete(WorkoutSession session) {
        if (getContext() == null) return;

        new AlertDialog.Builder(getContext())
                .setTitle("Supprimer la session")
                .setMessage("Êtes-vous sûr de vouloir supprimer cette session d'entraînement ?")
                .setPositiveButton("Supprimer", (dialog, which) -> deleteSession(session))
                .setNegativeButton("Annuler", null)
                .show();
    }

    private void showSessionDetailsDialog(WorkoutSession session) {
        if (getContext() == null || session == null) return;

        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_session_details, null);

        TextView tvDate = dialogView.findViewById(R.id.tvDate);
        TextView tvDuration = dialogView.findViewById(R.id.tvDuration);
        TextView tvWorkoutName = dialogView.findViewById(R.id.tvWorkoutName);
        TextView tvNotes = dialogView.findViewById(R.id.tvNotes);
        TextView tvNotesLabel = dialogView.findViewById(R.id.tvNotesLabel);

        // Format date
        tvDate.setText(android.text.format.DateFormat.format("EEEE, dd MMMM yyyy", session.getDate()));

        // Duration
        tvDuration.setText(session.getDuration() + " minutes");

        // Workout name (if available)
        if (session.getWorkoutName() != null && !session.getWorkoutName().isEmpty()) {
            tvWorkoutName.setText(session.getWorkoutName());
        } else {
            tvWorkoutName.setText("Entraînement");
        }

        // Notes
        if (session.getNotes() != null && !session.getNotes().isEmpty()) {
            tvNotes.setText(session.getNotes());
            tvNotes.setVisibility(View.VISIBLE);
            tvNotesLabel.setVisibility(View.VISIBLE);
        } else {
            tvNotes.setVisibility(View.GONE);
            tvNotesLabel.setVisibility(View.GONE);
        }

        new AlertDialog.Builder(getContext())
                .setTitle("Détails de la session")
                .setView(dialogView)
                .setPositiveButton("OK", null)
                .show();
    }

    private void deleteSession(WorkoutSession session) {
        firebaseHelper.deleteWorkoutSession(session.getId(), new DataCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                if (getActivity() != null && isAdded()) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Session supprimée", Toast.LENGTH_SHORT).show();
                        sessionList.remove(session);
                        adapter.notifyDataSetChanged();
                        updateEmptyState();
                        updateStatistics();
                    });
                }
            }

            @Override
            public void onFailure(String error) {
                if (getActivity() != null && isAdded()) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Erreur lors de la suppression: " + error, Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (currentUserId != null) {
            loadWorkoutSessions();
        }
    }
}