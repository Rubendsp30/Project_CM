package com.example.project_cm.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.project_cm.DataBase.Tables.PetProfileEntity;
import com.example.project_cm.R;
import com.example.project_cm.ViewModels.PetProfileViewModel;
import com.example.project_cm.ViewModels.ViewModelFactory;
import com.example.project_cm.FragmentChangeListener;
import com.example.project_cm.DataBase.PetProfileDao;
import com.example.project_cm.DataBase.AppDatabase;
import com.example.project_cm.ViewModels.UserViewModel;
import com.example.project_cm.User;
import android.util.Log;




public class PetProfileCreationFragment extends Fragment {
    private UserViewModel userViewModel;
    private PetProfileEntity currentPetProfile;
    private PetProfileViewModel petProfileViewModel;
    private EditText inputName, inputAge, inputWeight, inputSex, inputMicrochip;
    private Button saveButton;
    private ImageView profileImage;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.pet_profile_creation_fragment, container, false);
        userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);


        inputName = view.findViewById(R.id.inputName);
        inputAge = view.findViewById(R.id.inputAge);
        inputWeight = view.findViewById(R.id.inputWeight);
        inputSex = view.findViewById(R.id.inputSex);
        inputMicrochip = view.findViewById(R.id.inputMicrochip);
        saveButton = view.findViewById(R.id.saveButton);
        profileImage = view.findViewById(R.id.profile_image);

        AppDatabase database = AppDatabase.getDBinstance(getContext());
        ViewModelFactory factory = new ViewModelFactory(database.petProfileDao());
        petProfileViewModel = new ViewModelProvider(this, factory).get(PetProfileViewModel.class);
        observePetProfile();
        return view;
    }
    private void observePetProfile() {
        User currentUser = userViewModel.getCurrentUser().getValue();
        String userId = currentUser != null ? currentUser.getUserID() : "-1";

        petProfileViewModel.getPetProfilesByUserId(userId).observe(getViewLifecycleOwner(), petProfiles -> {
            if (petProfiles != null && !petProfiles.isEmpty()) {
                currentPetProfile = petProfiles.get(0);
            }
        });
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);

        saveButton.setOnClickListener(v -> createPetProfile());
    }
//TODO ver como por imagem e as permissões para isso

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
            String userId = currentUser != null ? currentUser.getUserID() : "-1";

            petProfileViewModel.getPetProfilesByUserId(userId).observe(getViewLifecycleOwner(), petProfiles -> {
                PetProfileEntity petProfile;
                boolean isUpdate = false;

                if (petProfiles != null && !petProfiles.isEmpty()) {
                    // Update existing profile
                    petProfile = petProfiles.get(0);
                    isUpdate = true;
                } else {
                    // Create new profile
                    petProfile = new PetProfileEntity();
                }

                petProfile.name = name;
                petProfile.age = age;
                petProfile.weight = weight;
                petProfile.gender = convertSexToGender(sex);
                petProfile.microchipNumber = microchip;
                petProfile.userID = userId;
                petProfile.animalType = 1;

                if (isUpdate) {
                    petProfileViewModel.updatePetProfile(petProfile);
                    Toast.makeText(getContext(), "Pet Profile Updated", Toast.LENGTH_SHORT).show();
                } else {
                    petProfileViewModel.insertPetProfile(petProfile);
                    Toast.makeText(getContext(), "Pet Profile Created", Toast.LENGTH_SHORT).show();
                }

                onPetInfoSaved();


                Toast.makeText(getContext(), "Pet Profile Created", Toast.LENGTH_SHORT).show();
                onPetInfoSaved();
            });
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Invalid input", Toast.LENGTH_SHORT).show();
        }
    }
    private void onPetInfoSaved() {
        if (getActivity() instanceof FragmentChangeListener) {
            ((FragmentChangeListener) getActivity()).replaceFragment(new HomeScreenFragment());
        } else {
            Log.e("PetProfileCreationFragment", "Activity must implement FragmentChangeListener");
        }
    }

    private int convertSexToGender(String sex) {
        return sex.equalsIgnoreCase("male") ? 0 : 1;
    }
}
