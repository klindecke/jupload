package wjhk.jupload;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class ButtonImpBrowse extends Panel implements Button{

  //------------- INFORMATION --------------------------------------------
  public static final double VERSION = 1.0;
  public static final String AUTHOR = "William Kwong Jinhua";
  public static final String AUTHOREMAIL = "wjhkwong@yahoo.com";
  public static final String DESCRIPTION = "Button Implementation.";
  public static final String LASTMODIFIED = "01 July 2002";

  private JButton button;
  private String actionCommand;

  public ButtonImpBrowse(){
    this.setLayout(new BorderLayout());
    actionCommand = "Browse...";
    button = new JButton(actionCommand);
    ImageIcon icon = new ImageIcon(getClass().getResource("/images/explorer.gif"));
    button.setIcon(icon);
    this.add(button);
  }

  public void addActionListener(ActionListener al){
    button.addActionListener(al);
  }

  public String getActionCommand(){
    return actionCommand;
  }

}