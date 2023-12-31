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

        if (newNameVaccine.isEmpty() || newNextDose.isEmpty()) {
            //todo Usar os seterrors
            Toast.makeText(getContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // Aqui você pode converter a string da data em um formato de data ou long, conforme necessário
            Date vaccineDate = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(newNextDose);

            VaccineEntity newVaccine = new VaccineEntity();
            newVaccine.vaccineName = newNameVaccine;
            newVaccine.vaccineDate = vaccineDate.getTime();

            //todo guarda e vai logo buscar só o ID, n precisamos de estar a meter sempre o objeto todo de um lado para o outro
            PetProfileEntity currentPetProfile = petProfileViewModel.getCurrentPet().getValue();
            newVaccine.petId = currentPetProfile.id;

            Log.d("VaccineFragment", "Creating new vaccine: " + newVaccine.petId);
            vaccinesViewModel.insertVaccine(newVaccine, new VaccinesViewModel.InsertCallback() {
                @Override
                public void onInsertCompleted(long vaccineId) {
                    //todo, pq é q estas a criar um handler só para um toast
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getContext(), "Vaccine added successfully", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });

        } catch (ParseException e) {
            Toast.makeText(getContext(), "Invalid date format", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(getContext(), "Error adding vaccine", Toast.LENGTH_SHORT).show();
        }

        dismiss();
    }

    //todo porquê esta validação? no maximo verificamos se está empty, n sabemos os nomes das vacinas. also, mudar o setError
    private boolean validateNameVaccine(String input) {
        if (input != null && input.length() >= 3) {
            editName.setError(null);
            return true;
        } else {
            editName.setError("Username too short");
            return false;
        }
    }

    private boolean validateDateVaccine(String dataString) {
        long dateMillis = converteStringParaData(dataString);
        if (dateMillis == -1) {
            return false;
        }
        else if (dateMillis > System.currentTimeMillis()) {
            editNextDose.setError("Esta vacina já foi administrada");
            return true;
        } else return false;
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

    //todo melhorar um pouco o nome das variaveis q eu nem tinha apercebido q era bools
    private boolean validateAllInput() {
        boolean name = validateNameVaccine(editName.getText().toString());
        boolean date = validateDateVaccine(editNextDose.getText().toString());

        return name && date;
    }

}