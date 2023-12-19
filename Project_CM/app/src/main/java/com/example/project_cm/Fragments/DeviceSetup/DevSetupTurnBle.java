package com.example.project_cm.Fragments.DeviceSetup;


import static android.app.Activity.RESULT_OK;

import android.Manifest;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;

import android.os.Build;
import android.os.Bundle;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import androidx.fragment.app.Fragment;


import com.example.project_cm.Activities.HomeActivity;

import com.example.project_cm.R;

import java.util.Map;


public class DevSetupTurnBle extends Fragment {

    @Nullable
    private com.example.project_cm.FragmentChangeListener FragmentChangeListener;
    private BluetoothAdapter bluetoothAdapter;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.device_setup_turn_ble, container, false);

        // Initialize the FragmentChangeListener
        this.FragmentChangeListener = (HomeActivity) inflater.getContext();

        // Initialize BluetoothAdapter
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            // Device doesn't support Bluetooth
        }

        return view;
    }

    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button turnBTButton = view.findViewById(R.id.turnBTButton);

        turnBTButton.setOnClickListener(v -> ConnectBT());
    }

    public void ConnectBT() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // For Android 12 and above, request necessary permissions
            requestMultiplePermissions.launch(new String[]{
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION

            });
        } else {
            // For older versions, directly attempt to enable Bluetooth
            enableBluetooth();
        }
    }

    private void enableBluetooth() {
        if (!bluetoothAdapter.isEnabled()) {
            // Request to enable Bluetooth
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            requestBluetooth.launch(enableBtIntent);
        } else {
            // Bluetooth is already enabled
            transitionToScanFragment();
            // Toast.makeText(getActivity(), "Bluetooth already ON", Toast.LENGTH_SHORT).show();
        }
    }

    private final ActivityResultLauncher<String[]> requestMultiplePermissions = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(),
            permissions -> {
                boolean allPermissionsGranted = true;
                for (Map.Entry<String, Boolean> entry : permissions.entrySet()) {
                    if (!entry.getValue()) {
                        allPermissionsGranted = false;
                        break;
                    }
                }
                if (allPermissionsGranted) {
                    // Permissions are granted, attempt to enable Bluetooth
                    enableBluetooth();
                } else {
                    // Handle the case where permissions are not granted
                    Toast.makeText(getActivity(), "Bluetooth permissions required", Toast.LENGTH_SHORT).show();
                }
            }
    );


    private final ActivityResultLauncher<Intent> requestBluetooth = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    // granted
                    transitionToScanFragment();
                } else {
                    // deny
                }
            }
    );

    private void transitionToScanFragment() {

        if (FragmentChangeListener != null) {
            DevScanFragment fragment = new DevScanFragment();
            FragmentChangeListener.replaceFragment(fragment);
        } else {
            // Handle the case where FragmentChangeListener is null
            Log.e("DevSetupTurnBle", "FragmentChangeListener is null. Unable to replace the fragment.");
        }

    }


}