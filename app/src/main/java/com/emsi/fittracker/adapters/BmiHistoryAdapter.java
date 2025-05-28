package com.emsi.fittracker.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.emsi.fittracker.R;
import com.emsi.fittracker.models.BmiRecord;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class BmiHistoryAdapter extends RecyclerView.Adapter<BmiHistoryAdapter.BmiViewHolder> {

    private List<BmiRecord> bmiRecords;
    private OnBmiRecordDeleteListener deleteListener;
    private SimpleDateFormat dateFormat;

    public interface OnBmiRecordDeleteListener {
        void onDelete(BmiRecord record);
    }

    public BmiHistoryAdapter(List<BmiRecord> bmiRecords, OnBmiRecordDeleteListener deleteListener) {
        this.bmiRecords = bmiRecords;
        this.deleteListener = deleteListener;
        this.dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
    }

    @NonNull
    @Override
    public BmiViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_bmi_record, parent, false);
        return new BmiViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BmiViewHolder holder, int position) {
        BmiRecord record = bmiRecords.get(position);
        holder.bind(record);
    }

    @Override
    public int getItemCount() {
        return bmiRecords.size();
    }

    public class BmiViewHolder extends RecyclerView.ViewHolder {
        private TextView tvDate;
        private TextView tvBmi;
        private TextView tvCategory;
        private TextView tvWeight;
        private TextView tvHeight;
        private ImageButton btnDelete;

        public BmiViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tv_date);
            tvBmi = itemView.findViewById(R.id.tv_bmi);
            tvCategory = itemView.findViewById(R.id.tv_category);
            tvWeight = itemView.findViewById(R.id.tv_weight);
            tvHeight = itemView.findViewById(R.id.tv_height);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }

        public void bind(BmiRecord record) {
            // Set date
            tvDate.setText(dateFormat.format(record.getDate()));

            // Set BMI value
            tvBmi.setText(String.format(Locale.getDefault(), "%.1f", record.getBmi()));

            // Set category with color
            tvCategory.setText(record.getBmiCategory());
            tvCategory.setTextColor(itemView.getContext().getResources()
                    .getColor(record.getBmiCategoryColor(), null));

            // Set weight and height
            tvWeight.setText(String.format(Locale.getDefault(), "%.1f kg", record.getWeight()));
            tvHeight.setText(String.format(Locale.getDefault(), "%.1f cm", record.getHeight()));

            // Set delete button listener
            btnDelete.setOnClickListener(v -> {
                if (deleteListener != null) {
                    deleteListener.onDelete(record);
                }
            });
        }
    }

    // Method to update the list
    public void updateData(List<BmiRecord> newRecords) {
        this.bmiRecords.clear();
        this.bmiRecords.addAll(newRecords);
        notifyDataSetChanged();
    }

    // Method to add a new record
    public void addRecord(BmiRecord record) {
        bmiRecords.add(0, record);
        notifyItemInserted(0);
    }

    // Method to remove a record
    public void removeRecord(int position) {
        if (position >= 0 && position < bmiRecords.size()) {
            bmiRecords.remove(position);
            notifyItemRemoved(position);
        }
    }
}