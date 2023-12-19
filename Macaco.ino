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

  //#include "Arduino.h"
  #include "uRTCLib.h"  

  #define SDA_PIN 33
  #define SCL_PIN 32
/*################# END INCLUDES AND DEFINES ################################
#############################################################################*/


/*###########################################################################
################### VARIABLES OR FIXED ######################################*/
  // Scale
  HX711 scale;

  uRTCLib rtc(0x68);

  char daysOfTheWeek[7][12] = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};

  // Scale Calbration
  float calibration_factor = -115; //Isso foi testando coisa
  // Himudity and temperature 
  DHT dht(DHTPIN, DHTTYPE);

  // Define pin connections & motor's steps per revolution
  const int dirPin = 4;
  const int step = 2;
  const int stepsPerRevolution = 200;
  const int enable_motor = 15;


  
/*################# END VARIABLES OR FIXED ##################################
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
  Serial.begin(9600);
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

/*################# END LOOP ##################################
#############################################################################*/