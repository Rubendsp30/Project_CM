package com.example.project_cm.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.content.SharedPreferences;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.project_cm.Activities.HomeActivity;
import com.example.project_cm.Activities.LoginActivity;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.example.project_cm.R;

public class MenuFragment extends Fragment {

    @Nullable
    private com.example.project_cm.FragmentChangeListener fragmentChangeListener;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        this.fragmentChangeListener = (HomeActivity) inflater.getContext();
        return inflater.inflate(R.layout.menu, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setTitle(" ");

        ((AppCompatActivity) requireActivity()).setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> {
            if (fragmentChangeListener != null) {
                fragmentChangeListener.replaceFragment(new HomeScreenFragment());
            }
        });

        Button profileCreationButton = view.findViewById(R.id.profile);
        profileCreationButton.setOnClickListener(v -> {
            if (fragmentChangeListener != null) {

                fragmentChangeListener.replaceFragment(new PetProfileCreationFragment());

            }
            //todo adicionar depois os outros botões quando estiverem feitos
        });

        Button settingsButton = view.findViewById(R.id.settings);
        settingsButton.setOnClickListener(v -> {
            if (fragmentChangeListener != null) {
                fragmentChangeListener.replaceFragment(new SettingsFragment());
            }
        });

        // o PetProfile Vai ficar para ja assim improvisado
        Button profileButton = view.findViewById(R.id.about);
        profileButton.setOnClickListener(v -> {
            if (fragmentChangeListener != null) {
                fragmentChangeListener.replaceFragment(new PetProfileFragment());
            }
        });

        Button logoutButton = view.findViewById(R.id.action_logout);
        logoutButton.setOnClickListener(v -> {
            logoutUser();
        });
    }

    private void logoutUser() {
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("isLoggedIn", false);
        editor.remove("loggedInUserId");
        editor.apply();

        Intent intent = new Intent(getActivity(), LoginActivity.class);
        startActivity(intent);
        getActivity().finish();
    }

}
