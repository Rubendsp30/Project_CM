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
import androidx.lifecycle.ViewModelProvider;

import com.example.project_cm.Activities.HomeActivity;
import com.example.project_cm.Fragments.PetProfileCreationFragment;
import com.example.project_cm.R;
import com.example.project_cm.ViewModels.DeviceViewModel;

import java.security.SecureRandom;

public class DevSetupInitial extends Fragment {

    @Nullable
    private com.example.project_cm.FragmentChangeListener FragmentChangeListener;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.device_setup_initial, container, false);

        // Initialize the FragmentChangeListener
        this.FragmentChangeListener = (HomeActivity) inflater.getContext();

        return view;
    }

    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button deviceLightConfirmation = view.findViewById(R.id.deviceLightConfirmation);

        deviceLightConfirmation.setOnClickListener(v -> transitionToTurnBleFragment());

        Button debugCreateDeviceButton = view.findViewById(R.id.debugCreateDeviceButton);
        String CHAR_LOWER = "abcdefghijklmnopqrstuvwxyz";
        String CHAR_UPPER = CHAR_LOWER.toUpperCase();
        String NUMBER = "0123456789";
        String DATA_FOR_RANDOM_STRING = CHAR_LOWER + CHAR_UPPER + NUMBER;
        SecureRandom random = new SecureRandom();

        StringBuilder sb = new StringBuilder(20);
        for (int i = 0; i < 18; i++) {
            int rndCharAt = random.nextInt(DATA_FOR_RANDOM_STRING.length());
            char rndChar = DATA_FOR_RANDOM_STRING.charAt(rndCharAt);
            sb.append(rndChar);
        }
        String deviceId = sb.toString();

        debugCreateDeviceButton.setOnClickListener(v -> {
            DeviceViewModel deviceViewModel = new ViewModelProvider(requireActivity()).get(DeviceViewModel.class);
            deviceViewModel.setNewDeviceId(deviceId);
            if (FragmentChangeListener != null) {
                PetProfileCreationFragment fragment = new PetProfileCreationFragment();
                FragmentChangeListener.replaceFragment(fragment);
            }
        });

    }


    private void transitionToTurnBleFragment() {

        if (FragmentChangeListener != null) {
            DevSetupTurnBle fragment = new DevSetupTurnBle();
            FragmentChangeListener.replaceFragment(fragment);
        } else {
            // Handle the case where FragmentChangeListener is null
            Log.e("DevSetupInitial", "FragmentChangeListener is null. Unable to replace the fragment.");
        }

    }


}