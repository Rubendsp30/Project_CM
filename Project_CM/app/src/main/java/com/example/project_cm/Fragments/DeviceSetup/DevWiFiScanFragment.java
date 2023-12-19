package com.example.project_cm.Fragments.DeviceSetup;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.project_cm.Activities.HomeActivity;
import com.example.project_cm.R;
import com.example.project_cm.ViewModels.BluetoothViewModel;

import java.util.ArrayList;
import java.util.List;

public class DevWiFiScanFragment extends Fragment {

    private WifiManager wifiManager;
    private List<ScanResult> wifiList;
    private ArrayAdapter<String> adapter;
    private ListView wifiListView;

    @Nullable
    private com.example.project_cm.FragmentChangeListener FragmentChangeListener;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BluetoothViewModel bluetoothViewModel = new ViewModelProvider(requireActivity()).get(BluetoothViewModel.class);
        wifiManager = (WifiManager) requireActivity().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.device_setup_scan_wifi, container, false);

        // Initialize the FragmentChangeListener
        this.FragmentChangeListener = (HomeActivity) inflater.getContext();

        // Setup ListView
        wifiListView = view.findViewById(R.id.listViewWifiNetworks);
        adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1);
        wifiListView.setAdapter(adapter);

        wifiListView.setOnItemClickListener((adapterView, view1, position, id) -> {
            String networkSSID = wifiList.get(position).SSID;

            Bundle bundle = new Bundle();
            bundle.putString("SSID", networkSSID);
            getParentFragmentManager().setFragmentResult("SSID", bundle);

            WifiPasswordFragment fragment = new WifiPasswordFragment();
            fragment.setArguments(bundle);
            FragmentChangeListener.replaceFragment(fragment);
        });

        // Start Wi-Fi Scan
        scanWifiNetworks();

        return view;
    }

    private void scanWifiNetworks() {
        if (!wifiManager.isWifiEnabled()) {
            Toast.makeText(getContext(), "Enabling Wi-Fi...", Toast.LENGTH_SHORT).show();
            wifiManager.setWifiEnabled(true);
        }

        BroadcastReceiver wifiScanReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context c, Intent intent) {
                boolean success = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false);
                if (success) {
                    displayWifiList();
                } else {
                    // handle failure in wifi scan
                }
                requireActivity().unregisterReceiver(this); // Don't forget to unregister this receiver
            }
        };

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        requireActivity().registerReceiver(wifiScanReceiver, intentFilter);

        wifiManager.startScan();
    }

    private void displayWifiList() {
        wifiList = wifiManager.getScanResults();
        List<String> networkNames = new ArrayList<>();
        for (ScanResult scanResult : wifiList) {
            if (scanResult.SSID != null && !scanResult.SSID.isEmpty()) {
                networkNames.add(scanResult.SSID);
            }
        }
        adapter.clear();
        adapter.addAll(networkNames);
        adapter.notifyDataSetChanged();
    }

}
