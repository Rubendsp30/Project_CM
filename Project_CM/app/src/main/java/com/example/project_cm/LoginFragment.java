package com.example.project_cm;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.project_cm.ViewModels.UserViewModel;
import com.example.project_cm.utils.SecurityUtils;

public class LoginFragment extends Fragment {

    private EditText usernameLogin;
    private EditText passwordLogin;
    @Nullable private FragmentChangeListener FragmentChangeListener;
    private UserViewModel userViewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the login fragment layout
        View view = inflater.inflate(R.layout.login_fragment, container, false);

        // Initialize the FragmentChangeListener
        this.FragmentChangeListener = (MainActivity) inflater.getContext();

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
        Button createAccountButton = view.findViewById(R.id.createAccountButton);
        Button loginButton = view.findViewById(R.id.loginButton);
        this.usernameLogin = view.findViewById(R.id.usernameLogin);
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

    private void login() {
        String username = usernameLogin.getText().toString();
        String password = passwordLogin.getText().toString();

        // Hash the password
        String hashedPassword = SecurityUtils.hashPassword(password);

        userViewModel.getUserByUsernameAndPassword(username, hashedPassword, user -> {
            if (user != null) {
                // Login successful
                // Navigate to the next screen or show success message
                Toast.makeText(getContext(), "Login Successful", Toast.LENGTH_SHORT).show();
                // goToNextScreen(); // Implement this method
            } else {
                // Login failed
                Toast.makeText(getContext(), "Invalid username or password", Toast.LENGTH_SHORT).show();
            }
        });
    }


}
