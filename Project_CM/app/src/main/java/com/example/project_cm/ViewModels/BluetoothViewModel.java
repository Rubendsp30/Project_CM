package com.example.project_cm.ViewModels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class BluetoothViewModel extends ViewModel {

    private BluetoothSocket socket;
    private OutputStream outputStream;
    private BluetoothMessageListener messageListener;

    private final MutableLiveData<Boolean> passwordErrorLiveData = new MutableLiveData<>();

    public LiveData<Boolean> getPasswordError() {
        return passwordErrorLiveData;
    }

    public void setPasswordError(boolean hasError) {
        passwordErrorLiveData.postValue(hasError);
    }


    public void setSocket(BluetoothSocket socket) {
        this.socket = socket;
        try {
            outputStream = socket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(String message) {
        if (outputStream != null) {
            try {
                outputStream.write(message.getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public interface BluetoothMessageListener {
        void onMessageReceived(String message);
    }

    public void setMessageListener(BluetoothMessageListener listener) {
        this.messageListener = listener;
    }

    // Call this method to start listening for messages
    public void startListeningForMessages() {
        new Thread(() -> {
            try {
                InputStream inputStream = socket.getInputStream();
                byte[] buffer = new byte[1024];
                int bytes;

                while (true) {
                    // Read from the InputStream
                    bytes = inputStream.read(buffer);
                    if (bytes > 0) {
                        // Send the obtained bytes to the UI
                        final String readMessage = new String(buffer, 0, bytes);
                        if (messageListener != null) {
                            messageListener.onMessageReceived(readMessage);
                        }
                    }
                }
            } catch (IOException e) {
            }
        }).start();
    }
    public void disconnectBluetooth() {
        try {
            if (outputStream != null) {
                outputStream.close();
                outputStream = null;
            }
            if (socket != null) {
                socket.close();
                socket = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void onCleared() {
        super.onCleared();
        try {
            if (outputStream != null) {
                outputStream.close();
            }
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}