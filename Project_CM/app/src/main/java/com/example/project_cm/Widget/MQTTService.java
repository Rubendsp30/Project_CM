package com.example.project_cm.Widget;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.lifecycle.LiveData;

import com.example.project_cm.Device;
import com.example.project_cm.MQTTHelper;
import com.example.project_cm.R;
import com.example.project_cm.ViewModels.DeviceViewModel;
import com.example.project_cm.utils.ClientNameUtil;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.security.SecureRandom;
import java.util.ArrayList;

public class MQTTService extends Service {
    MQTTHelper mqttHelper;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("MQTTService", "Service started");

        try {
            Log.d("MQTTService", "Starting foreground service");
            startForegroundService();

            Log.d("MQTTService", "Sending MQTT message");
            sendMQTTMessage();
        } catch (Exception e) {
            Log.e("MQTTService", "Error in service: " + e.toString());
            e.printStackTrace();
        }

        return START_NOT_STICKY;
    }


    private void startForegroundService() {
        Notification notification = createNotification();
        startForeground(202, notification);
    }

    private Notification createNotification() {
        // Create a notification channel for Android O and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel(
                    "Widget Notification",
                    "MQTT Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "Widget Notification")
                .setContentTitle("MQTT Service")
                .setContentText("Sending MQTT message")
                .setSmallIcon(R.drawable.treat_button)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        return builder.build();
    }



    private void sendMQTTMessage() {
        Log.e("MQTT Widget", "Entered sendMqttMessage");

        String userId = getLoggedInUserId(); // You need to implement this method
        if (userId == null) {
            Log.e("MQTTService", "No logged-in user found");
            stopService();
            return;
        }
        DeviceViewModel deviceViewModel = new DeviceViewModel();
        deviceViewModel.getDevicesForUserWidget(userId, new DeviceViewModel.DeviceIdLoadCallback()   {
            @Override
            public void onDeviceIdLoaded(String deviceId) {
                    String topic = "/project/treat/" + deviceId;
                    Log.e("Widget", topic);
                    ClientNameUtil.generateClientName();
                    String clientName = ClientNameUtil.getClientName();
                    mqttHelper = MQTTHelper.getInstance(getBaseContext(), clientName);

                    mqttHelper.setCallback(new MqttCallbackExtended() {
                        @Override
                        public void connectionLost(Throwable cause) {
                            Log.e("MQTT Widget", "MQTT connection lost: " + cause.getMessage());
                            stopService();
                        }

                        @Override
                        public void messageArrived(String topic, MqttMessage message) throws Exception {

                        }

                        @Override
                        public void deliveryComplete(IMqttDeliveryToken token) {
                            stopService();
                        }

                        @Override
                        public void connectComplete(boolean reconnect, String serverURI) {
                            publishMessage(topic);
                        }

                    });
                    mqttHelper.connect();
                }
            @Override
            public void onError(Exception e) {
                Log.e("MQTTService", "Error loading devices: " + e.getMessage());
                stopService();
            }
        });

    }

    private void publishMessage(String topic) {
        //String topic = "/project/treat/rcL9kl2gSYbLusKe4N";
        String message = "15";
        Log.d("MQTT Widget", "Sending Message");
        try {
            mqttHelper.publishToTopic(topic, message, 2);
            Log.d("MQTT Widget", "MQTT message sent: " + message + " to topic: " + topic);
        } catch (Exception e) {
            Log.e("MQTT Widget", "Error sending MQTT message: " + e.getMessage());
        }
    }

    private String getLoggedInUserId() {
        SharedPreferences sharedPreferences = getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        return sharedPreferences.getString("loggedInUserId", null);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void stopService() {
        Log.e("MQTT Widget", "Stoping Service ");
        mqttHelper.stop();
        stopForeground(true); // Remove notification and stop the service
        stopSelf();
        Log.e("MQTT Widget", "Stoped Service ");
    }

    @Override
    public void onDestroy() {
        Log.d("MQTTService", "Destroying service");
        super.onDestroy();
        //TODO Forces exit because for some reason the app is staying alive
        System.exit(0);
        Log.d("MQTTService", "Service destroyed");
    }


}