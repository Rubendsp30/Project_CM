package com.example.project_cm.Adapters;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.RecyclerView;
import com.example.project_cm.DataBase.Tables.PetProfileEntity;
import com.example.project_cm.Fragments.DeviceDeletePop;
import com.example.project_cm.R;
import com.example.project_cm.ViewModels.PetProfileViewModel;
import com.example.project_cm.Device;
import java.util.ArrayList;

public class DeviceManagerAdapter extends RecyclerView.Adapter<DeviceManagerAdapter.ViewHolder> {
    private ArrayList<Device> deviceList;
    private final PetProfileViewModel petProfileViewModel;
    private final LayoutInflater inflater;
    @Nullable
    private final FragmentManager fragmentManager;


    public DeviceManagerAdapter(@Nullable FragmentManager fragmentManager,ArrayList<Device> deviceList, PetProfileViewModel petProfileViewModel, LayoutInflater inflater) {
        this.deviceList = deviceList;
        this.petProfileViewModel = petProfileViewModel;
        this.inflater = inflater;
        this.fragmentManager = fragmentManager;
    }
    @Override
    public DeviceManagerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = inflater.inflate(R.layout.device_item, parent, false);
        return new ViewHolder(v);
    }
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Device device = deviceList.get(position);
        LiveData<PetProfileEntity> petProfileLiveData = petProfileViewModel.getPetProfileById(device.getPet_id());

        petProfileLiveData.observe((LifecycleOwner) inflater.getContext(), petProfileEntity -> {
            if (petProfileEntity != null) {
                holder.petNameView.setText(petProfileEntity.name);
            }
        });

        holder.deleteButton.setOnClickListener((v) -> {
            DeviceDeletePop fragment = new DeviceDeletePop(device.getDeviceID(), (int) device.getPet_id());
            if (fragmentManager != null) {
                fragment.show(fragmentManager, "DeviceDeletePop");
            }

        });

    }

    @Override
    public int getItemCount() {
        return deviceList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView petNameView;
        public ImageButton switchStatusButton;
        public ImageButton deleteButton;

        public ViewHolder(View v) {
            super(v);
            petNameView = v.findViewById(R.id.PetName);
            switchStatusButton = v.findViewById(R.id.switchDeviceStatus);
            deleteButton = v.findViewById(R.id.btnDeleteDevice);
        }
    }
}