package com.example.project_cm.Fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.appcompat.widget.Toolbar;
import android.widget.ProgressBar;
import java.util.ArrayList;
import java.util.List;


import com.example.project_cm.Activities.HomeActivity;
import com.example.project_cm.MealSchedule;
import com.example.project_cm.ViewModels.DeviceViewModel;
import com.example.project_cm.ViewModels.ScheduleViewModel;
import com.example.project_cm.R;
import com.example.project_cm.ViewModels.FoodInfoViewModel;

public class FoodInfoFragment extends Fragment {
    @Nullable
    private com.example.project_cm.FragmentChangeListener fragmentChangeListener;
    private FoodInfoViewModel foodInfoViewModel;
    List<MealSchedule> mealScheduleList;
    private ScheduleViewModel scheduleViewModel;
    private DeviceViewModel deviceViewModel;
    private TextView textViewTemperature;
    private TextView textViewHumidity;
    private TextView textViewMealsLeft;
    private TextView textViewDaysLeft;
    private TextView textViewFoodSupply;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, @Nullable Bundle savedInstanceState) {
        // ViewModels
        foodInfoViewModel = new ViewModelProvider(requireActivity()).get(FoodInfoViewModel.class);
        scheduleViewModel = new ViewModelProvider(requireActivity()).get(ScheduleViewModel.class);
        deviceViewModel = new ViewModelProvider(requireActivity()).get(DeviceViewModel.class);

        this.fragmentChangeListener = (HomeActivity)inflater.getContext();

        String deviceId = deviceViewModel.getCurrentDeviceId();

        foodInfoViewModel.listenToDeviceUpdates(deviceId);
        return inflater.inflate(R.layout.food_info_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //Textviews
        textViewFoodSupply = view.findViewById(R.id.foodSupplyPercentage);
        textViewTemperature = view.findViewById(R.id.textViewTemperature);
        textViewHumidity = view.findViewById(R.id.textViewHumidity);
        textViewMealsLeft = view.findViewById(R.id.textViewMealsLeft);
        textViewDaysLeft = view.findViewById(R.id.textViewDaysLeft);

        foodInfoViewModel.getFoodSupply().observe(getViewLifecycleOwner(), foodSupply -> {
            // Update the UI with food supply information
            updateFoodSupplyUI(view, foodSupply);

            recalculateMealsAndDays();

            if (mealScheduleList != null) {
                recalculateMealsAndDays();
            }
        });

        scheduleViewModel.getMealSchedulesForDevice(deviceViewModel.getCurrentDeviceId())
                .observe(getViewLifecycleOwner(), mealSchedules -> {
                    mealScheduleList = mealSchedules;
                    recalculateMealsAndDays();
                });

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> {
            if (fragmentChangeListener != null) {
                fragmentChangeListener.replaceFragment(new HomeScreenFragment());
            }
        });
        foodInfoViewModel.getTemperature().observe(getViewLifecycleOwner(), temperature -> {
            textViewTemperature.setText(temperature + " " + "°C");
        });

        foodInfoViewModel.getHumidity().observe(getViewLifecycleOwner(), humidity -> {
            textViewHumidity.setText( humidity + " " + "%");
        });



    }
    // Recalculate meals and days
    private void recalculateMealsAndDays() {
        if (foodInfoViewModel.getFoodSupply().getValue() != null && mealScheduleList != null) {
            foodInfoViewModel.calculateMealsAndDaysLeft(mealScheduleList);

            foodInfoViewModel.getmealsLeft().observe(getViewLifecycleOwner(), mealsLeft ->
                    textViewMealsLeft.setText("" + mealsLeft));

            foodInfoViewModel.getdaysLeft().observe(getViewLifecycleOwner(), daysLeft ->
                    textViewDaysLeft.setText("" + daysLeft));
        }
    }
    // Update the UI with food supply information
    private void updateFoodSupplyUI(View view, Integer foodSupply) {
        TextView textViewFoodSupply = view.findViewById(R.id.foodSupplyPercentage);
        ProgressBar foodStorageProgress = view.findViewById(R.id.foodStorageProgress);

        textViewFoodSupply.setText("Food Supply: " + foodSupply + "%");
        foodStorageProgress.setProgress(foodSupply);
    }
}