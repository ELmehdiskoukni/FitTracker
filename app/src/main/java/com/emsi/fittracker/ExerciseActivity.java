package com.emsi.fittracker;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.emsi.fittracker.R;
import com.emsi.fittracker.fragments.ExerciceFragment;

public class ExerciseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        // Get data from intent
        String workoutId = getIntent().getStringExtra("workout_id");
        String workoutTitle = getIntent().getStringExtra("workout_title");

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(workoutTitle);
        }

        // Add the fragment
        if (savedInstanceState == null) {
            ExerciceFragment fragment = ExerciceFragment.newInstance(workoutId, workoutTitle);
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}