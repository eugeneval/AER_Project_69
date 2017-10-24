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