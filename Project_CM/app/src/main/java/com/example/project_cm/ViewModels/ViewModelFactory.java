package com.example.project_cm.ViewModels;
import com.example.project_cm.DataBase.PetProfileDao;
import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

public class ViewModelFactory implements ViewModelProvider.Factory {
    private final PetProfileDao petProfileDao;

    public ViewModelFactory(PetProfileDao petProfileDao) {
        this.petProfileDao = petProfileDao;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(PetProfileViewModel.class)) {
            return (T) new PetProfileViewModel(petProfileDao);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}
