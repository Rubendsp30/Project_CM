//////////////////////////////////////////////
// Include Wifi and communication libraries //
//////////////////////////////////////////////
  #include "BluetoothSerial.h"
  #include <Preferences.h>
  #include <WiFi.h>
  #include <PubSubClient.h>
  #include <Firebase_ESP_Client.h>
  #include <Adafruit_Sensor.h>
  #include <ArduinoJson.h>

  //Initialize Bluetooth
  BluetoothSerial serialBT;
  Preferences preferences;

  // Initialize Firebase
  FirebaseData firebaseData;
  FirebaseAuth auth;
  FirebaseConfig config;

  //Wifi stuff
  String ssid = "";
  String password = "";

  //BLuetooth Stuff
  String incomingData = "";
  bool isBtOn;
  #define  BT_NAME "ESP32-BT-7fHkT29BzXpLqV4JsNwE"

  // Define random ID
  String ID_MQTT;
  char * letters = "abcdefghijklmnopqrstuvwxyz0123456789";

  // Define MQTT Topics
  #define TOPIC_TEST "/project/pet"
  #define TOPIC_TREAT "/project/treat/7fHkT29BzXpLqV4JsNwE"
  #define TOPIC_TREAT_ANSWER "/project/treatAnswer/7fHkT29BzXpLqV4JsNwE"
  #define TOPIC_UPDATE_MEALS "/project/updateMeals/7fHkT29BzXpLqV4JsNwE"
  #define TOPIC_TEMPERATURE "/project/temperature/7fHkT29BzXpLqV4JsNwE"
  #define TOPIC_HUMIDITY "/project/humidity/7fHkT29BzXpLqV4JsNwE"
  

  // Define Firebase credentials
  #define FIREBASE_PROJECT_ID "cm-project-pet"
  #define API_KEY "AIzaSyAhLyvKS0Fte6829SHSe9hmva2524gJBto"

  // Define Firebase paths
  #define MEAL_HISTORY_PATH "DEVICES/7fHkT29BzXpLqV4JsNwE/MEALS_HISTORY/"
  #define MEAL_SCHEDULES_PATH "DEVICES/7fHkT29BzXpLqV4JsNwE/MEAL_SCHEDULES/"
  #define DELETE_MEAL_PATH "DEVICES/7fHkT29BzXpLqV4JsNwE/MEAL_SCHEDULES/"
  #define DEVICE_PATH "DEVICES/7fHkT29BzXpLqV4JsNwE"

  // Define MQTT Broker and PORT
  const char * BROKER_MQTT = "broker.hivemq.com";
  int BROKER_PORT = 1883;

  // Global variables
  WiFiClient espClient;
  PubSubClient MQTT(espClient);
  bool isWiFiConnected = false;
  unsigned long lastMealCheckMillis = 0;
  const long mealCheckInterval = 60000;
  float cal = 20;
  unsigned long lastSendTime = 0;
  const long threeHoursInMillis = 3 * 60 * 60 * 1000;

//////////////////////////////////////////////////
// End Include Wifi and communication libraries //
//////////////////////////////////////////////////

//////////////////////////////////////////////
//     Equipment includes and defines       //
//////////////////////////////////////////////

    //Loadcell
    #include "HX711.h"

    //Sensor de temperatura e humidade
    #include "DHT.h"

    //loadcell
    #define LOADCELL_DOUT_PIN 23
    #define LOADCELL_SCK_PIN 22

    // Sensor de temperatura e humidade
    #define DHTPIN 5
    #define DHTTYPE DHT22

    #include "uRTCLib.h"

    #define SDA_PIN 33
    #define SCL_PIN 32

    // Define pin connections & motor's steps per revolution
    const int dirPin = 4;
    const int step = 2;
    const int stepsPerRevolution = 200;
    const int enable_motor = 15;



//////////////////////////////////////////////
//   End Equipment includes and defines     //
//////////////////////////////////////////////

//////////////////////////////////////////////
//      Variables and fixed of feeder       //
//////////////////////////////////////////////
    // Scale
    HX711 scale;

    uRTCLib rtc(0x68);

    char daysOfTheWeek[7][12] = {
      "Sunday",
      "Monday",
      "Tuesday",
      "Wednesday",
      "Thursday",
      "Friday",
      "Saturday"
    };

    // Scale Calbration
    float calibration_factor = -115; //Isso foi testando coisa. Tinhas -115, mudei para 0.4200983 no wokwi

    // Humidity and temperature 
    DHT dht(DHTPIN, DHTTYPE);

    // Global variable to store the schedule
    DynamicJsonDocument scheduleData(4096);

//////////////////////////////////////////////
//    End Variables and fixed of feeder     //
//////////////////////////////////////////////

//////////////////////////////////////////////
//        Aux functions for Firebase        //
//////////////////////////////////////////////

    // Generate Random ID to create doc for firebase
    String generateRandomID() {
      String randomID = "";
      char characters[] = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

      for (int i = 0; i < 20; i++) {
        int index = random(62);
        randomID += characters[index];
      }

      return randomID;
    }

    // Prints the mealSchedule Data stored on the esp
    void printScheduleData() {
      if (!scheduleData.isNull()) {
        serializeJsonPretty(scheduleData, Serial);
        Serial.println();
      } else {
        Serial.println("Schedule data is empty or not initialized");
      }
    }

    // Creates a new doc to store in a better way the meal schedules 
    void processMealSchedules(const String & firestoreData) {
      DynamicJsonDocument doc(4096); // Adjust size based on data size
      deserializeJson(doc, firestoreData);

      JsonObject root = doc.as < JsonObject > ();
      JsonArray documents = root["documents"];

      DynamicJsonDocument scheduleDoc(4096);
      JsonObject repeating = scheduleDoc.createNestedObject("repeating");
      JsonArray oneTime = scheduleDoc.createNestedArray("oneTime");

      for (JsonObject document: documents) {
        JsonObject fields = document["fields"];

        // Check if the schedule is active
        if (!fields["active"]["booleanValue"].as < bool > ()) {
          continue; // Skip this schedule as it is not active
        }

        String mealTime = fields["mealTime"]["timestampValue"].as < String > ();
        int portionSize = fields["portionSize"]["integerValue"].as < int > ();
        String scheduleId = fields["mealScheduleId"]["stringValue"].as < String > ();
        JsonObject repeatDays = fields["repeatDays"]["mapValue"]["fields"].as < JsonObject > ();

        // Check if the schedule is repeating or one-time
        bool isRepeating = false;
        for (JsonPair day: repeatDays) {
          if (day.value()["booleanValue"].as < bool > ()) {
            isRepeating = true;
            // Add to repeating schedules
            if (!repeating.containsKey(day.key().c_str())) {
              repeating.createNestedArray(day.key().c_str());
            }
            JsonObject schedule = repeating[day.key().c_str()].createNestedObject();
            schedule["time"] = mealTime;
            schedule["portionSize"] = portionSize;
            schedule["id"] = scheduleId;
          }
        }

      if (!isRepeating) {
        // Add to one-time schedules
        JsonObject schedule = oneTime.createNestedObject();
        schedule["time"] = mealTime;
        schedule["portionSize"] = portionSize;
        schedule["id"] = scheduleId;
      }
    }

    String output;
    serializeJsonPretty(scheduleDoc, output);

    // Save the processed data to the global variable
    scheduleData.clear();
    scheduleData = scheduleDoc;
    doc.clear();
    scheduleDoc.clear();
    //printScheduleData();
    }

    //Update value on Firebase
    //todo, sem uso por enquanto mas é esta função q usamos para dar updates dos valores na firebase
    void updateValueInFirestore(int percentage) {
      // Define the document path
      String documentPath = DEVICE_PATH;

      // Prepare the data to update
      FirebaseJson json;

      // Set the new value for the foodSuply field
      json.set("fields/foodSuply/integerValue", percentage);

      // Update the document
      if (Firebase.Firestore.patchDocument( & firebaseData, FIREBASE_PROJECT_ID, "", documentPath, json.raw(), "foodSuply")) {
      // Serial.println(json.raw());
        Serial.println("Updated value successfully");
      } else {
        Serial.println("Failed to update value");
        //Serial.println(json.raw());
        Serial.println(firebaseData.errorReason());
      }
    }

    //Temperature
    void updateTemperatureInFirestore(float temperature) {
      // Define the document path
      String documentPath = DEVICE_PATH;

      // Prepare the data to update
      FirebaseJson json;

      // Set the new values for the temperature and humidity fields
      json.set("fields/sensor_temperature/doubleValue", temperature);
      

      // Update the temperature in the document
      if (Firebase.Firestore.patchDocument(&firebaseData, FIREBASE_PROJECT_ID, "", documentPath, json.raw(), "sensor_temperature")) {
        Serial.println("Updated temperature successfully");
      } else {
        Serial.println("Failed to update temperature");
        Serial.println(firebaseData.errorReason());
      }
    }

    void updateHumidityInFirestore(float humidity) {
      // Define the document path
      String documentPath = DEVICE_PATH;

      // Prepare the data to update
      FirebaseJson json;

      // Set the new values for the humidity field
      json.set("fields/sensor_humidity/doubleValue", humidity);

      // Update the humidity in the document
      if (Firebase.Firestore.patchDocument(&firebaseData, FIREBASE_PROJECT_ID, "", documentPath, json.raw(), "sensor_humidity")) {
        Serial.println("Updated humidity successfully");
      } else {
        Serial.println("Failed to update temperature");
        Serial.println(firebaseData.errorReason());
      }
    }

    //Creates a new doc that stores the meal in the fistory after giving the meal
    void writeMealHistoryToFirestore(int newValue) {
      // Create a document with a specific path
      String documentID = generateRandomID();
      String documentPath = MEAL_HISTORY_PATH + documentID;

      // Prepare data as a JSON object
      FirebaseJson json;
      json.set("fields/quantity_served/doubleValue", newValue);

      rtc.refresh();
      char timestamp[25];
      snprintf(timestamp, sizeof(timestamp), "%04d-%02d-%02dT%02d:%02d:%02dZ",
        rtc.year() + 2000, rtc.month(), rtc.day(), rtc.hour(), rtc.minute(), rtc.second());

      json.set("fields/meal_time/timestampValue", timestamp);

      // Write data
      if (Firebase.Firestore.createDocument( & firebaseData, FIREBASE_PROJECT_ID, "", documentPath, json.raw())) {
        Serial.println("Document created successfully");
      } else {
        Serial.println("Failed to create document");
        Serial.println(firebaseData.errorReason());
      }
    }

    //Gets all the meal schedules of the device from the firebase
    void readMealSchedulesFromFirestore() {
      String collectionPath = MEAL_SCHEDULES_PATH;

      // Fetch all documents in the collection
      if (Firebase.Firestore.listDocuments( & firebaseData, FIREBASE_PROJECT_ID, "", collectionPath.c_str(), 100, "", "", "", false)) {
        Serial.println("Fetched documents successfully");

        processMealSchedules(firebaseData.payload());
      } else {
        Serial.println("Failed to fetch documents");
        Serial.println(firebaseData.errorReason());
      }
    }

    void deleteDocumentFromFirestore(const String& documentId) {
        // Base path for meal schedules in Firestore
        String basePath = DELETE_MEAL_PATH;

        // Construct the full document path
        String documentPath = basePath + documentId;

        // Check if the documentId is not empty
        if (documentId.isEmpty()) {
            Serial.println("Document ID is empty. Cannot delete document.");
            return;
        }

        // Delete the document
        if (Firebase.Firestore.deleteDocument(&firebaseData, FIREBASE_PROJECT_ID, "", documentPath)) {
            Serial.println("Document deleted successfully: " + documentPath);
        } else {
            Serial.println("Failed to delete document: " + documentPath);
            Serial.println(firebaseData.errorReason());
        }
    }


//////////////////////////////////////////////
//      End Aux functions for Firebase      //
//////////////////////////////////////////////

//////////////////////////////////////////////
//                 Bluetooth                //
//////////////////////////////////////////////

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
        config.api_key = API_KEY;
        auth.user.email = "feederdevice@test.com";
        auth.user.password = "123456";
        Firebase.begin( & config, & auth);
        Firebase.reconnectWiFi(true);
        readMealSchedulesFromFirestore();
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

    void clearWiFiCredentials() {
      preferences.begin("wifi", false); 
      Serial.println("Clearing WiFi credentials...");
      preferences.clear();      
      Serial.println("Credentials cleared.");
      isBtOn = true; 
      String boolToString = isBtOn ? "true" : "false";
      preferences.putString("isBtOn", boolToString);   
      preferences.end(); 
      ESP.restart();     
      serialBT.begin(BT_NAME);
      Serial.println("Restarting ESP32...");       
    }

      //Cleanup
      void cleanup() {
        preferences.end(); // Close the Preferences
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

        setupWifi(ssid, password);
      }

//////////////////////////////////////////////
//               End Bluetooth              //
//////////////////////////////////////////////

//////////////////////////////////////////////
//                    Motor                 //
//////////////////////////////////////////////

  void stepper(float Screw_turns, int motorpin, bool direction) {
    int speed = 600;
    int full_turn = 10000;

    int steps = Screw_turns * full_turn; // get how many steps

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

  //void feed(float cal,int amount) { 
  void feed(float cal,int amount) {
    float turns = amount / cal;  //  to get how many turns to get the desired amount
    //float turns = 50 / 103; // todo, visto q disseste q 1 turn = 103g mudei a conta para calcular automaticamente
    for (int i = 0; i <= amount; i = i + amount) {
      stepper(turns, step, HIGH); //1 turn = 4000 steps = 103g
      stepper(0.2, step, LOW); //spins backwards chug control  
    }
      float h = dht.readHumidity();
      float t = dht.readTemperature();
      digitalWrite(enable_motor, HIGH);

     // int simulatedWeightReduction = amount; // Quantidade de comida dispensada
      int currentWeight = scale.get_units(); // Peso atual
     // int newSimulatedWeight = currentWeight - simulatedWeightReduction;

      int totalCapacity = 1230; // Capacidade total em gramas
      int percentageRemaining = (currentWeight * 100) / totalCapacity;
      Serial.print("Valor sendo enviado para a Firebase: ");
      Serial.println(percentageRemaining);
      Serial.println(t);
      Serial.println(h);
      updateValueInFirestore(percentageRemaining);
      updateTemperatureInFirestore(t);
      updateHumidityInFirestore(h);
  }
//////////////////////////////////////////////
//                   End Motor              //
//////////////////////////////////////////////

//////////////////////////////////////////////
//      Aux functions for MQTT and Wifi     //
//////////////////////////////////////////////

  //Reconnect Wifi
  void reconnectWiFi(void) {
    if (WiFi.status() == WL_CONNECTED) {
      return;
    }

    WiFi.begin(ssid, password); // Conecta na rede WI-FI

    Serial.print("* Connecting to Wifi ");
     int attempts = 0;
      while (WiFi.status() != WL_CONNECTED && attempts < 30) {
        delay(500);
        Serial.print(".");
        attempts++;
      }
    if  (attempts == 30) {
      return;
    }

    Serial.println("");
    Serial.print("* Successfully connected to Wi-Fi, with local IP: ");
    Serial.println(WiFi.localIP());
      // Initialize Firebase
    config.api_key = API_KEY;
    auth.user.email = "feederdevice@test.com";
    auth.user.password = "123456";
    Firebase.begin( & config, & auth);
    Firebase.reconnectWiFi(true);
    readMealSchedulesFromFirestore();
  }

  // Callback from Android 
  // --- Get the messages here
  void callbackMQTT(char * topic, byte * payload, unsigned int length) {
    String msg;

    // Convert payload to string
    for (int i = 0; i < length; i++) {
      char c = (char) payload[i];
      msg += c;
    }
    Serial.printf("Topic: %s\n", topic);
    Serial.printf("Message: %s\n", msg, topic);

    if (String(topic) == TOPIC_TREAT) {
      int treatSize = msg.toInt();
      MQTT.publish(TOPIC_TREAT_ANSWER, "true");
      feed(cal, treatSize);    
    } else if (String(topic) == TOPIC_UPDATE_MEALS) {
      readMealSchedulesFromFirestore();
      
    }
  }

  // Starts the Wi-Fi
  void startWifi(void) {
    reconnectWiFi();
  }

  // Starts everything from MQTT
  void initMQTT(void) {
    MQTT.setServer(BROKER_MQTT, BROKER_PORT);
    MQTT.setCallback(callbackMQTT);
  }

  // Connects to the Broker with a specific random ID
  void reconnectMQTT(void) {
    while (!MQTT.connected()) {
      ID_MQTT = "";
      Serial.print("* Starting connection with broker: ");
      Serial.println(BROKER_MQTT);

      int i = 0;
      for (i = 0; i < 10; i++) {
        ID_MQTT = ID_MQTT + String(letters[random(0, 36)]);
      }

      if (MQTT.connect(ID_MQTT.c_str())) {
        Serial.print("* Connected to broker successfully with ID: ");
        Serial.println(ID_MQTT);
        MQTT.subscribe(TOPIC_TREAT);
        MQTT.subscribe(TOPIC_UPDATE_MEALS);
        MQTT.subscribe(TOPIC_HUMIDITY);
        MQTT.subscribe(TOPIC_TEMPERATURE);
      } else {
        Serial.println("* Failed to connected to broker. Trying again in 2 secons.");
        delay(2000);
        return;
      }
    }
  }

  // Checks both Wi-Fi and MQTT state, and reconnects if something failed.
  void checkWiFIAndMQTT(void) {
    if (!MQTT.connected())
    reconnectMQTT();
    reconnectWiFi();
  }

//////////////////////////////////////////////
//   End Aux functions for MQTT and Wifi    //
//////////////////////////////////////////////

//////////////////////////////////////////////
//           Aux Meal Functions             //
//////////////////////////////////////////////

  void triggerMeal(int portionSize, const String& scheduleId) {
      Serial.println("Triggering meal: " + scheduleId + " Portion Size: " + portionSize);
      readMealSchedulesFromFirestore();
      Serial.println("Feeding");
      feed(cal, portionSize);
      writeMealHistoryToFirestore(portionSize);    
  }

  void checkAndTriggerMeals() {
      //Serial.println("//////////////////////////////////////////////////////////////////////////////////////////");
      //Serial.println("RTC DateTime: " + String(rtc.year()) + '/' + String(rtc.month()) + '/' + String(rtc.day()) + ' ' + String(rtc.hour()) + ':' + String(rtc.minute()) + ':' + String(rtc.second()) + " DOW: " + String(rtc.dayOfWeek()));

      int currentDayOfWeek = rtc.dayOfWeek() -1; // O rtc dá o DOW de 1 a 7 mas o array é de 0 a 6
      int currentHour = rtc.hour();
      int currentMinute = rtc.minute();

      String currentDayStr = daysOfTheWeek[currentDayOfWeek];

      // Check repeating schedules
      if (scheduleData.containsKey("repeating") && scheduleData["repeating"].containsKey(currentDayStr)) {
        //Serial.println("Contains Repeating: ");
          for (JsonObject schedule : scheduleData["repeating"][currentDayStr].as<JsonArray>()) {
              int scheduleHour = schedule["time"].as<String>().substring(11, 13).toInt();
              int scheduleMinute = schedule["time"].as<String>().substring(14, 16).toInt();

              //Serial.println("Current time: " + String(currentHour) + ":" + String(currentMinute) + " // Schedule time: " + String(scheduleHour) + ":" + String(scheduleMinute));
              if (currentHour == scheduleHour && currentMinute == scheduleMinute) {
                  triggerMeal(schedule["portionSize"], schedule["id"]);
              }
          }
      }

      // Check one-time schedules
      if (scheduleData.containsKey("oneTime") && scheduleData["oneTime"].as<JsonArray>().size() > 0) {
      // Serial.println("Contains oneTime: ");
          for (JsonObject schedule : scheduleData["oneTime"].as<JsonArray>()) {
              int scheduleHour = schedule["time"].as<String>().substring(11, 13).toInt();
              int scheduleMinute = schedule["time"].as<String>().substring(14, 16).toInt();

            //  Serial.println("Current time: " + String(currentHour) + ":" + String(currentMinute) + " // Schedule time: " + String(scheduleHour) + ":" + String(scheduleMinute));
              if (currentHour == scheduleHour && currentMinute == scheduleMinute) {
                  triggerMeal(schedule["portionSize"], schedule["id"]);
                  deleteDocumentFromFirestore(schedule["id"]);
              }
            
          }
      }
  }
//////////////////////////////////////////////
//          End Aux Meal Functions          //
//////////////////////////////////////////////

//////////////////////////////////////////////
//                   Setup                  //
//////////////////////////////////////////////
  void setup() {
    Serial.begin(115200);
      //Feeder stuff
    dht.begin();
    // Setup Serial connection
    Wire.begin(SDA_PIN, SCL_PIN);
    URTCLIB_WIRE.begin();
    
    //rtc.set(0, 30, 19, 5, 12, 1, 24);
    //RTCLib::set(byte second, byte minute, byte hour, byte dayOfWeek, byte dayOfMonth, byte month, byte year)

    scale.begin(LOADCELL_DOUT_PIN, LOADCELL_SCK_PIN);
    scale.set_scale();
    scale.tare(); //Reset the scale to 0

    long zero_factor = scale.read_average(); //Get a baseline reading

    // Declare pins as Outputs
    pinMode(step, OUTPUT);
    pinMode(dirPin, OUTPUT);
    pinMode(enable_motor, OUTPUT);

    //clearWiFiCredentials();

    preferences.begin("wifi", false);
    randomSeed(analogRead(0));
    //Mqtt and wifi start
    //startWifi();
    ssid = preferences.getString("ssid", "");
    password = preferences.getString("password", ""); 
    String storedValue = preferences.getString("isBtOn", "true"); // Default to "true" if not set
    isBtOn = (storedValue == "true");
    initMQTT();

      if (ssid != "" && password != "") {
      startWifi();
      return;
    }
    
    serialBT.begin(BT_NAME);
  
  }
//////////////////////////////////////////////
//                End Setup                 //
//////////////////////////////////////////////

//////////////////////////////////////////////
//                   Loop                   //
//////////////////////////////////////////////

  void loop() {

    while (serialBT.available()) {
        char receivedChar = serialBT.read();
        incomingData += receivedChar; 
        if (receivedChar == '\n') {
          parseIncomingData(incomingData);
          incomingData = "";
          checkWiFIAndMQTT();
        }
        
  }
  if (Serial.available() > 0) {
      String receivedData = Serial.readStringUntil('\n'); // Read data until newline
      receivedData.trim(); // Remove any whitespace

        // Check if the received data is "CLEAR"
        if (receivedData.equals("CLEAR")) {
          clearWiFiCredentials();
          Serial.println("WiFi credentials cleared.");
        }
      }

    if (isWiFiConnected) {
        
    checkWiFIAndMQTT();
    scale.set_scale(calibration_factor); //Adjust to this calibration factor
    //Change the g to kg and re-adjust the calibration factor if you follow SI units like a sane person
    Serial.println("Reading: " + String(scale.get_units(), 1) + " g");

    float h = dht.readHumidity();
    float t = dht.readTemperature();

    //float hic = dht.computeHeatIndex(t, h, false);
    Serial.println("Humidity: " + String(h) + "%  Temperature: " + String(t) + "°C");

    //stepper(1,step,HIGH);

    rtc.refresh();
    
    unsigned long currentMillis = millis();
      if (currentMillis - lastMealCheckMillis >= mealCheckInterval) {
          // Call only when 1 minute passes
          checkAndTriggerMeals();
          lastMealCheckMillis = currentMillis;
      }

      if (currentMillis - lastSendTime >= threeHoursInMillis) {
        updateTemperatureInFirestore(t);
        updateHumidityInFirestore(h);
        lastSendTime = currentMillis; 
      }
      
      
    Serial.println("RTC DateTime: " + String(rtc.year()) + '/' + String(rtc.month()) + '/' + String(rtc.day()) + ' ' + String(rtc.hour()) + ':' + String(rtc.minute()) + ':' + String(rtc.second()) + " DOW: " + String(rtc.dayOfWeek()));
    
    delay(3000);
    MQTT.loop();
    }
  }

//////////////////////////////////////////////
//                 End Loop                 //
//////////////////////////////////////////////
