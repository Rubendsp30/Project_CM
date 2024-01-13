package com.example.project_cm.Fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
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

import com.example.project_cm.Activities.LoginActivity;
import com.example.project_cm.R;
import com.example.project_cm.User;
import com.example.project_cm.ViewModels.UserViewModel;
import com.example.project_cm.utils.SecurityUtils;
import com.google.android.material.textfield.TextInputLayout;

import java.io.IOException;

public class RegisterFragment extends Fragment {

    private TextInputLayout usernameRegisterLayout;
    private TextInputLayout emailRegisterLayout;
    private TextInputLayout passwordRegisterLayout;
    private TextInputLayout confirmPasswordRegisterLayout;
    private EditText usernameRegister;
    private EditText emailRegister;
    private EditText passwordRegister;
    private EditText confirmPasswordRegister;
    @Nullable
    private com.example.project_cm.FragmentChangeListener FragmentChangeListener;
    private UserViewModel userViewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the fragment's layout
        View view = inflater.inflate(R.layout.register_fragment, container, false);

        // Get FragmentChangeListener from the parent activity
        this.FragmentChangeListener = (LoginActivity) inflater.getContext();
        try {
            userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);
        } catch (Exception e) {
            Log.e("RegisterFragment", "Error creating UserViewModel: " + e.getMessage());
        }

        return view;
    }

    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize UI elements
        TextView backButton = view.findViewById(R.id.backButton);
        this.usernameRegisterLayout = view.findViewById(R.id.usernameRegisterLayout);
        this.usernameRegister = view.findViewById(R.id.usernameRegister);
        this.emailRegisterLayout = view.findViewById(R.id.emailRegisterLayout);
        this.emailRegister = view.findViewById(R.id.emailRegister);
        this.passwordRegisterLayout = view.findViewById(R.id.passwordRegisterLayout);
        this.passwordRegister = view.findViewById(R.id.passwordRegister);
        this.confirmPasswordRegisterLayout = view.findViewById(R.id.confirmPasswordLayout);
        this.confirmPasswordRegister = view.findViewById(R.id.confirmPasswordRegister);
        Button registerButton = view.findViewById(R.id.registerButton);

        // Add text change listeners for input validation
        usernameRegister.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateUsernameLength(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        emailRegister.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateEmail(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        passwordRegister.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validatePasswordLength(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        confirmPasswordRegister.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateConfirmPassword(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // Set Back Button Listener
        backButton.setOnClickListener(v -> goToLoginDisplay());

        // Set Register Button Listener
        registerButton.setOnClickListener(v -> {
            if (validateAllInput()) {
                try {
                    registerUser();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    private boolean validateUsernameLength(String input) {
        if (input != null && input.length() >= 3 && input.length() <= 15) {
            usernameRegisterLayout.setError(null);
            usernameRegisterLayout.setErrorEnabled(false);
            return true;
        } else {
            usernameRegisterLayout.setError("Username too short/long");
            return false;
        }
    }

    private boolean validateEmail(String input) {
        if (input != null && !input.isEmpty() && Patterns.EMAIL_ADDRESS.matcher(input).matches()) {
            emailRegisterLayout.setError(null);
            emailRegisterLayout.setErrorEnabled(false);
            return true;
        } else {
            emailRegisterLayout.setError("Email format invalid");
            return false;
        }
    }

    private boolean validatePasswordLength(String input) {
        if (input != null && input.length() >= 5) {
            passwordRegisterLayout.setError(null);
            passwordRegisterLayout.setErrorEnabled(false);
            return true;
        } else {
            passwordRegisterLayout.setError("Password too short");
            return false;
        }
    }

    private boolean validateConfirmPassword(String input) {
        if (input.equals(passwordRegister.getText().toString())) {
            confirmPasswordRegisterLayout.setError(null);
            confirmPasswordRegisterLayout.setErrorEnabled(false);
            return true;
        } else {
            confirmPasswordRegisterLayout.setError("Password not identical");
            return false;
        }
    }

    private boolean validateAllInput() {
        boolean isUsernameValid = validateUsernameLength(usernameRegister.getText().toString());
        boolean isEmailValid = validateEmail(emailRegister.getText().toString());
        boolean isPasswordValid = validatePasswordLength(passwordRegister.getText().toString());
        boolean isConfirmPasswordValid = validateConfirmPassword(confirmPasswordRegister.getText().toString());

        return isUsernameValid && isEmailValid && isPasswordValid && isConfirmPasswordValid;
    }

    private void goToLoginDisplay() {
        if (FragmentChangeListener != null) {
            LoginFragment fragment = new LoginFragment();
            FragmentChangeListener.replaceFragment(fragment);
        } else {
            // Handle the case where FragmentChangeListener is null
            Log.e("RegisterFragment", "FragmentChangeListener is null. Unable to replace the fragment.");
        }
    }

    private void registerUser() throws IOException {
        String newUsername = usernameRegister.getText().toString();
        String newEmail = emailRegister.getText().toString();
        String newPassword = passwordRegister.getText().toString();

        userViewModel.checkUsernameExists(newUsername, usernameExists -> {
            if (usernameExists) {
                // Username exists, handle accordingly
                usernameRegisterLayout.setError("Username already exists!");
            } else {
                // Username does not exist, check if the email exists
                userViewModel.checkEmailExists(newEmail, emailExists -> {
                    if (emailExists) {
                        // Email exists, handle accordingly
                        emailRegisterLayout.setError("Email already in use!");
                    } else {
                        // Email does not exist, proceed with registration
                        User newUser = new User(newUsername, newEmail, SecurityUtils.hashPassword(newPassword));
                        userViewModel.registerUser(newUser);
                        goToLoginDisplay();
                    }
                });
            }
        });
    }


}
