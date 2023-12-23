package com.example.project_cm.Fragments;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.project_cm.R;
import com.example.project_cm.ViewModels.DeviceViewModel;

public class TreatPopUpFragment extends DialogFragment {
    private final DeviceViewModel deviceViewModel;
    private SeekBar portionSeekBar;

    public TreatPopUpFragment(DeviceViewModel deviceViewModel1) {
        this.deviceViewModel = deviceViewModel1;
    }

    // This method is called to create and configure the dialog when this fragment is displayed.
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Create an AlertDialog instance and set its appearance style.
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.home_pop_up, null);

        TextView deviceId = dialogView.findViewById(R.id.deviceIdText);
        deviceId.setText(deviceViewModel.getCurrentDevice().getValue().getDeviceID());

        portionSeekBar = dialogView.findViewById(R.id.portionSeekBar);
        TextView portionTreatValue = dialogView.findViewById(R.id.portionTreaValue);

        portionSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,boolean fromUser) {
                // TODO Auto-generated method stub

                portionTreatValue.setText(progress +"g");

            }
        });
        // Find buttons in the dialog layout.
        //this.editTitlePopUp = dialogView.findViewById(R.id.editTitlePopUp);
        //Button erasePopUpButton = dialogView.findViewById(R.id.erasePopUpButton);


        /*
        loggedInUser = deviceViewModel.getLogedUser();
        Note selectedNote = notesViewModel.getSelectedNote();
        docId = selectedNote.getNoteId();*/

        // Set the view for the AlertDialog.
        builder.setView(dialogView);

        //editTitlePopUp.setText(selectedNote.getTitle());

        // Set a click listener for the erase button.
       // erasePopUpButton.setOnClickListener((v) -> eraseNote());

        // Set a click listener for the save changes button.
        //savePopUpButton.setOnClickListener((v) -> saveNewTitle());

        // Show an overlay in the parent fragment when the dialog is displayed.
        //((ListNotesFragment) getParentFragment()).showOverlay();

        return builder.create(); // Return the configured dialog.
    }

    // This method is called when the dialog is dismissed.
    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        /*notesViewModel.setSelectedNote(new Note());
        // Hide the overlay in the parent fragment when the dialog is dismissed.
        ((ListNotesFragment) getParentFragment()).hideOverlay();*/
    }

/*
    public void saveNewTitle() {
        String newNoteTitle = editTitlePopUp.getText().toString();
        if (newNoteTitle.isEmpty()) {
            editTitlePopUp.setError("Title is required");
            return;
        }

        notesViewModel.updateNote(loggedInUser, docId, newNoteTitle, null,requireContext());
        dismiss();
    }*/
}
