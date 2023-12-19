#include "BluetoothSerial.h"
#include <WiFi.h>
#include <Preferences.h>

BluetoothSerial serialBT;
Preferences preferences;

String incomingData = "";
String ssid = "";
String password = "";

void setup() {
  Serial.begin(115200);

  preferences.begin("wifi", false);

  ssid = preferences.getString("ssid", "");
  password = preferences.getString("password", ""); 

  if (ssid != "" && password != "") {
    if (connectToWiFi(ssid, password)) {
      return;
    }
  }

  serialBT.begin("ESP32-BT Woooo");
}

void loop() {
  if (ssid = "") {
  while (serialBT.available()) {
    char receivedChar = serialBT.read();
    incomingData += receivedChar; 
    if (receivedChar == '\n') {
      parseIncomingData(incomingData);
      incomingData = "";
    }
  }
  }

    if (Serial.available()) {
    char receivedChar = Serial.read();
    incomingData += receivedChar;
    if (receivedChar == '\n') {
      if (incomingData.startsWith("CLEAR")) {
        clearWiFiCredentials();
        Serial.println("WiFi credentials cleared.");
      } 
      incomingData = "";
    }
  }
  delay(20);
}

void parseIncomingData(String data) {
  // Assume the data format is "SSID:yourSSID;PASSWORD:yourPassword"
  int ssidStart = data.indexOf("SSID:") + 5;
  int ssidEnd = data.indexOf(";PASSWORD:");
  ssid = data.substring(ssidStart, ssidEnd);

  int passwordStart = ssidEnd + 10;
  password = data.substring(passwordStart);

  ssid.trim();
  password.trim();

  Serial.println("Received SSID: " + ssid);
  Serial.println("Received Password: " + password);

  connectToWiFi(ssid, password);
}

bool connectToWiFi(const String& ssid, const String& password) {
  Serial.println("Connecting to Wi-Fi...");
  WiFi.begin(ssid.c_str(), password.c_str());

  int attempts = 0;
  while (WiFi.status() != WL_CONNECTED && attempts < 30) {
    delay(500);
    Serial.print(".");
    attempts++;
  }

  if (WiFi.status() == WL_CONNECTED) {
    Serial.println("\nConnected to Wi-Fi.");
    Serial.print("IP Address: ");
    Serial.println(WiFi.localIP());
    serialBT.println("CONNECTED");

    preferences.putString("ssid", ssid);
    preferences.putString("password", password);
    serialBT.end();
    return true;
  } else {
    Serial.println("\nFailed to connect to Wi-Fi.");
    serialBT.println("FAILED");
    return false;
  }
}

void clearWiFiCredentials() {
  preferences.begin("wifi", false); 
  preferences.clear();              
  preferences.end(); 
  ssid=""; 
  serialBT.begin("ESP32-BT Woooo");            
}

void cleanup() {
  preferences.end(); // Close the Preferences
}

