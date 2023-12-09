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
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.project_cm.Activities.HomeActivity;
import com.example.project_cm.Activities.LoginActivity;
import com.example.project_cm.R;
import com.example.project_cm.ViewModels.UserViewModel;

public class HomeScreenFragment extends Fragment {


    @Nullable private com.example.project_cm.FragmentChangeListener FragmentChangeListener;
    private UserViewModel userViewModel;
    private TextView textView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the login fragment layout
        View view = inflater.inflate(R.layout.home_screen, container, false);

        // Initialize the FragmentChangeListener
        this.FragmentChangeListener = (HomeActivity) inflater.getContext();

        try {
            userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);
        } catch (Exception e) {
            Log.e("HomeScreenFragment", "Error creating UserViewModel: " + e.getMessage());
        }

        return view;
    }

    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button buttonLogOut = view.findViewById(R.id.buttonLogOut);
        this.textView = view.findViewById(R.id.textView);

        userViewModel.getCurrentUser().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                textView.setText("Welcome, " + user.getUsername() + "!");
            }
        });

        buttonLogOut.setOnClickListener(v -> logoutUser());

    }

    public void logoutUser() {
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("isLoggedIn", false);
        editor.remove("loggedInUserId");
        editor.apply();

        Intent intent = new Intent(getActivity(), LoginActivity.class);
        startActivity(intent);
        getActivity().finish();
    }

}
