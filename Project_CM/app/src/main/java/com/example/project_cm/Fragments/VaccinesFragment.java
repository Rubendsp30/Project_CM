package com.example.project_cm.Fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project_cm.Activities.HomeActivity;
import com.example.project_cm.Adapters.HomeAdapter;
import com.example.project_cm.DataBase.Tables.PetProfileEntity;
import com.example.project_cm.Device;
import com.example.project_cm.R;
import com.example.project_cm.User;
import com.example.project_cm.ViewModels.DeviceViewModel;
import com.example.project_cm.ViewModels.PetProfileViewModel;
import com.example.project_cm.ViewModels.UserViewModel;
import com.example.project_cm.ViewModels.VaccinesViewModel;
import com.example.project_cm.Fragments.VaccineAdapter;

import java.util.ArrayList;

public class VaccinesFragment extends Fragment {

    private UserViewModel userViewModel;
    private PetProfileEntity currentPetProfile;
    private PetProfileViewModel petProfileViewModel;

    private VaccinesViewModel vaccinesViewModel;
    private VaccineAdapter vaccineAdapter;
    @Nullable
    private com.example.project_cm.FragmentChangeListener FragmentChangeListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

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
            vaccinesViewModel = new ViewModelProvider(requireActivity()).get(VaccinesViewModel.class);
        } catch (Exception e) {
            Log.e("ListVaccinesFragment", "Error creating VaccinesViewModel: " + e.getMessage());
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

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setTitle(" ");
        ((AppCompatActivity) requireActivity()).setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> {
            if (FragmentChangeListener != null) {
                FragmentChangeListener.replaceFragment(new PetProfileFragment());
            } else {
                // Handle the case where FragmentChangeListener is null
                Log.e("RegisterFragment", "FragmentChangeListener is null. Unable to replace the fragment.");
            }
        });

        // Logic to get the data for vaccines
        MutableLiveData<User> loggedInUser = userViewModel.getCurrentUser();
        loggedInUser.observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                int currentPetProfileId = currentPetProfile.id;
                vaccinesViewModel.getVaccinesByPetProfileId(currentPetProfileId)
                        .observe(getViewLifecycleOwner(), vaccines -> {
                            vaccineAdapter = new VaccineAdapter(vaccines);
                            setupRecyclerView(view);
                        });
            }
        });
    }

    public void setupRecyclerView(@NonNull View view){
        // RecyclerView Layout Manager
        LinearLayoutManager vaccinesLayoutManager = new LinearLayoutManager(getContext(),
                LinearLayoutManager.VERTICAL, false);

        // RecyclerView for displaying vaccines
        RecyclerView vaccinesListRecycler = view.findViewById(R.id.recyclerViewVaccines);
        vaccinesListRecycler.setLayoutManager(vaccinesLayoutManager);
        vaccinesListRecycler.setAdapter(vaccineAdapter);
    }

    private void loadPetProfile() {
        User currentUser = userViewModel.getCurrentUser().getValue();
        String userId = currentUser != null ? currentUser.getUserID() : "-1";

        petProfileViewModel.getPetProfilesByUserId(userId).observe(getViewLifecycleOwner(), petProfiles -> {
            if (petProfiles != null && !petProfiles.isEmpty()) {
                currentPetProfile = petProfiles.get(0);
            }
        });
    }
}
