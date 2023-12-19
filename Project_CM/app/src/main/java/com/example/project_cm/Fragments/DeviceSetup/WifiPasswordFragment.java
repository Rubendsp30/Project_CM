package com.example.project_cm.Fragments.DeviceSetup;

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
import com.example.project_cm.R;
import com.example.project_cm.ViewModels.BluetoothViewModel;


public class WifiPasswordFragment extends Fragment {

    private BluetoothViewModel bluetoothViewModel;
    private String selectedSSID;
    private com.example.project_cm.FragmentChangeListener FragmentChangeListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.device_setup_wifi_pass, container, false);

        bluetoothViewModel = new ViewModelProvider(requireActivity()).get(BluetoothViewModel.class);

        // Initialize the FragmentChangeListener
        this.FragmentChangeListener = (HomeActivity) inflater.getContext();

        // Retrieve the SSID from the arguments
        if (getArguments() != null) {
            selectedSSID = getArguments().getString("SSID");
        }

        return view;
    }

    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        EditText ssidEditText = view.findViewById(R.id.wifiSSID);
        EditText passwordEditText = view.findViewById(R.id.passwordWifi);
        Button connectButton = view.findViewById(R.id.connectButton);

        ssidEditText.setText(selectedSSID);

        TextView changeSSID = view.findViewById(R.id.selectOtherText);
        changeSSID.setOnClickListener(v -> transitionToWifiScanFragment());

        connectButton.setOnClickListener(v -> {
            String password = passwordEditText.getText().toString();
            sendPasswordOverBluetooth(selectedSSID, password);

            Bundle bundle = new Bundle();
            bundle.putString("SSID", selectedSSID);
            getParentFragmentManager().setFragmentResult("SSID", bundle);

            DevWifiConnectingFragment fragment = new DevWifiConnectingFragment();
            fragment.setArguments(bundle);
            FragmentChangeListener.replaceFragment(fragment);

        });
    }

    private void sendPasswordOverBluetooth(String ssid, String password) {
        String message = "SSID:" + ssid + ";PASSWORD:" + password + "\n";
        bluetoothViewModel.sendMessage(message);
    }

    private void transitionToWifiScanFragment() {

        if (FragmentChangeListener != null) {
            DevWiFiScanFragment fragment = new DevWiFiScanFragment();
            FragmentChangeListener.replaceFragment(fragment);
        } else {
            // Handle the case where FragmentChangeListener is null
            Log.e("WifiPasswordFragment", "FragmentChangeListener is null. Unable to replace the fragment.");
        }

    }
}