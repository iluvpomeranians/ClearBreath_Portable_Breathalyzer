#include "BluetoothSerial.h"

const int MQ_PIN = 36;    // Analog pin connected to sensor
float RL_VALUE = 180;      // RL Resistance in Kilo ohms -> from data sheets
float R0 = 0.5;          // R0 Resistance in kilo ohms -> calibrate from air

BluetoothSerial SerialBT; // Create a BluetoothSerial object

const int READ_SAMPLE_INTERVAL = 100;  
const int READ_SAMPLE_TIMES = 5;       

// Add next points values according to your datasheet Rs/R0 graph
const float X0 = 50;
const float Y0 = 0.18;
const float X1 = 500;
const float Y1 = 0.012;

// Calculate the curve points {X, Y}
const float pointA[] = { log10(X0), log10(Y0) };
const float pointB[] = { log10(X1), log10(Y1) };
// Calculate the values of the graph
const float a = (pointB[1] - pointA[1]) / (pointB[0] - pointA[0]);
const float b = pointA[1] - pointA[0] * a;

void setup() {
  pinMode(MQ_PIN, INPUT);
  Serial.begin(9600);

  // Initialize Bluetooth serial
  if (SerialBT.begin("ClearBreath")) { // Bluetooth device name
    Serial.println("Bluetooth initialized");
  } else {
    Serial.println("Bluetooth initialization failed!");
    while (1); // Halt execution if Bluetooth initialization fails
  }
}

void loop() {
  float rs_med = readMQ(MQ_PIN);      // Get mean RS resistance value 
  float concentration = getConcentration(rs_med / R0);   // Get concentration
  
  // Display the value
  float BAC = map(concentration, 20, 500, 7, 184); // 20 to 500 scale is from the MQ-3 datasheet graph
  BAC = BAC / 1000;  

  Serial.print("PPM: ");
  Serial.println(concentration);

  Serial.print("BAC: ");
  Serial.println(BAC, 3);

  // Check if Bluetooth is connected before sending data
  if (SerialBT.hasClient()) {
    SerialBT.print("BAC: ");
    SerialBT.println(BAC, 3);
  } else {
    Serial.println("No Bluetooth client connected");
  }

  delay(2000); // Add delay to avoid rapid looping
}

// Get the average resistance over N samples
float readMQ(int mq_pin) {
  float rs = 0;
  for (int i = 0; i < READ_SAMPLE_TIMES; i++) {
    int adc_value = analogRead(mq_pin);
    if (adc_value == -1) {
      Serial.println("Analog read error");
      return -1;
    }
    rs += getMQResistance(adc_value);
    delay(READ_SAMPLE_INTERVAL);
  }
  return rs / READ_SAMPLE_TIMES;
}

// Get resistance from analog reading
float getMQResistance(int raw_adc) {
  if (raw_adc == 0) {
    Serial.println("Raw ADC value is 0, can't calculate resistance");
    return -1;
  }
  return (((float)RL_VALUE / 1000.0 * (4095 - raw_adc) / raw_adc));
}

// Calculates the concentration (PPM) with the given formula of 10^(b + a * log(rs/r0))
float getConcentration(float rs_ro_ratio) {
  return pow(10, b + a * log(rs_ro_ratio));
}
