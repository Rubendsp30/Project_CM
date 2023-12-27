package com.example.project_cm.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.project_cm.DataBase.Tables.PetProfileEntity;
import com.example.project_cm.R;
import com.example.project_cm.ViewModels.PetProfileViewModel;
import com.example.project_cm.FragmentChangeListener;
import com.example.project_cm.ViewModels.UserViewModel;
import com.example.project_cm.User;

public class PetProfileCreationFragment extends Fragment {
    // ViewModel instances for user and pet profile data
    private UserViewModel userViewModel;
    private PetProfileViewModel petProfileViewModel;

    // UI stuff
    private EditText inputName, inputAge, inputWeight, inputSex, inputMicrochip;
    private Button saveButton;
    private ImageView profileImage;

    // Flags to determine if we are editing an existing pet profile
    private boolean isEditMode = false;
    private String petProfileId = null;
    private PetProfileEntity currentPetProfile;

    // Initialize fragment and check if it's opened in edit mode
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Check if arguments are passed to the fragment
        if (getArguments() != null) {
            petProfileId = getArguments().getString("petProfileId");
            // Determine if the fragment is in edit mode based on petProfileId
            isEditMode = petProfileId != null && !petProfileId.isEmpty();
            Log.d("PetProfileFragment", "onCreate: isEditMode = " + isEditMode + ", petProfileId = " + petProfileId);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.pet_profile_creation_fragment, container, false);

        // Initialize ViewModel instances
        userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);
        petProfileViewModel = new ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().getApplication())).get(PetProfileViewModel.class);

        // UI shit
        inputName = view.findViewById(R.id.inputName);
        inputAge = view.findViewById(R.id.inputAge);
        inputWeight = view.findViewById(R.id.inputWeight);
        inputSex = view.findViewById(R.id.inputSex);
        inputMicrochip = view.findViewById(R.id.inputMicrochip);
        saveButton = view.findViewById(R.id.saveButton);
        profileImage = view.findViewById(R.id.profile_image);

        saveButton.setOnClickListener(v -> createPetProfile());
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Retrieve the current user
        User currentUser = userViewModel.getCurrentUser().getValue();
        if (currentUser != null) {
            // If in edit mode and a pet profile ID is provided
            if (isEditMode && petProfileId != null) {
                // Load the pet profile for editing
                observePetProfile();
            } else {
                // If not in edit mode, load the first pet profile of the user if it exists
                petProfileViewModel.getPetProfilesByUserId(currentUser.getUserID()).observe(getViewLifecycleOwner(), petProfiles -> {
                    if (!petProfiles.isEmpty()) {
                        currentPetProfile = petProfiles.get(0);
                        fillInProfileDetails(currentPetProfile);
                        isEditMode = true;
                    }
                });
                    }
            }
     }


    // Method to load pet profile details when in edit mode
        private void observePetProfile() {
        if (isEditMode && petProfileId != null) {
            petProfileViewModel.getPetProfileById(petProfileId).observe(getViewLifecycleOwner(), this::fillInProfileDetails);
            }
        }




    //Fill com as coisas da UI e as informações gravadas anteriormente
    private void fillInProfileDetails(PetProfileEntity petProfile) {
        if (petProfile != null) {
            inputName.setText(petProfile.name != null ? petProfile.name : "");
            inputAge.setText(String.valueOf(petProfile.age));
            inputWeight.setText(String.valueOf(petProfile.weight));
            //female or male
            String genderText = convertSexToGender(petProfile.gender == 0 ? "male" : "female") == 0 ? "male" : "female";
            inputSex.setText(genderText);
            inputMicrochip.setText(petProfile.microchipNumber != null ? petProfile.microchipNumber : "");
        } else {
            Toast.makeText(getContext(), "Pet profile not found", Toast.LENGTH_SHORT).show();
        }
    }

    // Logic for creating a new pet profile or updating an existing one
    private void createPetProfile() {
        String name = inputName.getText().toString();
        String ageInput = inputAge.getText().toString();
        String weightInput = inputWeight.getText().toString();
        String sex = inputSex.getText().toString();
        String microchip = inputMicrochip.getText().toString();

        if (name.isEmpty() || ageInput.isEmpty() || weightInput.isEmpty() || sex.isEmpty() || microchip.isEmpty()) {
            Toast.makeText(getContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            int age = Integer.parseInt(ageInput);
            float weight = Float.parseFloat(weightInput);

            User currentUser = userViewModel.getCurrentUser().getValue();
            if (currentUser == null) {
                Toast.makeText(getContext(), "User must be logged in to create or edit a pet profile", Toast.LENGTH_SHORT).show();
                return;
            }

            PetProfileEntity petProfile = isEditMode ? currentPetProfile : new PetProfileEntity();
            petProfile.name = name;
            petProfile.age = age;
            petProfile.weight = weight;
            petProfile.gender = convertSexToGender(sex);
            petProfile.microchipNumber = microchip;

            if (isEditMode) {
                Log.d("PetProfileFragment", "Updating pet profile: " + petProfile.name);
                petProfileViewModel.updatePetProfile(petProfile);
                Toast.makeText(getContext(), "Pet Profile Updated", Toast.LENGTH_SHORT).show();
            } else {
                Log.d("PetProfileFragment", "Creating new pet profile: " + petProfile.name);
                petProfile.userID = currentUser.getUserID();
                petProfileViewModel.insertPetProfile(petProfile);
                Toast.makeText(getContext(), "Pet Profile Created", Toast.LENGTH_SHORT).show();
            }

            onPetInfoSaved();

        } catch (NumberFormatException e) {
            Log.e("PetProfileFragment", "Invalid input", e);
            Toast.makeText(getContext(), "Invalid input", Toast.LENGTH_SHORT).show();
        }
    }

    // Handle actions after saving pet profile information
    private void onPetInfoSaved() {
        if (getActivity() instanceof FragmentChangeListener) {
            ((FragmentChangeListener) getActivity()).replaceFragment(new HomeScreenFragment());
        } else {
            Toast.makeText(getContext(), "Activity must implement FragmentChangeListener", Toast.LENGTH_SHORT).show();
        }
    }

    // Convert the input sex string to a gender value
    private int convertSexToGender(String sex) {
        return sex.equalsIgnoreCase("male") ? 0 : 1;
    }
}
