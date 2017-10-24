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