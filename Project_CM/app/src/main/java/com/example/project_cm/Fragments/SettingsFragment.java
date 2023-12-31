package com.example.project_cm.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.example.project_cm.Activities.HomeActivity;
import com.example.project_cm.DataBase.Tables.PetProfileEntity;
import com.example.project_cm.Device;
import com.example.project_cm.R;
import com.example.project_cm.ViewModels.DeviceViewModel;
import com.example.project_cm.ViewModels.PetProfileViewModel;
//import com.example.project_cm.ViewModels.ViewModelFactory;
import com.example.project_cm.FragmentChangeListener;
import com.example.project_cm.DataBase.PetProfileDao;
import com.example.project_cm.DataBase.AppDatabase;
import com.example.project_cm.ViewModels.UserViewModel;
import com.example.project_cm.User;
import android.util.Log;

import java.util.ArrayList;
import java.util.Locale;


public class SettingsFragment extends Fragment {

    private boolean isActiveNotifications;
    private boolean isActiveAnimatedNotifications;
    private SeekBar seekBarVolume;
    private Button buttonDateAndTime;
    private Button buttonLanguage;
    @Nullable
    private com.example.project_cm.FragmentChangeListener FragmentChangeListener;
    private UserViewModel userViewModel;
    private DeviceViewModel deviceViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.settings_fragment, container, false);

        this.FragmentChangeListener = (HomeActivity) inflater.getContext();

        // Initialize ViewModel instances
        //todo pq é q estás a chamar se nem vais usar nenhum
        try {
            userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);
        } catch (Exception e) {
            Log.e("PetProfileFragment", "Error creating UserViewModel: " + e.getMessage());
        }
        try {
            deviceViewModel = new ViewModelProvider(requireActivity()).get(DeviceViewModel.class);
        } catch (Exception e) {
            Log.e("PetProfileFragment", "Error creating DeviceViewModel: " + e.getMessage());
        }

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SwitchMaterial switchNotifications = view.findViewById(R.id.switchNotifications);
        SwitchMaterial switchAnimatedNotifications = view.findViewById(R.id.switchAnimatedNotifications);
        isActiveNotifications = switchNotifications.isChecked();
        isActiveAnimatedNotifications = switchAnimatedNotifications.isChecked();

        seekBarVolume = view.findViewById(R.id.seekBarVolume);
        buttonDateAndTime = view.findViewById(R.id.buttonDateAndTime);
        buttonLanguage = view.findViewById(R.id.buttonLanguage);

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setTitle(" ");
        ((AppCompatActivity) requireActivity()).setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> {
            if (FragmentChangeListener != null) {
                FragmentChangeListener.replaceFragment(new MenuFragment());
            } else {
                Log.e("RegisterFragment", "FragmentChangeListener is null. Unable to replace the fragment.");
            }
        });

        switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Toast.makeText(getContext(), "Notifications: " + isChecked, Toast.LENGTH_SHORT).show();
        });

        switchAnimatedNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Toast.makeText(getContext(), "Animated Notifications: " + isChecked, Toast.LENGTH_SHORT).show();
        });

        seekBarVolume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // Handle progress change
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Handle start of touch
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Handle end of touch
                Toast.makeText(getContext(), "Volume: " + seekBar.getProgress(), Toast.LENGTH_SHORT).show();
            }
        });

        buttonDateAndTime.setOnClickListener(v -> {
            // todo - aparece popup
        });

        buttonLanguage.setOnClickListener(v -> {
            // todo - aparece um popup com os idiomas
        });
    }
}




