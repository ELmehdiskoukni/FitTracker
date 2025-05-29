package com.emsi.fittracker.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.emsi.fittracker.R;
import com.emsi.fittracker.models.Exercise;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class ActiveExerciseAdapter extends RecyclerView.Adapter<ActiveExerciseAdapter.ActiveExerciseViewHolder> {

    private List<Exercise> exerciseList;
    private OnExerciseCompletedListener listener;
    private DecimalFormat weightFormat;
    private boolean[] completedSets; // Track completed sets for each exercise

    public interface OnExerciseCompletedListener {
        void onExerciseCompleted(Exercise exercise, int position);
    }

    public ActiveExerciseAdapter(List<Exercise> exerciseList, OnExerciseCompletedListener listener) {
        this.exerciseList = exerciseList != null ? exerciseList : new ArrayList<>();
        this.listener = listener;
        this.weightFormat = new DecimalFormat("0.#");
        this.completedSets = new boolean[this.exerciseList.size()];
    }

    @NonNull
    @Override
    public ActiveExerciseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_active_exercise, parent, false);
        return new ActiveExerciseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ActiveExerciseViewHolder holder, int position) {
        Exercise exercise = exerciseList.get(position);
        holder.bind(exercise, position);
    }

    @Override
    public int getItemCount() {
        return exerciseList.size();
    }

    public boolean areAllExercisesCompleted() {
        if (completedSets.length == 0) {
            return false;
        }
        for (boolean completed : completedSets) {
            if (!completed) return false;
        }
        return true;
    }

    public class ActiveExerciseViewHolder extends RecyclerView.ViewHolder {

        private TextView tvExerciseName;
        private TextView tvSetsReps;
        private TextView tvWeight;
        private LinearLayout llSetCheckboxes;
        private View cardView;

        public ActiveExerciseViewHolder(@NonNull View itemView) {
            super(itemView);

            tvExerciseName = itemView.findViewById(R.id.tvExerciseName);
            tvSetsReps = itemView.findViewById(R.id.tvSetsReps);
            tvWeight = itemView.findViewById(R.id.tvWeight);
            llSetCheckboxes = itemView.findViewById(R.id.llSetCheckboxes);
            cardView = itemView.findViewById(R.id.cardView);
        }

        public void bind(Exercise exercise, int position) {
            tvExerciseName.setText(exercise.getName());

            // Sets x Reps format
            tvSetsReps.setText(exercise.getSets() + " × " + exercise.getReps());

            // Weight
            if (exercise.getWeight() > 0) {
                tvWeight.setText(weightFormat.format(exercise.getWeight()) + " kg");
                tvWeight.setVisibility(View.VISIBLE);
            } else {
                tvWeight.setText("Poids corporel");
                tvWeight.setVisibility(View.VISIBLE);
            }

            // Create checkboxes for each set
            llSetCheckboxes.removeAllViews();
            for (int i = 0; i < exercise.getSets(); i++) {
                CheckBox checkBox = new CheckBox(itemView.getContext());
                checkBox.setText("Série " + (i + 1));
                checkBox.setTextColor(itemView.getContext().getColor(R.color.text_secondary));
                checkBox.setButtonTintList(itemView.getContext().getColorStateList(R.color.primary_blue));

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                params.setMargins(0, 8, 16, 8);
                checkBox.setLayoutParams(params);

                final int setIndex = i;
                checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    checkExerciseCompletion(exercise, position);
                });

                llSetCheckboxes.addView(checkBox);
            }

            // Update card appearance based on completion status
            updateCardAppearance(position);
        }

        private void checkExerciseCompletion(Exercise exercise, int position) {
            // Safety check for array bounds
            if (position < 0 || position >= completedSets.length) {
                return;
            }

            // Check if all sets are completed for this exercise
            boolean allSetsCompleted = true;
            for (int i = 0; i < llSetCheckboxes.getChildCount(); i++) {
                CheckBox checkBox = (CheckBox) llSetCheckboxes.getChildAt(i);
                if (!checkBox.isChecked()) {
                    allSetsCompleted = false;
                    break;
                }
            }

            completedSets[position] = allSetsCompleted;
            updateCardAppearance(position);

            if (allSetsCompleted && listener != null) {
                listener.onExerciseCompleted(exercise, position);
            }
        }

        private void updateCardAppearance(int position) {
            // Safety check for array bounds
            if (position < 0 || position >= completedSets.length) {
                return;
            }

            if (completedSets[position]) {
                // Exercise completed - add visual feedback
                cardView.setBackgroundTintList(
                        itemView.getContext().getColorStateList(R.color.success_green));
                cardView.setAlpha(0.8f);
                tvExerciseName.setTextColor(itemView.getContext().getColor(R.color.white));
            } else {
                // Exercise not completed - normal appearance
                cardView.setBackgroundTintList(
                        itemView.getContext().getColorStateList(R.color.background_card));
                cardView.setAlpha(1.0f);
                tvExerciseName.setTextColor(itemView.getContext().getColor(R.color.text_primary));
            }
        }
    }

    // Update the completed sets array when exercises change
    public void updateExercises(List<Exercise> newExercises) {
        this.exerciseList.clear();
        if (newExercises != null) {
            this.exerciseList.addAll(newExercises);
        }
        this.completedSets = new boolean[this.exerciseList.size()];
        notifyDataSetChanged();
    }
}