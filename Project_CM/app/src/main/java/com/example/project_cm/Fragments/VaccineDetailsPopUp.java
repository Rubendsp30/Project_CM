package com.example.project_cm.Fragments;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import android.app.Dialog;
import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.example.project_cm.DataBase.Tables.VaccineEntity;
import com.example.project_cm.R;
import com.example.project_cm.ViewModels.VaccinesViewModel;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class VaccineDetailsPopUp extends DialogFragment {

    EditText tvVaccineName;
    EditText tvVaccineDate;
    private VaccineEntity vaccine;
    private VaccinesViewModel vaccinesViewModel;

    public VaccineDetailsPopUp (VaccinesViewModel vaccinesViewModel, VaccineEntity vaccine) {
        this.vaccinesViewModel = vaccinesViewModel;
        this.vaccine = vaccine;
    }

    @NonNull
    @Override
    //todo este pop up em vez de só dar os detalhes podia ser mais como o do challenge 2 em q dava para editar o titulo
    //O popup mostrava o nome da vacina, daria apra editar a data e apagar
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.vaccine_update_popup, null);

        this.tvVaccineName = view.findViewById(R.id.vaccineName);
        this.tvVaccineDate = view.findViewById(R.id.dateVaccine);
        ImageView deleteButton = view.findViewById(R.id.deleteButton);
        Button cancelButton = view.findViewById(R.id.cancelButton);
        Button saveButton = view.findViewById(R.id.saveButton);

        tvVaccineName.setText(vaccine.getVaccineName());
        tvVaccineDate.setText(vaccine.getVaccineDate());

        deleteButton.setOnClickListener(v -> {
            vaccinesViewModel.deleteVaccine(vaccine);
            dismiss();
        });

        cancelButton.setOnClickListener((v) -> cancelEdit());

        saveButton.setOnClickListener((v) -> saveVaccine());

        builder.setView(view);
        return builder.create();
    }

    public void cancelEdit() {
        // discard changes
        dismiss();
    }

    public void saveVaccine() {
        String vacineNameString = tvVaccineName.getText().toString();
        String vacineDateString = tvVaccineDate.getText().toString();

        if (validateNameVaccine(vacineNameString) && validateDateVaccine(vacineDateString)){
            vaccine.vaccineName = vacineNameString;
            vaccine.vaccineDate = converteStringParaData(vacineDateString);
            vaccinesViewModel.updateVaccine(vaccine, new VaccinesViewModel.UpdateCallback() {
                @Override
                public void onUpdateCompleted(long vaccineId) {
                    Toast.makeText(getContext(), "Vaccine updated successfully", Toast.LENGTH_SHORT).show();
                }
            });

            dismiss();
        }
    }

    private boolean validateNameVaccine(String input) {
        if (input == null || input.trim().isEmpty()) {
            tvVaccineName.setError("This field is required");
            return false;
        } else return true;
    }

    private boolean validateDateVaccine(String dataString) {
        long dateMillis = converteStringParaData(dataString);
        if (dateMillis == -1) {
            return false;
        }
        else if (dateMillis < System.currentTimeMillis()) {
            tvVaccineDate.setError("This vaccine has already been administered");
            return false;
        } else return true;
    }

    private long converteStringParaData(String dataString) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            dateFormat.setLenient(false);
            Date data = dateFormat.parse(dataString);
            return data.getTime();
        } catch (ParseException e) {
            tvVaccineDate.setError("Formato de data inválido");
            return -1;
        }
    }

}
