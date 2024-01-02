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
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project_cm.Activities.HomeActivity;
import com.example.project_cm.DataBase.Tables.PetProfileEntity;
import com.example.project_cm.DataBase.Tables.VaccineEntity;
import com.example.project_cm.R;
import com.example.project_cm.User;
import com.example.project_cm.ViewModels.PetProfileViewModel;
import com.example.project_cm.ViewModels.UserViewModel;
import com.example.project_cm.ViewModels.VaccinesViewModel;
import com.example.project_cm.Adapters.VaccineAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;
import java.util.Objects;

public class VaccinesFragment extends Fragment {
    private TextView tvEmptyMessage;
    private int currentPetProfile = -1;
    private PetProfileViewModel petProfileViewModel;

    private VaccinesViewModel vaccinesViewModel;
    private VaccineAdapter vaccineAdapter;
    @Nullable
    private com.example.project_cm.FragmentChangeListener FragmentChangeListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.vaccines_fragment, container, false);
        this.FragmentChangeListener = (HomeActivity) inflater.getContext();

        // Initialize ViewModel instances
        try {
            vaccinesViewModel = new ViewModelProvider(requireActivity()).get(VaccinesViewModel.class);
        } catch (Exception e) {
            Log.e("VaccinesFragment", "Error creating VaccinesViewModel: " + e.getMessage());
        }
        try {
            petProfileViewModel = new ViewModelProvider(requireActivity()).get(PetProfileViewModel.class);
        } catch (Exception e) {
            Log.e("VaccinesFragment", "Error creating PetProfileViewModel: " + e.getMessage());
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
                Log.e("VaccineFragment", "FragmentChangeListener is null. Unable to replace the fragment.");
            }
        });
        loadPetProfile();

        // frase que indica que a lista está vazia
        tvEmptyMessage = view.findViewById(R.id.tvEmptyMessage);

        // Logic to get the data for vaccines
        // eu sei que o observer aqui não é preciso, mas se tirar, vai dar muitos problemas por causa de se tratar de um livedata
        if (currentPetProfile != -1) {
            vaccinesViewModel.getVaccinesByPetProfileId(currentPetProfile)
                    .observe(getViewLifecycleOwner(), vaccines -> {
                        if (vaccines == null || vaccines.isEmpty()) {
                            tvEmptyMessage.setVisibility(View.VISIBLE);
                        } else {
                            tvEmptyMessage.setVisibility(View.GONE);
                            vaccineAdapter = new VaccineAdapter(vaccines);
                            setupRecyclerView(view, vaccines);
                        }
                    });
        } else {
            Log.e("VaccineFragment", "currentPetProfile is null.");
        }

        FloatingActionButton fabAddVaccine = view.findViewById(R.id.fabAddVaccine);
        fabAddVaccine.setOnClickListener(v -> {
            if (FragmentChangeListener != null) {
                VaccinePopUp vaccinePopUp = new VaccinePopUp(vaccinesViewModel);
                vaccinePopUp.show(getChildFragmentManager(), "vaccinePopUp");
            } else {
                Log.e("VaccineFragment", "FragmentChangeListener is null. Unable to open the vaccine creation popup.");
            }
        });
    }

    public void setupRecyclerView(@NonNull View view, List<VaccineEntity> vaccines){
        // RecyclerView Layout Manager
        LinearLayoutManager vaccinesLayoutManager = new LinearLayoutManager(getContext(),
                LinearLayoutManager.VERTICAL, false);

        // RecyclerView for displaying vaccines
        RecyclerView vaccinesListRecycler = view.findViewById(R.id.recyclerViewVaccines);
        vaccinesListRecycler.setLayoutManager(vaccinesLayoutManager);
        vaccinesListRecycler.setAdapter(vaccineAdapter);

        vaccineAdapter = new VaccineAdapter(vaccines);
        vaccineAdapter.setOnItemClickListener(vaccine -> openVaccineDetailsPopup(vaccine));
        vaccinesListRecycler.setAdapter(vaccineAdapter);
    }

    private void openVaccineDetailsPopup(VaccineEntity vaccine) {
        VaccinePopUp vaccinePopUp = new VaccinePopUp(vaccinesViewModel, vaccine);
        vaccinePopUp.show(getChildFragmentManager(), "vaccinePopUp");
    }

    private void loadPetProfile() {
        currentPetProfile = petProfileViewModel.getCurrentPet().getValue().id;
    }
}
