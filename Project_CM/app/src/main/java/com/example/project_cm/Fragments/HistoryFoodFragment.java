package com.example.project_cm.Fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project_cm.Activities.HomeActivity;
import com.example.project_cm.Adapters.HistoryAdapter;
import com.example.project_cm.DataBase.Tables.PetProfileEntity;
import com.example.project_cm.R;
import com.example.project_cm.User;
import com.example.project_cm.ViewModels.HistoryViewModel;
import com.example.project_cm.ViewModels.PetProfileViewModel;
import com.example.project_cm.ViewModels.UserViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Objects;

public class HistoryFoodFragment extends Fragment {

    private int currentPetProfile;
    private PetProfileViewModel petProfileViewModel;
    private HistoryViewModel historyViewModel;
    private HistoryAdapter historyAdapter;
    @Nullable
    private com.example.project_cm.FragmentChangeListener FragmentChangeListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.pet_profile_history, container, false);
        this.FragmentChangeListener = (HomeActivity) inflater.getContext();

        // Initialize ViewModel instances
        try {
            petProfileViewModel = new ViewModelProvider(requireActivity()).get(PetProfileViewModel.class);
        } catch (Exception e) {
            Log.e("PetProfileFragment", "Error creating PetProfileViewModel: " + e.getMessage());
        }
        /*
        try {
            historyViewModel = new ViewModelProvider(requireActivity()).get(PetProfileViewModel.class);
        } catch (Exception e) {
            Log.e("PetProfileFragment", "Error creating PetProfileViewModel: " + e.getMessage());
        }
        */

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
                Log.e("HistoryFoodFragment", "FragmentChangeListener is null. Unable to replace the fragment.");
            }
        });

        loadPetProfile();
    }

    public void setupRecyclerView(@NonNull View view){
        // RecyclerView Layout Manager
        LinearLayoutManager historyLayoutManager = new LinearLayoutManager(getContext(),
                LinearLayoutManager.VERTICAL, false);

        // RecyclerView for displaying history
        RecyclerView historyListRecycler = view.findViewById(R.id.recyclerViewHistory);
        historyListRecycler.setLayoutManager(historyLayoutManager);
        historyListRecycler.setAdapter(historyAdapter);
    }

    //todo O current pet vamos guardar mesmo só o ID no viewmodel, essa mudança tb vai acontecer com os outros objetos, é só para estares a par q vai mudar ligeiramente
    private void loadPetProfile() {
        currentPetProfile = petProfileViewModel.getCurrentPet().getValue().id;
    }
}
