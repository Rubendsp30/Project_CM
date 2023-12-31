package com.example.project_cm.Adapters;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import com.example.project_cm.FragmentChangeListener;
import com.example.project_cm.Fragments.ScheduleFragment;
import com.example.project_cm.ViewModels.ScheduleViewModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class HomeAdapter extends RecyclerView.Adapter<HomeAdapter.HomeViewHolder> {

    @Nullable
    private com.example.project_cm.FragmentChangeListener FragmentChangeListener;
    @Nullable
    private final FragmentManager fragmentManager;

    private ArrayList<Device> viewPagerIDeviceArrayList;
    private final DeviceViewModel deviceViewModel;
    private final PetProfileViewModel petProfileViewModel;
    private final LifecycleOwner lifecycleOwner;
    private final ScheduleViewModel scheduleViewModel;


    public HomeAdapter(String userId, ArrayList<Device> viewPagerIDeviceArrayList, @Nullable FragmentManager fragmentManager, DeviceViewModel deviceViewModel, PetProfileViewModel petProfileViewModel, LifecycleOwner lifecycleOwner, ScheduleViewModel scheduleViewModel) {
        this.viewPagerIDeviceArrayList = viewPagerIDeviceArrayList;
        this.fragmentManager = fragmentManager;
        this.deviceViewModel = deviceViewModel;
        this.petProfileViewModel = petProfileViewModel;
        this.lifecycleOwner = lifecycleOwner;
        LiveData<ArrayList<Device>> devicesLiveData = deviceViewModel.listenForDeviceUpdates(userId);
        devicesLiveData.observe(lifecycleOwner, this::updateDevices);
        this.scheduleViewModel = scheduleViewModel;
    }

    private void updateDevices(ArrayList<Device> devices) {
        this.viewPagerIDeviceArrayList = devices;
        notifyDataSetChanged();
    }

    public void setFragmentChangeListener(@Nullable FragmentChangeListener fragmentChangeListener) {
        this.FragmentChangeListener = fragmentChangeListener;
    }


    @Override
    public void onBindViewHolder(@NonNull HomeAdapter.HomeViewHolder holder, int position) {

        Device viewPagerItem = viewPagerIDeviceArrayList.get(position);
        long petProfileId = viewPagerItem.getPet_id();
        petProfileViewModel.getPetProfileById(petProfileId).observe(lifecycleOwner, petProfileEntity -> {
            if (petProfileEntity != null) {
                holder.petNameText.setText(petProfileEntity.name);
            }
        });

        List<MealSchedule> meals = new ArrayList<>();

        MealScheduleAdapter mealScheduleAdapter = new MealScheduleAdapter(fragmentManager,meals,viewPagerItem.getDeviceID());
        holder.schedulesRecycler.setLayoutManager(new LinearLayoutManager(holder.itemView.getContext()));
        holder.schedulesRecycler.setAdapter(mealScheduleAdapter);
        scheduleViewModel.getMealSchedulesForDevice(viewPagerItem.getDeviceID()).observe(lifecycleOwner, mealSchedules -> {
            meals.clear();
            meals.addAll(mealSchedules);
            mealScheduleAdapter.notifyDataSetChanged();
            updateNextMealHourText( meals,holder.nextMealHourText);
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
        holder.addScheduleButton.setOnClickListener(v -> {
            Device selectedDevice = viewPagerIDeviceArrayList.get(position);
            deviceViewModel.setCurrentDevice(selectedDevice);
            if (FragmentChangeListener != null) {
                ScheduleFragment scheduleFragment = new ScheduleFragment();
                //scheduleFragment.setDeviceId(selectedDevice.getDeviceID());
                scheduleFragment.setFragmentChangeListener(FragmentChangeListener);
                FragmentChangeListener.replaceFragment(scheduleFragment);
            }
        });


    }

    private void updateNextMealHourText(List<MealSchedule> mealScheduleList, TextView nextMealHourText) {
        Calendar current = Calendar.getInstance();
        int currentDayOfWeek = current.get(Calendar.DAY_OF_WEEK);
        long currentTimeInMillis = current.getTimeInMillis();

        MealSchedule nextMeal = null;
        long minTimeDiff = Long.MAX_VALUE;

        for (MealSchedule meal : mealScheduleList) {
            // Check if the meal is active
            if (!meal.isActive()) {
                continue; // Skip this meal if it's not active
            }

            Calendar mealTimeCal = Calendar.getInstance();
            mealTimeCal.setTime(meal.getMealTime());
            int mealHour = mealTimeCal.get(Calendar.HOUR_OF_DAY);
            int mealMinute = mealTimeCal.get(Calendar.MINUTE);

            if (!meal.getRepeatDays().containsValue(true)) {
                // Non-repeating meal
                if (mealTimeCal.before(current)) {
                    mealTimeCal.add(Calendar.DAY_OF_YEAR, 1); // Consider it for the next day
                }
                long diff = mealTimeCal.getTimeInMillis() - currentTimeInMillis;
                if (diff < minTimeDiff) {
                    minTimeDiff = diff;
                    nextMeal = meal;
                }
            } else {
                // Repeating meal
                for (String day : meal.getRepeatDays().keySet()) {
                    if (meal.getRepeatDays().get(day)) {
                        int dayIndex = getDayIndex(day);
                        Calendar nextMealTime = (Calendar) current.clone();
                        nextMealTime.set(Calendar.HOUR_OF_DAY, mealHour);
                        nextMealTime.set(Calendar.MINUTE, mealMinute);
                        nextMealTime.set(Calendar.SECOND, 0);
                        nextMealTime.set(Calendar.MILLISECOND, 0);

                        int daysUntilNext = (dayIndex - currentDayOfWeek + 7) % 7;
                        if (daysUntilNext == 0 && nextMealTime.before(current)) {
                            daysUntilNext = 7; // Next week
                        }
                        nextMealTime.add(Calendar.DAY_OF_YEAR, daysUntilNext);

                        long diff = nextMealTime.getTimeInMillis() - currentTimeInMillis;
                        if (diff < minTimeDiff) {
                            minTimeDiff = diff;
                            nextMeal = meal;
                        }
                    }
                }
            }
        }

        if (nextMeal != null) {
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
            String formattedTime = timeFormat.format(nextMeal.getMealTime());
            nextMealHourText.setText(formattedTime);
        } else {
            nextMealHourText.setText("No upcoming meals");
        }
    }


    private int getDayIndex(String day) {
        switch (day) {
            case "Sunday": return Calendar.SUNDAY;
            case "Monday": return Calendar.MONDAY;
            case "Tuesday": return Calendar.TUESDAY;
            case "Wednesday": return Calendar.WEDNESDAY;
            case "Thursday": return Calendar.THURSDAY;
            case "Friday": return Calendar.FRIDAY;
            case "Saturday": return Calendar.SATURDAY;
            default: return -1;
        }
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
        Button addScheduleButton;
        TextView petNameText;
        ImageButton treatButton;
        RecyclerView schedulesRecycler;
        TextView supplyText;
        TextView nextMealHourText;
        ProgressBar supplyProgressBar;

        public HomeViewHolder(@NonNull View homeView) {
            super(homeView);

            addScheduleButton = itemView.findViewById(R.id.addScheduleButton);
            petNameText = itemView.findViewById(R.id.petNameText);
            treatButton = itemView.findViewById(R.id.treatButton);
            schedulesRecycler = itemView.findViewById(R.id.schedulesRecycler);
            supplyText = homeView.findViewById(R.id.supplyText);
            supplyProgressBar = itemView.findViewById(R.id.supplyProgressBar);
            nextMealHourText = itemView.findViewById(R.id.nextMealHourText);


        }
    }

}