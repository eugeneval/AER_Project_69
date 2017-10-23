/*  CSV_READER
 Version 4.0.0

 Improvements from last version:
 - Completely rebuilt from the ground up to properly use classes, objects and methods.
   Significantly longer but now also significantly more easy to understand and modify.

 Potential improvements:
 - Spin motor up to speed
 - Need to get current speed data back from motor
 - AO1 is current motor speed, 0-5V
 - Maybe add some way of manual and auto interacting to prevent rapid speed changes
 - Spin up to auto speed before starting?
 - Add error-checking if wrong file type or data can otherwise not be read
 - (When availible) Accept error signal from ECU and stop motor
 - Better capability to program an endurance run

 Currently working on:
 -
 
 Notes:
 - Error messages are set to always true

 */

import processing.serial.*;

Serial port; //The serial port of the Arduino connection
RPMTable rpmProfile;
RunState runState = new RunState();

Text chooseEngine, speedoTimeValue, speedoTimeText, manualRPMValue, manualRPMText, runFinished, timeRunningValue, timeRunningText;
Button waterTempError, oilTempError, waterPressureError, oilLevelError;
Button startPause, stop, manualAuto;
Button p91;
Dial speedo;
Slider manualSlider;

Text[] engineSelectionScreen;
Text[] runningScreen;
Text[] stopScreen;

int pulleyRatio;
int manualRPM;
boolean debounce;
int timeRunning;

////////////////////////////////////////////////////////////////////////////////
//  SETUP
//  Sets window size, loads file, opens serial port, and creates all objects
////////////////////////////////////////////////////////////////////////////////
void setup()
{
  size(720, 360, P2D);
  background(50);

  selectInput("Select a file to process:", "fileSelected");
  //opens a system-specific file viewer to choose data

  //println(Serial.list()); // List COM-ports
  port = new Serial(this, Serial.list()[5], 19200); //selects the serial port the arduino is on
  //ensure baud rate is same as on Arduino!
  update(0);  //Ensures motor speed is set to 0 at program start.



  //Creates all objects
  chooseEngine = new Text("Choose an engine: ", 120, 120);
  p91 = new Button("P91", 'y', 120, 160, 50, 20, true);

  speedo = new Dial("rpm", new int[] {0, 191, 255}, 180, 180, 180, 180);
  speedoTimeValue = new Text("0.00", 25, 230);
  speedoTimeText = new Text("time (s)", 30, 240);
  manualRPMValue = new Text("0", 400, 320);
  manualRPMText = new Text("rpm", 400, 330);

  waterTempError = new Button("WATER TEMP", 'g', 500, 60, 120, 20, false);
  oilTempError = new Button("OIL TEMP", 'g', 500, 90, 120, 20, false);
  waterPressureError = new Button("WATER PRESSURE", 'g', 500, 120, 120, 20, false);
  oilLevelError = new Button ("OIL LEVEL", 'g', 500, 150, 120, 20, false);

  startPause = new Button("START", 'g', 160, 320, 50, 20, true);
  stop = new Button("STOP", 'r', 230, 320, 50, 20, true);
  manualAuto = new Button("MANUAL", 'o', 300, 320, 60, 20, true);

  manualSlider = new Slider(400, 60, 50, 240);

  timeRunningValue = new Text("0:00:00", 30, 320);
  timeRunningText = new Text("time", 30, 330);

  runFinished = new Text("Run finished.", 150, 150);

  engineSelectionScreen = new Text[] {chooseEngine, p91};
  runningScreen = new Text[] {speedo, speedoTimeText, speedoTimeValue, manualRPMValue, manualRPMText,
    waterTempError, oilTempError, waterPressureError, oilLevelError, manualSlider,
    startPause, stop, manualAuto, timeRunningValue, timeRunningText};
  stopScreen = new Text[] {runFinished};
}

////////////////////////////////////////////////////////////////////////////////
//  FILE SELECTION
//  Loads the file, and loads the first set of data
////////////////////////////////////////////////////////////////////////////////
void fileSelected(File selection) //selects the file
{
  if (selection == null)
  {
    println("Window was closed or the user hit cancel.");
  } else
  {
    println("User selected " + selection.getAbsolutePath());
    rpmProfile = new RPMTable(selection);
    runState.fileSelected();
  }
}

////////////////////////////////////////////////////////////////////////////////
//  DRAW
//  Main program loop
////////////////////////////////////////////////////////////////////////////////
void draw()
{
  clear();

  while (runState.equals("fileSelection")) //Does not run until a file has actually been selected
  {
    delay(1);
  }

  //  REFRESH ////////////////////////////////////////////
  runMethodOnTextArray(engineSelectionScreen, "refresh");
  runMethodOnTextArray(runningScreen, "refresh");
  runMethodOnTextArray(stopScreen, "refresh");

  //    ////////////////////////////////////////////
  switch(runState.getRunState()) //Only shows engine selection interface until engine is selected
  {

    //  ENGINE SELECTION  ////////////////////////////////////////////
  case "engineSelection":
    chooseEngine.show();
    p91.show();

    if (isPressed(p91))
    {
      rpmProfile.setEngineType("P91");
      runState.engineSelected();
      pulleyRatio = rpmProfile.getRatio();

      runMethodOnTextArray(engineSelectionScreen, "hide");
      runMethodOnTextArray(runningScreen, "show");
    }

    speedo.setValue(rpmProfile.getEngineRPM());
    break;
    //  PAUSED ////////////////////////////////////////////
  case "pause":
    if (isPressed(startPause))
    {
      runState.start();
      startPause.changeText("PAUSE");
      startPause.changeColour('o');
    }

    if (isPressed(manualAuto))
    {
      runState.manual();
      manualAuto.changeText("AUTO");
      manualAuto.changeColour('g');
    }

    break;

    //  MANUAL ////////////////////////////////////////////
  case "manual":
    if (isPressed(manualSlider, false))
    {
      manualRPM = manualSlider.updateFromClick(mouseY, pulleyRatio);
      update(manualRPM/pulleyRatio);
    }
    manualRPMValue.changeText(Integer.toString(manualRPM));

    if (isPressed(manualAuto))
    {
      runState.auto();
      manualAuto.changeText("MANUAL");
      manualAuto.changeColour('o');
    }
    break;
    //  AUTO ////////////////////////////////////////////
  case "auto":
    if (isPressed(startPause))
    {
      runState.pause();
      startPause.changeText("START");
      startPause.changeColour('g');
    }

    rpmProfile.update(runState.autoTimer.currentTime());
    speedo.setValue(rpmProfile.getEngineRPM());
    speedoTimeValue.changeText(Integer.toString(rpmProfile.getTime()));
    update(rpmProfile.getMotorRPM());
    manualRPM = rpmProfile.getEngineRPM();
    manualRPMValue.changeText(Integer.toString(manualRPM));
    manualSlider.updateFromRPM(manualRPM, pulleyRatio);
    break;

    //  STOP ////////////////////////////////////////////
  case "stop":
    runMethodOnTextArray(runningScreen, "hide");
    runMethodOnTextArray(stopScreen, "show");
    if (runState.stopTimer.currentTime() >= 4000)
      exit();
    break;
  }

  if (isPressed(stop))
  {
    runState.stop();
  }

  timeRunning = runState.manualTimer.currentTime() + runState.autoTimer.currentTime() + runState.pauseTimer.currentTime();
  int seconds = (timeRunning/1000) % 60;
  int minutes = ((timeRunning/1000)/60) % 60;
  int hours = (((timeRunning/1000)/60)/60) % 60;
  timeRunningValue.changeText(hours + ":" + minutes + ":" + seconds);

  //println(runState.pauseTimer.currentTime() + "\t" + runState.autoTimer.currentTime() + "\t"
  //  + runState.manualTimer.currentTime());
}



boolean isPressed(Button button)
{
  return isPressed(button, true);
}

boolean isPressed(Button button, boolean wantDebounce)
{
  if (mousePressed && button.overButton() && debounce && wantDebounce)
  {
    debounce = false;
    return true;
  }
  if (mousePressed && button.overButton() && wantDebounce == false)
    return true;
  else
    return false;
}

////////////////////////////////////////////////////////////////////////////////
//  DEBOUNCE
////////////////////////////////////////////////////////////////////////////////
void mouseReleased() //ensures mouse is released before it can be pressed again
{
  debounce = true;
}

////////////////////////////////////////////////////////////////////////////////
//  UPDATE
//  Sends data to arduino
////////////////////////////////////////////////////////////////////////////////
void update(int speed) //updates output to arduino
{
  int v = (int) map(speed, 0, 2880, 0, 4095); //scales rpm to accepted input for DAC
  // NOTE: this means that rpm is accurate to +- 1rpm due to limitaions of DAC
  port.write(Integer.toString(v));
  port.write('n');
  //println(v); //testing
}



private void runMethodOnTextArray(Text[] array, String method)
{
  for (int i = 0; i < array.length; i++)
  {
    switch (method)
    {
    case "show":
      array[i].show();
      break;

    case "hide":
      array[i].hide();
      break;

    case "refresh":
      array[i].refresh();
      break;
    }
  }
}
