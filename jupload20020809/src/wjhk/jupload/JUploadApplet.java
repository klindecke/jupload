package wjhk.jupload;

import java.applet.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import javax.swing.*;

public class JUploadApplet extends Applet{

  //------------- INFORMATION --------------------------------------------
  public static final double VERSION = 1.0;
  public static final String AUTHOR = "William Kwong Jinhua";
  public static final String AUTHOREMAIL = "wjhkwong@yahoo.com";
  public static final String DESCRIPTION = "Java Applet wrapper for JUploadPanel.";
  public static final String LASTMODIFIED = "01 July 2002";

  public final static String DEFAULT_POST_URL = "http://localhost:8080/";
  //----------------------------------------------------------------------

  private boolean isStandalone = false;
  //Get a parameter value
  public String getParameter(String key, String def) {
    return isStandalone ? System.getProperty(key, def) :
      (getParameter(key) != null ? getParameter(key) : def);
  }

  //----------------------------------------------------------------------

  //Initialize the applet
  public void init() {
    // Getting Parameters.
    String postURL = this.getParameter("postURL", DEFAULT_POST_URL);

    this.setLayout(new BorderLayout());

    JUploadPanel jp = new JUploadPanel(postURL, null,
         null, null, null, null, null, null);
    jp.addProgressPanel(new ProgressPanelImp());
    jp.addDoAfterUploadSucc(new AfterUploadSuccImp());

    this.add(jp, BorderLayout.CENTER);
  }
}