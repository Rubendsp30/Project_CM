package com.example.project_cm.Fragments.DeviceSetup;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.project_cm.Activities.HomeActivity;
import com.example.project_cm.Device;
import com.example.project_cm.Fragments.HomeScreenFragment;
import com.example.project_cm.MQTTHelper;
import com.example.project_cm.R;
import com.example.project_cm.ViewModels.DeviceViewModel;
import com.example.project_cm.ViewModels.UserViewModel;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.sql.Timestamp;
import java.time.Instant;


public class DevSetupFinal extends Fragment {

    @Nullable
    private com.example.project_cm.FragmentChangeListener FragmentChangeListener;
    private UserViewModel userViewModel;
    private DeviceViewModel deviceViewModel;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.device_setup_final, container, false);

        // Initialize the FragmentChangeListener
        this.FragmentChangeListener = (HomeActivity) inflater.getContext();

        userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);
        deviceViewModel = new ViewModelProvider(requireActivity()).get(DeviceViewModel.class);

        return view;
    }

    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button createProfileButton = view.findViewById(R.id.createProfileButton);

        createProfileButton.setOnClickListener(v -> transitionToHomeScreenFragment());
    }


    private void transitionToHomeScreenFragment() {

        if (FragmentChangeListener != null) {
            HomeScreenFragment fragment = new HomeScreenFragment();
            FragmentChangeListener.replaceFragment(fragment);
        } else {
            // Handle the case where FragmentChangeListener is null
            Log.e("DevSetupFinal", "FragmentChangeListener is null. Unable to replace the fragment.");
        }

    }



}