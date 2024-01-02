package com.example.project_cm.Fragments;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.project_cm.DataBase.Tables.PetProfileEntity;
import com.example.project_cm.DataBase.Tables.VaccineEntity;
import com.example.project_cm.R;
import com.example.project_cm.ViewModels.PetProfileViewModel;
import com.example.project_cm.ViewModels.VaccinesViewModel;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class VaccineCreationPopUp extends DialogFragment {

    private EditText editName;
    private EditText editNextDose;
    private PetProfileViewModel petProfileViewModel;
    private VaccinesViewModel vaccinesViewModel;

    public VaccineCreationPopUp(VaccinesViewModel vaccinesViewModel) {

        this.vaccinesViewModel = vaccinesViewModel;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.CustomDialog);
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.vaccine_input_pop_up, null);

        try {
            petProfileViewModel = new ViewModelProvider(requireActivity()).get(PetProfileViewModel.class);
        } catch (Exception e) {
            Log.e("PetProfileFragment", "Error creating PetProfileViewModel: " + e.getMessage());
        }

        editName = dialogView.findViewById(R.id.editName);
        editNextDose = dialogView.findViewById(R.id.editNextDose);
        Button erasePopUpButton = dialogView.findViewById(R.id.eraseButton);
        Button savePopUpButton = dialogView.findViewById(R.id.saveButton);

        builder.setView(dialogView);

        erasePopUpButton.setOnClickListener((v) -> dismiss());
        savePopUpButton.setOnClickListener(v -> {
            if (validateAllInput()) {
                saveNewVaccine();
            }
        });

        return builder.create();
    }

    private void saveNewVaccine() {
        String newNameVaccine = editName.getText().toString();
        String newNextDose = editNextDose.getText().toString();

        try {
            Date vaccineDate = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(newNextDose);

            VaccineEntity newVaccine = new VaccineEntity();
            newVaccine.vaccineName = newNameVaccine;
            newVaccine.vaccineDate = vaccineDate.getTime();

            newVaccine.petId = petProfileViewModel.getCurrentPet().getValue().id;

            Log.d("VaccineCreationPopUp", "Creating new vaccine: " + newVaccine.petId);
            vaccinesViewModel.insertVaccine(newVaccine, new VaccinesViewModel.InsertCallback() {
                @Override
                public void onInsertCompleted(long vaccineId) {
                    Toast.makeText(getContext(), "Vaccine added successfully", Toast.LENGTH_SHORT).show();
                }
            });

        } catch (ParseException e) {
            Toast.makeText(getContext(), "Invalid date format", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(getContext(), "Error adding vaccine", Toast.LENGTH_SHORT).show();
        }

        dismiss();
    }

    private boolean validateNameVaccine(String input) {
        if (input == null || input.trim().isEmpty()) { // o trim verifica se o nome tem espaços em branco (o input == null é necessário)
            editName.setError("This field is required");
            return false;
        } else return true;
    }

    private boolean validateDateVaccine(String dataString) {
        long dateMillis = converteStringParaData(dataString);
        if (dateMillis == -1) {
            return false;
        }
        else if (dateMillis < System.currentTimeMillis()) {
            editNextDose.setError("This vaccine has already been administered");
            return false;
        } else return true;
    }

    //todo em vez de usar strig usar mesmo aquela cena de aparecer o calendário e escolher a data
    private long converteStringParaData(String dataString) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            dateFormat.setLenient(false); // garante que strings inválidas como "30/02/2021" sejam rejeitadas
            Date data = dateFormat.parse(dataString);
            return data.getTime();
        } catch (ParseException e) {
            editNextDose.setError("Formato de data inválido");
            return -1;
        }
    }

    private boolean validateAllInput() {
        boolean nameIsValid = validateNameVaccine(editName.getText().toString());
        boolean dateIsValid = validateDateVaccine(editNextDose.getText().toString());

        return nameIsValid && dateIsValid;
    }

}