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

import com.example.project_cm.Activities.HomeActivity;
import com.example.project_cm.DataBase.Tables.PetProfileEntity;
import com.example.project_cm.Device;
import com.example.project_cm.Fragments.DeviceSetup.DevSetupFinal;
import com.example.project_cm.MQTTHelper;
import com.example.project_cm.R;
import com.example.project_cm.ViewModels.DeviceViewModel;
import com.example.project_cm.ViewModels.PetProfileViewModel;
import com.example.project_cm.ViewModels.UserViewModel;
import com.example.project_cm.User;
import com.example.project_cm.utils.ClientNameUtil;

public class PetProfileCreationFragment extends Fragment {
    // ViewModel instances for managing user and pet profile data
    private UserViewModel userViewModel;
    private PetProfileViewModel petProfileViewModel;
    private DeviceViewModel deviceViewModel;
    private MQTTHelper mqttHelper;

    // Nullable as it may not be initialized if the fragment is not attached to an activity
    @Nullable
    private com.example.project_cm.FragmentChangeListener FragmentChangeListener;

    // UI stuff
    private EditText inputName, inputAge, inputWeight, inputGender, inputMicrochip;
    private ImageView profileImage;
    private User currentUser;

    // Flags to determine if the fragment is in edit mode or creating a new pet profile
    private boolean isEditMode = false;
    private long petProfileId = -1;
    private PetProfileEntity currentPetProfile;

    // Initialize fragment and check if it's opened in edit mode
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Check for any arguments passed to the fragment
        if (getArguments() != null) {
            petProfileId = getArguments().getLong("petProfileId");
            isEditMode = petProfileId != -1;
            Log.d("PetProfileFragment", "onCreate: isEditMode = " + isEditMode + ", petProfileId = " + petProfileId);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        this.FragmentChangeListener = (HomeActivity) inflater.getContext();

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.pet_profile_creation_fragment, container, false);

        // Initialize ViewModel instances
        userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);
        deviceViewModel = new ViewModelProvider(requireActivity()).get(DeviceViewModel.class);
        petProfileViewModel = new ViewModelProvider(requireActivity()).get(PetProfileViewModel.class);
        String clientName = ClientNameUtil.getClientName();
        mqttHelper = MQTTHelper.getInstance(requireContext(), clientName);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Retrieve the current user
        currentUser = userViewModel.getCurrentUser().getValue();

        // UI things
        inputName = view.findViewById(R.id.inputName);
        inputAge = view.findViewById(R.id.inputAge);
        inputWeight = view.findViewById(R.id.inputWeight);
        inputGender = view.findViewById(R.id.inputGender);
        inputMicrochip = view.findViewById(R.id.inputMicrochip);
        Button saveButton = view.findViewById(R.id.saveButton);
        profileImage = view.findViewById(R.id.profile_image);

        // Determine if editing an existing profile or creating a new one
        if (currentUser != null) {
            if (petProfileId != -1) {
                // If we have a petProfileId, we are in edit mode. Retrieve the profile and populate the UI
                petProfileViewModel.getPetProfileById(petProfileId).observe(getViewLifecycleOwner(), petProfile -> {
                    if (petProfile != null) {
                        currentPetProfile = petProfile;
                        fillInProfileDetails(currentPetProfile);
                        isEditMode = true;
                    } else {
                        // No profile was found
                        isEditMode = false;
                    }
                });
            }
        }
        saveButton.setOnClickListener(v -> createPetProfile());
    }


    //Fill com as coisas da UI e as informações gravadas anteriormente
    private void fillInProfileDetails(PetProfileEntity petProfile) {
        if (petProfile != null) {
            if (petProfile.name != null) {
                inputName.setText(petProfile.name);
            } else {
                inputName.setText("");
            }
            inputAge.setText(String.valueOf(petProfile.age));
            inputWeight.setText(String.valueOf(petProfile.weight));
            String genderText;
            if (petProfile.gender == 0) {
                genderText = "male";
            } else {
                genderText = "female";
            }
            inputGender.setText(genderText);
            if (petProfile.microchipNumber != null) {
                inputMicrochip.setText(petProfile.microchipNumber);
            } else {
                inputMicrochip.setText("");
            }
        } else {
            Toast.makeText(getContext(), "Pet profile not found", Toast.LENGTH_SHORT).show();
        }
    }


    // Logic for creating a new pet profile or updating an existing one
    private void createPetProfile() {
        String name = inputName.getText().toString();
        String ageInput = inputAge.getText().toString();
        String weightInput = inputWeight.getText().toString();
        String gender = inputGender.getText().toString();
        String microchip = inputMicrochip.getText().toString();

        //Fill all fields warning
        if (name.isEmpty() || ageInput.isEmpty() || weightInput.isEmpty() || gender.isEmpty() || microchip.isEmpty()) {
            Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            int age = Integer.parseInt(ageInput);
            float weight = Float.parseFloat(weightInput);

            if (currentUser == null) {
                Log.e("createPetProfile", "Null user");
                return;
            }


            PetProfileEntity petProfile;
            if (isEditMode && currentPetProfile != null) {
                petProfile = currentPetProfile;
            } else {
                petProfile = new PetProfileEntity();
            }
            petProfile.name = name;
            petProfile.age = age;
            petProfile.weight = weight;
            petProfile.gender = convertToGender(gender);
            petProfile.microchipNumber = microchip;
            petProfile.userID = currentUser.getUserID();

            if (isEditMode) {

                // Atualiza o perfil existente
                petProfileViewModel.updatePetProfile(currentPetProfile);
                Toast.makeText(getContext(), "Pet Profile Updated", Toast.LENGTH_SHORT).show();
                returnToHomepage();
            } else {
                // Cria um novo perfil
                petProfileViewModel.insertPetProfile(petProfile, new PetProfileViewModel.InsertCallback() {
                    @Override
                    public void onInsertCompleted(long newPetProfileId) {
                        Device device = new Device();
                        device.setUser_id(currentUser.getUserID());
                        device.setPet_id(newPetProfileId);
                        deviceViewModel.registerDevice(device);
                        mqttHelper.subscribeToDeviceTopic(deviceViewModel.getNewDeviceId().getValue());
                        transitionToDevSetFinal();
                    }
                });
            }

        } catch (NumberFormatException e) {
            Log.e("PetProfileFragment", "Invalid input", e);
            Toast.makeText(getContext(), "Invalid input", Toast.LENGTH_SHORT).show();
        }
    }


    // Handle actions after saving pet profile information
    private void transitionToDevSetFinal() {

        if (FragmentChangeListener != null) {
            DevSetupFinal fragment = new DevSetupFinal();
            FragmentChangeListener.replaceFragment(fragment);
        } else {
            Log.e("transitionToDevSetFinal", "Activity must implement FragmentChangeListener");
        }
    }

    // Convert the input gender string to a gender value
    private int convertToGender(String gender) {
        if (gender.equalsIgnoreCase("male")) {
            return 0;
        } else {
            return 1;
        }
    }

    private void returnToHomepage() {
        if (FragmentChangeListener != null) {
            HomeScreenFragment homeFragment = new HomeScreenFragment();
            FragmentChangeListener.replaceFragment(homeFragment);
        } else {
            Log.e("returnToHomepage", "Activity must implement FragmentChangeListener");
        }
    }
}