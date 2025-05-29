package com.emsi.fittracker;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.emsi.fittracker.R;
import com.emsi.fittracker.fragments.WorkoutFragment;
import com.emsi.fittracker.fragments.ProgressFragment;
import com.emsi.fittracker.fragments.StatsFragment;
import com.emsi.fittracker.fragments.BmiCalculatorFragment;
import com.emsi.fittracker.fragments.ProfileFragment;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private FragmentManager fragmentManager;

    // Fragment instances
    private WorkoutFragment workoutFragment;
    private ProgressFragment progressFragment;
    private StatsFragment statsFragment;
    private BmiCalculatorFragment bmiCalculatorFragment;
    private ProfileFragment profileFragment;

    private Fragment activeFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        initFragments();
        setupBottomNavigation();

        // Set default fragment
        if (savedInstanceState == null) {
            showFragment(workoutFragment);
            bottomNavigationView.setSelectedItemId(R.id.nav_workouts);
        }

        // Handle intent extras (for switching to progress tab after workout session)
        handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (intent != null && intent.getBooleanExtra("switch_to_progress", false)) {
            // Switch to progress tab
            bottomNavigationView.setSelectedItemId(R.id.nav_progress);
            showFragment(progressFragment);
        }
    }

    private void initViews() {
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        fragmentManager = getSupportFragmentManager();
    }

    private void initFragments() {
        workoutFragment = new WorkoutFragment();
        progressFragment = new ProgressFragment();
        statsFragment = new StatsFragment();
        bmiCalculatorFragment = new BmiCalculatorFragment();
        profileFragment = new ProfileFragment();

        // Add all fragments to the container but hide them initially
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.add(R.id.nav_host_fragment, workoutFragment, "WORKOUT");
        transaction.add(R.id.nav_host_fragment, progressFragment, "PROGRESS");
        transaction.add(R.id.nav_host_fragment, statsFragment, "STATS");
        transaction.add(R.id.nav_host_fragment, bmiCalculatorFragment, "BMI");
        transaction.add(R.id.nav_host_fragment, profileFragment, "PROFILE");

        // Hide all fragments initially
        transaction.hide(workoutFragment);
        transaction.hide(progressFragment);
        transaction.hide(statsFragment);
        transaction.hide(bmiCalculatorFragment);
        transaction.hide(profileFragment);

        transaction.commit();
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == R.id.nav_workouts) {
                    showFragment(workoutFragment);
                    return true;
                } else if (itemId == R.id.nav_progress) {
                    showFragment(progressFragment);
                    return true;
                } else if (itemId == R.id.nav_stats) {
                    showFragment(statsFragment);
                    return true;
                } else if (itemId == R.id.nav_bmi) {
                    showFragment(bmiCalculatorFragment);
                    return true;
                } else if (itemId == R.id.nav_profile) {
                    showFragment(profileFragment);
                    return true;
                }

                return false;
            }
        });
    }

    private void showFragment(Fragment fragment) {
        FragmentTransaction transaction = fragmentManager.beginTransaction();

        // Hide current active fragment
        if (activeFragment != null) {
            transaction.hide(activeFragment);
        }

        // Show selected fragment
        transaction.show(fragment);
        transaction.commit();

        activeFragment = fragment;
    }

    @Override
    public void onBackPressed() {
        // If not on the first tab (Workouts), go back to Workouts
        if (bottomNavigationView.getSelectedItemId() != R.id.nav_workouts) {
            bottomNavigationView.setSelectedItemId(R.id.nav_workouts);
        } else {
            super.onBackPressed();
        }
    }

    // Method to get current active fragment
    public Fragment getActiveFragment() {
        return activeFragment;
    }

    // Method to programmatically switch tabs
    public void switchToTab(int tabId) {
        bottomNavigationView.setSelectedItemId(tabId);
    }
}