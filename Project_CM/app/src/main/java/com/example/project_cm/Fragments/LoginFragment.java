package com.example.project_cm.Fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.project_cm.Activities.HomeActivity;
import com.example.project_cm.Activities.LoginActivity;
import com.example.project_cm.R;
import com.example.project_cm.ViewModels.UserViewModel;
import com.example.project_cm.utils.SecurityUtils;
import com.google.android.material.textfield.TextInputLayout;

public class LoginFragment extends Fragment {

    private TextInputLayout usernameLoginLayout;
    private TextInputLayout passwordLoginLayout;
    private EditText usernameLogin;
    private EditText passwordLogin;
    @Nullable private com.example.project_cm.FragmentChangeListener FragmentChangeListener;
    private UserViewModel userViewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the login fragment layout
        View view = inflater.inflate(R.layout.login_fragment, container, false);

        // Initialize the FragmentChangeListener
        this.FragmentChangeListener = (LoginActivity) inflater.getContext();

        try {
            userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);
        } catch (Exception e) {
            Log.e("LoginFragment", "Error creating UserViewModel: " + e.getMessage());
        }

        return view;
    }

    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize UI elements and set click listeners
        TextView createAccountButton = view.findViewById(R.id.createAccountButton);
        Button loginButton = view.findViewById(R.id.loginButton);
        this.usernameLoginLayout = view.findViewById(R.id.usernameLoginLayout);
        this.usernameLogin = view.findViewById(R.id.usernameLogin);
        this.passwordLoginLayout = view.findViewById(R.id.passwordLoginLayout);
        this.passwordLogin = view.findViewById(R.id.passwordLogin);

        createAccountButton.setOnClickListener(v -> goToRegisterDisplay());
        loginButton.setOnClickListener(v -> login());
    }

    private void goToRegisterDisplay() {
        // Navigate to the registration fragment
        if (FragmentChangeListener != null) {
            FragmentChangeListener.replaceFragment(new RegisterFragment());
        } else {
            Log.e("LoginFragment-goToRegisterDisplay", "FragmentChangeListener is null. Unable to replace the fragment.");
        }
    }

    private void goToHomeScreen() {
        Intent intent = new Intent(getActivity(), HomeActivity.class);
        startActivity(intent);
        getActivity().finish();
    }

    private void login() {
        String username = usernameLogin.getText().toString();
        String password = passwordLogin.getText().toString();

        // Check if the username or password fields are empty
        if (username.isEmpty()) {
            usernameLoginLayout.setError("Please enter a username");
            return; // Stop the method execution if validation fails
        }
        else{
            usernameLoginLayout.setError(null);
        }

        if (password.isEmpty()) {
            passwordLoginLayout.setError("Please enter a password");
            return; // Stop the method execution if validation fails
        }
        else{
            passwordLoginLayout.setError(null);
        }

        userViewModel.authenticateUser(username, SecurityUtils.hashPassword(password), (isAuthenticated, userId) -> {
            if (isAuthenticated) {
                passwordLoginLayout.setError(null);
                //String userId =;
                saveLoggedInUser(getActivity(), userId);
                // User authenticated, navigate to the next screen or show success message
                goToHomeScreen();
                // Navigate to next fragment or activity
            } else {
                // Authentication failed, show error message
                //TODO sETERROR on the field that is wrong
                passwordLoginLayout.setError("Invalid username or password");
            }
        });

    }

    public void saveLoggedInUser(Context context, String userId) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("isLoggedIn", true);
        editor.putString("loggedInUserId", userId);
        editor.apply();
    }


}
