package wjhk.jupload;

import java.awt.*;
import javax.swing.*;

public class ProgressPanelImp extends Panel implements ProgressPanel{

  //------------- INFORMATION --------------------------------------------
  public static final double VERSION = 1.0;
  public static final String AUTHOR = "William Kwong Jinhua";
  public static final String AUTHOREMAIL = "wjhkwong@yahoo.com";
  public static final String DESCRIPTION = "Progress Implementation.";
  public static final String LASTMODIFIED = "01 July 2002";

  private JProgressBar pgBar;

  //------------- CONSTRUCTORS -------------------------------------------
  public ProgressPanelImp(){
    this.setLayout(new BorderLayout());
    pgBar = new JProgressBar(0, 1000);
    pgBar.setValue(0);
    pgBar.setStringPainted(true);
    this.add(pgBar);
  }

  //----------------------------------------------------------------------
  public void updateDisplay(float percentage){
    pgBar.setValue((int)(percentage * 10));
  }
}