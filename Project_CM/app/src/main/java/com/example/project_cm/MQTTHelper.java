package com.example.project_cm;

import android.content.Context;
import android.util.Log;

import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.nio.charset.StandardCharsets;
import java.util.List;

import info.mqtt.android.service.Ack;
import info.mqtt.android.service.MqttAndroidClient;


public class MQTTHelper {
    public MqttAndroidClient mqttAndroidClient;
    private static MQTTHelper instance;

    //final String server = "tcp://2.80.198.184:1883";
    final String server = "tcp://broker.hivemq.com:1883";
    final String TAG = "MQTT";
    private final String name;
    private List<String> deviceIds;


    public void setDeviceIds(List<String> deviceIds) {
        this.deviceIds = deviceIds;
    }


    public MQTTHelper(Context context, String name) {
        this.name = name;

        mqttAndroidClient = new MqttAndroidClient(context, server, name, Ack.AUTO_ACK);
    }

    public static synchronized MQTTHelper getInstance(Context context, String name) {
        if (instance == null) {
            instance = new MQTTHelper(context, name);
        }
        return instance;
    }

    public void setCallback(MqttCallbackExtended callback) {
        mqttAndroidClient.setCallback(callback);
    }

    public void connect() {
        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setAutomaticReconnect(true);
        mqttConnectOptions.setCleanSession(true);

        mqttAndroidClient.connect(mqttConnectOptions, null, new IMqttActionListener() {
            @Override
            public void onSuccess(IMqttToken asyncActionToken) {

                //Adjusting the set of options that govern the behaviour of Offline (or Disconnected) buffering of messages
                DisconnectedBufferOptions disconnectedBufferOptions = new DisconnectedBufferOptions();
                disconnectedBufferOptions.setBufferEnabled(true);
                disconnectedBufferOptions.setBufferSize(100);
                disconnectedBufferOptions.setPersistBuffer(false);
                disconnectedBufferOptions.setDeleteOldestMessages(false);
                mqttAndroidClient.setBufferOpts(disconnectedBufferOptions);

                Log.w(TAG, "Connected to: " + server);
                subscribeToDeviceTopics();
            }

            @Override
            public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                Log.w(TAG, "Failed to connect to: " + server + " " + exception.toString());
            }
        });


    }

    private void subscribeToDeviceTopics() {
        if (deviceIds != null) {
            for (String deviceId : deviceIds) {
                subscribeToDeviceTopic(deviceId);
            }
        }
    }

    public void stop() {
        Log.e("MQTT", "Stoping MQTT");
        instance=null;
        mqttAndroidClient.unregisterResources();
        mqttAndroidClient.disconnect();
        Log.e("MQTT", "Stopped MQTT");

    }

    public void subscribeToDeviceTopic(String deviceId) {
        Log.e("MQTT", "Subscribing");

        String treatTopic = "/project/treat/" + deviceId;
        subscribeToTopic(treatTopic);

        String treatAnswerTopic = "/project/treatAnswer/" + deviceId;
        subscribeToTopic(treatAnswerTopic);
    }


    public void subscribeToTopic(String topic) {
        mqttAndroidClient.subscribe(topic, 0, null, new IMqttActionListener() {
            @Override
            public void onSuccess(IMqttToken asyncActionToken) {
                Log.w(TAG, "Subscribed to: " + topic);
            }

            @Override
            public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                Log.w(TAG, "Subscribed fail!");
            }
        });

    }

    public void unsubscribeToDeviceTopics(String deviceId) {
        Log.e("MQTT", "Unsubscribing");

        String treatTopic = "/project/treat/" + deviceId;
        unsubscribeToTopic(treatTopic);

        String treatAnswerTopic = "/project/treatAnswer/" + deviceId;
        unsubscribeToTopic(treatAnswerTopic);
    }


    public void unsubscribeToTopic(String topic) {
        mqttAndroidClient.unsubscribe(topic, null, new IMqttActionListener() {
            @Override
            public void onSuccess(IMqttToken asyncActionToken) {
                Log.w(TAG, "Unsubscribed!");
            }

            @Override
            public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                Log.w(TAG, "Unsubscribed fail!");
            }
        });

    }

    public void publishToTopic(String topic, String msg, int qos) {
        byte[] encodedPayload = msg.getBytes(StandardCharsets.UTF_8);
        MqttMessage message = new MqttMessage(encodedPayload);
        message.setQos(qos);

        mqttAndroidClient.publish(topic, message);
    }

    public String getName() {
        return name;
    }
}
