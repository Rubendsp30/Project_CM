/*###########################################################################
################### INCLUDES AND DEFINES ####################################*/

  //Loadcell
  #include "HX711.h"

  //Sensor de temperatura e humidade
  #include "DHT.h"

  //loadcell
  #define LOADCELL_DOUT_PIN  23
  #define LOADCELL_SCK_PIN  22

  // Sensor de temperatura e humidade
  #define DHTPIN 5
  #define DHTTYPE DHT22 

  #include "uRTCLib.h"  

  #define SDA_PIN 33
  #define SCL_PIN 32

  #include "BluetoothSerial.h"
  #include <WiFi.h>
  #include <Preferences.h>
  #include <PubSubClient.h>
  BluetoothSerial serialBT;
  Preferences preferences;

  //Bluetooth stuff
  String incomingData = "";
  String ssid = "";
  String password = "";
  bool isBtOn;

  // Define random ID
  String ID_MQTT;
  const char *letters = "abcdefghijklmnopqrstuvwxyz0123456789";

  // Define MQTT Topics
  #define TOPIC_TEST "/project/pet"

  // Define MQTT Broker and PORT
  const char *BROKER_MQTT = "broker.hivemq.com";
  int BROKER_PORT = 1883;

  // Global variables
  WiFiClient espClient;
  PubSubClient MQTT(espClient); 
  bool isWiFiConnected = false;

  


   // Define pin connections & motor's steps per revolution
  const int dirPin = 4;
  const int step = 2;
  const int stepsPerRevolution = 200;
  const int enable_motor = 15;
/*################# END INCLUDES AND DEFINES ################################
#############################################################################*/

/*###########################################################################
################### AUX FUNCTIONS FOR MQTT ######################################*/
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

      // Convert payload to string
      for (int i = 0; i < length; i++) {
        char c = (char)payload[i];
        msg += c;
      }
  /*################# END CALLBACKMQTT ###########################################
  #############################################################################*/


      Serial.printf("Topic: %s\n", topic);
      Serial.printf("Message: %s\n", msg, topic);

    }

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
       MQTT.subscribe(TOPIC_TEST);
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

/*################# END AUX FUNCTIONS FOR MQTT ################################
#############################################################################*/



/*###########################################################################
################### VARIABLES OR FIXED OF FEEDER ######################################*/
  // Scale
  HX711 scale;

  uRTCLib rtc(0x68);

  char daysOfTheWeek[7][12] = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};

  // Scale Calbration
  float calibration_factor = -115; //Isso foi testando coisa
  // Himudity and temperature 
  DHT dht(DHTPIN, DHTTYPE);




  
/*################# END VARIABLES OR FIXED OF FEEDER ##################################
#############################################################################*/

/*###########################################################################
################### MOTOR ######################################*/

  void stepper(float Screw_turns, int motorpin, bool direction) {

    int speed = 600;  
    int full_turn = 10000;

    int steps = Screw_turns * full_turn;  // get how many steps

    digitalWrite(enable_motor, LOW);
    digitalWrite(dirPin, direction);

    for (int x = 0; x < steps; x++) {
      digitalWrite(motorpin, HIGH);
      delayMicroseconds(speed);
      digitalWrite(motorpin, LOW);
      delayMicroseconds(speed);
    }
    digitalWrite(enable_motor, HIGH);
  }

  void feed(float cal,int amount) { 
  // float turns = 50 / cal;  //  to get how many turns to get 50g
    float turns = 50 / cal;
    for (int i = 0; i <= amount; i = i + 50) {
      stepper(turns, step, HIGH);  //1 turn = 4000 steps = 103g
      stepper(0.2, step, LOW);    //spins backwards chug control  
    }
    digitalWrite(enable_motor, HIGH);
  }

/*################# END MOTOR ##################################
#############################################################################*/


/*###########################################################################
################### SETUP ######################################*/
  void setup() {
  Serial.begin(115200);

  //Mqtt stuff
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

  serialBT.begin("ESP32-BT-5XzQ3NpJU7mVbTw9Hc4F");

  //Feeder stuff
  dht.begin();
  // Setup Serial connection
  Wire.begin(SDA_PIN, SCL_PIN);
	URTCLIB_WIRE.begin();

	//rtc.set(0, 13, 13, 3, 19, 12, 23);
	//  RTCLib::set(byte second, byte minute, byte hour, byte dayOfWeek, byte dayOfMonth, byte month, byte year)
  
  scale.begin(LOADCELL_DOUT_PIN, LOADCELL_SCK_PIN);
  scale.set_scale();
  scale.tare(); //Reset the scale to 0
  
  long zero_factor = scale.read_average(); //Get a baseline reading


	// Declare pins as Outputs
	pinMode(step, OUTPUT);
	pinMode(dirPin, OUTPUT); 
  pinMode(enable_motor, OUTPUT);
  }
/*################# END SETUP ##################################
#############################################################################*/

/*###########################################################################
################### LOOP ######################################*/
  void loop() {
    while (serialBT.available()) {
        char receivedChar = serialBT.read();
        incomingData += receivedChar; 
        if (receivedChar == '\n') {
          parseIncomingData(incomingData);
          incomingData = "";
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

    delay(200);

    if (isWiFiConnected) {

    scale.set_scale(calibration_factor); //Adjust to this calibration factor
    Serial.print("Reading: ");
    Serial.print(scale.get_units(), 1);
    Serial.print(" g"); //Change this to kg and re-adjust the calibration factor if you follow SI units like a sane person
    Serial.println();

    float h = dht.readHumidity();
    float t = dht.readTemperature();

    float hic = dht.computeHeatIndex(t, h, false);
    Serial.print(F("Humidity: "));
    Serial.print(h);
    Serial.print(F("%  Temperature: "));
    Serial.print(t);
    Serial.print(F("°C "));

    // Wait one second before repeating
    delay (1000);

  // TODO isso aqui foi pra testar quantos steps o motor dá e ele soltava aproximadamente 30g a cada volta
  // TODO criar a função que vai ser chamada no android, a cada volta ele dá 30g, então dependendo da quantidade
  // inserida vai dar mais ou menos voltas
    delay(5000); // Wait a second
    stepper(1,step,HIGH);

    rtc.refresh();

    Serial.print("RTC DateTime: ");
    Serial.print(rtc.year());
    Serial.print('/');
    Serial.print(rtc.month());
    Serial.print('/');
    Serial.print(rtc.day());

    Serial.print(' ');

    Serial.print(rtc.hour());
    Serial.print(':');
    Serial.print(rtc.minute());
    Serial.print(':');
    Serial.print(rtc.second());

    Serial.print(" DOW: ");
    Serial.print(rtc.dayOfWeek());

    Serial.println();

    //delay(1000);
  }
  }
  }

/*################# END LOOP ##################################
#############################################################################*/

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
      isWiFiConnected = true;
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


