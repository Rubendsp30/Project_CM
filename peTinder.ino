#include "BluetoothSerial.h"
#include <WiFi.h>
#include <Preferences.h>
#include <PubSubClient.h>
BluetoothSerial serialBT;
Preferences preferences;

String incomingData = "";
String ssid = "";
String password = "";
bool isBtOn;

// Define random ID
String ID_MQTT;
char *letters = "abcdefghijklmnopqrstuvwxyz0123456789";

// Define MQTT Topics
#define TOPIC_TEST "/project/pet"
#define  TOPIC_TREAT "/project/treat/GMCFo711FUaakBzcQ5Px"
#define  TOPIC_TREAT_ANSWER "/project/treatAnswer/GMCFo711FUaakBzcQ5Px"


// Define MQTT Broker and PORT
const char *BROKER_MQTT = "broker.hivemq.com";
int BROKER_PORT = 1883;

// Global variables
WiFiClient espClient;
PubSubClient MQTT(espClient); 

// Declarations
void startWifi(void);
void initMQTT(void);
void callbackMQTT(char *topic, byte *payload, unsigned int length);
void reconnectMQTT(void);
void reconnectWiFi(void);
void checkWiFIAndMQTT(void);


// Starts the Wi-Fi
/*###########################################################################
################### STARTWIFI ###########################################*/

void startWifi(void) {
  reconnectWiFi();
}
/*################# END STARTWIFI ###########################################
#############################################################################*/


// Starts everything from MQTT
/*###########################################################################
################### INITMQTT ###########################################*/

void initMQTT(void) {
  MQTT.setServer(BROKER_MQTT, BROKER_PORT);
  MQTT.setCallback(callbackMQTT);
}
/*################# END INITMQTT ###########################################
#############################################################################*/


// Callback from Android 
// --- Get the messages here
/*###########################################################################
################### CALLBACKMQTT ###########################################*/

void callbackMQTT(char *topic, byte *payload, unsigned int length) {
  String msg;
  String topicStr = String(topic); // Convert char* to String

  // Convert payload to string
  for (int i = 0; i < length; i++) {
    char c = (char)payload[i];
    msg += c;
  }

  Serial.printf("Topic: %s\n", topic);
  Serial.printf("Message: %s\n", msg.c_str());

  // Check if the message is on the treat topic
  if (topicStr.equals(TOPIC_TREAT)) {
    MQTT.publish(TOPIC_TREAT_ANSWER, "true");
  }
}

/*################# END CALLBACKMQTT ###########################################
#############################################################################*/

// Connects to the Broker with a specific random ID
/*###########################################################################
################### RECONNECTMQTT ###########################################*/

void reconnectMQTT(void) {
  while (!MQTT.connected()) {
    ID_MQTT = "";
    Serial.print("* Starting connection with broker: ");
    Serial.println(BROKER_MQTT);

    int i = 0;
    for (i = 0; i < 10; i++) {
      ID_MQTT = ID_MQTT + letters[random(0, 36)];
    }
/*################# END RECONNECTMQTT ###########################################
#############################################################################*/


    if (MQTT.connect(ID_MQTT.c_str())) {
      Serial.print("* Connected to broker successfully with ID: ");
      Serial.println(ID_MQTT);
      MQTT.subscribe(TOPIC_TREAT);
    } else {
      Serial.println("* Failed to connected to broker. Trying again in 2 seconds.");
      delay(2000);
    }
  }
}

// Checks both Wi-Fi and MQTT state, and reconnects if something failed.
/*###########################################################################
################### CHECKWIFIANDMQTT ###########################################*/

void checkWiFIAndMQTT(void) {
  if (!MQTT.connected())
    reconnectMQTT();
  reconnectWiFi();
}
/*################# END CHECKWIFIANDMQTT ###########################################
#############################################################################*/


/*###########################################################################
################### RECONNECTWIFI ###########################################*/

void reconnectWiFi(void) {
  if (WiFi.status() == WL_CONNECTED)
    return;

  WiFi.begin(ssid, password); // Conecta na rede WI-FI

  Serial.print("* Connecting to Wifi ");
  while (WiFi.status() != WL_CONNECTED) {
    delay(100);
    Serial.print(".");
  }
/*################# END RECONNECTWIFI ###########################################
#############################################################################*/


  Serial.println("");
  Serial.print("* Successfully connected to Wi-Fi, with local IP: ");
  Serial.println(WiFi.localIP());
}

/*###########################################################################
################### SETUP ###########################################*/

void setup() {
  Serial.begin(115200);

  preferences.begin("wifi", false);

  ssid = preferences.getString("ssid", "");
  password = preferences.getString("password", ""); 
  String storedValue = preferences.getString("isBtOn", "true"); // Default to "true" if not set
  isBtOn = (storedValue == "true");
  initMQTT();

  if (ssid != "" && password != "") {
    startWifi();
    return;
  }
/*################# END SETUP ###########################################
#############################################################################*/


  serialBT.begin("ESP32-BT-5XzQ3NpJU7mVbTw9Hc4F");
}

/*###########################################################################
################### LOOP ###########################################*/

void loop() {
  while (serialBT.available()) {
    char receivedChar = serialBT.read();
    incomingData += receivedChar; 
    if (receivedChar == '\n') {
      parseIncomingData(incomingData);
      incomingData = "";
    }
/*################# END LOOP ###########################################
#############################################################################*/

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

 if (!isBtOn) {
  checkWiFIAndMQTT();
  MQTT.loop();
  }

  delay(20);
}

/*###########################################################################
################### PARSEINCOMINGDATA ###########################################*/

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

  setupWifi(ssid, password);
}
/*################# END PARSEINCOMINGDATA ###########################################
#############################################################################*/


/*###########################################################################
################### SETUPWIFI ###########################################*/
bool setupWifi(const String& ssid, const String& password) {
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
    isBtOn = false;
    String boolToString = isBtOn ? "true" : "false";
    preferences.putString("isBtOn", boolToString);
    serialBT.end();

    return true;
  } else {
    Serial.println("\nFailed to connect to Wi-Fi.");
     isBtOn = true;
     String boolToString = isBtOn ? "true" : "false";
    preferences.putString("isBtOn", boolToString);
    serialBT.println("FAILED");
    return false;
  }
}
/*###########################################################################
################### END SETUPWIFI ###########################################*/

/*###########################################################################
################### CLEARWIFICREDENTIALS ###########################################*/

void clearWiFiCredentials() {
  preferences.begin("wifi", false); 
  preferences.clear();      
  isBtOn = true; 
  String boolToString = isBtOn ? "true" : "false";
  preferences.putString("isBtOn", boolToString);       
  serialBT.begin("ESP32-BT-5XzQ3NpJU7mVbTw9Hc4F");            
}
/*################# END CLEARWIFICREDENTIALS ###########################################
#############################################################################*/


/*###########################################################################
################### CLEANUP ###########################################*/

void cleanup() {
  preferences.end(); // Close the Preferences
}
/*################# END CLEANUP ###########################################
#############################################################################*/


