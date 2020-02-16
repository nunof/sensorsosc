import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PVector;

/**
 * Pan-Zoom Controller.
 * 
 * Allows to move and scale a drawing using mouse and keyboard. Mouse wheel
 * changes the scale, smartphone accelerometer mouse drag or keyboard arrows 
 * change the panning (movement).
 * 
 * @author Nuno Ferreira
 * @license MIT
 * 
 * Based on previous work of Bohumir Zamecnik, inspired by 
 * "Pan And Zoom" by Dan Thompson, licensed under Creative Commons
 * Attribution-Share Alike 3.0 and GNU GPL license. Work:
 * http://openprocessing.org/visuals/?visualID= 46964
 * 
 */
 
public class PanZoom {

  private final PVector DIR_UP = new PVector(0, -1);
  private final PVector DIR_DOWN = new PVector(0, 1);
  private final PVector DIR_LEFT = new PVector(-1, 0);
  private final PVector DIR_RIGHT = new PVector(1, 0);

  private float panVelocity = 40;
  private float scaleVelocity = 0.01f;
  private float minLogScale = -5;
  private float maxLogScale = 5;

  private float logScale = 0;
  private float scale = 1;
  private PVector pan = new PVector();

  private PApplet p;

  public PanZoom(PApplet p) {
    this.p = p;
  }

  public void mouseDragged() {
    PVector mouse = new PVector(p.mouseX, p.mouseY);
    PVector pmouse = new PVector(p.pmouseX, p.pmouseY);
    pan.add(PVector.sub(mouse, pmouse));
  }
  
  public void accelMoveX(int num) {
    PVector m;
    if (num > 0) m = new PVector(num, 0);
    else m = new PVector(num, 0);
    pan.add(PVector.mult(m, panVelocity));
  }
  
  public void accelMoveY(int num) {
    PVector m;
    if (num > 0) m = new PVector(0, num);
    else m = new PVector(0, num);
    pan.add(PVector.mult(m, panVelocity));
  
  }

  public void keyPressed() {
    if (p.key == PConstants.CODED) {
      switch (p.keyCode) {
      case PApplet.UP:
        moveByKey(DIR_UP);
        break;
      case PApplet.DOWN:
        moveByKey(DIR_DOWN);
        break;
      case PApplet.LEFT:
        moveByKey(DIR_LEFT);
        break;
      case PApplet.RIGHT:
        moveByKey(DIR_RIGHT);
        break;
      }
    }
  }

  public void mouseWheel(int step) {
    logScale = PApplet.constrain(logScale + step * scaleVelocity,
      minLogScale,
      maxLogScale);
    float prevScale = scale;
    scale = (float) Math.pow(2, logScale);

    PVector mouse = new PVector(p.mouseX, p.mouseY);
    pan = PVector.add(mouse,
      PVector.mult(PVector.sub(pan, mouse), scale / prevScale));
  }

  private void moveByKey(PVector direction) {
    pan.add(PVector.mult(direction, panVelocity));
  }

  public float getScale() {
    return scale;
  }

  public void setScale(float scale) {
    this.scale = scale;
  }

  public PVector getPan() {
    return pan;
  }

  public void setPan(PVector pan) {
    this.pan = pan;
  }

  public void setPanVelocity(float panVelocity) {
    this.panVelocity = panVelocity;
  }

  public void setScaleVelocity(float scaleVelocity) {
    this.scaleVelocity = scaleVelocity;
  }

  public void setMinLogScale(float minLogScale) {
    this.minLogScale = minLogScale;
  }

  public void setMaxLogScale(float maxLogScale) {
    this.maxLogScale = maxLogScale;
  }
}