package wjhk.jupload2.gui;

import java.io.File;

import java.awt.Container;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.Timer;

import wjhk.jupload2.policies.UploadPolicy;
import wjhk.jupload2.policies.UploadPolicyFactory;
import wjhk.jupload2.upload.FileUploadThread;
import wjhk.jupload2.upload.FileUploadThreadFTP;
import wjhk.jupload2.upload.FileUploadThreadHTTP;

/**
 * Overwriting the default Append class such that it scrolls to the bottom of
 * the text. The JFC doesn't always remember to do that. <BR>
 */

class JUploadPopupMenu extends JPopupMenu implements ActionListener, ItemListener {

	/** A generated serialVersionUID */
	private static final long serialVersionUID = -5473337111643079720L;
	
	/**
	 * Identifies the menu item that will set debug mode on or off (on means: debugLevel=100)
	 */
	JCheckBoxMenuItem cbMenuItemDebugOnOff = null;
	
	/**
	 * The current upload policy.
	 */
	private UploadPolicy uploadPolicy;

	
	JUploadPopupMenu(UploadPolicy uploadPolicy) {
		this.uploadPolicy = uploadPolicy;
		//Creation of the menu items
		cbMenuItemDebugOnOff = new JCheckBoxMenuItem("Debug on");
		add(cbMenuItemDebugOnOff);
		cbMenuItemDebugOnOff.addItemListener(this);		
	}

	/**
	 * This methods receive the event triggered by our popup menu.
	 */
	public void actionPerformed(ActionEvent action) {
		// TODO Auto-generated method stub		
	}

	public void itemStateChanged(ItemEvent e) {
		if (cbMenuItemDebugOnOff == e.getItem()) {
			uploadPolicy.setDebugLevel( (cbMenuItemDebugOnOff.isSelected() ? 100 : 0) );
		}
		// TODO Auto-generated method stub
		
	}
	
}

/**
 * Main code for the applet (or frame) creation. It contains all creation for necessary
 * elements, or calls to {@link wjhk.jupload2.policies.UploadPolicy} to allow easy personalization.
 */
public class JUploadPanel extends JPanel implements ActionListener, MouseListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1212601012568225757L;
	
	/** The popup menu of the applet */
	private JUploadPopupMenu jUploadPopupMenu;
	
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

	private JScrollPane jStatusScrollPane = null;
	private JUploadTextArea statusArea = null;
	private boolean isStatusAreaVisible = false;

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
		jUploadPopupMenu = new JUploadPopupMenu(uploadPolicy);
		
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		statusParam.addMouseListener(this);

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
			fileChooser = new JFileChooser();
			//BasicFileChooserUI fileChooser2 = new BasicFileChooserUI(fileChooser);
			fileChooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
			fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
			fileChooser.setMultiSelectionEnabled(true);
			fileChooser.setFileFilter(new JUploadFileFilter(uploadPolicy));
			fileChooser.setFileView(new JUploadFileView(uploadPolicy));
			//fileChooser.setFileView(new BasicFileChooserUI.BasicFileView());
		}catch(Exception e){
			uploadPolicy.displayErr(e);
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
		jStatusScrollPane = new JScrollPane();
		jStatusScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		jStatusScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

		jStatusScrollPane.getViewport().add(statusArea);
		
		//See viewStatusBar
		showOrHideStatusBar();
		//this.add(jStatusScrollPane);
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
		uploadPolicy.displayDebug("Action : " + e.getActionCommand(), 1);
		if(e.getActionCommand() == browse.getActionCommand()){
			if(null!=fileChooser){
				try{
					if(JFileChooser.APPROVE_OPTION ==
						fileChooser.showOpenDialog(new Frame())){
						addFiles(fileChooser.getSelectedFiles());
					}
				}catch(Exception ex){
					uploadPolicy.displayErr(ex);
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

				//The FileUploadThread instance depends on the protocol.
				if (uploadPolicy.getPostURL().substring(0, 4).equals("ftp:")) {
					//fileUploadThread = new FileUploadThreadFTP(filePanel.getFiles(), uploadPolicy, progress);
					fileUploadThread = new FileUploadThreadFTP(filePanel.getFiles(), uploadPolicy, progress);
				} else {
					//fileUploadThread = new FileUploadThreadV4(filePanel.getFiles(), uploadPolicy, progress);
					fileUploadThread = new FileUploadThreadHTTP(filePanel.getFiles(), uploadPolicy, progress);
				}
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

	/**
	 * This methods show or hides the statusArea, depending on the following applet parameters. The following 
	 * conditions must be met, to hide the status area:
	 * <DIR>
	 * <LI>showStatusBar (must be False)
	 * <LI>debugLevel (must be 0 or less)
	 * </DIR>
	 *
	 */
	public void showOrHideStatusBar() {
		if (uploadPolicy.getShowStatusBar() || uploadPolicy.getDebugLevel()>0) {
			//The status bar should be visible. Is it visible already?
			if (!isStatusAreaVisible) {
				add(jStatusScrollPane, -1);
				isStatusAreaVisible = true;
				//Let's recalculate the component display
				validate();
			}
		} else {
			//It should be hidden.
			if (isStatusAreaVisible) {
				remove(jStatusScrollPane);
				isStatusAreaVisible = false;
				//Let's recalculate the component display
				validate();
			}
		}
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////   MouseListener interface   //////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////	
	public void mouseClicked(MouseEvent mouseEvent) {
		maybeOpenPopupMenu(mouseEvent);
	}
	public void mouseEntered(MouseEvent mouseEvent) {
		maybeOpenPopupMenu(mouseEvent);
	}
	public void mouseExited(MouseEvent mouseEvent) {
		maybeOpenPopupMenu(mouseEvent);
	}
	public void mousePressed(MouseEvent mouseEvent) {
		maybeOpenPopupMenu(mouseEvent);
	}
	public void mouseReleased(MouseEvent mouseEvent) {
		maybeOpenPopupMenu(mouseEvent);
	}
	/**
	 * This method opens the popup menu, if the mouseEvent is relevant. In this case it returns true. Otherwise,
	 * it does nothing and returns false.
	 * 
	 * @param mouseEvent The triggered mouse event.
	 * @return true if the popup menu was opened, false otherwise.
	 */
	boolean maybeOpenPopupMenu(MouseEvent mouseEvent) {
		if (mouseEvent.isPopupTrigger()  &&  ( (mouseEvent.getModifiersEx()&InputEvent.CTRL_DOWN_MASK) == InputEvent.CTRL_DOWN_MASK)) {
			if (jUploadPopupMenu != null) {
				jUploadPopupMenu.show(mouseEvent.getComponent(), mouseEvent.getX(), mouseEvent.getY());
				return true;
			}
		}
		return false;
	}

}

