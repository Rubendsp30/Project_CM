package com.example.project_cm.Fragments;

import android.os.Bundle;
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
import com.example.project_cm.MealSchedule;
import androidx.fragment.app.Fragment;

import com.example.project_cm.Device;
import com.example.project_cm.ViewModels.DeviceViewModel;
import com.example.project_cm.ViewModels.ScheduleViewModel;
import com.example.project_cm.FragmentChangeListener;
import androidx.lifecycle.ViewModelProvider;
import com.example.project_cm.R;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
public class ScheduleFragment extends Fragment {
    private FragmentChangeListener fragmentChangeListener;
    private String deviceId;
    private DeviceViewModel deviceViewModel;
    private NumberPicker numberPickerHour, numberPickerMinute;
    private SeekBar portionSeekBar;
    private Switch notificationSwitch;
    private Button[] dayButtons = new Button[7];
    private Button saveButton, cancelButton;
    private ScheduleViewModel mealScheduleViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.schedule_fragment, container, false);


        mealScheduleViewModel = new ViewModelProvider(this).get(ScheduleViewModel.class);
        deviceViewModel = new ViewModelProvider(requireActivity()).get(DeviceViewModel.class);

        // Initialize UI components
        initUI(view);

        // Set listeners
        setListeners();

        return view;
    }
    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }
    public void setFragmentChangeListener(FragmentChangeListener fragmentChangeListener) {
        this.fragmentChangeListener = fragmentChangeListener;
    }
    private void initUI(View view) {
        numberPickerHour = view.findViewById(R.id.numberPickerHour);
        numberPickerMinute = view.findViewById(R.id.numberPickerMinute);
        portionSeekBar = view.findViewById(R.id.portionSeekBar);
        notificationSwitch = view.findViewById(R.id.switch1);
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
                v.getBackground().setTint(v.isSelected() ? getResources().getColor(R.color.orange_100) : getResources().getColor(R.color.orange_300));
            });
        }
    }

    private void setListeners() {
        saveButton.setOnClickListener(v -> saveMealSchedule());
        cancelButton.setOnClickListener(v -> switchToHomePage());
    }
    private void switchToHomePage() {
        if (fragmentChangeListener != null) {
            fragmentChangeListener.replaceFragment(new HomeScreenFragment());
        }
    }
    private void saveMealSchedule() {
        Device currentDevice = deviceViewModel.getCurrentDevice().getValue();
        if (currentDevice == null) {
            Toast.makeText(getContext(), "Device not found", Toast.LENGTH_SHORT).show();
            return;
        }
        String deviceId = currentDevice.getDeviceID();
        if (deviceId == null || deviceId.isEmpty()) {
            Toast.makeText(getContext(), "Device ID is invalid", Toast.LENGTH_SHORT).show();
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
        MealSchedule mealSchedule = new MealSchedule(
                calendar.getTime(),
                repeatDays,
                true,
                notification,
                portionSize
        );

        // Save to Firebase
        mealScheduleViewModel.addMealSchedule(deviceId, mealSchedule);
        Toast.makeText(getContext(), "Schedule saved successfully", Toast.LENGTH_SHORT).show();
        switchToHomePage();
    }

    private String getDayString(int dayIndex) {
        switch (dayIndex) {
            case 0: return "Monday";
            case 1: return "Tuesday";
            case 2: return "Wednesday";
            case 3: return "Thursday";
            case 4: return "Friday";
            case 5: return "Saturday";
            case 6: return "Sunday";
            default: return "";
        }
    }
}