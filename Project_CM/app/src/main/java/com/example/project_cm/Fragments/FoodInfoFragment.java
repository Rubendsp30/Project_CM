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


import com.example.project_cm.Activities.HomeActivity;
import com.example.project_cm.ViewModels.DeviceViewModel;
import com.example.project_cm.R;
import com.example.project_cm.ViewModels.FoodInfoViewModel;

public class FoodInfoFragment extends Fragment {
    @Nullable
    private com.example.project_cm.FragmentChangeListener fragmentChangeListener;
    private FoodInfoViewModel foodInfoViewModel;
    private TextView textViewTemperature;
    private TextView textViewHumidity;
    private TextView textViewMealsLeft;
    private TextView textViewDaysLeft;
    private TextView textViewFoodSupply;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, @Nullable Bundle savedInstanceState) {
        foodInfoViewModel = new ViewModelProvider(requireActivity()).get(FoodInfoViewModel.class);
        this.fragmentChangeListener = (HomeActivity)inflater.getContext();
        DeviceViewModel deviceViewModel = new ViewModelProvider(requireActivity()).get(DeviceViewModel.class);
        foodInfoViewModel.setDeviceId(deviceViewModel.getCurrentDeviceId());
        return inflater.inflate(R.layout.food_info_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        textViewFoodSupply = view.findViewById(R.id.foodSupplyPercentage);
        textViewTemperature = view.findViewById(R.id.textViewTemperature);
        textViewHumidity = view.findViewById(R.id.textViewHumidity);
        textViewMealsLeft = view.findViewById(R.id.textViewMealsLeft);
        textViewDaysLeft = view.findViewById(R.id.textViewDaysLeft);

        foodInfoViewModel.getFoodSupply().observe(getViewLifecycleOwner(), foodSupply -> {
            TextView textViewFoodSupply = view.findViewById(R.id.foodSupplyPercentage);
            ProgressBar foodStorageProgress = view.findViewById(R.id.foodStorageProgress);

            textViewFoodSupply.setText("Food Supply: " + foodSupply + "%");
            foodStorageProgress.setProgress(foodSupply);
        });

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> {
            if (fragmentChangeListener != null) {
                fragmentChangeListener.replaceFragment(new MenuFragment());
            }
        });

        foodInfoViewModel.getTemperature().observe(getViewLifecycleOwner(), temperature -> {
            textViewTemperature.setText(temperature + "°C");
        });

        foodInfoViewModel.getHumidity().observe(getViewLifecycleOwner(), humidity -> {
            textViewHumidity.setText( humidity + "%");
        });

        //TODO função para mealsleft
      /* foodInfoViewModel.getMealsLeft().observe(getViewLifecycleOwner(), mealsLeft ->
                textViewMealsLeft.setText("Meals Left: " + mealsLeft));

        //TODO função para daysleft
        foodInfoViewModel.getDaysLeft().observe(getViewLifecycleOwner(), daysLeft ->
                textViewDaysLeft.setText("Days Left: " + daysLeft));*/

    }
}