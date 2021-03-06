import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import oscP5.*; 
import netP5.*; 
import websockets.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class oscr_pde extends PApplet {

/*
 *  O Muro do Jerico by nunof - 20170206
 *  or just a proof of concept of an OSC server that receives input movement data from user device's sensors and verses from Poemario.js
 */
 




OscP5 oscP5;
WebsocketServer ws;
String full_msg = "INIT";
PImage map;
int imgW;
int imgH;
int centerX;
int centerY;
PanZoom pz;
String pverse = "";
ArrayList<String> muro = new ArrayList<String>();
PFont wfont;
 
public void setup() {
  map = loadImage("wall.png");
 
  imgW = map.width;
  imgH = map.height;
  centerX = imgW / 2;
  centerY = imgH / 2;
 
  //size(1024, 650);
  
  frameRate(2);
  
  noLoop();
    
  pz = new PanZoom(this);

  oscP5 = new OscP5(this, 8891);
  ws = new WebsocketServer(this, 8892, "/poemario");
  
  //String[] fontList = PFont.list();
  //printArray(fontList);
  wfont = createFont("MontserratAlternates-ExtraBold", 16); 
  
}
 
 
public void draw() {
 
  PVector pan = pz.getPan();
  float scale = pz.getScale();
  
  //background(64);
  background(64, 0, 0);
  
  pushMatrix();
 
   
   textSize(26);
   fill(0, 102, 153);
   rotate(0.3f);
   for (int j = 20; j <= 10000; j += 400) {
     for (int l = -400; l <= 640; l += 40) {
       text("O muro do Jerico", j, l);
     }
   }
   rotate(-0.3f);
   
 
  translate(pan.x, pan.y);
  scale(scale);

  imageMode(CENTER);
  image(map, centerX, centerY, imgW, imgH);
  write_on_the_wall();
  
  popMatrix();
}

public void write_on_the_wall() {
  int curX = 10;
  int curY = 250;
  textFont(wfont);
  textSize(16);
  fill(0, 180);
  for (int i = 0; i < muro.size(); i++) {
    text(muro.get(i), curX, curY);
    if (curY >= 620) {
      if (curX >= 9999) {
        curX = 10;
        curY = 250;
      }
      else {
        curX += 400;
        curY = 250;
      }
    }
    else {
      curY += 30;
    }
  }
}

public void keyPressed() {
  pz.keyPressed();
  redraw();
}

public void mouseDragged() {
  pz.mouseDragged();
  redraw();
}

public void mouseWheel(MouseEvent event) {
  pz.mouseWheel(event.getCount());
  redraw();
}
 
public void oscEvent(OscMessage osc_msg) {
  String msg_addr = osc_msg.addrPattern();

  if (msg_addr.equals("/inputmov")) {
    Object[] msg_args = osc_msg.arguments();
    if (msg_args.length == 4 && osc_msg.checkTypetag("siii")) {
        full_msg = "full_msg: " + msg_args[0] + ":" + msg_args[1] + ":" + msg_args[2] + ":" + msg_args[3];
        int x = (int)msg_args[1];
        int y = (int)msg_args[2];
        int z = (int)msg_args[3];
        if (x != 0) pz.accelMoveX(x);
        if (y != 0) pz.accelMoveY(y);
        if (z != 0) pz.mouseWheel(z);
        //println("x="+x+";y="+y+";z="+z);
        redraw();
    }
  }
}

public void webSocketServerEvent(String ws_msg){
  pverse = ws_msg;
  //println(pverse);
  muro.add(pverse);
  redraw(); 
}
  public void settings() {  fullScreen();  smooth(); }
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "oscr_pde" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
