/*  CSV_READER
    Version 1.2

    Improvements from last version:
    - Upping baud rate seems to have resolved freezing issues

    Improvements to be made:
    - (When availible) Accept error signal from ECU and stop motor

    Notes:
    - Code for handshake with processing exists but is commented out
    
*/

#include <Adafruit_MCP4725.h>

Adafruit_MCP4725 dac;
int v = 0;
boolean start = true;

////////////////////////////////////////////////////////////////////
//  SETUP
//  Connect to processing and to DAC
////////////////////////////////////////////////////////////////////
void setup() 
{
  Serial.begin(19200); 
  Serial.println("RacePump Activating");
  dac.begin(0x62);
}

////////////////////////////////////////////////////////////////////
//  LOOP
//  Main loop; accept rpm from serial port and output to DAC
////////////////////////////////////////////////////////////////////
void loop() 
{
  
   while (Serial.available()) 
   {
      char c = Serial.read();
      
      // handle digits
      if ((c >= '0') && (c <= '9') && start == true) 
      {
        v = 10 * v + c - '0';
      }
      
      // handle delimiter
      else if (c == 'n' && start == true) 
      {
        dac.setVoltage(v, false);
        v = 0;
      }

    }
    
    //Serial.write("s"); //heartbeat signal back to processing

}
