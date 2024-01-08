package com.example.project_cm.Fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project_cm.Activities.HomeActivity;
import com.example.project_cm.Adapters.HistoryAdapter;
import com.example.project_cm.R;
import com.example.project_cm.ViewModels.DeviceViewModel;
import com.example.project_cm.ViewModels.HistoryViewModel;
import com.example.project_cm.ViewModels.PetProfileViewModel;

public class HistoryFoodFragment extends Fragment {
    private TextView tvEmptyMessage;
    private int currentPetProfile = -1;
    private PetProfileViewModel petProfileViewModel;
    private HistoryViewModel historyViewModel;
    private HistoryAdapter historyAdapter;
    private DeviceViewModel deviceViewModel;
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
            Log.e("HistoryFoodFragment", "Error creating HistoryFoodFragment: " + e.getMessage());
        }
        try {
            historyViewModel = new ViewModelProvider(requireActivity()).get(HistoryViewModel.class);
        } catch (Exception e) {
            Log.e("HistoryFoodFragment", "Error creating HistoryFoodFragment: " + e.getMessage());
        }
        try {
            deviceViewModel = new ViewModelProvider(requireActivity()).get(DeviceViewModel.class);
        } catch (Exception e) {
            Log.e("HistoryFoodFragment", "Error creating HistoryFoodFragment: " + e.getMessage());
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
                Log.e("HistoryFoodFragment", "FragmentChangeListener is null. Unable to replace the fragment.");
            }
        });

        loadPetProfile();

        String currentDevice;
        currentDevice = deviceViewModel.getCurrentDeviceId();

        // frase que indica que a lista está vazia
        tvEmptyMessage = view.findViewById(R.id.tvEmptyHistoryMessage);

        // Logic to get the data for history
        if (currentPetProfile != -1) {
            // apaga as historys meal que já passaram dos 7 dias (fora de prazo de validade hehehe)
            historyViewModel.deleteOldMealHistories(currentDevice);

            // retorna a lista do historico de refeições
            historyViewModel.getHistoryMeals(currentDevice)
                    .observe(getViewLifecycleOwner(), history -> {
                        if (history == null || history.isEmpty()) {
                            tvEmptyMessage.setVisibility(View.VISIBLE);
                        } else {
                            tvEmptyMessage.setVisibility(View.GONE);
                            historyAdapter = new HistoryAdapter(history);
                            setupRecyclerView(view);
                        }
                    });
        } else {
            Log.e("HistoryFoodFragment", "currentPetProfile is null.");
        }
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

    private void loadPetProfile() {
        currentPetProfile = petProfileViewModel.getCurrentPet().getValue().id;
    }
}
