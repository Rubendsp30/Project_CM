package com.example.project_cm.Fragments.DeviceSetup;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.project_cm.Activities.HomeActivity;
import com.example.project_cm.ViewModels.BluetoothViewModel;
import com.example.project_cm.R;
import com.example.project_cm.ViewModels.DeviceViewModel;

import java.io.IOException;
import java.util.UUID;

public class DevScanFragment extends Fragment {

    private com.example.project_cm.FragmentChangeListener FragmentChangeListener;
    private BluetoothAdapter bluetoothAdapter;
    private boolean isDeviceFound = false;
    private final Handler scanTimeoutHandler = new Handler();
    private final Runnable scanTimeoutRunnable = () -> {
        if (!isDeviceFound) {
            transitionToNotDetectedFragment();
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.device_setup_scan_bt, container, false);

        this.FragmentChangeListener = (HomeActivity) inflater.getContext();

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        checkLocationAndScanDevices();
    }


    private void startBluetoothDiscovery() {
        if (bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }
        bluetoothAdapter.startDiscovery();
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        getContext().registerReceiver(receiver, filter);

        scanTimeoutHandler.postDelayed(scanTimeoutRunnable, 10000); // 10 seconds
    }


    private void promptEnableLocation() {
        Toast.makeText(getContext(), "Please enable location services to scan for Bluetooth devices", Toast.LENGTH_LONG).show();
        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivity(intent);
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device.getName() != null && device.getName().startsWith("ESP32") && !isDeviceFound) {
                    isDeviceFound = true;
                    bluetoothAdapter.cancelDiscovery();
                    PairDevice(device);
                }
            }
        }
    };

    private void PairDevice(BluetoothDevice device) {
        try {
            BluetoothSocket socket = device.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
            socket.connect();

            BluetoothViewModel bluetoothViewModel = new ViewModelProvider(requireActivity()).get(BluetoothViewModel.class);
            DeviceViewModel deviceViewModel = new ViewModelProvider(requireActivity()).get(DeviceViewModel.class);
            bluetoothViewModel.setSocket(socket);
            String deviceId = extractDeviceId(device.getName());
            deviceViewModel.setNewDeviceId(deviceId);
            transitionWifiScanFragment();
            scanTimeoutHandler.removeCallbacks(scanTimeoutRunnable);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String extractDeviceId(String deviceName) {
        int index = deviceName.lastIndexOf("-");
        if (index != -1) {
            return deviceName.substring(index + 1);
        }
        return null;
    }

    private void transitionWifiScanFragment() {
        if (FragmentChangeListener != null) {
            DevWiFiScanFragment fragment = new DevWiFiScanFragment();

            // Use FragmentChangeListener to replace the fragment
            FragmentChangeListener.replaceFragment(fragment);
        } else {
            // Handle the case where FragmentChangeListener is null
            Log.e("DevScanFragment", "FragmentChangeListener is null. Unable to replace the fragment.");
        }
    }

    private void transitionToNotDetectedFragment() {
        if (FragmentChangeListener != null) {
            DevSetupNotDetected fragment = new DevSetupNotDetected();

            FragmentChangeListener.replaceFragment(fragment);
        } else {
            // Handle the case where FragmentChangeListener is null
            Log.e("DevScanFragment", "FragmentChangeListener is null. Unable to replace the fragment.");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getContext().unregisterReceiver(receiver);
        if (bluetoothAdapter != null) {
            bluetoothAdapter.cancelDiscovery();
        }
    }


    private void checkLocationAndScanDevices() {
        if (bluetoothAdapter == null) {
            return;
        }

        if (!bluetoothAdapter.isEnabled()) {
            return;
        }

        LocationManager locationManager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            promptEnableLocation();
            return;
        }

        startBluetoothDiscovery();
    }
}