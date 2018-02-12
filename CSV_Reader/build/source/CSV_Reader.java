import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import processing.serial.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class CSV_Reader extends PApplet {

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



Serial port; //The serial port of the Arduino connection
RPMTable rpmProfile;
RunState runState = new RunState();

//engineSelectionScreen
Text chooseEngine = new Text("Choose an engine: ", 120, 120);
Button p91 = new Button("P91", 'y', 120, 160, 50, 20, true);

//runningScreen
Dial speedo = new Dial("rpm", new int[] {0, 191, 255}, 180, 180, 180, 180);
Text speedoTimeValue = new Text("0.00", 25, 230);
Text speedoTimeText = new Text("time (s)", 30, 240);
Text manualRPMValue = new Text("0", 400, 320);
Text manualRPMText = new Text("rpm", 400, 330);
Button waterTempError = new Button("WATER TEMP", 'g', 500, 60, 120, 20, false);
Button oilTempError = new Button("OIL TEMP", 'g', 500, 90, 120, 20, false);
Button waterPressureError = new Button("WATER PRESSURE", 'g', 500, 120, 120, 20, false);
Button oilLevelError = new Button ("OIL LEVEL", 'g', 500, 150, 120, 20, false);
Button arduinoError = new Button("ARDUINO", 'r', 500, 180, 120, 20, false);
Button startPause = new Button("START", 'g', 160, 320, 50, 20, true);
Button stop = new Button("STOP", 'r', 230, 320, 50, 20, true);
Button manualAuto = new Button("MANUAL", 'o', 300, 320, 60, 20, true);
Slider manualSlider = new Slider(400, 60, 50, 240);
Text timeRunningValue = new Text("0:00:00", 30, 320);
Text timeRunningText = new Text("time", 30, 330);

//stopScreen
Text runFinished = new Text("Run finished.", 150, 150);

//Collects them into arrays
Text[] engineSelectionScreen = new Text[] {chooseEngine, p91};
Text[] runningScreen = new Text[] {speedo, speedoTimeText, speedoTimeValue, manualRPMValue, manualRPMText,
  waterTempError, oilTempError, waterPressureError, arduinoError, oilLevelError, manualSlider,
  startPause, stop, manualAuto, timeRunningValue, timeRunningText};
Text[] stopScreen = new Text[] {runFinished};

int pulleyRatio;
int manualRPM;
boolean debounce;
int timeRunning;

////////////////////////////////////////////////////////////////////////////////
//  SETUP
//  Sets window size, loads file, opens serial port, and creates all objects
////////////////////////////////////////////////////////////////////////////////
public void setup()
{
  
  background(50);

  selectInput("Select a file to process:", "fileSelected");
  //opens a system-specific file viewer to choose data

  //println(Serial.list()); // List COM-ports
  port = new Serial(this, Serial.list()[5], 19200); //selects the serial port the arduino is on
  //ensure baud rate is same as on Arduino!
  update(0);  //Ensures motor speed is set to 0 at program start.

}

////////////////////////////////////////////////////////////////////////////////
//  FILE SELECTION
//  Loads the file, and loads the first set of data
////////////////////////////////////////////////////////////////////////////////
public void fileSelected(File selection) //selects the file
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
public void draw()
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
  switch(runState.getRunState()) //Works depending on the current runstate.
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



public boolean isPressed(Button button)
{
  return isPressed(button, true);
}

public boolean isPressed(Button button, boolean wantDebounce)
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
public void mouseReleased() //ensures mouse is released before it can be pressed again
{
  debounce = true;
}

////////////////////////////////////////////////////////////////////////////////
//  UPDATE
//  Sends data to arduino
////////////////////////////////////////////////////////////////////////////////
public void update(int speed) //updates output to arduino
{
  int v = (int) map(speed, 0, 2880, 0, 4095); //scales rpm to accepted input for DAC
  // NOTE: this means that rpm is accurate to +- 1rpm due to limitaions of DAC
  port.write(Integer.toString(v));
  port.write('n');
  //println(v); //testing
}

public boolean handshake()
{
    port.write("a");
    delay(1);
    if (port.read() == 'b') {
        return true;
    } else {
        return false;
    }


}

////////////////////////////////////////////////////////////////////////////////
//  RUN METHOD ON TEXT ARRAY
//  Runs the chosen method on an array of the custom class Text
////////////////////////////////////////////////////////////////////////////////
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
// A class to allow buttons to be created and manipulated easily.

public class Button extends Text {

  protected float xSize, ySize;
  private int[] backgroundColour;
  protected boolean pressable;


  ////////////////////////////////////////////////////////////////////////////////
  //   CONSTRUCTORS
  ////////////////////////////////////////////////////////////////////////////////

  public Button()
  {}

  //Constructor with manual background colour entry and auto text colour
  public Button(String inText, char inTextColour, int[] inBackgroundColour, 
    int inXPos, int inYPos, int inXSize, int inYSize, boolean inPress)
  {
    text = inText;
    textColour = chooseTextColour(inTextColour);
    backgroundColour = inBackgroundColour;
    xPos = inXPos;
    yPos = inYPos;
    xSize = inXSize;
    ySize = inYSize;
    pressable = inPress;
    visible = false;

  }
  
  //Constructor with manual background colour entry and set text colour(black)
  public Button(String inText, int[] inBackgroundColour, 
    int inXPos, int inYPos, int inXSize, int inYSize, boolean inPress)
  {
    text = inText;
    textColour = 0;
    backgroundColour = inBackgroundColour;
    xPos = inXPos;
    yPos = inYPos;
    xSize = inXSize;
    ySize = inYSize;
    pressable = inPress;
    visible = false;

  }

  //Constructor with auto bakground colour selection and auto text colour
  public Button(String inText, char inTextColour, char inBackgroundColour, 
    int inXPos, int inYPos, int inXSize, int inYSize, boolean inPress)
  {
    text = inText;
    textColour = chooseTextColour(inTextColour);
    backgroundColour = chooseBackgroundColour(inBackgroundColour);
    xPos = inXPos;
    yPos = inYPos;
    xSize = inXSize;
    ySize = inYSize;
    pressable = inPress;
    visible = false;

  }
  
  //Constructor with auto background colour selection and set text colour (black)
  public Button(String inText, char inBackgroundColour, 
    int inXPos, int inYPos, int inXSize, int inYSize, boolean inPress)
  {
    text = inText;
    textColour = 0;
    backgroundColour = chooseBackgroundColour(inBackgroundColour);
    xPos = inXPos;
    yPos = inYPos;
    xSize = inXSize;
    ySize = inYSize;
    pressable = inPress;
    visible = false;

  }

  ////////////////////////////////////////////////////////////////////////////////
  //  
  ////////////////////////////////////////////////////////////////////////////////
  protected void display() {
    fill(backgroundColour[0], backgroundColour[1], backgroundColour[2]);
    rect(xPos, yPos, xSize, ySize);
    fill(textColour);
    text(text, xPos + 5, yPos + 15);
  }

  public boolean overButton() //checks to see if the mouse is over the defined rectangle
  {
    if (!pressable)
      return false;
    else if (mouseX >= xPos && mouseX <= xPos+xSize && mouseY >= yPos && mouseY <= yPos+ySize) 
      return true;
    else 
    return false;
  }

    ////////////////////////////////////////////////////////////////////////////////
  //  
  ////////////////////////////////////////////////////////////////////////////////

  public void changeColour(char newColour)
  {
    backgroundColour = chooseBackgroundColour(newColour);
  }


  ////////////////////////////////////////////////////////////////////////////////
  //  
  ////////////////////////////////////////////////////////////////////////////////

  private int[] chooseBackgroundColour(char inBGColour)
  {
    switch(inBGColour)
    {
    case 'g': //green
      return new int[] {51, 255, 51};


    case 'r': //red
      return new int[] {255, 0, 0};

    case 'o': //orange
      return new int[] {255, 153, 51};

    case 'y': //yellow
      return new int[] {255, 255, 55};

    default:
      println("BUTTON BG COLOUR ERROR");
      return new int[] {255, 255, 255};
    }
  }
}
public class Dial extends Text {

  private int[] arcColour;
  private float xSize, ySize, xPos, yPos;
  private float arcPos;
  private int value;


    ////////////////////////////////////////////////////////////////////////////////
  //   CONSTRUCTORS
  ////////////////////////////////////////////////////////////////////////////////
  public Dial(String inText, int[] inArcColour, int inXPos, int inYPos, 
    int inXSize, int inYSize)
  {
    arcPos = 0;
    value = 0;
    text = inText;
    textColour = 255;
    arcColour = inArcColour;
    xPos = inXPos;
    yPos = inYPos;
    xSize = inXSize;
    ySize = inYSize;
    visible = false;
  }

  public Dial(String inText, char inTextColour, int[] inArcColour, int inXPos, int inYPos, 
    int inXSize, int inYSize)
  {
    arcPos = 0;
    value = 0;
    text = inText;
    textColour = chooseTextColour(inTextColour);
    arcColour = inArcColour;
    xPos = inXPos;
    yPos = inYPos;
    xSize = inXSize;
    ySize = inYSize;
    visible = false;
  }

 ////////////////////////////////////////////////////////////////////////////////
  //   UPDATE
  ////////////////////////////////////////////////////////////////////////////////

  public void setValue(int newValue)
  {
    value = newValue;
    arcPos = radians(newValue/24);
    
  }


   ////////////////////////////////////////////////////////////////////////////////
  //   DISPLAY 
  ////////////////////////////////////////////////////////////////////////////////

  public void refresh()
  {
    if (visible)
      display();
  }

  public void display()
  {
    strokeWeight(10);
    stroke(arcColour[0], arcColour[1], arcColour[2]);
    fill(0);
    arc(xPos, yPos, xSize, ySize, 0, arcPos); //draws an arc whic represents the value
    fill(255);
    text(value, 163, 185);
    text(text, 166, 195);
    
    strokeWeight(0);
    stroke(0);
  }
}
// The class where the csv RPM data is kept and accessed from.

public class RPMTable 
{

  private Table table;
  private int rowCount;
  private int timeResolution;
  private TableRow row;
  private int engineRPM, motorRPM;
  private String fileName;
  private String engineType;
  
  private final int p91PulleyRatio = 3;

////////////////////////////////////////////////////////////////////////////////
//   CONSTRUCTORS
////////////////////////////////////////////////////////////////////////////////
  public RPMTable()
  {
    this.rowCount = 0;
  }
  
  public RPMTable(File file)
  {
    fileName = file.getName();  
    table = loadTable(file.getAbsolutePath(), "header");
    
    this.rowCount = 0;
    TableRow row1 = table.getRow(1);
    TableRow row2 = table.getRow(2);
    int time1 = row1.getInt("time");
    int time2 = row2.getInt("time");
    this.timeResolution = time2 - time1;
    this.row = table.getRow(0);
    engineRPM = row.getInt("rpm");
  }

////////////////////////////////////////////////////////////////////////////////
//  UPDATE
//  Updates the current rpm if the time has reached that point in the table
////////////////////////////////////////////////////////////////////////////////
  public void update(int elapsedTime)
  {
    row = table.getRow(rowCount);
    int time = row.getInt("time");
    if (time <= elapsedTime) //loads new rpm if we have reached that time
    {
      engineRPM = row.getInt("rpm"); 
      updateMotorRPM();
      rowCount++;
    }
    if (time + timeResolution < elapsedTime) //catches up if it falls behind
    {
      rowCount++; 
    }
  }
  
  public void updateMotorRPM()
  {
     if (engineType.equals("P91"))
       motorRPM = engineRPM / p91PulleyRatio;
    else
      println("ENGINE SELECTION ERROR");
  }
  
  
////////////////////////////////////////////////////////////////////////////////
//   SETTERS AND GETTERS
////////////////////////////////////////////////////////////////////////////////
  
  public void setEngineType(String newType)
  {
    engineType = newType;
  }
  
  public String getFileName()
  {
   return fileName; 
  }
  
  public int getEngineRPM()
  {
    return engineRPM;
  }
  
  public int getMotorRPM()
  {
   return motorRPM; 
  }
  
  public int getTime()
  {
     return row.getInt("time");
  }
  
  public int getRatio()
  {
   if (engineType.equals("P91"))
      return p91PulleyRatio;
      else
      return 0;
  }
  
  
  
  
  
}
// A class to determine and hold the current run state of the program

public class RunState {

  private String runState, prevState;
  Timer autoTimer = new Timer(), manualTimer = new Timer(), pauseTimer = new Timer(), stopTimer = new Timer();

  /*Possible states: 
   fileSelection
   engineSelection
   auto
   manual
   pause
   stop
   
   */

  ////////////////////////////////////////////////////////////////////////////////
  //   CONSTRUCTORS
  ////////////////////////////////////////////////////////////////////////////////
  public RunState()
  {
    runState = "fileSelection";
  }



  ////////////////////////////////////////////////////////////////////////////////
  //   SETTERS
  ////////////////////////////////////////////////////////////////////////////////


  public void fileSelected()
  {
    runState = "engineSelection";
  }

  public void engineSelected()
  {
    runState = "pause";
    timerUpdate();
  }

  public void start()
  {
    runState = "auto";
    timerUpdate();
  }

  public void manual()
  {
    prevState = runState;
    runState = "manual"; 
    timerUpdate();
  }

  public void auto()
  {
    runState = prevState; 
    timerUpdate();
  }

  public void pause()
  {
    runState = "pause"; 
    timerUpdate();
  }

  public void stop()
  {
   runState = "stop";
   timerUpdate();
  }

  private void timerUpdate()
  {
    switch(runState)
    {
    case "auto":
      manualTimer.stop();
      pauseTimer.stop();
      autoTimer.start();
      break;
    case "manual":
      pauseTimer.stop();
      autoTimer.stop();
      manualTimer.start();
      break;
    case "pause":
      autoTimer.stop();
      manualTimer.stop();
      pauseTimer.start();
      break;
     case "stop":
      stopTimer.start();
    default:
      autoTimer.stop();
      manualTimer.stop();
      pauseTimer.stop();
      break;
    }
  }

  ////////////////////////////////////////////////////////////////////////////////
  //   SETTERS
  ////////////////////////////////////////////////////////////////////////////////

  public String getRunState()
  {
    return runState;
  }



  ////////////////////////////////////////////////////////////////////////////////
  //   CHECKS 
  ////////////////////////////////////////////////////////////////////////////////

  public boolean equals(String checkState)
  {
    if (runState == checkState)
      return true;
    else
      return false;
  }

  public boolean isRunning()
  {
    switch(runState)
    {
    case "fileSelection": 
    case "engineSelection": 
    case "stop":
      return false;
    default:
      return true;
    }
  }
}
public class Slider extends Button
{
    int sliderHeight;
    boolean visible;
  
  public Slider(int inXPos, int inYPos, int inXSize, int inYSize)
  {
    xPos = inXPos;
    yPos = inYPos;  
    xSize = inXSize;
    ySize = inYSize;
    pressable = true;
  }
  
  
 protected void display()
 {
    fill(56, 124, 255);
    rect(xPos, yPos, xSize, ySize-sliderHeight);
    fill(0, 56, 256);
    rect(xPos, yPos+ySize-sliderHeight, xSize, sliderHeight);
 }
  
  public void updateFromRPM(int manualRPM, int ratio)
  {
    sliderHeight = (int) map(manualRPM, 0, 2880*ratio, 0, ySize);
  }
  
  public int updateFromClick(int click, int ratio)
  {
    sliderHeight = (int) yPos + (int) ySize - click;
    return (int) map(sliderHeight, 0, ySize, 0, 2880*ratio);
  }
  
  /*public void press()
  {
  
  if (mousePressed && overRect(400, 60, 50, 240)) //detects the slider being moved
    {
      sliderHeight = 360 - mouseY - 60;
      manualRPM = (int) map(sliderHeight, 0, 240, 0, maxRPM);
    }
  
  }*/
  
}
// A class for easily creating text objects

public class Text {

  protected String text;
  protected int textColour;
  protected float xPos, yPos;
  protected boolean visible;


////////////////////////////////////////////////////////////////////////////////
//   CONSTRUCTORS
////////////////////////////////////////////////////////////////////////////////
  public Text()
  {
    visible = false;
  }

  //Constructor with default text colour (white)
  public Text(String inText, int inXPos, int inYPos)
  {
    text = inText;
    textColour = 255;
    xPos = inXPos;
    yPos = inYPos;
    visible = false;

  }

  //Constructor with auto text colour
  public Text(String inText, char inTextColour, int inXPos, int inYPos)
  {
    text = inText;
    textColour = chooseTextColour(inTextColour);
    xPos = inXPos;
    yPos = inYPos;
    visible = false;

  }

////////////////////////////////////////////////////////////////////////////////
//   SETTERS
////////////////////////////////////////////////////////////////////////////////

  public void changeText(String newText)
  {
    text = newText;
  }

  public void show()
  {
    visible = true;
  }

  public void hide()
  {
    visible = false;
  }

////////////////////////////////////////////////////////////////////////////////
//   DISPLAY
////////////////////////////////////////////////////////////////////////////////

  public void refresh()
  {
    if (visible)
      display();
  }

  protected void display() {
    fill(textColour);
    text(text, xPos, yPos);
  }

////////////////////////////////////////////////////////////////////////////////
//   INTERNAL FUNCTIONS
////////////////////////////////////////////////////////////////////////////////

  protected int chooseTextColour(char inTextColour)
  {
    switch(inTextColour)
    {
    case 'w':
      return 255;

    case 'b':
      return 0;

    default:
      println("TEXT COLOUR ERROR");
      return 55;
    }
  }
}

public class Timer
{
  int timeRunningThisLoop;
  int timeRunningTotal;
  int startTime;
  boolean running;

  public Timer()
  {
    timeRunningThisLoop = 0;
    timeRunningTotal = 0;
    running = false;
  }

  public void start()
  {
    startTime = millis();
    running = true;
  }

  public int currentTime()
  {
    if (running)
    {
      timeRunningThisLoop = millis() - startTime;
      return timeRunningThisLoop + timeRunningTotal;
    } else
      return timeRunningTotal;
  }

  public void stop()
  {
    if (running)
    {
      timeRunningThisLoop = millis() - startTime;
      timeRunningTotal += timeRunningThisLoop;
      timeRunningThisLoop = 0;
      startTime = 0;
      running = false;
    }
  }
}
  public void settings() {  size(720, 360, P2D); }
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "CSV_Reader" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
