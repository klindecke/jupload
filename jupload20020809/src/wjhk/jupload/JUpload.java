package wjhk.jupload;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class JUpload extends JFrame{

  //------------- INFORMATION --------------------------------------------
  public static final double VERSION = 1.0;
  public static final String AUTHOR = "William Kwong Jinhua";
  public static final String AUTHOREMAIL = "wjhkwong@yahoo.com";
  public static final String DESCRIPTION = "Java Frame wrapper for JUploadPanel.";
  public static final String LASTMODIFIED = "01 July 2002";

  public JUpload(String postURL){
    super("Java Multiple Upload Frame.");
    this.addWindowListener(
      new WindowAdapter() {
        public void windowClosing(WindowEvent e) {
          System.exit(0);
        }
      }
    );
    this.setSize(640,300);

    Container c = this.getContentPane();
    c.setLayout(new BorderLayout());

    JUploadPanel jp = new JUploadPanel(postURL, null,
         null, null, null, null, null, null);
    jp.addProgressPanel(new ProgressPanelImp());
    jp.addDoAfterUploadSucc(new AfterUploadSuccImp());

    c.add(jp, BorderLayout.CENTER);
    this.show();
  }

  //Main method
  public static void main(String[] args) {
    String postURL = "http://localhost:8080/writeOut.jsp";
    JUpload ju = new JUpload(postURL);
  }

}