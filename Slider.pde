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