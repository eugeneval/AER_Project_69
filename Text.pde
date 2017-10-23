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