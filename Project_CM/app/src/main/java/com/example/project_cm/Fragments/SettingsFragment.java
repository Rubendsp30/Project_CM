package com.example.project_cm.Fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.example.project_cm.Activities.HomeActivity;
import com.example.project_cm.R;

import android.util.Log;

import java.util.Locale;

public class SettingsFragment extends Fragment {

    private boolean isActiveNotifications;
    private boolean isActiveAnimatedNotifications;
    private SeekBar seekBarVolume;
    private Button buttonDateAndTime;
    private Button buttonLanguage;
    @Nullable
    private com.example.project_cm.FragmentChangeListener FragmentChangeListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.settings_fragment, container, false);

        this.FragmentChangeListener = (HomeActivity) inflater.getContext();

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
            showLanguageSelectionPopup();
        });
    }
    private void showLanguageSelectionPopup() {
        final String[] languages = {"pt", "en"};
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.CustomDialog);
        builder.setTitle("Escolha o Idioma");
        builder.setItems(languages, (dialog, which) -> {
            String selectedLanguage = languages[which];
            Toast.makeText(getContext(), "Idioma selecionado: " + selectedLanguage, Toast.LENGTH_SHORT).show();

            Locale local = new Locale(selectedLanguage);
            Resources resources = getResources();
            DisplayMetrics dm = resources.getDisplayMetrics();
            Configuration config = resources.getConfiguration();
            config.locale = local;
            resources.updateConfiguration(config, dm);

            SharedPreferences sharedPreferences = getContext().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("language", selectedLanguage);
            editor.apply();

            // faz reload da página
            getActivity().recreate();
        });
        builder.show();
    }
}




