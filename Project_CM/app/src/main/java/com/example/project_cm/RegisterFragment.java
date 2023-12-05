package com.example.project_cm;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.project_cm.DataBase.Tables.UserEntity;
import com.example.project_cm.ViewModels.UserViewModel;
import com.example.project_cm.utils.SecurityUtils;

import java.io.IOException;

public class RegisterFragment extends Fragment {

    private EditText usernameRegister;
    private EditText passwordRegister;
    private EditText confirmPasswordRegister;
    @Nullable private FragmentChangeListener FragmentChangeListener;
    private UserViewModel userViewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the fragment's layout
        View view = inflater.inflate(R.layout.register_fragment, container, false);

        // Get FragmentChangeListener from the parent activity
        this.FragmentChangeListener = (MainActivity) inflater.getContext();
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
        ImageButton backButton = view.findViewById(R.id.backButton);
        this.usernameRegister = view.findViewById(R.id.usernameRegister);
        this.passwordRegister = view.findViewById(R.id.passwordRegister);
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
            return true;
        } else {
            usernameRegister.setError("Username too short/long");
            return false;
        }
    }

    private boolean validatePasswordLength(String input) {
        if (input != null && input.length() >= 5) {
            return true;
        } else {
            passwordRegister.setError("Password too short");
            return false;
        }
    }

    private boolean validateConfirmPassword(String input) {
        if (input.equals(passwordRegister.getText().toString())) {
            return true;
        } else {
            confirmPasswordRegister.setError("Password not identical");
            return false;
        }
    }

    private boolean validateAllInput() {
        boolean isUsernameValid = validateUsernameLength(usernameRegister.getText().toString());
        boolean isPasswordValid = validatePasswordLength(passwordRegister.getText().toString());
        boolean isConfirmPasswordValid = validateConfirmPassword(confirmPasswordRegister.getText().toString());

        return isUsernameValid && isPasswordValid && isConfirmPasswordValid;
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
        String newPassword = passwordRegister.getText().toString();

        userViewModel.checkUserExists(newUsername, count -> {
            if (count > 0) {
                // Username already exists
                usernameRegister.setError("Username already exists!");
            } else {
                // Username is unique, proceed with registration
                UserEntity newUser = new UserEntity();
                newUser.username = newUsername;
                newUser.password = SecurityUtils.hashPassword(newPassword);
                userViewModel.insertUserEntity(newUser);

                goToLoginDisplay();
            }
        });


    }

}
