package com.example.project_cm.Fragments;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.project_cm.DataBase.Tables.VaccineEntity;
import com.example.project_cm.R;
import com.example.project_cm.ViewModels.PetProfileViewModel;
import com.example.project_cm.ViewModels.VaccinesViewModel;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


public class VaccinePopUp extends DialogFragment {
    private EditText editName;
    private EditText editNextDose;
    private VaccineEntity vaccine;
    private PetProfileViewModel petProfileViewModel;
    private final VaccinesViewModel vaccinesViewModel;
    final Calendar calendar = Calendar.getInstance();

    // construtor para criar uma nova vacina
    public VaccinePopUp(VaccinesViewModel vaccinesViewModel) {
        this.vaccinesViewModel = vaccinesViewModel;
    }

    // construtor para editar a vacina
    public VaccinePopUp (VaccinesViewModel vaccinesViewModel, VaccineEntity vaccine) {
        this.vaccinesViewModel = vaccinesViewModel;
        this.vaccine = vaccine;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.CustomDialog);
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.vaccine_pop_up, null);

        try {
            petProfileViewModel = new ViewModelProvider(requireActivity()).get(PetProfileViewModel.class);
        } catch (Exception e) {
            Log.e("VaccinePopUp", "Error creating PetProfileViewModel: " + e.getMessage());
        }

        TextView title = dialogView.findViewById(R.id.title);
        editName = dialogView.findViewById(R.id.editName);
        editNextDose = dialogView.findViewById(R.id.editNextDose);
        ImageView deleteButton = dialogView.findViewById(R.id.deleteButton);
        Button erasePopUpButton = dialogView.findViewById(R.id.eraseButton);
        Button savePopUpButton = dialogView.findViewById(R.id.saveButton);

        editNextDose.setOnClickListener(v -> {
            int currentYear = calendar.get(Calendar.YEAR);
            int currentMonth = calendar.get(Calendar.MONTH);
            int currentDay = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    getContext(),
                    (datePickerView, selectedYear, selectedMonth, selectedDay) -> {
                        calendar.set(Calendar.YEAR, selectedYear);
                        calendar.set(Calendar.MONTH, selectedMonth);
                        calendar.set(Calendar.DAY_OF_MONTH, selectedDay);
                        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                        editNextDose.setText(dateFormat.format(calendar.getTime()));
                    }, currentYear, currentMonth, currentDay);

            datePickerDialog.show();
        });

        // caso seja para editar a vacina, preeche os campos de input
        if (vaccine != null) {
            title.setText("Vaccine");

            editName.setText(vaccine.getVaccineName());
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            try {
                Date vaccineDate = dateFormat.parse(vaccine.getVaccineDate());
                calendar.setTime(vaccineDate);
                editNextDose.setText(dateFormat.format(vaccineDate));
            } catch (ParseException e) {
                Log.e("VaccinePopUp", "Error parsing vaccine date: " + e.getMessage());
            }

            deleteButton.setVisibility(View.VISIBLE);
            deleteButton.setOnClickListener(v -> {
                vaccinesViewModel.deleteVaccine(vaccine);
                dismiss();
            });
        }

        // botão de cancelar
        erasePopUpButton.setOnClickListener((v) -> dismiss());

        // botão save
        savePopUpButton.setOnClickListener(v -> {
            if (validateAllInput()) {
                if (vaccine == null){
                    saveNewVaccine();  // cria nova vacina
                } else saveVaccine();  // dá update na nova vacina
            }
        });

        builder.setView(dialogView);
        return builder.create();
    }

    private void saveNewVaccine() {
        String newNameVaccine = editName.getText().toString();
        Date vaccineDate = calendar.getTime();

        try {
            VaccineEntity newVaccine = new VaccineEntity();
            newVaccine.vaccineName = newNameVaccine;
            newVaccine.vaccineDate = vaccineDate.getTime();

            newVaccine.petId = petProfileViewModel.getCurrentPet().getValue().id;

            Log.d("VaccinePopUp", "Creating new vaccine: " + newVaccine.petId);
            vaccinesViewModel.insertVaccine(newVaccine);
        } catch (Exception e) {
            Toast.makeText(getContext(), "Error adding vaccine", Toast.LENGTH_SHORT).show();
        }

        dismiss();
    }

    public void saveVaccine() {
        String vacineNameString = editName.getText().toString();
        Date vaccineDate = calendar.getTime();

        if (validateNameVaccine(vacineNameString) && validateDateVaccine(vaccineDate)){
            vaccine.vaccineName = vacineNameString;
            vaccine.vaccineDate = vaccineDate.getTime();
            vaccinesViewModel.updateVaccine(vaccine);
            dismiss();
        }
    }

    private boolean validateNameVaccine(String input) {
        if (input.isEmpty()) {
            editName.setError("This field is required");
            return false;
        } else return true;
    }

    private boolean validateDateVaccine(Date dataString) {
        if (dataString.getTime() < System.currentTimeMillis()) {
            editNextDose.setError("Earlier dates are not accepted!");
            return false;
        } else return true;
    }

    private boolean validateAllInput() {
        boolean nameIsValid = validateNameVaccine(editName.getText().toString());
        boolean dateIsValid = validateDateVaccine(calendar.getTime());

        return nameIsValid && dateIsValid;
    }
}

