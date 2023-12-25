package com.example.project_cm.Fragments;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.example.project_cm.MQTTHelper;
import com.example.project_cm.R;
import com.example.project_cm.ViewModels.DeviceViewModel;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttMessage;


public class TreatPopUpFragment extends DialogFragment {
    private final DeviceViewModel deviceViewModel;
    private SeekBar portionSeekBar;
    private int treatSize = 85;
    private final MQTTHelper mqttHelper;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private boolean responseReceived = false;
    private ImageButton giveTreatButton;

    public TreatPopUpFragment(DeviceViewModel deviceViewModel1) {
        this.deviceViewModel = deviceViewModel1;
        this.mqttHelper = MQTTHelper.getInstance(getContext(), "yourClientName");
    }

    // This method is called to create and configure the dialog when this fragment is displayed.
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Create an AlertDialog instance and set its appearance style.
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.CustomDialog);
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.home_treat_pop_up, null);
        builder.setView(dialogView);

        portionSeekBar = dialogView.findViewById(R.id.portionSeekBar);
        TextView portionTreatValue = dialogView.findViewById(R.id.portionTreaValue);
        giveTreatButton = dialogView.findViewById(R.id.giveTreatButton);

        portionSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
                treatSize = seekBar.getProgress();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // TODO Auto-generated method stub

                portionTreatValue.setText(progress + "g");

            }
        });

        mqttHelper.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {
                Log.d("MQTT", "Connection (re)established to: " + serverURI);
            }

            @Override
            public void connectionLost(Throwable cause) {
                Log.d("MQTT", "Connection lost: " + cause.toString());
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                if (topic.equals("/project/treatAnswer/" + deviceViewModel.getCurrentDevice().getValue().getDeviceID())) {
                    responseReceived = true;
                    Log.d("MQTT", "Response received: " + new String(message.getPayload()));

                    getActivity().runOnUiThread(() -> {
                        try {
                            View dialogView = getDialog().getWindow().getDecorView();

                            ImageView treatSuccessIcon = dialogView.findViewById(R.id.treatSuccessIcon);
                            TextView treatSuccessText = dialogView.findViewById(R.id.treatSuccessText);
                            TextView portionTreatText = dialogView.findViewById(R.id.portionTreatText);


                            giveTreatButton.setVisibility(View.GONE);
                            portionSeekBar.setVisibility(View.GONE);
                            portionTreatValue.setVisibility(View.GONE);
                            portionTreatText.setVisibility(View.GONE);
                            treatSuccessIcon.setVisibility(View.VISIBLE);
                            treatSuccessText.setVisibility(View.VISIBLE);

                            // Delay dismissal of the dialog
                            handler.postDelayed(() -> {
                                dismiss();
                                enableComponentsAndAllowDismiss();
                            }, 3000); // 2000 milliseconds = 2 seconds

                        } catch (Exception e) {
                            Log.e("MQTT", "Error updating UI: " + e.getMessage());
                        }
                    });
                }
            }


            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                Log.d("MQTT", "Delivery complete for token: " + token.toString());
            }
        });


        giveTreatButton.setOnClickListener(v -> {

            giveTreatButton.setEnabled(false);
            portionSeekBar.setEnabled(false);
            Dialog dialog = getDialog();
            if (dialog != null) {
                dialog.setCancelable(false);
                dialog.setCanceledOnTouchOutside(false);
            }

            String topic = "/project/treat/" + deviceViewModel.getCurrentDevice().getValue().getDeviceID();
            String message = String.valueOf(treatSize); // Convert treatSize to string
            mqttHelper.publishToTopic(topic, message, 2);

            Log.e("Treat", "Treat size: " + treatSize + "g to " + deviceViewModel.getCurrentDevice().getValue().getDeviceID());

            // Reset the flag
            responseReceived = false;

            // Setup a delayed action to check the response
            handler.postDelayed(() -> {
                if (!responseReceived) {
                    Log.d("MQTT", "No response received in the specified time.");
                    enableComponentsAndAllowDismiss();
                }
            }, 5000); // 5000 milliseconds = 5 seconds

        });

        Dialog dialog = builder.create();

        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);

        return dialog;
    }

    private void enableComponentsAndAllowDismiss() {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                giveTreatButton.setEnabled(true);
                portionSeekBar.setEnabled(true);
                Dialog dialog = getDialog();
                if (dialog != null) {
                    dialog.setCancelable(true);
                    dialog.setCanceledOnTouchOutside(true);
                }
            });
        }
    }


    // This method is called when the dialog is dismissed.
    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        handler.removeCallbacksAndMessages(null);

    }

}
