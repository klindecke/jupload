package wjhk.jupload;

import java.awt.*;

public class StatusPanelImp extends Panel implements StatusPanel{

  //------------- INFORMATION --------------------------------------------
  public static final double VERSION = 1.0;
  public static final String AUTHOR = "William Kwong Jinhua";
  public static final String AUTHOREMAIL = "wjhkwong@yahoo.com";
  public static final String DESCRIPTION = "StatusPanel Implementation.";
  public static final String LASTMODIFIED = "01 July 2002";

  private TextArea status;

  public StatusPanelImp(){
    this.setLayout(new BorderLayout());
    status = new TextArea();
    status.setRows(5);
    status.setColumns(20);
    status.setEditable(false);
    this.add(status);
  }

  public void writeCommand(String s){
    status.append("Command :> " + s + "\n");
  }
  public void writeStatus(String s){
    status.append(s);
  }
}