package com.example.project_cm.Activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.project_cm.FragmentChangeListener;
import com.example.project_cm.Fragments.DeviceSetup.DevSetupInitial;
import com.example.project_cm.Fragments.HomeScreenFragment;
import com.example.project_cm.MQTTHelper;
import com.example.project_cm.R;
import com.example.project_cm.User;
import com.example.project_cm.ViewModels.UserViewModel;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity implements FragmentChangeListener {
    private static final String USERS_COLLECTION = "USERS";
    private static final String DEVICES_COLLECTION = "DEVICES";
    private MQTTHelper mqttHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        setContentView(R.layout.activity_home);

        String userId = getLoggedInUserId();
        if (userId != null) {
            FirebaseFirestore firestore = FirebaseFirestore.getInstance();

            firestore.collection(USERS_COLLECTION)
                    .document(userId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        User user = documentSnapshot.toObject(User.class);
                        UserViewModel userViewModel = new ViewModelProvider(this).get(UserViewModel.class);
                        userViewModel.setCurrentUser(user);

                        mqttHelper = setupMqtt();

                        checkUserHasDevice(firestore, userId, this);
                    })
                    .addOnFailureListener(e -> Log.e("HomeActivity", "Error fetching user details: " + e.getMessage()));
        }
        else{
            redirectToLogin();
        }

    }

    private String getLoggedInUserId() {
        SharedPreferences sharedPreferences = getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        return sharedPreferences.getString("loggedInUserId", null);
    }

    private MQTTHelper setupMqtt() {
        mqttHelper = MQTTHelper.getInstance(this,"CMProjectPetDoDesespero");

        mqttHelper.setCallback(new MqttCallbackExtended() {

            @Override
            public void connectComplete(boolean reconnect, String serverURI) {
                Log.e("MQTT", "CONNECTED: " + serverURI);
            }

            @Override
            public void connectionLost(Throwable cause) {
                Log.d("MQTT", "CONNECTION LOST");
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) {
                Log.d("MQTT", "Message arrived on topic: " + topic + " Message: " + new String(message.getPayload()));
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                Log.d("MQTT", "DELIVERY COMPLETED");
            }
        });

        mqttHelper.connect();
        return mqttHelper;
    }


    private void checkUserHasDevice(FirebaseFirestore firestore, String userId, AppCompatActivity activity) {
        Query query = firestore.collection(DEVICES_COLLECTION).whereEqualTo("user_id", userId);
        query.get().addOnCompleteListener(task -> {
            if (isActivityActive(activity)) {
                if (task.isSuccessful() && !task.getResult().isEmpty()) {
                    List<String> deviceIds = new ArrayList<>();
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        deviceIds.add(document.getId()); // Assuming the document ID is the device ID
                    }
                    mqttHelper.setDeviceIds(deviceIds);
                    // User has a device, load HomeScreenFragment
                    loadFragment(new HomeScreenFragment(), "home_screen");
                } else {
                    // User does not have a device or DEVICES collection does not exist
                    loadFragment(new DevSetupInitial(), "device_setup_initial");
                }
            }
        }).addOnFailureListener(e -> {
            if (isActivityActive(activity)) {
                //loadFragment(new DevSetupInitial(), "device_setup_initial");
            }
        });
    }

    private boolean isActivityActive(AppCompatActivity activity) {
        return activity != null && !activity.isFinishing() && !activity.isDestroyed();
    }

    private void redirectToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    public void replaceFragment(Fragment fragment) {
        if (fragment != null) {
            loadFragment(fragment, fragment.toString());
        }
    }

    private void loadFragment(Fragment fragment, String tag) {
        // Load the specified fragment into the fragment container view
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainerView, fragment, tag)
                    .commit();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Disconnect MQTT
        if (mqttHelper != null) {
            mqttHelper.stop();
        }
    }

}