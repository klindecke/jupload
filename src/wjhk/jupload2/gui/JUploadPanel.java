package wjhk.jupload2.gui;

import java.io.File;

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
import javax.swing.Timer;

import wjhk.jupload2.policies.UploadPolicy;
import wjhk.jupload2.policies.UploadPolicyFactory;
import wjhk.jupload2.upload.FileUploadThread;
import wjhk.jupload2.upload.FileUploadThreadV4;

/**
 * Main code for the applet (or frame) creation. It contains all creation for necessary
 * elements, or calls to {@link wjhk.jupload2.policies.UploadPolicy} to allow easy personalization.
 */
public class JUploadPanel extends JPanel implements ActionListener{

	/**
	 * 
	 */
	private static final long serialVersionUID = -1212601012568225757L;
//	------------- INFORMATION --------------------------------------------
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
	private JButton browse, remove, removeAll;
	private JFileChooser fileChooser = null;

	private FilePanel filePanel = null;

	private JPanel progressPanel;
	private JButton upload, stop;
	private JProgressBar progress = null;

	private JUploadTextArea statusArea = null;

	private Timer timer = null;

	private UploadPolicy uploadPolicy = null;

	private FileUploadThread fileUploadThread = null;
	//------------- CONSTRUCTOR --------------------------------------------


	/**
	 * Constructor to call, when running from outside of an applet : this is taken from the 
	 * version 1 of Jupload. It can be used for test (like originally), but this is now 
	 * useless as eclipse allow direct execution of applet, for tests. It can also be used
	 * to use this code from within a java application.
	 *  
	 * @param containerParam The container, where all GUI elements are to be created.
	 * @param statusParam The status area that should already have been created. This allows putting text into it, before the effective creation of the layout.
	 * @param uploadPolicyParam The current UploadPolicy. Null if a new one must be created.
	 * @see UploadPolicyFactory#getUploadPolicy(wjhk.jupload2.JUploadApplet)
	 */
	public JUploadPanel(Container containerParam, JUploadTextArea statusParam, UploadPolicy uploadPolicyParam) throws Exception {
		this.statusArea = statusParam;
		this.uploadPolicy = uploadPolicyParam;
		
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		// Setup Top Panel
		setupTopPanel();

		// Setup File Panel.
		this.filePanel = (null==filePanel)?new FilePanelTableImp(this, uploadPolicy):filePanel;
		this.add((Container)this.filePanel);

		// Setup Progress Panel.
		setupProgressPanel(upload, progress, stop);

		// Setup Status Area.
		setupStatus();

		// Setup File Chooser.
		try{
			this.fileChooser = new JFileChooser();
			fileChooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
			fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
			fileChooser.setMultiSelectionEnabled(true);
		}catch(Exception e){
			this.statusArea.append("ERROR  : " + e.getMessage() + "\n");
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

	private void setupStatus(){
		JScrollPane pane = new JScrollPane();
		pane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		pane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

		pane.getViewport().add(statusArea);
		this.add(pane);
	}

	//----------------------------------------------------------------------
	protected void addFiles(File[] f) {
		filePanel.addFiles(f);
		if(0 < filePanel.getFilesLength()){
			remove.setEnabled(true);
			removeAll.setEnabled(true);
			upload.setEnabled(true);
		}
	}

	public void actionPerformed(ActionEvent e) {
		statusArea.append("Action : " + e.getActionCommand() + "\n");
		if(e.getActionCommand() == browse.getActionCommand()){
			if(null!=fileChooser){
				try{
					if(JFileChooser.APPROVE_OPTION ==
						fileChooser.showOpenDialog(new Frame())){
						addFiles(fileChooser.getSelectedFiles());
					}
				}catch(Exception ex){
					statusArea.append("ERROR  : " + ex.getMessage() + "\n");
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

			//Check that the upload is ready (we ask the uploadPolicy. Then, we'll call beforeUpload for each 
			//FileData instance, that exists in allFiles[].

			/////////////////////////////////////////////////////////////////////////////////////////////////
			//IMPORTANT: It's up to the UploadPolicy to explain to the user that the upload is not ready!
			/////////////////////////////////////////////////////////////////////////////////////////////////
			if (uploadPolicy.isUploadReady()) {
				uploadPolicy.beforeUpload();
				
				browse.setEnabled(false);
				remove.setEnabled(false);
				removeAll.setEnabled(false);
				upload.setEnabled(false);
				stop.setEnabled(true);

				fileUploadThread = new FileUploadThreadV4(filePanel.getFiles(), uploadPolicy, progress);
				fileUploadThread.start();


				//Create a timer.
				timer = new Timer(DEFAULT_TIMEOUT, new ActionListener() {
					public void actionPerformed(ActionEvent evt) {
						if(!fileUploadThread.isAlive()){
							uploadPolicy.displayDebug("JUploadPanel: after !fileUploadThread.isAlive()", 60);
							timer.stop();
							String svrRet=fileUploadThread.getServerOutput();
							Exception e = fileUploadThread.getException();
							
							//Restore enable state, as the upload is finished.
							stop.setEnabled(false);

							browse.setEnabled(true);
							remove.setEnabled(true);
							removeAll.setEnabled(true);
							upload.setEnabled(true);

							//Free resources of the upload thread.
							fileUploadThread.close();
							fileUploadThread = null;

							uploadPolicy.afterUpload(e, svrRet);
							// Do something (eg Redirect to another page for processing).
							//EGR if((null != aus) && isSuccess) aus.executeThis(svrRet);

						}
					}
				});
				timer.start();
				uploadPolicy.displayDebug("Timer started", 60);

			}//if isIploadReady()
		}else if(e.getActionCommand() == stop.getActionCommand()){
			//We request the thread to stop its job. 
			fileUploadThread.stopUpload();
			/*
			 * There was a bug here: this command should only ask the thread to stop.
			 * 
			 * It's up to the timer to wait until the thread's end. This insure that all ressources are freed 
			 * by it
			 
			stop.setEnabled(false);
			if(null != timer){
				timer.stop();
			}
			timer = null;
			if(null != fileUploadThread){
				if(fileUploadThread.isAlive()){
					fileUploadThread.stopUpload();
					try{
						fileUploadThread.join(1000);
					}catch(InterruptedException ie){}
				}
				fileUploadThread.close();
				
				bug ici: il faut utiliser le timer, pour attendre la fin de la thread.
				Fonction de libération à mutualiser
				
				Hum, un click sur stop ne devrait fare qu'appeler fileUploadThread.stop().
				Le timer gèrera la libération de ress et les boutons.
			}
			fileUploadThread = null;
			remove.setEnabled(true);
			removeAll.setEnabled(true);
			upload.setEnabled(true);
			browse.setEnabled(true);
			*/
		}
	}//actionPerformed

	/**
	 * @return the uploadPolicy
	 */
	public UploadPolicy getUploadPolicy() {
		return uploadPolicy;
	}

	/**
	 * @return the filePanel
	 */
	public FilePanel getFilePanel() {
		return filePanel;
	}


}

