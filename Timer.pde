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