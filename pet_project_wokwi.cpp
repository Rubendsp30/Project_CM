//////////////////////////////////////////////
// Include Wifi and communication libraries //
//////////////////////////////////////////////

#include <WiFi.h>
#include <PubSubClient.h>
#include <Firebase_ESP_Client.h>
#include <Adafruit_Sensor.h>
#include <ArduinoJson.h>
 // Initialize Firebase
FirebaseData firebaseData;
FirebaseAuth auth;
FirebaseConfig config;

//Wifi stuff
String ssid = "Wokwi-GUEST";
String password = "";

// Define random ID
String ID_MQTT;
char * letters = "abcdefghijklmnopqrstuvwxyz0123456789";

// Define MQTT Topics
#define TOPIC_TREAT "/project/treat/Q3sZ0r8Frv7G2hd8SE"
#define TOPIC_TREAT_ANSWER "/project/treatAnswer/Q3sZ0r8Frv7G2hd8SE"
#define TOPIC_UPDATE_MEALS "/project/updateMeals/Q3sZ0r8Frv7G2hd8SE"

// Define Firebase credentials
#define FIREBASE_PROJECT_ID "cm-project-pet"
#define API_KEY "AIzaSyAhLyvKS0Fte6829SHSe9hmva2524gJBto"

// Define Firebase paths
#define MEAL_HISTORY_PATH "DEVICES/Q3sZ0r8Frv7G2hd8SE/MEAL_HISTORY/"
#define MEAL_SCHEDULES_PATH "DEVICES/Q3sZ0r8Frv7G2hd8SE/MEAL_SCHEDULES"

// Define MQTT Broker and PORT
const char * BROKER_MQTT = "broker.hivemq.com";
int BROKER_PORT = 1883;

// Global variables
WiFiClient espClient;
PubSubClient MQTT(espClient);
bool isWiFiConnected = false;
unsigned long lastMealCheckMillis = 0;
const long mealCheckInterval = 60000;

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
float calibration_factor = 0.4200983; //Isso foi testando coisa. Tinhas -115, mudei para 0.4200983 no wokwi

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
/*void updateValueInFirestore(int newValue) {
  // Define the document path
  String documentPath = "DEVICES/Q3sZ0r8Frv7G2hd8SE";

  // Prepare the data to update
  FirebaseJson json;

  // Set the new value for the foodSuply field
  json.set("fields/foodSuply/integerValue", newValue);

  // Update the document
  if (Firebase.Firestore.patchDocument( & firebaseData, FIREBASE_PROJECT_ID, "", documentPath, json.raw(), "foodSuply")) {
    Serial.println("Updated value successfully");
  } else {
    Serial.println("Failed to update value");
    Serial.println(firebaseData.errorReason());
  }
}*/

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

//////////////////////////////////////////////
//      End Aux functions for Firebase      //
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
void feed(int amount) {
  // float turns = 50 / cal;  //  to get how many turns to get 50g
  float turns = 50 / 103; // todo, visto q disseste q 1 turn = 103g mudei a conta para calcular automaticamente
  for (int i = 0; i <= amount; i = i + 50) {
    stepper(turns, step, HIGH); //1 turn = 4000 steps = 103g
    stepper(0.2, step, LOW); //spins backwards chug control  
  }
  digitalWrite(enable_motor, HIGH);
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
  while (WiFi.status() != WL_CONNECTED) {
    delay(100);
    Serial.print(".");
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
    feed(treatSize);    
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
    } else {
      Serial.println("* Failed to connected to broker. Trying again in 2 seconds.");
      delay(2000);
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
    writeMealHistoryToFirestore(portionSize);
    readMealSchedulesFromFirestore();
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
  randomSeed(analogRead(0));
  //Mqtt and wifi start
  initMQTT();
  startWifi();

  //Feeder stuff
  dht.begin();
  // Setup Serial connection
  Wire.begin(SDA_PIN, SCL_PIN);
  URTCLIB_WIRE.begin();

  //rtc.set(0, 13, 13, 3, 19, 12, 23);
  //RTCLib::set(byte second, byte minute, byte hour, byte dayOfWeek, byte dayOfMonth, byte month, byte year)

  scale.begin(LOADCELL_DOUT_PIN, LOADCELL_SCK_PIN);
  scale.set_scale();
  scale.tare(); //Reset the scale to 0

  long zero_factor = scale.read_average(); //Get a baseline reading

  // Declare pins as Outputs
  pinMode(step, OUTPUT);
  pinMode(dirPin, OUTPUT);
  pinMode(enable_motor, OUTPUT);
}
//////////////////////////////////////////////
//                End Setup                 //
//////////////////////////////////////////////

//////////////////////////////////////////////
//                   Loop                   //
//////////////////////////////////////////////

void loop() {

  checkWiFIAndMQTT();
  MQTT.loop();
  //delay(200);

  scale.set_scale(calibration_factor); //Adjust to this calibration factor
  //Change the g to kg and re-adjust the calibration factor if you follow SI units like a sane person
  //Serial.println("Reading: " + String(scale.get_units(), 1) + " g");

  float h = dht.readHumidity();
  float t = dht.readTemperature();

  float hic = dht.computeHeatIndex(t, h, false);
  //Serial.println("Humidity: " + String(h) + "%  Temperature: " + String(t) + "°C");

  // TODO isso aqui foi pra testar quantos steps o motor dá e ele soltava aproximadamente 30g a cada volta
  // TODO criar a função que vai ser chamada no android, a cada volta ele dá 30g, então dependendo da quantidade
  // inserida vai dar mais ou menos voltas
  // stepper(1,step,HIGH);

  rtc.refresh();

   unsigned long currentMillis = millis();
    if (currentMillis - lastMealCheckMillis >= mealCheckInterval) {
        // Call only when 1 minute passes
        checkAndTriggerMeals();
        lastMealCheckMillis = currentMillis;
    }

  //Serial.println("RTC DateTime: " + String(rtc.year()) + '/' + String(rtc.month()) + '/' + String(rtc.day()) + ' ' + String(rtc.hour()) + ':' + String(rtc.minute()) + ':' + String(rtc.second()) + " DOW: " + String(rtc.dayOfWeek()));

  delay(1000);

}
//////////////////////////////////////////////
//                 End Loop                 //
//////////////////////////////////////////////