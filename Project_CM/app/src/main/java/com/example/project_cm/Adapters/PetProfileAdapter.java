package com.example.project_cm.Adapters;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project_cm.DataBase.Tables.PetProfileEntity;
import com.example.project_cm.Fragments.HistoryFoodFragment;
import com.example.project_cm.Fragments.PetProfileCreationFragment;
import com.example.project_cm.Fragments.VaccinesFragment;
import com.example.project_cm.R;
import com.example.project_cm.ViewModels.PetProfileViewModel;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.List;
import java.util.Locale;

public class PetProfileAdapter extends RecyclerView.Adapter<PetProfileAdapter.PetProfileViewHolder> {

    private final List<PetProfileEntity> petProfiles;
    private final PetProfileViewModel petProfileViewModel;
    @Nullable
    private final com.example.project_cm.FragmentChangeListener fragmentChangeListener;
    private final LifecycleOwner lifecycleOwner;

    //todo
    public PetProfileAdapter(List<PetProfileEntity> petProfiles, PetProfileViewModel petProfileViewModel, LifecycleOwner lifecycleOwner,
                             @Nullable com.example.project_cm.FragmentChangeListener fragmentChangeListener) {
        this.petProfiles = petProfiles;
        this.petProfileViewModel = petProfileViewModel;
        this.lifecycleOwner = lifecycleOwner;
        this.fragmentChangeListener = fragmentChangeListener;
    }

    @Override
    public void onBindViewHolder(@NonNull PetProfileAdapter.PetProfileViewHolder holder, int position) {
        PetProfileEntity petProfile = petProfiles.get(position);
        petProfileViewModel.getPetProfileById(petProfile.id).observe(lifecycleOwner, petProfileEntity -> {
            if (petProfileEntity != null) {
                holder.petNameTextView.setText(petProfile.name);
                holder.petAgeTextView.setText(String.format(Locale.getDefault(), "%d years", petProfile.age));
                holder.petWeightTextView.setText(String.format(Locale.getDefault(), "%.1f kg", petProfile.weight));
                holder.petSexTextView.setText(petProfile.gender == 0 ? "Male" : "Female");
                holder.petMicrochipTextView.setText(petProfile.microchipNumber);
                if (petProfile.photoPath != null && !petProfile.photoPath.isEmpty()) {
                    File imgFile = new File(petProfile.photoPath);
                    if (imgFile.exists()) {
                        Picasso.get().load(imgFile).into(holder.petProfileImageView);
                        Log.d("PetProfileAdapter", "Carregando imagem para o pet: " + petProfile.name);
                    }else {
                        Log.d("PetProfileAdapter", "Caminho da imagem não encontrado: " + petProfile.photoPath);
                    }
                }
            }
        });

        holder.editButton.setOnClickListener(v -> {
            if (holder.FragmentChangeListener != null) {
                petProfileViewModel.setCurrentPet(petProfile);

                Bundle bundle = new Bundle();
                bundle.putLong("petProfileId", petProfile.id);
                Log.d("PetProfileFragment", "currentPet: " + petProfile.name);

                PetProfileCreationFragment fragment = new PetProfileCreationFragment();
                fragment.setArguments(bundle);

                holder.FragmentChangeListener.replaceFragment(fragment);
            } else {
                Log.e("PetProfileAdapter", "FragmentChangeListener is null. Unable to replace the fragment.");
            }
        });

        // Botão Vacinas
        holder.vaccinesButton.setOnClickListener(v -> {
            if (holder.FragmentChangeListener != null) {
                petProfileViewModel.setCurrentPet(petProfile);
                holder.FragmentChangeListener.replaceFragment(new VaccinesFragment());
            } else {
                Log.e("PetProfileAdapter", "FragmentChangeListener is null. Unable to replace the fragment.");
            }
        });

        // Botão Histórico
        holder.historyButton.setOnClickListener(v -> {
            if (holder.FragmentChangeListener != null) {
                petProfileViewModel.setCurrentPet(petProfile);
                holder.FragmentChangeListener.replaceFragment(new HistoryFoodFragment());
            } else {
                Log.e("PetProfileAdapter", "FragmentChangeListener is null. Unable to replace the fragment.");
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
        Button editButton;
        Button vaccinesButton;
        Button historyButton;
        ImageView petProfileImageView;
        @Nullable
        private final com.example.project_cm.FragmentChangeListener FragmentChangeListener;

        public PetProfileViewHolder(@NonNull View itemView, @Nullable com.example.project_cm.FragmentChangeListener fragmentChangeListener) {
            super(itemView);
            petProfileImageView = itemView.findViewById(R.id.petProfileImageView);
            petNameTextView = itemView.findViewById(R.id.petNameTextView);
            petAgeTextView = itemView.findViewById(R.id.petAgeTextView);
            petWeightTextView = itemView.findViewById(R.id.petWeightTextView);
            petSexTextView = itemView.findViewById(R.id.petSexTextView);
            petMicrochipTextView = itemView.findViewById(R.id.petMicrochipTextView);

            editButton = itemView.findViewById(R.id.editButton);
            vaccinesButton = itemView.findViewById(R.id.petVaccinesTextView);
            historyButton = itemView.findViewById(R.id.petHistoryTextView);

            this.FragmentChangeListener = fragmentChangeListener;
        }
    }
}
