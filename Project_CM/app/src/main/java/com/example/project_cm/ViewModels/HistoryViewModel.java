package com.example.project_cm.ViewModels;

import android.app.Application;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.project_cm.DataBase.AppDatabase;
import com.example.project_cm.DataBase.Tables.VaccineEntity;
import com.example.project_cm.History;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.MemoryCacheSettings;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HistoryViewModel extends AndroidViewModel {

    private FirebaseFirestore firestore;
    private final MutableLiveData<List<History>> historyLiveData = new MutableLiveData<>();
    private final ExecutorService networkExecutor = Executors.newSingleThreadExecutor();
    private final Handler uiHandler = new Handler(Looper.getMainLooper());
    private static final String HISTORY_COLLECTION = "history";
    private boolean isNetworkAvailable;
    private String loggedUser;

    public interface InsertCallback {
        void onInsertCompleted(long vaccineId);
    }

    public HistoryViewModel(@NonNull Application application) {
        super(application);

        try {
            firestore = FirebaseFirestore.getInstance();
            // Disable Firestore cache
            FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                    .setLocalCacheSettings(MemoryCacheSettings.newBuilder().build())
                    .build();
            firestore.setFirestoreSettings(settings);
        } catch (Exception e) {
            Log.e("HistoryViewModel", "Error initializing HistoryViewModel: " + e.getMessage());
        }
    }
/*
    public void setNetworkAvailable(boolean isNetworkAvailable, Context context) throws IOException, ClassNotFoundException {
        this.isNetworkAvailable = isNetworkAvailable;
        if (isNetworkAvailable) {
            uploadFromFile(context);
            Log.e("NotesViewModel", "Network is available");
        } else {
            Log.e("NotesViewModel", "Network is not available");
        }
    }

 */
}
