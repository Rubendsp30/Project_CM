package com.example.project_cm.Adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project_cm.DataBase.Tables.PetProfileEntity;
import com.example.project_cm.Fragments.HistoryFoodFragment;
import com.example.project_cm.Fragments.VaccinesFragment;
import com.example.project_cm.R;
import com.example.project_cm.ViewModels.PetProfileViewModel;

import java.util.List;
import java.util.Locale;

public class PetProfileAdapter extends RecyclerView.Adapter<PetProfileAdapter.PetProfileViewHolder> {

    private final List<PetProfileEntity> petProfiles;
    private final PetProfileViewModel petProfileViewModel;
    @Nullable
    private final FragmentManager fragmentManager;
    @Nullable
    private final com.example.project_cm.FragmentChangeListener fragmentChangeListener;
    private final LifecycleOwner lifecycleOwner;

    //todo
    public PetProfileAdapter(List<PetProfileEntity> petProfiles, PetProfileViewModel petProfileViewModel,
                             @Nullable FragmentManager fragmentManager, LifecycleOwner lifecycleOwner,
                             @Nullable com.example.project_cm.FragmentChangeListener fragmentChangeListener) {
        this.petProfiles = petProfiles;
        this.petProfileViewModel = petProfileViewModel;
        this.fragmentManager = fragmentManager;
        this.lifecycleOwner = lifecycleOwner;
        this.fragmentChangeListener = fragmentChangeListener;
    }

    @Override
    public void onBindViewHolder(@NonNull PetProfileAdapter.PetProfileViewHolder holder, int position) {
        PetProfileEntity petProfile = petProfiles.get(position);
        petProfileViewModel.getPetProfileById(petProfile.id).observe(lifecycleOwner, petProfileEntity -> {
            if (petProfileEntity != null) {
                holder.bind(petProfile, petProfileViewModel);
            }
        });
    }

    @Override
    public int getItemCount() {
        return petProfiles.size();
    }

    @NonNull
    @Override
    public PetProfileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.pet_profile_fragment, parent, false);
        return new PetProfileViewHolder(view, fragmentChangeListener);
    }

    static class PetProfileViewHolder extends RecyclerView.ViewHolder {
        TextView petNameTextView;
        TextView petAgeTextView;
        TextView petWeightTextView;
        TextView petSexTextView;
        TextView petMicrochipTextView;
        Button vaccinesButton;
        Button historyButton;
        @Nullable
        private com.example.project_cm.FragmentChangeListener FragmentChangeListener;

        public PetProfileViewHolder(@NonNull View itemView, @Nullable com.example.project_cm.FragmentChangeListener fragmentChangeListener) {
            super(itemView);
            petNameTextView = itemView.findViewById(R.id.petNameTextView);
            petAgeTextView = itemView.findViewById(R.id.petAgeTextView);
            petWeightTextView = itemView.findViewById(R.id.petWeightTextView);
            petSexTextView = itemView.findViewById(R.id.petSexTextView);
            petMicrochipTextView = itemView.findViewById(R.id.petMicrochipTextView);

            vaccinesButton = itemView.findViewById(R.id.petVaccinesTextView);
            historyButton = itemView.findViewById(R.id.petHistoryTextView);

            this.FragmentChangeListener = fragmentChangeListener;
        }

        //todo n estou a dizer q está bem ou mal mas isto pode estar no onBindViewHolder pq n sei se é pesado passar viewmodels por parametro e assim
        // como tip, para isso só tens q meter o "holder." antes das cenas q tens aqui
        // tenta rever o homeadapter e o uso dos viewPagerItem q parecem ser os petProfiles q estás a usar
        public void bind(PetProfileEntity petProfile, PetProfileViewModel petProfileViewModel) {
            petNameTextView.setText(petProfile.name);
            petAgeTextView.setText(String.format(Locale.getDefault(), "%d years", petProfile.age));
            petWeightTextView.setText(String.format(Locale.getDefault(), "%.1f kg", petProfile.weight));
            petSexTextView.setText(petProfile.gender == 0 ? "Male" : "Female");
            petMicrochipTextView.setText(petProfile.microchipNumber);

            // Botão Vacinas
            vaccinesButton.setOnClickListener(v -> {
                if (FragmentChangeListener != null) {
                    petProfileViewModel.setCurrentPet(petProfile);
                    FragmentChangeListener.replaceFragment(new VaccinesFragment());
                } else {
                    Log.e("PetProfileAdapter", "FragmentChangeListener is null. Unable to replace the fragment.");
                }
            });


            // Botão Histórico
            historyButton.setOnClickListener(v -> {
                if (FragmentChangeListener != null) {
                    petProfileViewModel.setCurrentPet(petProfile);
                    FragmentChangeListener.replaceFragment(new HistoryFoodFragment());
                } else {
                    Log.e("PetProfileAdapter", "FragmentChangeListener is null. Unable to replace the fragment.");
                }
            });
        }


    }
}
