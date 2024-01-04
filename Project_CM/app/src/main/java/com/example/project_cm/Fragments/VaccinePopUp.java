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

//todo pelo q entendi estás a usar isto tanto para a criação como para editar, teria sido melhor separar talvez pq torna-se confuso mas agora fica assim
public class VaccinePopUp extends DialogFragment {
    private TextView title;
    private EditText editName;
    private EditText editNextDose;
    private VaccineEntity vaccine;
    private PetProfileViewModel petProfileViewModel;
    private VaccinesViewModel vaccinesViewModel;
    final Calendar calendar = Calendar.getInstance();

    //todo adicionar uns comentários a identificar q métodos são para criar a vacina e quais são os de editar
    public VaccinePopUp(VaccinesViewModel vaccinesViewModel) {
        this.vaccinesViewModel = vaccinesViewModel;
    }

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

        title = dialogView.findViewById(R.id.title);
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

        if (vaccine != null) {
            title.setText("Vaccine");

            editName.setText(vaccine.getVaccineName());
            editNextDose.setText(vaccine.getVaccineDate());

            deleteButton.setVisibility(View.VISIBLE);
            deleteButton.setOnClickListener(v -> {
                vaccinesViewModel.deleteVaccine(vaccine);
                dismiss();
            });
        }

        erasePopUpButton.setOnClickListener((v) -> dismiss());

        savePopUpButton.setOnClickListener(v -> {
            if (validateAllInput()) {
                if (vaccine == null){
                    saveNewVaccine();
                } else saveVaccine();
            }
        });

        builder.setView(dialogView);
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

            Log.d("VaccinePopUp", "Creating new vaccine: " + newVaccine.petId);
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

    public void saveVaccine() {
        String vacineNameString = editName.getText().toString();
        String vacineDateString = editNextDose.getText().toString();

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
        if (input == null || input.trim().isEmpty()) { // o trim verifica se o nome tem espaços em branco (o input == null é necessário)
            //todo a vacina pode ter 2 nomes separados por espaço ex:Hepatite Viral, melhor só usar o empty sem o trim
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
            //todo o texto do erro pode ser mais a avisar que n se aceitam datas passadas, pode n ter sido dada
            editNextDose.setError("This vaccine has already been administered");
            return false;
        } else return true;
    }

    //todo visto q estamos a usar o calendar n sei se isto vai ser necessário e tudo o q envolve isto de string.
    //N sei mesmo como funciona o calendar mas onde está a ser usado ele parece estar a retornar um date mas dps estás tu a mudar para string para voltar a mudar para data
    //revê essa lógica, posso estar enganado mas dá double check nisso
    private long converteStringParaData(String dataString) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            dateFormat.setLenient(false);
            Date data = dateFormat.parse(dataString);
            return data.getTime();
        } catch (ParseException e) {
            editNextDose.setError("Invalid date format.");
            return -1;
        }
    }

    private boolean validateAllInput() {
        boolean nameIsValid = validateNameVaccine(editName.getText().toString());
        boolean dateIsValid = validateDateVaccine(editNextDose.getText().toString());

        return nameIsValid && dateIsValid;
    }
}

