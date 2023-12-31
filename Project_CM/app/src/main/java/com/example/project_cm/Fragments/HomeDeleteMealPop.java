package com.example.project_cm.Fragments;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.project_cm.MQTTHelper;
import com.example.project_cm.R;
import com.example.project_cm.ViewModels.DeviceViewModel;
import com.example.project_cm.ViewModels.ScheduleViewModel;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttMessage;


public class HomeDeleteMealPop extends DialogFragment {
    private String deleteMealId;
    private String mealDeviceId;
    private ScheduleViewModel scheduleViewModel;

    public HomeDeleteMealPop( String mealDeviceId,String deleteMealId) {
        this.deleteMealId = deleteMealId;
        this.mealDeviceId = mealDeviceId;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getActivity() != null) {
            scheduleViewModel = new ViewModelProvider(getActivity()).get(ScheduleViewModel.class);
        }
    }

    // This method is called to create and configure the dialog when this fragment is displayed.
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Create an AlertDialog instance and set its appearance style.
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.CustomDialog);
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.home_meal_pop_up, null);
        builder.setView(dialogView);

        Button mealNolButton = dialogView.findViewById(R.id.mealNolButton);
        Button mealYesButton = dialogView.findViewById(R.id.mealYesButton);

        mealNolButton.setOnClickListener(v -> {
        dismiss();
        });

        mealYesButton.setOnClickListener(v -> {
           scheduleViewModel.deleteMealSchedule(mealDeviceId, deleteMealId);
            dismiss();
        });

        Dialog dialog = builder.create();


        return dialog;
    }



    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
    }

}
