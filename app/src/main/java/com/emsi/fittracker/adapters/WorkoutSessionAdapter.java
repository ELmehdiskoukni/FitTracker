package com.emsi.fittracker.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.emsi.fittracker.R;
import com.emsi.fittracker.models.WorkoutSession;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class WorkoutSessionAdapter extends RecyclerView.Adapter<WorkoutSessionAdapter.SessionViewHolder> {

    private List<WorkoutSession> sessionList;
    private OnSessionClickListener listener;
    private SimpleDateFormat dateFormat;
    private SimpleDateFormat timeFormat;

    public interface OnSessionClickListener {
        void onSessionClick(WorkoutSession session);
        void onSessionDelete(WorkoutSession session);
    }

    public WorkoutSessionAdapter(List<WorkoutSession> sessionList, OnSessionClickListener listener) {
        this.sessionList = sessionList;
        this.listener = listener;
        this.dateFormat = new SimpleDateFormat("EEEE, dd MMM", Locale.getDefault());
        this.timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
    }

    @NonNull
    @Override
    public SessionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_workout_session, parent, false);
        return new SessionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SessionViewHolder holder, int position) {
        WorkoutSession session = sessionList.get(position);
        holder.bind(session);
    }

    @Override
    public int getItemCount() {
        return sessionList.size();
    }

    public class SessionViewHolder extends RecyclerView.ViewHolder {

        private TextView tvDate;
        private TextView tvTime;
        private TextView tvDuration;
        private TextView tvWorkoutName;
        private TextView tvCalories;
        private TextView tvRelativeDate;
        private ImageButton btnDelete;
        private View cardView;

        public SessionViewHolder(@NonNull View itemView) {
            super(itemView);

            tvDate = itemView.findViewById(R.id.tvDate);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvDuration = itemView.findViewById(R.id.tvDuration);
            tvWorkoutName = itemView.findViewById(R.id.tvWorkoutName);
            tvCalories = itemView.findViewById(R.id.tvCalories);
            tvRelativeDate = itemView.findViewById(R.id.tvRelativeDate);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            cardView = itemView.findViewById(R.id.cardView);
        }

        public void bind(WorkoutSession session) {
            // Date formatting
            Date sessionDate = session.getDate();
            tvDate.setText(dateFormat.format(sessionDate));
            tvTime.setText(timeFormat.format(sessionDate));

            // Relative date (Today, Yesterday, etc.)
            String relativeDate = getRelativeDate(sessionDate);
            tvRelativeDate.setText(relativeDate);

            // Duration
            long duration = session.getDuration();
            if (duration < 60) {
                tvDuration.setText(duration + " min");
            } else {
                long hours = duration / 60;
                long minutes = duration % 60;
                tvDuration.setText(String.format("%dh %dmin", hours, minutes));
            }

            // Workout name
            if (session.getWorkoutName() != null && !session.getWorkoutName().isEmpty()) {
                tvWorkoutName.setText(session.getWorkoutName());
            } else {
                tvWorkoutName.setText("Session d'entraînement");
            }

            // Estimated calories (7.5 cal/min for moderate intensity)
            double calories = duration * 7.5;
            tvCalories.setText(String.format(Locale.getDefault(), "%.0f cal", calories));

            // Click listeners
            cardView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onSessionClick(session);
                }
            });

            btnDelete.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onSessionDelete(session);
                }
            });
        }

        private String getRelativeDate(Date date) {
            Calendar today = Calendar.getInstance();
            today.set(Calendar.HOUR_OF_DAY, 0);
            today.set(Calendar.MINUTE, 0);
            today.set(Calendar.SECOND, 0);
            today.set(Calendar.MILLISECOND, 0);

            Calendar yesterday = Calendar.getInstance();
            yesterday.add(Calendar.DAY_OF_YEAR, -1);
            yesterday.set(Calendar.HOUR_OF_DAY, 0);
            yesterday.set(Calendar.MINUTE, 0);
            yesterday.set(Calendar.SECOND, 0);
            yesterday.set(Calendar.MILLISECOND, 0);

            Calendar sessionCal = Calendar.getInstance();
            sessionCal.setTime(date);
            sessionCal.set(Calendar.HOUR_OF_DAY, 0);
            sessionCal.set(Calendar.MINUTE, 0);
            sessionCal.set(Calendar.SECOND, 0);
            sessionCal.set(Calendar.MILLISECOND, 0);

            if (sessionCal.equals(today)) {
                return "Aujourd'hui";
            } else if (sessionCal.equals(yesterday)) {
                return "Hier";
            } else {
                long diffInMillis = today.getTimeInMillis() - sessionCal.getTimeInMillis();
                long diffInDays = diffInMillis / (1000 * 60 * 60 * 24);

                if (diffInDays < 7) {
                    return "Il y a " + diffInDays + " jours";
                } else if (diffInDays < 30) {
                    long weeks = diffInDays / 7;
                    return "Il y a " + weeks + (weeks == 1 ? " semaine" : " semaines");
                } else {
                    long months = diffInDays / 30;
                    return "Il y a " + months + (months == 1 ? " mois" : " mois");
                }
            }
        }
    }

    public void updateSessions(List<WorkoutSession> newSessions) {
        this.sessionList.clear();
        this.sessionList.addAll(newSessions);
        notifyDataSetChanged();
    }
}