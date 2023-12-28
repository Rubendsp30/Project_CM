package com.example.project_cm.Adapters;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project_cm.Device;
import com.example.project_cm.Fragments.TreatPopUpFragment;
import com.example.project_cm.MealSchedule;
import com.example.project_cm.R;
import com.example.project_cm.ViewModels.DeviceViewModel;
import com.example.project_cm.ViewModels.PetProfileViewModel;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeAdapter extends RecyclerView.Adapter<HomeAdapter.HomeViewHolder> {

    @Nullable
    private com.example.project_cm.FragmentChangeListener FragmentChangeListener;
    @Nullable
    private final FragmentManager fragmentManager;

    private ArrayList<Device> viewPagerIDeviceArrayList;
    private final DeviceViewModel deviceViewModel;
    private final PetProfileViewModel petProfileViewModel;
    private final LifecycleOwner lifecycleOwner;
    private LiveData<ArrayList<Device>> devicesLiveData;


    public HomeAdapter(String userId,ArrayList<Device> viewPagerIDeviceArrayList, @Nullable FragmentManager fragmentManager, DeviceViewModel deviceViewModel, PetProfileViewModel petProfileViewModel, LifecycleOwner lifecycleOwner) {
        this.viewPagerIDeviceArrayList = viewPagerIDeviceArrayList;
        this.fragmentManager = fragmentManager;
        this.deviceViewModel = deviceViewModel;
        this.petProfileViewModel = petProfileViewModel;
        this.lifecycleOwner = lifecycleOwner;
        this.devicesLiveData = deviceViewModel.listenForDeviceUpdates(userId);
        this.devicesLiveData.observe(lifecycleOwner, this::updateDevices);
    }

    private void updateDevices(ArrayList<Device> devices) {
        this.viewPagerIDeviceArrayList = devices;
        notifyDataSetChanged(); // Note: For better performance, consider using more specific notify methods
    }


    @Override
    public void onBindViewHolder(@NonNull HomeAdapter.HomeViewHolder holder, int position) {

        Map<String, Boolean> repeatDays1 = new HashMap<>();
        repeatDays1.put("Monday", true);
        repeatDays1.put("Tuesday", false);
        repeatDays1.put("Wednesday", true);
        repeatDays1.put("Thursday", false);
        repeatDays1.put("Friday", false);
        repeatDays1.put("Saturday", true);
        repeatDays1.put("Sunday", true);

        List<MealSchedule> meals = new ArrayList<MealSchedule>();
        meals.add(new MealSchedule("schedule_1", Timestamp.from(Instant.now()), repeatDays1, true, false, 3));
        meals.add(new MealSchedule("schedule_2", Timestamp.from(Instant.now()), repeatDays1, true, false, 5));
        meals.add(new MealSchedule("schedule_3", Timestamp.from(Instant.now()), repeatDays1, true, false, 10));
        meals.add(new MealSchedule("schedule_4", Timestamp.from(Instant.now()), repeatDays1, true, false, 10));
        meals.add(new MealSchedule("schedule_5", Timestamp.from(Instant.now()), repeatDays1, true, false, 10));

        MealScheduleAdapter mealScheduleAdapter = new MealScheduleAdapter(meals);
        holder.schedulesRecycler.setLayoutManager(new LinearLayoutManager(holder.itemView.getContext()));
        holder.schedulesRecycler.setAdapter(mealScheduleAdapter);

        Device viewPagerItem = viewPagerIDeviceArrayList.get(position);
        long petProfileId = viewPagerItem.getPet_id();
        petProfileViewModel.getPetProfileById(petProfileId).observe(lifecycleOwner, petProfileEntity -> {
            if (petProfileEntity != null) {
                holder.petNameText.setText(petProfileEntity.name);
            }
        });

        int foodSuply = viewPagerItem.getFoodSuply();
        String foodSuplyText = foodSuply + "% Food Supply";
        holder.supplyText.setText(foodSuplyText);
        holder.supplyProgressBar.setProgress(foodSuply);

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

        TextView petNameText;
        ImageButton treatButton;
        RecyclerView schedulesRecycler;
        TextView supplyText;
        ProgressBar supplyProgressBar;

        public HomeViewHolder(@NonNull View homeView) {
            super(homeView);

            petNameText = itemView.findViewById(R.id.petNameText);
            treatButton = itemView.findViewById(R.id.treatButton);
            schedulesRecycler = itemView.findViewById(R.id.schedulesRecycler);
            supplyText = homeView.findViewById(R.id.supplyText);
            supplyProgressBar = itemView.findViewById(R.id.supplyProgressBar);

        }
    }

}