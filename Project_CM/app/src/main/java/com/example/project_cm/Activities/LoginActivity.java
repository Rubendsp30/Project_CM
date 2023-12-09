package com.example.project_cm.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import android.os.Bundle;

import com.example.project_cm.FragmentChangeListener;
import com.example.project_cm.Fragments.LoginFragment;
import com.example.project_cm.R;

public class LoginActivity extends AppCompatActivity implements FragmentChangeListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        setContentView(R.layout.activity_login);

        // Load the initial LoginFragment when the activity is created.
        loadFragment(new LoginFragment(), "login_fragment");
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