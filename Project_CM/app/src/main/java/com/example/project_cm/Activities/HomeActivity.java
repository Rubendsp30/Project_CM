package com.example.project_cm.Activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.project_cm.FragmentChangeListener;
import com.example.project_cm.Fragments.DeviceSetup.DevSetupInitial;
import com.example.project_cm.Fragments.DeviceSetup.DevSetupTurnBle;
import com.example.project_cm.Fragments.HomeScreenFragment;
import com.example.project_cm.R;
import com.example.project_cm.User;
import com.example.project_cm.ViewModels.UserViewModel;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class HomeActivity extends AppCompatActivity implements FragmentChangeListener {
    private static final String USERS_COLLECTION = "USERS";
    private static final String DEVICES_COLLECTION = "DEVICES";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        setContentView(R.layout.activity_home);

        String userId = getLoggedInUserId();
        if (userId != null) {
            FirebaseFirestore firestore = FirebaseFirestore.getInstance();

            firestore.collection(USERS_COLLECTION)
                    .document(userId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        User user = documentSnapshot.toObject(User.class);
                        UserViewModel userViewModel = new ViewModelProvider(this).get(UserViewModel.class);
                        userViewModel.setCurrentUser(user);

                        checkUserHasDevice(firestore, userId, this);
                    })
                    .addOnFailureListener(e -> Log.e("HomeActivity", "Error fetching user details: " + e.getMessage()));
        }
        else{
            redirectToLogin();
        }

        // Load the initial LoginFragment when the activity is created.
        //loadFragment(new HomeScreenFragment(), "home_screen");
        //loadFragment(new DevSetupInitial(), "device_setup_initial");
    }

    private String getLoggedInUserId() {
        SharedPreferences sharedPreferences = getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        return sharedPreferences.getString("loggedInUserId", null);
    }


    private void checkUserHasDevice(FirebaseFirestore firestore, String userId, AppCompatActivity activity) {
        Query query = firestore.collection(DEVICES_COLLECTION).whereEqualTo("user_id", userId);
        query.get().addOnCompleteListener(task -> {
            if (isActivityActive(activity)) {
                if (task.isSuccessful() && !task.getResult().isEmpty()) {
                    // User has a device, load HomeScreenFragment
                    loadFragment(new HomeScreenFragment(), "home_screen");
                } else {
                    // User does not have a device or DEVICES collection does not exist
                    loadFragment(new DevSetupInitial(), "device_setup_initial");
                }
            }
        }).addOnFailureListener(e -> {
            if (isActivityActive(activity)) {
                //loadFragment(new DevSetupInitial(), "device_setup_initial");
            }
        });
    }

    private boolean isActivityActive(AppCompatActivity activity) {
        return activity != null && !activity.isFinishing() && !activity.isDestroyed();
    }

    private void redirectToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void replaceFragment(Fragment fragment) {
        // Replace the current fragment with the provided fragment.
        if (fragment != null) {
            loadFragment(fragment, fragment.toString());
        }
    }

    private void loadFragment(Fragment fragment, String tag) {
        // Load the specified fragment into the fragment container view
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainerView, fragment, tag)
                    .commit();
        }
    }
}