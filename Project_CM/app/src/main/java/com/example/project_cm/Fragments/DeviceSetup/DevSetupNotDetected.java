package com.example.project_cm.Fragments.DeviceSetup;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.project_cm.Activities.HomeActivity;
import com.example.project_cm.R;


public class DevSetupNotDetected extends Fragment {

    @Nullable
    private com.example.project_cm.FragmentChangeListener FragmentChangeListener;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.device_setup_not_detected, container, false);

        // Initialize the FragmentChangeListener
        this.FragmentChangeListener = (HomeActivity) inflater.getContext();

        return view;
    }

    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button tryAgainButton = view.findViewById(R.id.tryAgainButton);

        tryAgainButton.setOnClickListener(v -> transitionToDevSetupInitial());
    }


    private void transitionToDevSetupInitial() {

        if (FragmentChangeListener != null) {
            DevSetupInitial fragment = new DevSetupInitial();
            FragmentChangeListener.replaceFragment(fragment);
        } else {
            // Handle the case where FragmentChangeListener is null
            Log.e("DevSetupNotDetected", "FragmentChangeListener is null. Unable to replace the fragment.");
        }

    }


}