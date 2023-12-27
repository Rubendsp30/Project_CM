package com.example.project_cm.Fragments.DeviceSetup;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.project_cm.Activities.HomeActivity;
import com.example.project_cm.Fragments.PetProfileCreationFragment;
import com.example.project_cm.R;
import com.example.project_cm.ViewModels.BluetoothViewModel;


public class DevWifiConnectingFragment extends Fragment {
    private BluetoothViewModel bluetoothViewModel;
    @Nullable
    private com.example.project_cm.FragmentChangeListener FragmentChangeListener;
    private String selectedSSID;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.device_setup_connecting, container, false);
        // Initialize the FragmentChangeListener
        this.FragmentChangeListener = (HomeActivity) inflater.getContext();

        bluetoothViewModel = new ViewModelProvider(requireActivity()).get(BluetoothViewModel.class);
        listenToBluetoothResponses();

        if (getArguments() != null) {
            selectedSSID = getArguments().getString("SSID", selectedSSID); // Use the passed SSID if available
        }
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    private void listenToBluetoothResponses() {
        bluetoothViewModel.setMessageListener(message -> {
            getActivity().runOnUiThread(() -> {
                if ("CONNECTED".equals(message.trim())) {
                    // Transition to PetProfileCreation
                    if (FragmentChangeListener != null) {
                        PetProfileCreationFragment fragment = new PetProfileCreationFragment();
                        FragmentChangeListener.replaceFragment(fragment);
                    }
                } else if ("FAILED".equals(message.trim())) {
                    handleFailedConnection();
                }
            });
        });

        bluetoothViewModel.startListeningForMessages();
    }

    private void handleFailedConnection() {
        if (FragmentChangeListener != null) {
            WifiPasswordFragment fragment = new WifiPasswordFragment();

            // Create a bundle to pass the SSID
            Bundle bundle = new Bundle();
            bundle.putString("SSID", selectedSSID); // Ensure selectedSSID is stored in ConnectingFragment
            fragment.setArguments(bundle);

            FragmentChangeListener.replaceFragment(fragment);
        }
    }

}