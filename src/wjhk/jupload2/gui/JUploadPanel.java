package wjhk.jupload2.gui;

import java.io.File;

import java.applet.Applet;
import java.awt.Container;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.Timer;


import wjhk.jupload2.policies.UploadPolicy;
import wjhk.jupload2.policies.UploadPolicyFactory;
import wjhk.jupload2.upload.FileUploadThreadV3;

/**
 * Main code for the applet (or frame) creation. It contains all creation for necessary
 * elements, or calls to {@link wjhk.jupload2.policies.UploadPolicy} to allow easy personalization.
 */
public class JUploadPanel extends JPanel implements ActionListener{

  //------------- INFORMATION --------------------------------------------
  public static final String TITLE = "JUpload JUploadPanel";
  public static final String DESCRIPTION =
      "Main Panel for JUpload Application/Applet.";
  public static final String AUTHOR = "William JinHua Kwong";

  public static final double VERSION = 1.3;
  public static final String LAST_MODIFIED = "20 July 2006 (E Gauthier)";

  // Timeout at DEFAULT_TIMEOUT milliseconds
  private final static int DEFAULT_TIMEOUT = 100;
  
  //------------- VARIABLES ----------------------------------------------
  private JPanel topPanel;
  private JPanel buttonPanel;
  private JButton browse, remove, removeAll;
  private JFileChooser fileChooser = null;

  private FilePanel filePanel = null;

  private JPanel progressPanel;
  private JButton upload, stop;
  private JProgressBar progress = null;

  private JTextArea status = null;

  private Timer timer;
  
  private UploadPolicy uploadPolicy;

  FileUploadThreadV3 pictureFileUploadThread;
  //------------- CONSTRUCTOR --------------------------------------------

  /**
   * Constuctor to call, when running in standard way : as an applet.
   * 
   */
  public JUploadPanel(Applet applet) {
  	init(applet, applet, null);
  }
  
  /**
   * Constructor to call, when running from outside of an applet : this is taken from the 
   * version 1 of Jupload. It can be used for test (like originally), but this is now 
   * useless as eclipse allow direct execution of applet, for tests. It can also be used
   * to use this code from within a java application.
   *  
   * @param container The container, where all GUI elements are to be created.
   * @param uploadPolicy The current UploadPolicy. Null if a new one must be created.
   * @see UploadPolicyFactory#getUploadPolicy(Applet, JTextArea)
   */
  public JUploadPanel(Container container, UploadPolicy uploadPolicy) {
  	init(null, container, uploadPolicy);
  }

  /**
   * Initialization code : called by all constructors.
   * 
   * @param applet The current applet. Null if not called from an applet.
   * @param container The current container. Contains the same value as 'applet', if called from an applet.
   * @param uploadPolicyParam The current UploadPolicy. Null if a new one must be created.
   */
  private void init (Applet applet, Container container, UploadPolicy uploadPolicyParam) {  	
    JTextArea status = new StatusArea(5, 20);
    this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

    if (uploadPolicyParam == null) {    	
        // Getting Parameters : this is done by getting the upload policy.
        this.uploadPolicy = UploadPolicyFactory.getUploadPolicy(applet, status);
    } else {
    	this.uploadPolicy = uploadPolicyParam;
    }
    // Setup Top Panel
    setupTopPanel();

    // Setup File Panel.
    this.filePanel = (null==filePanel)?new FilePanelTableImp(this, uploadPolicy):filePanel;
    this.add((Container)this.filePanel);

    // Setup Progress Panel.
    setupProgressPanel(upload, progress, stop);

    // Setup Status Area.
    setupStatus(status);

    // Setup File Chooser.
  try{
    this.fileChooser = new JFileChooser();
    fileChooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
    fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
    fileChooser.setMultiSelectionEnabled(true);
  }catch(Exception e){
    this.status.append("ERROR  : " + e.getMessage() + "\n");
  }
  }

  //----------------------------------------------------------------------


  private void setupTopPanel(){

    // -------- JButton browse --------
    browse = new JButton(uploadPolicy.getString("buttonBrowse"));
    browse.setIcon(new ImageIcon(getClass().getResource("/images/explorer.gif")));
    browse.addActionListener(this);

    // -------- JButton remove --------
	remove = new JButton(uploadPolicy.getString("buttonRemoveSelected"));
    remove.setIcon(new ImageIcon(getClass().getResource("/images/recycle.gif")));
    remove.setEnabled(false);
    remove.addActionListener(this);

    // -------- JButton removeAll --------
    removeAll = new JButton(uploadPolicy.getString("buttonRemoveAll"));
    removeAll.setIcon(new ImageIcon(getClass().getResource("/images/cross.gif")));
    removeAll.setEnabled(false);
    removeAll.addActionListener(this);

    // ------- Then ask the current upload policy to create the top panel --------//
    topPanel = uploadPolicy.createTopPanel(browse, remove, removeAll, this);
    this.add(topPanel);
  }

  private void setupProgressPanel(JButton jbUpload, JProgressBar jpbProgress, JButton jbStop){
    progressPanel = new JPanel();
    progressPanel.setLayout(new BoxLayout(progressPanel, BoxLayout.X_AXIS));

    // -------- JButton upload --------
    if(null==jbUpload){
      upload = new JButton(uploadPolicy.getString("buttonUpload"));
      upload.setIcon(new ImageIcon(getClass().getResource("/images/up.gif")));
    }else{
      upload = jbUpload;
    }
    upload.setEnabled(false);
    upload.addActionListener(this);
    progressPanel.add(upload);

    // -------- JProgressBar progress --------
    if(null == jpbProgress){
      progress = new JProgressBar(JProgressBar.HORIZONTAL);
      progress.setStringPainted(true);
    }else{
      progress = jpbProgress;
    }
    progressPanel.add(progress);

    // -------- JButton stop --------
    if(null==jbStop){
      stop = new JButton(uploadPolicy.getString("buttonStop"));
      stop.setIcon(new ImageIcon(getClass().getResource("/images/cross.gif")));
    }else{
      stop = jbStop;
    }
    stop.setEnabled(false);
    stop.addActionListener(this);
    progressPanel.add(stop);

    this.add(progressPanel);
  }

  private void setupStatus(JTextArea ta){
    // -------- JTextArea status --------
    if(null==ta){
      status = new StatusArea(5, 20);
    }else{
      status = ta;
    }

    JScrollPane pane = new JScrollPane();
    pane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
    pane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

    pane.getViewport().add(status);
    this.add(pane);
  }

  //----------------------------------------------------------------------
  protected void addFiles(File[] f){
    filePanel.addFiles(f);
    if(0 < filePanel.getFilesLength()){
      remove.setEnabled(true);
      removeAll.setEnabled(true);
      upload.setEnabled(true);
    }
  }

  public void actionPerformed(ActionEvent e) {
    status.append("Action : " + e.getActionCommand() + "\n");
    if(e.getActionCommand() == browse.getActionCommand()){
      if(null!=fileChooser){
        try{
          if(JFileChooser.APPROVE_OPTION ==
            fileChooser.showOpenDialog(new Frame())){
            addFiles(fileChooser.getSelectedFiles());
          }
        }catch(Exception ex){
          status.append("ERROR  : " + ex.getMessage() + "\n");
        }
      }
    }else if(e.getActionCommand() == remove.getActionCommand()){
      filePanel.removeSelected();
      if(0 >= filePanel.getFilesLength()){
        remove.setEnabled(false);
        removeAll.setEnabled(false);
        upload.setEnabled(false);
      }
    }else if(e.getActionCommand() == removeAll.getActionCommand()){
      filePanel.removeAll();
      remove.setEnabled(false);
      removeAll.setEnabled(false);
      upload.setEnabled(false);
    }else if(e.getActionCommand() == upload.getActionCommand()){
	    browse.setEnabled(false);
	    remove.setEnabled(false);
	    removeAll.setEnabled(false);
	    upload.setEnabled(false);
	    stop.setEnabled(true);

	  	pictureFileUploadThread = new FileUploadThreadV3(filePanel.getFiles(), uploadPolicy);
	    pictureFileUploadThread.setProgressPanel(progress);
	    pictureFileUploadThread.start();
	    boolean isSuccess = false;


	    //Create a timer.
	    timer = new Timer(DEFAULT_TIMEOUT, new ActionListener() {
	      public void actionPerformed(ActionEvent evt) {
	        if(!pictureFileUploadThread.isAlive()){
	          timer.stop();
	          boolean isSuccess = false;
	          StringBuffer svrRet=pictureFileUploadThread.getServerOutput();
	          Exception e = pictureFileUploadThread.getException();
	          /*EGR

	          if(null != pictureFileUploadThread.getException()){
	          	status.append("ERROR  : " + pictureFileUploadThread.getException().toString() + "\n");
	          }else{
	          	status.append("INFO   : " + filePanel.getFilesLength() + " Files uploaded.\n");
	          	filePanel.removeAll();
	            isSuccess = true;
	          }
	          */
	          pictureFileUploadThread.close();
	          pictureFileUploadThread = null;

	          uploadPolicy.afterUpload(filePanel, e, svrRet.toString());
	          // Do something (eg Redirect to another page for processing).
	          //EGR if((null != aus) && isSuccess) aus.executeThis(svrRet);

	        }
	      }
	    });
	    timer.start();
    	
        stop.setEnabled(false);
        if(!isSuccess){
          remove.setEnabled(true);
          removeAll.setEnabled(true);
          upload.setEnabled(true);
        }
        browse.setEnabled(true);
    }else if(e.getActionCommand() == stop.getActionCommand()){
	    stop.setEnabled(false);
	      if(null != timer){
	        timer.stop();
	      }
	      timer = null;
	      if(null != pictureFileUploadThread){
	        if(pictureFileUploadThread.isAlive()){
	          pictureFileUploadThread.stopUpload();
	          try{
	            pictureFileUploadThread.join(1000);
	          }catch(InterruptedException ie){}
	        }
	        pictureFileUploadThread.close();
	      }
	      pictureFileUploadThread = null;
		remove.setEnabled(true);
		removeAll.setEnabled(true);
		upload.setEnabled(true);
		browse.setEnabled(true);
    }
  }//actionPerformed

  
}

