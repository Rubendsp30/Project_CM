package com.example.project_cm.Fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.project_cm.MQTTHelper;
import com.example.project_cm.MealSchedule;

import androidx.fragment.app.Fragment;
import com.example.project_cm.ViewModels.DeviceViewModel;
import com.example.project_cm.ViewModels.ScheduleViewModel;
import com.example.project_cm.FragmentChangeListener;

import androidx.lifecycle.ViewModelProvider;

import com.example.project_cm.R;
import com.example.project_cm.utils.ClientNameUtil;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class ScheduleFragment extends Fragment {
    private FragmentChangeListener fragmentChangeListener;
    //private String deviceId;
    private DeviceViewModel deviceViewModel;
    private NumberPicker numberPickerHour, numberPickerMinute;
    private SeekBar portionSeekBar;
    private Switch notificationSwitch;
    private final Button[] dayButtons = new Button[7];
    private Button saveButton, cancelButton;
    private ScheduleViewModel mealScheduleViewModel;
    private MealSchedule existingMealSchedule;
    private MQTTHelper mqttHelper;

    public void setMealSchedule(MealSchedule mealSchedule) {
        this.existingMealSchedule = mealSchedule;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.schedule_fragment, container, false);


        mealScheduleViewModel = new ViewModelProvider(this).get(ScheduleViewModel.class);
        deviceViewModel = new ViewModelProvider(requireActivity()).get(DeviceViewModel.class);
        String clientName = ClientNameUtil.getClientName();
        mqttHelper = MQTTHelper.getInstance(getContext(), clientName);


        return view;
    }

    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initUI(view);
        setListeners();

        if (existingMealSchedule != null) {
            displayMealSchedule(existingMealSchedule);
        }
    }

    private void setListeners() {
        saveButton.setOnClickListener(v -> saveMealSchedule());
        cancelButton.setOnClickListener(v -> switchToHomePage());
    }

    public void setFragmentChangeListener(FragmentChangeListener fragmentChangeListener) {
        this.fragmentChangeListener = fragmentChangeListener;
    }

    private void initUI(View view) {
        numberPickerHour = view.findViewById(R.id.numberPickerHour);
        numberPickerMinute = view.findViewById(R.id.numberPickerMinute);
        portionSeekBar = view.findViewById(R.id.portionSeekBar);
        saveButton = view.findViewById(R.id.saveButton);
        cancelButton = view.findViewById(R.id.cancelButton);

        // Initialize number pickers
        numberPickerHour.setMinValue(0);
        numberPickerHour.setMaxValue(23);
        numberPickerMinute.setMinValue(0);
        numberPickerMinute.setMaxValue(59);

        // Initialize day buttons
        initDayButtons(view);
    }

    private void displayMealSchedule(MealSchedule mealSchedule) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(mealSchedule.getMealTime());

        numberPickerHour.setValue(calendar.get(Calendar.HOUR_OF_DAY));
        numberPickerMinute.setValue(calendar.get(Calendar.MINUTE));
        portionSeekBar.setProgress(mealSchedule.getPortionSize());
        notificationSwitch.setChecked(mealSchedule.isNotification());

        // Set day buttons based on repeatDays
        for (int i = 0; i < dayButtons.length; i++) {
            String day = getDayString(i);
            if (mealSchedule.getRepeatDays().containsKey(day)) {
                boolean isSelected = mealSchedule.getRepeatDays().get(day);
                dayButtons[i].setSelected(isSelected);
                dayButtons[i].getBackground().setTint(isSelected ? getResources().getColor(R.color.orange_100) : getResources().getColor(R.color.orange_300));
            }
        }
    }

    private void initDayButtons(View view) {
        dayButtons[0] = view.findViewById(R.id.buttonMonday);
        dayButtons[1] = view.findViewById(R.id.buttonTuesday);
        dayButtons[2] = view.findViewById(R.id.buttonWednesday);
        dayButtons[3] = view.findViewById(R.id.buttonThursday);
        dayButtons[4] = view.findViewById(R.id.buttonFriday);
        dayButtons[5] = view.findViewById(R.id.buttonSaturday);
        dayButtons[6] = view.findViewById(R.id.buttonSunday);

        for (Button dayButton : dayButtons) {
            dayButton.setOnClickListener(v -> {
                v.setSelected(!v.isSelected());
                v.getBackground().setTint(v.isSelected() ? getResources().getColor(R.color.orange_300) : getResources().getColor(R.color.orange_100));
            });
            dayButton.getBackground().setTint(getResources().getColor(R.color.orange_300, null));
        }
    }


    private void switchToHomePage() {
        if (fragmentChangeListener != null) {
            fragmentChangeListener.replaceFragment(new HomeScreenFragment());
        } else {
            Log.e("fragment", "fragment null");
        }
    }

    private void saveMealSchedule() {
        String deviceId = deviceViewModel.getCurrentDeviceId();
        if (deviceId == null || deviceId.isEmpty()) {
            Toast.makeText(getContext(), "Invalid or missing device", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get data from UI
        int hour = numberPickerHour.getValue();
        int minute = numberPickerMinute.getValue();
        int portionSize = portionSeekBar.getProgress();
        boolean notification = notificationSwitch.isChecked();


        // Prepare repeat days
        Map<String, Boolean> repeatDays = new HashMap<>();
        for (int i = 0; i < dayButtons.length; i++) {
            repeatDays.put(getDayString(i), dayButtons[i].isSelected());
        }

        // Create MealSchedule object
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        MealSchedule mealSchedule;
        if (existingMealSchedule == null) {
            mealSchedule = new MealSchedule(calendar.getTime(), repeatDays, true, notification, portionSize);
        } else {
            existingMealSchedule.setMealTime(calendar.getTime());
            existingMealSchedule.setRepeatDays(repeatDays);
            existingMealSchedule.setNotification(notification);
            existingMealSchedule.setPortionSize(portionSize);
            mealSchedule = existingMealSchedule;
        }


        // Saving or updating to Firebase
        if (existingMealSchedule == null) {
            // Add new schedule
            mealScheduleViewModel.addMealSchedule(deviceId, mealSchedule, new ScheduleViewModel.MealScheduleCallback() {
                @Override
                public void onSuccess() {
                    String topic = "/project/updateMeals/" + deviceViewModel.getCurrentDeviceId();
                    mqttHelper.publishToTopic(topic, "update", 2);
                    switchToHomePage();
                }

                @Override
                public void onFailure() {
                }
            });
        } else {
            // Update existing schedule
            mealScheduleViewModel.updateMealSchedule(deviceId, mealSchedule, new ScheduleViewModel.MealScheduleCallback() {
                @Override
                public void onSuccess() {
                    String topic = "/project/updateMeals/" + deviceViewModel.getCurrentDeviceId();
                    mqttHelper.publishToTopic(topic, "update", 2);
                    switchToHomePage();
                }

                @Override
                public void onFailure() {
                }
            });
        }

    }

    private String getDayString(int dayIndex) {
        switch (dayIndex) {
            case 0:
                return "Monday";
            case 1:
                return "Tuesday";
            case 2:
                return "Wednesday";
            case 3:
                return "Thursday";
            case 4:
                return "Friday";
            case 5:
                return "Saturday";
            case 6:
                return "Sunday";
            default:
                return "";
        }
    }
}