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
import androidx.viewpager2.widget.ViewPager2;

import com.example.project_cm.Activities.HomeActivity;
import com.example.project_cm.Adapters.PetProfileAdapter;
import com.example.project_cm.DataBase.Tables.PetProfileEntity;
import com.example.project_cm.Device;
import com.example.project_cm.R;
import com.example.project_cm.ViewModels.DeviceViewModel;
import com.example.project_cm.ViewModels.PetProfileViewModel;

import com.example.project_cm.ViewModels.UserViewModel;
import com.example.project_cm.User;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PetProfileFragment extends Fragment {
    @Nullable
    private com.example.project_cm.FragmentChangeListener FragmentChangeListener;
    private String currentUserId;
    private UserViewModel userViewModel;
    private PetProfileViewModel petProfileViewModel;
    ViewPager2 viewPagerPetProfile;
    PetProfileAdapter petProfileAdapter;
    ArrayList<PetProfileEntity> viewPagerItemDeviceList;
    private List<PetProfileEntity> petProfilesList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.pet_profile, container, false);

        this.FragmentChangeListener = (HomeActivity) inflater.getContext();

        // Initialize ViewModel instances
        try {
            userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);
        } catch (Exception e) {
            Log.e("PetProfileFragment", "Error creating UserViewModel: " + e.getMessage());
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

        Toolbar toolbar = view.findViewById(R.id.toolbarPet);
        toolbar.setTitle(" ");
        AppCompatActivity activity = (AppCompatActivity) requireActivity();
        activity.setSupportActionBar(toolbar);

        if (activity.getSupportActionBar() != null) {
            activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            activity.getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> {
            if (FragmentChangeListener != null) {
                FragmentChangeListener.replaceFragment(new MenuFragment());
            } else {
                Log.e("PetProfileFragment", "FragmentChangeListener is null. Unable to replace the fragment.");
            }
        });

        getCurrentUser();

        viewPagerPetProfile = view.findViewById(R.id.viewPagerPetProfile);
        petProfileAdapter = new PetProfileAdapter(petProfilesList, petProfileViewModel, getChildFragmentManager(), getViewLifecycleOwner(), FragmentChangeListener);
        viewPagerPetProfile.setAdapter(petProfileAdapter);

        // a primeira é para fazer logo load de 2 páginas, a segunda é para tirar aquele efeito de esticar quando arrastas no primeiro ou último ecrã
        viewPagerPetProfile.setOffscreenPageLimit(2);
        viewPagerPetProfile.getChildAt(0).setOverScrollMode(View.OVER_SCROLL_NEVER);

        petProfileViewModel.getPetProfilesByUserId(currentUserId).observe(getViewLifecycleOwner(), this::updatePetProfiles);
    }

    private void getCurrentUser() {
        User currentUser = userViewModel.getCurrentUser().getValue();

        if (currentUser == null) {
            Log.e("PetProfileFragment", "Erro: currentUser é null");
            return;
        }
        currentUserId = currentUser.getUserID();
    }

    private void updatePetProfiles(List<PetProfileEntity> petProfiles) {
        petProfilesList.clear();
        petProfilesList.addAll(petProfiles);
        petProfileAdapter.notifyDataSetChanged();
    }
}

