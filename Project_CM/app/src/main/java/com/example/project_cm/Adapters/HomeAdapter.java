package com.example.project_cm.Adapters;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project_cm.Device;
import com.example.project_cm.Fragments.TreatPopUpFragment;
import com.example.project_cm.R;
import com.example.project_cm.ViewModels.DeviceViewModel;

import java.util.ArrayList;

public class HomeAdapter extends RecyclerView.Adapter<HomeAdapter.HomeViewHolder> {

    @Nullable private com.example.project_cm.FragmentChangeListener FragmentChangeListener;
    @Nullable private final FragmentManager fragmentManager;

    private final ArrayList<Device> viewPagerIDeviceArrayList;
    private final DeviceViewModel deviceViewModel;

    public HomeAdapter(ArrayList<Device> viewPagerIDeviceArrayList , @Nullable FragmentManager fragmentManager, DeviceViewModel deviceViewModel) {
        this.viewPagerIDeviceArrayList = viewPagerIDeviceArrayList;
        this.fragmentManager = fragmentManager;
        this.deviceViewModel = deviceViewModel;
    }

    @Override
    public void onBindViewHolder(@NonNull HomeAdapter.HomeViewHolder holder, int position) {

        Device viewPagerItem = viewPagerIDeviceArrayList.get(position);

        holder.deviceID.setText(viewPagerItem.getDeviceID());
        holder.treatButton.setOnClickListener(v -> {


            deviceViewModel.setCurrentDevice(viewPagerItem);
            TreatPopUpFragment fragment = new TreatPopUpFragment(deviceViewModel);
            if (fragmentManager != null) {
                fragment.show(fragmentManager, "TreatPopUpFragment");
            }

        });

    }

    @Override
    public int getItemCount() {
        return viewPagerIDeviceArrayList.size();
    }

    // Create a new ViewHolder for the RecyclerView items.
    @NonNull
    @Override
    public HomeAdapter.HomeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the layout for the note card.
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.home_page_item, parent, false);
        return new HomeViewHolder(view);
    }

    // ViewHolder class for holding the views of each note card.
    public static class HomeViewHolder extends RecyclerView.ViewHolder {

        TextView deviceID;
        ImageButton treatButton;

        public HomeViewHolder(@NonNull View homeView) {
            super(homeView);

            deviceID = itemView.findViewById(R.id.petNameText);
            treatButton = itemView.findViewById(R.id.treatButton);

        }
    }

}