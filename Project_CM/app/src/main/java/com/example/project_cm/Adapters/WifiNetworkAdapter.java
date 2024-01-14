package com.example.project_cm.Adapters;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project_cm.History;
import com.example.project_cm.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class WifiNetworkAdapter extends ArrayAdapter<String> {

    private List<ScanResult> wifiList;

    public WifiNetworkAdapter(Context context, List<String> networkNames) {
        super(context, 0, networkNames);
        this.wifiList = new ArrayList<>(); // Initialize an empty list for wifiList
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.device_setup_wifi_item, parent, false);
        }

        TextView textViewWifiName = convertView.findViewById(R.id.textViewWifiName);
        ImageView imageViewLock = convertView.findViewById(R.id.imageViewLock);
        ImageView imageViewSignalStrength = convertView.findViewById(R.id.imageViewSignalStrength);

        String wifiNetwork = getItem(position);
        String[] parts = wifiNetwork.split(" (?=\\()|(?<=\\)) | \\[");

        String ssid = parts[0];
        textViewWifiName.setText(ssid);
        boolean isSecured = parts[1].equals("(Secured)");

        imageViewLock.setVisibility(isSecured ? View.VISIBLE : View.GONE);

        int signalLevel = Integer.parseInt(parts[2].replaceAll("[\\[\\]]", ""));
        switch (signalLevel) {
            case 4:
                imageViewSignalStrength.setImageResource(R.drawable.baseline_signal_wifi_4_bar_24);
                break;
            case 3:
                imageViewSignalStrength.setImageResource(R.drawable.baseline_network_wifi_3_bar_24);
                break;
            case 2:
                imageViewSignalStrength.setImageResource(R.drawable.baseline_network_wifi_2_bar_24);
                break;
            default:
                imageViewSignalStrength.setImageResource(R.drawable.baseline_network_wifi_1_bar_24);
                break;
        }

        return convertView;
    }
}





