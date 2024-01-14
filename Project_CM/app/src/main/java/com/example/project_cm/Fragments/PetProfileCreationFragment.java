package com.example.project_cm.Fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
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
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class PetProfileCreationFragment extends Fragment {
    // ViewModel instances for managing user and pet profile data
    private UserViewModel userViewModel;
    private PetProfileViewModel petProfileViewModel;
    private DeviceViewModel deviceViewModel;
    private MQTTHelper mqttHelper;
    private static final int PICK_IMAGE_REQUEST = 1;

    // Nullable as it may not be initialized if the fragment is not attached to an activity
    @Nullable
    private com.example.project_cm.FragmentChangeListener FragmentChangeListener;

    // UI stuff

    private EditText inputName, inputAge, inputWeight, inputMicrochip;
    private RadioGroup genderRadioGroup;
    private RadioButton radioFemale, radioMale;
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
        profileImage = view.findViewById(R.id.profile_image);
        //todo what is this
        petProfileViewModel.getCurrentPet().observe(getViewLifecycleOwner(), this::loadPetImage);
        inputName = view.findViewById(R.id.inputName);
        inputAge = view.findViewById(R.id.inputAge);
        inputWeight = view.findViewById(R.id.inputWeight);
        inputMicrochip = view.findViewById(R.id.inputMicrochip);
        profileImage = view.findViewById(R.id.profile_image);
        genderRadioGroup = view.findViewById(R.id.inputGender);
        radioFemale = view.findViewById(R.id.radio_female);
        radioMale = view.findViewById(R.id.radio_male);
        Button saveButton = view.findViewById(R.id.saveButton);

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity != null) {
            activity.setSupportActionBar(toolbar);
        }
        profileImage.setOnClickListener(v -> openImageChooser());

        // Determine if editing an existing profile or creating a new one
        if (currentUser != null) {
            if (petProfileId != -1) {
                // If we have a petProfileId, we are in edit mode. Retrieve the profile and populate the UI
                petProfileViewModel.getPetProfileById(petProfileId).observe(getViewLifecycleOwner(), petProfile -> {
                    if (petProfile != null) {
                        currentPetProfile = petProfile;
                        loadPetImage(currentPetProfile);
                        fillInProfileDetails(currentPetProfile);
                        isEditMode = true;
                        if (activity.getSupportActionBar() != null) {
                            activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                            activity.getSupportActionBar().setDisplayShowHomeEnabled(true);
                            toolbar.setNavigationIcon(R.drawable.arrowleft2);
                            toolbar.setNavigationOnClickListener(v -> handleBackPress());
                            // toolbar.setTitle("Edit Profile");
                        }
                    } else {
                        profileImage.setImageResource(R.drawable.insertimage);
                        if (activity.getSupportActionBar() != null) {
                            activity.getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                            activity.getSupportActionBar().setDisplayShowHomeEnabled(false);
                        }
                        //toolbar.setTitle("Create Profile");
                        isEditMode = false;
                    }
                });
            }
        }

        TextView textView = new TextView(activity);
        textView.setText(isEditMode ? "Edit Profile" : "Create Profile");
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        textView.setTypeface(null, Typeface.BOLD);
        Toolbar.LayoutParams layoutParams = new Toolbar.LayoutParams(
                Toolbar.LayoutParams.WRAP_CONTENT,
                Toolbar.LayoutParams.WRAP_CONTENT
        );
        layoutParams.gravity = Gravity.CENTER;
        textView.setLayoutParams(layoutParams);
        toolbar.addView(textView);

        if (activity.getSupportActionBar() != null) {
            activity.getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        saveButton.setOnClickListener(v -> createPetProfile());
    }

    private void handleBackPress() {
        if (isEditMode) {
            returnToPetProfile();
        }
    }

    private void loadPetImage(PetProfileEntity petProfile) {
        if (petProfile != null && petProfile.photoPath != null && !petProfile.photoPath.isEmpty()) {
            File imgFile = new File(petProfile.photoPath);
            if (imgFile.exists()) {
                Picasso.get().load(imgFile).into(profileImage);
            }
        } else {
            profileImage.setImageResource(R.drawable.insertimage);
        }
    }

    private void openImageChooser() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();

            String imagePath = saveImageToInternalStorage(imageUri);
            if (currentPetProfile == null) {
                currentPetProfile = new PetProfileEntity();
            }

            currentPetProfile.photoPath = imagePath;
            Log.d("PetProfileEdit", "Caminho da imagem atualizado: " + imagePath);
            petProfileViewModel.updatePetProfile(currentPetProfile);
            profileImage.setImageURI(imageUri);
        }
    }

    private String saveImageToInternalStorage(Uri imageUri) {
        Context context = getContext();
        if (context == null) {
            return null;
        }
        try {
            File directory = context.getDir("images", Context.MODE_PRIVATE);
            String fileName = "pet_image_" + System.currentTimeMillis() + ".jpg";
            File filePath = new File(directory, fileName);

            InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
            if (inputStream == null) {
                return null;
            }

            OutputStream outputStream = new FileOutputStream(filePath);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
            outputStream.close();
            inputStream.close();

            return filePath.getAbsolutePath();

        } catch (FileNotFoundException e) {
            Log.e("ImageSave", "FileNotFoundException: " + e.getMessage());
        } catch (IOException e) {
            Log.e("ImageSave", "IOException: " + e.getMessage());
        }

        return null;
    }

    private void fillInProfileDetails(PetProfileEntity petProfile) {
        if (petProfile != null) {
            if (petProfile.name != null) {
                inputName.setText(petProfile.name);
            } else {
                inputName.setText("");
            }
            inputAge.setText(String.valueOf(petProfile.age));
            inputWeight.setText(String.valueOf(petProfile.weight));
            if (petProfile.gender == 0) {
                radioMale.setChecked(true);
            } else {
                radioFemale.setChecked(true);
            }
            if (petProfile.microchipNumber != null) {
                inputMicrochip.setText(petProfile.microchipNumber);
            } else {
                inputMicrochip.setText("");
            }
        } else {
            Log.e("fillInProfileDetails", "Pet profile not found");
        }
    }


    // Logic for creating a new pet profile or updating an existing one
    private void createPetProfile() {
        String name = inputName.getText().toString();
        String ageInput = inputAge.getText().toString();
        String weightInput = inputWeight.getText().toString();
        String gender;
        int selectedGenderId = genderRadioGroup.getCheckedRadioButtonId();
        String microchip = inputMicrochip.getText().toString();

        if (selectedGenderId == R.id.radio_female) {
            gender = "female";
        } else if (selectedGenderId == R.id.radio_male) {
            gender = "male";
        } else {
            Toast.makeText(getContext(), "Please select a gender", Toast.LENGTH_SHORT).show();
            return;
        }

        //Fill all fields warning
        if (name.isEmpty() || ageInput.isEmpty() || weightInput.isEmpty() || microchip.isEmpty()) {
            Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            int age = Integer.parseInt(ageInput);
            float weight = Float.parseFloat(weightInput);

            if (currentUser == null) {
                Log.e("createPetProfile", "User is null");
                return;
            }


            PetProfileEntity petProfile;
            if (isEditMode && currentPetProfile != null) {
                petProfile = currentPetProfile;
            } else {
                petProfile = new PetProfileEntity();
                Log.d("PetProfileCreation", "Criando novo PetProfileEntity para novo pet.");
                if (currentPetProfile != null && currentPetProfile.photoPath != null) {
                    petProfile.photoPath = currentPetProfile.photoPath;
                    Log.d("PetProfileCreation", "Caminho da imagem adicionado ao novo pet: " + petProfile.photoPath);
                } else {
                    Log.d("PetProfileCreation", "Nenhuma imagem selecionada para o novo pet.");
                }
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
                returnToPetProfile();
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
        }
    }

    // Convert the input gender string to a gender value
    private int convertToGender(String gender) {
        return gender.equalsIgnoreCase("male") ? 0 : 1;
    }

    private void returnToPetProfile() {
        if (FragmentChangeListener != null) {
            PetProfileFragment petProfileFragment = new PetProfileFragment();
            FragmentChangeListener.replaceFragment(petProfileFragment);
        }
    }
}