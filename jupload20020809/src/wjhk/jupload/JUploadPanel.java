package wjhk.jupload;

import java.awt.*;
import java.awt.event.*;
import java.io.*;

public class JUploadPanel extends Panel{

  //------------- INFORMATION --------------------------------------------
  public static final double VERSION = 1.0;
  public static final String AUTHOR = "William Kwong Jinhua";
  public static final String AUTHOREMAIL = "wjhkwong@yahoo.com";
  public static final String DESCRIPTION = "Java Panel for Selecting Multiple files.";
  public static final String LASTMODIFIED = "01 July 2002";

  //----------------------------------------------------------------------
  public static final String DEFAULT_POST_URL = "http://localhost:8080/";

  //----------------------------------------------------------------------

  private GridBagLayout gb = null;
  private String postURL = null;

  private boolean isStandalone = false;

  private FilePanel fp = null;

  private StatusPanel sp = null;

  private Button bBrowse, bRemove, bRemoveAll, bUpload;

  private FileChooser fc = null;
  private ProgressPanel pp = null;
  private AfterUploadSucc aus = null;

  //------------- CONSTRUCTOR --------------------------------------------

  //Initialize the applet
  public JUploadPanel(String postURL,
                      FilePanel fp,
                      StatusPanel sp,
                      Button bBrowse,
                      Button bRemove,
                      Button bRemoveAll,
                      Button bUpload,
                      FileChooser fc) {

    gb = new GridBagLayout();
    this.setLayout(gb);

    this.postURL = (null==postURL)?DEFAULT_POST_URL:postURL;

    this.fp = (null==fp)?new FilePanelImp():fp;
    initDisplayFilePanel();

    this.sp = (null==sp)?new StatusPanelImp():sp;
    initDisplayTextAreaStatus();

    this.bBrowse = (null==bBrowse)?new ButtonImpBrowse():bBrowse;
    initDisplayButtonBrowse();

    this.bRemove = (null==bRemove)?new ButtonImpRemove():bRemove;
    initDisplayButtonRemove();

    this.bRemoveAll = (null==bRemoveAll)?new ButtonImpRemoveAll():bRemoveAll;
    initDisplayButtonRemoveAll();

    this.bUpload = (null==bUpload)?new ButtonImpUpload():bUpload;
    initDisplayButtonUpload();

    if(fc == null){
      try{
        this.fc = new FileChooserImp();
      }catch(Exception e){
        this.sp.writeStatus("File Chooser Exception: " + e.getMessage() + "\n");
      }
    }else{
      this.fc = fc;
    }
  }

  private void initDisplayFilePanel(){
    GridBagConstraints c = new GridBagConstraints();
    c.fill = GridBagConstraints.BOTH;
    c.gridx = 0;
    c.gridy = 1;
    c.gridwidth = 5;
    c.weightx = 1.0;
    c.weighty = 1.0;
    gb.setConstraints((Container)fp, c);
    this.add((Container)fp);
  }

  private void initDisplayTextAreaStatus(){
    GridBagConstraints c = new GridBagConstraints();
    c.fill = GridBagConstraints.HORIZONTAL;
    c.gridx = 0;
    c.gridy = 3;
    c.gridwidth = 5;
    gb.setConstraints((Container)sp, c);
    this.add((Container)sp);
  }

  private void initDisplayButtonBrowse(){
    bBrowse.addActionListener(new Command());
    GridBagConstraints c = new GridBagConstraints();
    c.gridx = 0;
    c.gridy = 0;
    gb.setConstraints((Container)bBrowse, c);
    this.add((Container)bBrowse);
  }

  private void initDisplayButtonRemove(){
    bRemove.addActionListener(new Command());
    GridBagConstraints c = new GridBagConstraints();
    c.gridx = 1;
    c.gridy = 0;
    gb.setConstraints((Container)bRemove, c);
    this.add((Container)bRemove);
  }

  private void initDisplayButtonRemoveAll(){
    bRemoveAll.addActionListener(new Command());
    GridBagConstraints c = new GridBagConstraints();
    c.gridx = 2;
    c.gridy = 0;
    gb.setConstraints((Container)bRemoveAll, c);
    this.add((Container)bRemoveAll);
  }

  private void initDisplayButtonUpload(){
    bUpload.addActionListener(new Command());
    GridBagConstraints c = new GridBagConstraints();
    c.anchor = GridBagConstraints.EAST;
    c.gridx = 4;
    c.gridy = 0;
    gb.setConstraints((Container)bUpload, c);
    this.add((Container)bUpload);
  }

  public void addProgressPanel(ProgressPanel pp){
    this.pp = pp;
    GridBagConstraints c = new GridBagConstraints();
    c.fill = GridBagConstraints.HORIZONTAL;
    c.gridx = 0;
    c.gridy = 2;
    c.gridwidth = 5;
    gb.setConstraints((Container)pp, c);
    this.add((Container)pp);
  }

  public void addDoAfterUploadSucc(AfterUploadSucc aus){
    this.aus = aus;
  }

  //----------------------------------------------------------------------

  private float calPer(FileUploadThread fut){
    float f = 0;
    f = fut.getUploadedFilesLength();
    f += fut.getUploadedBytes(fut.getFileUploading());
    f = 100 * f / fut.getTotalFilesLength();
    return f;
  }

  class Command implements ActionListener {
    public synchronized void actionPerformed(ActionEvent e) {
      sp.writeCommand(e.getActionCommand());
      if(e.getActionCommand() == bBrowse.getActionCommand()){
        if(null != pp) pp.updateDisplay(0);
        if(null!=fc){
          try{
            fc.showOpenDialog(new Frame());
            fp.addFiles(fc.getSelectedFiles());
          }catch(Exception ex){
            sp.writeStatus("File Chooser Exception: " + ex.getMessage() + "\n");
          }
        }
      }else if(e.getActionCommand() == bRemove.getActionCommand()){
        fp.removeSelected();
      }else if(e.getActionCommand() == bRemoveAll.getActionCommand()){
        fp.removeAll();
      }else if(e.getActionCommand() == bUpload.getActionCommand()){
        boolean isSuccess = false;
        StringBuffer svrRet;
        sp.writeStatus("POST URL = " + postURL + "\n");
        File[] arrayFiles = fp.getFiles();
        if(1 > arrayFiles.length){
          sp.writeStatus("Files Uploaded = 0\n");
        }else{
          FileUploadThread fut = new FileUploadThread(arrayFiles, postURL);
          fut.start();

          while(fut.isAlive()){
            try{
              if(null != pp) pp.updateDisplay(calPer(fut));
              // Wait Half a Second.
              wait(500);
            }catch(InterruptedException ie){}
          }
          if(null != pp) pp.updateDisplay(calPer(fut));

          if(null != fut.getException()){
            sp.writeStatus("Upload Exception : " + fut.getException().getMessage() + "\n");
          }else if(arrayFiles.length == fut.getUploadedFiles().length){
            fp.removeAll();
            sp.writeStatus("Files uploaded : " + arrayFiles.length + "\n");
            isSuccess = true;
          }
          svrRet = fut.getServerOutput();
          fut.close();
          fut = null;
          arrayFiles = null;
          // Do something (eg Redirect to another page for processing).
          if((null != aus) && isSuccess) aus.executeThis(svrRet, sp);
        }
      }
    }
  }
}

