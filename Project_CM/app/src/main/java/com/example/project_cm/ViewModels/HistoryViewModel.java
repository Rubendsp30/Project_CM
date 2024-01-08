package com.example.project_cm.ViewModels;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.project_cm.History;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HistoryViewModel extends AndroidViewModel {

    private FirebaseFirestore firestore;
    private static final String HISTORY_COLLECTION = "MEALS_HISTORY";
    private static final String DEVICES_COLLECTION = "DEVICES";
    private final ExecutorService networkExecutor = Executors.newSingleThreadExecutor();
    private final Handler uiHandler = new Handler(Looper.getMainLooper());

    public HistoryViewModel(@NonNull Application application) {
        super(application);

        try {
            firestore = FirebaseFirestore.getInstance();
            // Disable Firestore cache
            /*FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                    .setLocalCacheSettings(MemoryCacheSettings.newBuilder().build())
                    .build();
            firestore.setFirestoreSettings(settings);*/
        } catch (Exception e) {
            Log.e("HistoryViewModel", "Error initializing HistoryViewModel: " + e.getMessage());
        }
    }

    public LiveData<ArrayList<History>> getHistoryMeals(String currentDeviceId) {
        MutableLiveData<ArrayList<History>> historyLiveData = new MutableLiveData<>();
        Date sevenDaysAgo = getSevenDaysAgoDate();
        Timestamp sevenDaysAgoTimestamp = new Timestamp(sevenDaysAgo);

        networkExecutor.execute(() -> {
            firestore.collection(DEVICES_COLLECTION)
                    .document(currentDeviceId)
                    .collection(HISTORY_COLLECTION)
                    .whereGreaterThanOrEqualTo("meal_time", sevenDaysAgoTimestamp)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        ArrayList<History> history = new ArrayList<>();
                        for (DocumentSnapshot snapshot : queryDocumentSnapshots.getDocuments()) {
                            Timestamp mealTimeTimestamp = snapshot.getTimestamp("meal_time");
                            Long quantityServedLong = snapshot.getLong("quantity_served");
                            int quantityServed = quantityServedLong != null ? quantityServedLong.intValue() : 0;

                            // Converte o Timestamp para Date
                            Date mealTimeDate = mealTimeTimestamp != null ? mealTimeTimestamp.toDate() : null;

                            // Cria um novo objeto History
                            History history_meal = new History(mealTimeDate, quantityServed);

                            history.add(history_meal);
                        }
                        uiHandler.post(() -> historyLiveData.setValue(history));
                    })
                    .addOnFailureListener(e -> Log.e("getHistoryMealsByPetProfileId", "Error getting meals_history for pet: " + e.getMessage()));
        });

        return historyLiveData;
    }

    public void deleteOldMealHistories (String currentDeviceId) {
        Date sevenDaysAgo = getSevenDaysAgoDate();

        networkExecutor.execute(() -> {
            firestore.collection(DEVICES_COLLECTION)
                    .document(currentDeviceId)
                    .collection(HISTORY_COLLECTION)
                    .whereLessThan("meal_time", sevenDaysAgo)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        for (DocumentSnapshot snapshot : queryDocumentSnapshots.getDocuments()) {
                            snapshot.getReference().delete()
                                    .addOnSuccessListener(aVoid -> Log.d("deleteOldMealHistories", "Old meal history successfully deleted"))
                                    .addOnFailureListener(e -> Log.e("deleteOldMealHistories", "Error deleting old meal history: " + e.getMessage()));
                        }
                    })
                    .addOnFailureListener(e -> Log.e("deleteOldMealHistories", "Error getting old meals_history for pet: " + e.getMessage()));
        });
    }

    private Date getSevenDaysAgoDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -7);
        return calendar.getTime();
    }
}
