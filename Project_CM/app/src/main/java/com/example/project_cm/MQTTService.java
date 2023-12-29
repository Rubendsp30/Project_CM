package com.example.project_cm;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.security.SecureRandom;

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
                    "200",
                    "MQTT Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "200")
                .setContentTitle("MQTT Service")
                .setContentText("Sending MQTT message")
                .setSmallIcon(R.drawable.ic_launcher_background) // Replace with your app's icon
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        return builder.build();
    }



    private void sendMQTTMessage() {
        Log.e("MQTT Widget", "Entered sendMqttMessage");

        String CHAR_LOWER = "abcdefghijklmnopqrstuvwxyz";
        String CHAR_UPPER = CHAR_LOWER.toUpperCase();
        String NUMBER = "0123456789";
        String DATA_FOR_RANDOM_STRING = CHAR_LOWER + CHAR_UPPER + NUMBER;
        SecureRandom random = new SecureRandom();

        StringBuilder sb = new StringBuilder(20);
        for (int i = 0; i < 18; i++) {
            int rndCharAt = random.nextInt(DATA_FOR_RANDOM_STRING.length());
            char rndChar = DATA_FOR_RANDOM_STRING.charAt(rndCharAt);
            sb.append(rndChar);
        }
        mqttHelper = MQTTHelper.getInstance(this, sb.toString());


            // Connect and set a callback to publish when connected
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
                    publishMessage();
                }

                // Implement other callback methods if needed
            });
            mqttHelper.connect();

    }

    private void publishMessage() {
        String topic = "/project/treat/rcL9kl2gSYbLusKe4N";
        String message = "30";
        Log.d("MQTT Widget", "Sending Message");
        try {
            mqttHelper.publishToTopic(topic, message, 2);
            Log.d("MQTT Widget", "MQTT message sent: " + message + " to topic: " + topic);
        } catch (Exception e) {
            Log.e("MQTT Widget", "Error sending MQTT message: " + e.getMessage());
        }
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