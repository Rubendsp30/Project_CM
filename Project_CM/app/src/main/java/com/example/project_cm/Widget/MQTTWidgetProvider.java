package com.example.project_cm.Widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.RemoteViews;

import androidx.core.content.ContextCompat;

import com.example.project_cm.Activities.LoginActivity;
import com.example.project_cm.R;

public class MQTTWidgetProvider extends AppWidgetProvider {

    private static final String ACTION_SEND_MQTT_MESSAGE = "ACTION_SEND_MQTT_MESSAGE";

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
        views.setImageViewResource(R.id.widget_button, R.drawable.treat_button);


        // Intent to handle button click on the widget
        Intent intent = new Intent(context, MQTTWidgetProvider.class);
        intent.setAction(ACTION_SEND_MQTT_MESSAGE);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        views.setOnClickPendingIntent(R.id.widget_button, pendingIntent);

        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // Update all widgets
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        // Check if the action is to send MQTT message
        if (ACTION_SEND_MQTT_MESSAGE.equals(intent.getAction())) {
            if (isUserLoggedIn(context)) {
                // Start the MQTTService if user is logged in
                Intent serviceIntent = new Intent(context, MQTTService.class);
                ContextCompat.startForegroundService(context, serviceIntent);
            } else {
                // Open the login activity if no user is logged in
                Intent loginIntent = new Intent(context, LoginActivity.class);
                loginIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(loginIntent);
            }
        }
    }

    private boolean isUserLoggedIn(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        return sharedPreferences.getString("loggedInUserId", null) != null;
    }
}
