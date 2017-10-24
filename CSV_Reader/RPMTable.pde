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