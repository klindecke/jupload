package wjhk.jupload;

import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class ProgressFrameImp extends JFrame implements ProgressFrame{

  //------------- INFORMATION --------------------------------------------
  public static final double VERSION = 1.0;
  public static final String AUTHOR = "William Kwong Jinhua";
  public static final String AUTHOREMAIL = "wjhkwong@yahoo.com";
  public static final String DESCRIPTION = "Uploading Progress Frame Implementation.";
  public static final String LASTMODIFIED = "01 July 2002";

  //----------------------------------------------------------------------

  private Label ltd, lcfd;
  private JProgressBar ptp, pcfp;

  //------------- CONSTRUCTORS -------------------------------------------
  public ProgressFrameImp(){
    super("File Upload Progress Bar");
    this.addWindowListener(
      new WindowAdapter() {
        public void windowClosing(WindowEvent e) {
          e.getWindow().dispose();
        }
      }
    );

    this.setSize(380,130);

    Label lt = new Label("Files Uploaded/Total : ");
    lt.setAlignment(Label.CENTER);

    ltd = new Label();
    ltd.setAlignment(Label.CENTER);

    Label ltp = new Label("Total Progress : ");
    ltp.setAlignment(Label.CENTER);

    ptp = new JProgressBar(0, 100);
    ptp.setValue(0);
    ptp.setStringPainted(true);

    Label lcf = new Label("Uploading File : ");
    lcf.setAlignment(Label.CENTER);

    lcfd = new Label();
    lcfd.setAlignment(Label.CENTER);

    Label lcfp = new Label("Current File Progress : ");
    lcfp.setAlignment(Label.CENTER);

    pcfp = new JProgressBar(0, 100);
    pcfp.setValue(0);
    pcfp.setStringPainted(true);

    Container cd = this.getContentPane();
    cd.setLayout(new GridLayout(0,2));
    cd.add(lt);
    cd.add(ltd);
    cd.add(ltp);
    cd.add(ptp);
    cd.add(lcf);
    cd.add(lcfd);
    cd.add(lcfp);
    cd.add(pcfp);
  }

  public void updateDisplay(FileUploadThread fut){
    ltd.setText(fut.getNumUploadedFiles() + " / " + fut.getNumTotalFiles());

    long tFileSize = 0;
    int upFileSize = 0;
    File[] tFiles = fut.getTotalFiles();
    File[] upFiles = fut.getUploadedFiles();
    for(int i = 0; i < tFiles.length; i++){
      tFileSize += tFiles[i].length();
    }
    for(int i = 0; i < upFiles.length; i++){
      upFileSize += upFiles[i].length();
    }
    ptp.setValue((int)((upFileSize * 100)/ tFileSize ));
    upFiles = null;
    tFiles = null;

    File upFile = fut.getFileUploading();
    // If Server is unreachable then it will be null.
    lcfd.setText((null==upFile)?"":upFile.getName());
    pcfp.setValue((int)(fut.getUploadedBytes(upFile) /  upFile.length()));
  }
}