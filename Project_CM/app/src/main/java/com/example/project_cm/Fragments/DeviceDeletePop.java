package com.example.project_cm.Fragments;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.project_cm.MQTTHelper;
import com.example.project_cm.R;
import com.example.project_cm.ViewModels.DeviceManagerViewModel;
import com.example.project_cm.utils.ClientNameUtil;


public class DeviceDeletePop extends DialogFragment {
    private final String deleteDeviceId;
    private final int deletePetId;
    private DeviceManagerViewModel deviceManagerViewModel;
    private MQTTHelper mqttHelper;

    public DeviceDeletePop(String deleteDeviceId, int deletePetId) {
        this.deleteDeviceId = deleteDeviceId;
        this.deletePetId = deletePetId;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getActivity() != null) {
            deviceManagerViewModel = new ViewModelProvider(getActivity()).get(DeviceManagerViewModel.class);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Create an AlertDialog instance and set its appearance style.
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.CustomDialog);
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.device_deletel_pop_up, null);
        builder.setView(dialogView);

        Button deleteNolButton = dialogView.findViewById(R.id.deleteNolButton);
        Button deleteYesButton = dialogView.findViewById(R.id.deleteYesButton);

        deleteNolButton.setOnClickListener(v -> dismiss());

        deleteYesButton.setOnClickListener(v -> {
            deviceManagerViewModel.deleteDevice(deleteDeviceId,deletePetId );
            mqttHelper = MQTTHelper.getInstance(requireContext(), ClientNameUtil.getClientName());
            mqttHelper.unsubscribeToDeviceTopics(deleteDeviceId);
            dismiss();
        });
        
        return builder.create();
    }


    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
    }

}
