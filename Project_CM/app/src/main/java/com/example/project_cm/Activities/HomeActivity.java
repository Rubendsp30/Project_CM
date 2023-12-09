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
import com.example.project_cm.Fragments.HomeScreenFragment;
import com.example.project_cm.R;
import com.example.project_cm.User;
import com.example.project_cm.ViewModels.UserViewModel;
import com.google.firebase.firestore.FirebaseFirestore;

public class HomeActivity extends AppCompatActivity implements FragmentChangeListener {
    private static final String USERS_COLLECTION = "USERS";

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
                    })
                    .addOnFailureListener(e -> Log.e("HomeActivity", "Error fetching user details: " + e.getMessage()));
        }
        else{
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish(); // Close HomeActivity
            return;
        }

        // Load the initial LoginFragment when the activity is created.
        loadFragment(new HomeScreenFragment(), "home_screen");
    }

    private String getLoggedInUserId() {
        SharedPreferences sharedPreferences = getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        return sharedPreferences.getString("loggedInUserId", null);
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