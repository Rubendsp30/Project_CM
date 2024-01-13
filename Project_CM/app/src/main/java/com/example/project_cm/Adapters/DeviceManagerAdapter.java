package com.example.project_cm.Adapters;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.RecyclerView;
import com.example.project_cm.DataBase.Tables.PetProfileEntity;
import com.example.project_cm.R;
import com.example.project_cm.ViewModels.DeviceViewModel;
import com.example.project_cm.ViewModels.PetProfileViewModel;
import com.example.project_cm.Device;
import java.util.List;
import android.util.Log;

public class DeviceManagerAdapter extends RecyclerView.Adapter<DeviceManagerAdapter.ViewHolder> {
    private List<Device> deviceList;
    private final PetProfileViewModel petProfileViewModel;
    private final LayoutInflater inflater;
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

    public DeviceManagerAdapter(List<Device> deviceList, PetProfileViewModel petProfileViewModel, LayoutInflater inflater) {
        this.deviceList = deviceList;
        this.petProfileViewModel = petProfileViewModel;
        this.inflater = inflater;
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
              //  Log.d("DeviceManagerAdapter", "Nome do Pet: " + petProfileEntity.name);
                holder.petNameView.setText(petProfileEntity.name);
            }
        });
    }
    public void setDevices(List<Device> deviceList) {
        this.deviceList = deviceList;
       // Log.d("DeviceManagerAdapter", "Set devices: " + deviceList.size());
    }
    @Override
    public int getItemCount() {
        return deviceList.size();
    }

}