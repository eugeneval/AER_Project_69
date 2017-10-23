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