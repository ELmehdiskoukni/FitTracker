package com.emsi.fittracker.fragments;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.emsi.fittracker.R;
import com.emsi.fittracker.adapters.BmiHistoryAdapter;
import com.emsi.fittracker.interfaces.DataCallback;
import com.emsi.fittracker.models.BmiRecord;
import com.emsi.fittracker.utils.FirebaseHelper;
import com.emsi.fittracker.utils.ValidationUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class BmiCalculatorFragment extends Fragment {

    // UI Components
    private EditText etWeight, etHeight;
    private RadioGroup rgUnitSystem;
    private RadioButton rbMetric, rbImperial;
    private Button btnCalculate, btnSave;
    private TextView tvBmiResult, tvBmiCategory, tvBmiDescription;
    private RecyclerView rvBmiHistory;

    // Data
    private BmiHistoryAdapter historyAdapter;
    private List<BmiRecord> bmiHistory;
    private FirebaseHelper firebaseHelper;
    private BmiRecord currentBmiRecord;

    // Constants for BMI categories
    private static final double UNDERWEIGHT_THRESHOLD = 18.5;
    private static final double NORMAL_THRESHOLD = 24.9;
    private static final double OVERWEIGHT_THRESHOLD = 29.9;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_bmi_calculator, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initializeViews(view);
        setupRecyclerView();
        setupEventListeners();
        loadBmiHistory();
    }

    private void initializeViews(View view) {
        etWeight = view.findViewById(R.id.et_weight);
        etHeight = view.findViewById(R.id.et_height);
        rgUnitSystem = view.findViewById(R.id.rg_unit_system);
        rbMetric = view.findViewById(R.id.rb_metric);
        rbImperial = view.findViewById(R.id.rb_imperial);
        btnCalculate = view.findViewById(R.id.btn_calculate);
        btnSave = view.findViewById(R.id.btn_save);
        tvBmiResult = view.findViewById(R.id.tv_bmi_result);
        tvBmiCategory = view.findViewById(R.id.tv_bmi_category);
        tvBmiDescription = view.findViewById(R.id.tv_bmi_description);
        rvBmiHistory = view.findViewById(R.id.rv_bmi_history);

        firebaseHelper = FirebaseHelper.getInstance();
        bmiHistory = new ArrayList<>();

        // Initially hide save button and results
        btnSave.setVisibility(View.GONE);
        hideResults();
    }

    private void setupRecyclerView() {
        historyAdapter = new BmiHistoryAdapter(bmiHistory, this::deleteBmiRecord);
        rvBmiHistory.setLayoutManager(new LinearLayoutManager(getContext()));
        rvBmiHistory.setAdapter(historyAdapter);
    }

    private void setupEventListeners() {
        btnCalculate.setOnClickListener(v -> calculateBmi());
        btnSave.setOnClickListener(v -> saveBmiRecord());

        rgUnitSystem.setOnCheckedChangeListener((group, checkedId) -> {
            updateUnitLabels();
            clearResults();
        });
    }

    private void updateUnitLabels() {
        if (rbMetric.isChecked()) {
            etWeight.setHint("Weight (kg)");
            etHeight.setHint("Height (cm)");
        } else {
            etWeight.setHint("Weight (lbs)");
            etHeight.setHint("Height (inches)");
        }
    }

    private void calculateBmi() {
        String weightStr = etWeight.getText().toString().trim();
        String heightStr = etHeight.getText().toString().trim();

        // Validate inputs
        if (TextUtils.isEmpty(weightStr) || TextUtils.isEmpty(heightStr)) {
            Toast.makeText(getContext(), "Please enter both weight and height", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double weight = Double.parseDouble(weightStr);
            double height = Double.parseDouble(heightStr);

            // Convert to metric if imperial is selected
            if (rbImperial.isChecked()) {
                weight = convertLbsToKg(weight);
                height = convertInchesToCm(height);
            }

            // Validate converted values
            if (!ValidationUtils.isValidWeight(weight)) {
                Toast.makeText(getContext(), "Please enter a valid weight (20-300 kg)", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!ValidationUtils.isValidHeight(height)) {
                Toast.makeText(getContext(), "Please enter a valid height (100-250 cm)", Toast.LENGTH_SHORT).show();
                return;
            }

            // Calculate BMI
            double heightInMeters = height / 100.0;
            double bmi = weight / (heightInMeters * heightInMeters);

            // Create BMI record
            currentBmiRecord = new BmiRecord(new Date(), weight, height);

            // Display results
            displayBmiResults(bmi);
            btnSave.setVisibility(View.VISIBLE);

        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Please enter valid numbers", Toast.LENGTH_SHORT).show();
        }
    }

    private void displayBmiResults(double bmi) {
        tvBmiResult.setText(String.format("BMI: %.1f", bmi));

        BmiCategory category = getBmiCategory(bmi);
        tvBmiCategory.setText(category.name);
        tvBmiCategory.setTextColor(getResources().getColor(category.colorRes, null));
        tvBmiDescription.setText(category.description);

        showResults();
    }

    private BmiCategory getBmiCategory(double bmi) {
        if (bmi < UNDERWEIGHT_THRESHOLD) {
            return new BmiCategory("Underweight",
                    "BMI below 18.5 indicates underweight. Consider consulting a healthcare provider.",
                    android.R.color.holo_blue_dark);
        } else if (bmi <= NORMAL_THRESHOLD) {
            return new BmiCategory("Normal Weight",
                    "BMI 18.5-24.9 indicates normal weight. Great job maintaining a healthy weight!",
                    android.R.color.holo_green_dark);
        } else if (bmi <= OVERWEIGHT_THRESHOLD) {
            return new BmiCategory("Overweight",
                    "BMI 25-29.9 indicates overweight. Consider lifestyle changes for better health.",
                    android.R.color.holo_orange_dark);
        } else {
            return new BmiCategory("Obese",
                    "BMI 30+ indicates obesity. Please consult a healthcare provider for guidance.",
                    android.R.color.holo_red_dark);
        }
    }

    private void saveBmiRecord() {
        if (currentBmiRecord == null) {
            Toast.makeText(getContext(), "Please calculate BMI first", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(getContext(), "Please log in to save BMI records", Toast.LENGTH_SHORT).show();
            return;
        }

        // Add user ID to the record
        currentBmiRecord.setUserId(currentUser.getUid());

        firebaseHelper.saveBmiRecord(currentBmiRecord, new DataCallback<BmiRecord>() {
            @Override
            public void onSuccess(BmiRecord result) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "BMI record saved successfully", Toast.LENGTH_SHORT).show();
                    bmiHistory.add(0, result);
                    historyAdapter.notifyItemInserted(0);
                    rvBmiHistory.scrollToPosition(0);
                    btnSave.setVisibility(View.GONE);
                    clearInputs();
                }
            }

            @Override
            public void onFailure(String error) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Failed to save BMI record: " + error, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void loadBmiHistory() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            return;
        }

        firebaseHelper.getUserBmiRecords(currentUser.getUid(), new DataCallback<List<BmiRecord>>() {
            @Override
            public void onSuccess(List<BmiRecord> result) {
                if (getContext() != null) {
                    bmiHistory.clear();
                    bmiHistory.addAll(result);
                    historyAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(String error) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Failed to load BMI history: " + error, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void deleteBmiRecord(BmiRecord record) {
        firebaseHelper.deleteBmiRecord(record.getId(), new DataCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "BMI record deleted", Toast.LENGTH_SHORT).show();
                    int position = bmiHistory.indexOf(record);
                    if (position != -1) {
                        bmiHistory.remove(position);
                        historyAdapter.notifyItemRemoved(position);
                    }
                }
            }

            @Override
            public void onFailure(String error) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Failed to delete BMI record: " + error, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // Utility methods
    private double convertLbsToKg(double lbs) {
        return lbs * 0.453592;
    }

    private double convertInchesToCm(double inches) {
        return inches * 2.54;
    }

    private void showResults() {
        tvBmiResult.setVisibility(View.VISIBLE);
        tvBmiCategory.setVisibility(View.VISIBLE);
        tvBmiDescription.setVisibility(View.VISIBLE);
    }

    private void hideResults() {
        tvBmiResult.setVisibility(View.GONE);
        tvBmiCategory.setVisibility(View.GONE);
        tvBmiDescription.setVisibility(View.GONE);
    }

    private void clearResults() {
        hideResults();
        btnSave.setVisibility(View.GONE);
        currentBmiRecord = null;
    }

    private void clearInputs() {
        etWeight.setText("");
        etHeight.setText("");
        clearResults();
    }

    // Inner class for BMI categories
    private static class BmiCategory {
        String name;
        String description;
        int colorRes;

        BmiCategory(String name, String description, int colorRes) {
            this.name = name;
            this.description = description;
            this.colorRes = colorRes;
        }
    }
}