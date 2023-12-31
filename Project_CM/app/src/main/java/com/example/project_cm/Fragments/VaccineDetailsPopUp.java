package com.example.project_cm.Fragments;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import android.app.Dialog;
import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.example.project_cm.DataBase.Tables.VaccineEntity;
import com.example.project_cm.R;
import com.example.project_cm.ViewModels.VaccinesViewModel;


public class VaccineDetailsPopUp extends DialogFragment {

    private VaccineEntity vaccine;
    private VaccinesViewModel vaccinesViewModel;

    public VaccineDetailsPopUp (VaccinesViewModel vaccinesViewModel, VaccineEntity vaccine) {
        this.vaccinesViewModel = vaccinesViewModel;
        this.vaccine = vaccine;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.vaccine_delete_popup, null);

        TextView tvVaccineName = view.findViewById(R.id.vaccineName);
        TextView tvVaccineDate = view.findViewById(R.id.dateVaccine);
        ImageView deleteButton = view.findViewById(R.id.deleteButton);

        tvVaccineName.setText(vaccine.getVaccineName());
        tvVaccineDate.setText(vaccine.getVaccineDate());

        deleteButton.setOnClickListener(v -> {
            vaccinesViewModel.deleteVaccine(vaccine);
            dismiss();
        });

        builder.setView(view);
        return builder.create();
    }

}
