package com.example.project_cm.ViewModels;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.project_cm.Callbacks.AuthCallback;
import com.example.project_cm.Callbacks.EmailCheckCallback;
import com.example.project_cm.User;
import com.example.project_cm.Callbacks.UsernameCheckCallback;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UserViewModel extends ViewModel {

    private FirebaseFirestore firestore;
    private final MutableLiveData<User> currentUser = new MutableLiveData<>();
    private static final String USERS_COLLECTION = "USERS";
    private final ExecutorService networkExecutor = Executors.newSingleThreadExecutor();
    private final Handler uiHandler = new Handler(Looper.getMainLooper());

    public UserViewModel() {
        try {
            firestore = FirebaseFirestore.getInstance();

        } catch (Exception e) {
            Log.e("UserViewModel", "Error initializing UserViewModel: " + e.getMessage());
        }
    }

    public void registerUser(User user) {

        networkExecutor.execute(() -> {
            DocumentReference documentReference;
            documentReference = firestore.collection(USERS_COLLECTION).document();
            String newUserID = documentReference.getId();
            user.setUserID(newUserID);

            documentReference.set(user).addOnSuccessListener(aVoid -> {

                    })
                    .addOnFailureListener(e -> {
                        Log.e("registerUser", "Failed to register user: " + e.getMessage());
                    });
        });
    }

    public void checkUsernameExists(String username, UsernameCheckCallback callback) {
        networkExecutor.execute(() -> {
            firestore.collection(USERS_COLLECTION)
                    .whereEqualTo("username", username)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        // If the query finds any documents, then the username already exists
                        boolean exists = !queryDocumentSnapshots.isEmpty();
                        uiHandler.post(() -> callback.onUsernameChecked(exists));
                    })
                    .addOnFailureListener(e -> {
                        Log.e("checkUsernameExists", "Error checking if username exists: " + e.getMessage());
                        uiHandler.post(() -> callback.onUsernameChecked(false));
                    });
        });
    }

    public void checkEmailExists(String email, EmailCheckCallback callback) {
        networkExecutor.execute(() -> {
            firestore.collection(USERS_COLLECTION)
                    .whereEqualTo("email", email)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        // If the query finds any documents, then the email already exists
                        boolean exists = !queryDocumentSnapshots.isEmpty();
                        uiHandler.post(() -> callback.onEmailChecked(exists));
                    })
                    .addOnFailureListener(e -> {
                        Log.e("checkEmailExists", "Error checking if email exists: " + e.getMessage());
                        uiHandler.post(() -> callback.onEmailChecked(false));
                    });
        });
    }


    public void authenticateUser(String username, String password, AuthCallback callback) {
        networkExecutor.execute(() -> {
            firestore.collection(USERS_COLLECTION)
                    .whereEqualTo("username", username)
                    .whereEqualTo("password", password)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        boolean isAuthenticated = !queryDocumentSnapshots.isEmpty();
                        String userId;
                        if (isAuthenticated) {
                            userId = queryDocumentSnapshots.getDocuments().get(0).getId();
                        } else {
                            userId = null;
                        }
                        uiHandler.post(() -> callback.onAuthCompleted(isAuthenticated, userId));
                    })
                    .addOnFailureListener(e -> {
                        Log.e("authenticateUser", "Error authenticating user: " + e.getMessage());
                        uiHandler.post(() -> callback.onAuthCompleted(false, null));
                    });
        });
    }

    // Getter for currentUser
    public MutableLiveData<User> getCurrentUser() {
        return currentUser;
    }

    // Method to set the current user
    public void setCurrentUser(User user) {
        currentUser.setValue(user);
    }


}
