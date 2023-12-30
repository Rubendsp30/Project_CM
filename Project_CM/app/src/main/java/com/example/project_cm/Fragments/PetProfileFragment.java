package com.example.project_cm.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.appcompat.widget.Toolbar;

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


public class PetProfileFragment extends Fragment {

    @Nullable
    private com.example.project_cm.FragmentChangeListener FragmentChangeListener;
    private UserViewModel userViewModel;
    private PetProfileEntity currentPetProfile;
    private PetProfileViewModel petProfileViewModel;
    private DeviceViewModel deviceViewModel;
    private TextView petNameTextView, petAgeTextView, petWeightTextView, petSexTextView, petMicrochipTextView;
    private ImageView petProfileImageView;
    ArrayList<Device> viewPagerItemDeviceList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.pet_profile_fragment, container, false);

        this.FragmentChangeListener = (HomeActivity) inflater.getContext();

        // Initialize ViewModel instances
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
        try {
            petProfileViewModel = new ViewModelProvider(requireActivity()).get(PetProfileViewModel.class);
        } catch (Exception e) {
            Log.e("PetProfileFragment", "Error creating PetProfileViewModel: " + e.getMessage());
        }

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        petProfileImageView = view.findViewById(R.id.petProfileImageView);
        petNameTextView = view.findViewById(R.id.petNameTextView);
        petAgeTextView = view.findViewById(R.id.petAgeTextView);
        petWeightTextView = view.findViewById(R.id.petWeightTextView);
        petSexTextView = view.findViewById(R.id.petSexTextView);
        petMicrochipTextView = view.findViewById(R.id.petMicrochipTextView);

        loadPetProfile();

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setTitle(" ");
        ((AppCompatActivity) requireActivity()).setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> {
            if (FragmentChangeListener != null) {
                FragmentChangeListener.replaceFragment(new MenuFragment());
            } else {
                // Handle the case where FragmentChangeListener is null
                Log.e("RegisterFragment", "FragmentChangeListener is null. Unable to replace the fragment.");
            }
        });

        /*
        if (getActivity() instanceof com.example.project_cm.FragmentChangeListener) {
            FragmentChangeListener = (com.example.project_cm.FragmentChangeListener) getActivity();
        }
         */


        // Botão Vacinas
        Button vaccinesButton = view.findViewById(R.id.petVaccinesTextView);
        vaccinesButton.setOnClickListener(v -> {
            if (FragmentChangeListener != null) {
                FragmentChangeListener.replaceFragment(new VaccinesFragment());
            } else {
                // Handle the case where FragmentChangeListener is null
                Log.e("RegisterFragment", "FragmentChangeListener is null. Unable to replace the fragment.");
            }
        });

        /*
        // Botão Histórico
        Button historyButton = view.findViewById(R.id.petHistoryTextView);
        historyButton.setOnClickListener(v -> {
            if (FragmentChangeListener != null) {
                FragmentChangeListener.replaceFragment(new HistoryFragment());
            } else {
                // Handle the case where FragmentChangeListener is null
                Log.e("RegisterFragment", "FragmentChangeListener is null. Unable to replace the fragment.");
            }
        });

         */
    }

    private void loadPetProfile() {
        User currentUser = userViewModel.getCurrentUser().getValue();
        String userId = currentUser != null ? currentUser.getUserID() : "-1";

        petProfileViewModel.getPetProfilesByUserId(userId).observe(getViewLifecycleOwner(), petProfiles -> {
            if (petProfiles != null && !petProfiles.isEmpty()) {
                currentPetProfile = petProfiles.get(0);
                petProfileViewModel.setCurrentPet(currentPetProfile);
                updateUI(currentPetProfile);
            }
        });
    }

    private void updateUI(PetProfileEntity petProfile) {
        petNameTextView.setText(petProfile.name);
        petAgeTextView.setText(String.format(Locale.getDefault(), "%d years", petProfile.age));
        petWeightTextView.setText(String.format(Locale.getDefault(), "%.1f kg", petProfile.weight));
        petSexTextView.setText(petProfile.gender == 0 ? "Male" : "Female");
        petMicrochipTextView.setText(petProfile.microchipNumber);
    }
}

