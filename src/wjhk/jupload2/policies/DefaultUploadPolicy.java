/*
 * Created on 4 mai 2006
 */
package wjhk.jupload2.policies;


import java.applet.Applet;
import java.awt.GridLayout;
import java.io.File;
import java.util.Iterator;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import wjhk.jupload2.JUploadApplet;
import wjhk.jupload2.filedata.FileData;
import wjhk.jupload2.gui.FilePanel;

/**
 * This class implements all {@link wjhk.jupload2.policies.UploadPolicy} methods. Its
 * way of working is he same as the JUpload version 1.
 * <BR>
 * Functionnalities:
 * <UL>
 * <LI>Default implementation for all {@link wjhk.jupload2.policies.UploadPolicy} methods.
 * <LI> Files are uploaded all in one HTTP request.
 * <LI> No handling for particular kind of files: files are transmitted without any transformation.
 * </UL>
 * 
 * @author Etienne Gauthier
 */

public class DefaultUploadPolicy implements UploadPolicy {

	/**
	 * theApplet contains the reference of the Applet. It's useful to interact with it.
	 */
	Applet theApplet = null;
	
	/**
	 * The URL where files should be posted : no default value.
	 */
	String postURL = null;	
	
	/**
	 * The applet will do as may HTTP requests to upload all files, with the number 
	 * as a maximum number of files for each HTTP request.
	 * <BR>
	 * Default : -1 
	 */
	int maxFilesPerUpload;
	
	/**
	 * @see UploadPolicy#getServerProtocol()
	 */
	String serverProtocol;
	
	/**
	 * This Vector contains headers that will be added for each upload. It may contains
	 * cookies, for instance.
	 * 
	 * @see #onAppendHeader(StringBuffer)
	 */
	private Vector headers = new Vector();
	
	/**
	 * The text area, where message are to be displayed.
	 * 
	 * @see #displayMsg(String)
	 */
	private JTextArea status = null;

	/**
	 * The resourceBundle contains all localized String (and others ??)
	 */
	ResourceBundle resourceBundle = null;
	
	/**
	 * The current debug level.
	 */
	private int debugLevel = 0;

	//////////////   Constructors
	
	
	/**
	 * The main constructor : use default values, and the given postURL.
	 * 
	 * @param postURL The URL where files should be uploaded. 
	 */
	protected DefaultUploadPolicy(String postURL, Applet theApplet, int debugLevel, JTextArea status) {
		this(postURL, -1, theApplet, debugLevel, status);
	}
	
	/**
	 * This constructor allows the caller to control the number of files for each 
	 * HTTP request toward the server.
	 * 
	 * @param postURL The URL where files should be uploaded.
	 * @param maxFilesPerUpload Number maximum of files for each HTTP Request. 
	 */
	DefaultUploadPolicy(String postURL, int maxFilesPerUpload, Applet theApplet, int debugLevel, JTextArea status) {
		//Call default constructor for all default initialization;.
		this.postURL = postURL;
		this.maxFilesPerUpload = maxFilesPerUpload;
		this.theApplet = theApplet;
		this.debugLevel = debugLevel;
		this.status = status;
		
		displayInfo("JUpload applet, version " + JUploadApplet.VERSION + " (" + JUploadApplet.LAST_MODIFIED + "), available at http://jupload.sourceforge.net/");
		
		//get the server protocol. 
		serverProtocol = UploadPolicyFactory.getParameter(theApplet, PROP_SERVER_PROTOCOL, DEFAULT_SERVER_PROTOCOL);

		//Get resource file.
		String lang = UploadPolicyFactory.getParameter(theApplet,PROP_LANG, DEFAULT_LANG);
		Locale locale;
		if (lang == null) {
			displayInfo("lang = null, taking default language");
			locale = Locale.getDefault();
		} else {
			locale = new Locale(lang);
		}
		displayDebug("debug : " + debugLevel, 1); 
		displayDebug("serverProtocole : " + serverProtocol, 20); 
		displayDebug("Java version  : " + System.getProperty("java.version"), 1); 
		displayDebug("lang (parameter) : " + lang, 20);
		displayDebug("language : " + locale.getLanguage(), 20);
		displayDebug("country : " + locale.getCountry(), 20);
		resourceBundle = ResourceBundle.getBundle("wjhk.jupload2.lang.lang", locale);
	}
		
	/**
	 * @see wjhk.jupload2.policies.UploadPolicy#getMaxFilesPerUpload()
	 */
	public int getMaxFilesPerUpload() {
		return maxFilesPerUpload;
	}

	/**
	 * @see wjhk.jupload2.policies.UploadPolicy#getPostURL()
	 */
	public String getPostURL() {
		return postURL;
	}


	/**
	 * @see wjhk.jupload2.policies.UploadPolicy#setPostURL(String)
	 */
	public void setPostURL(String postURL) {
		this.postURL = postURL;
	}

	/**
	 * 
	 * Default implementation of {@link wjhk.jupload2.policies.UploadPolicy#createTopPanel(JButton, JButton, JButton, JPanel)}. IT
	 * creates a JPanel, containing the three given JButton.  
	 * 
	 * @see wjhk.jupload2.policies.UploadPolicy#createTopPanel(JButton, JButton, JButton, JPanel)
	 */
	public JPanel createTopPanel(JButton browse, JButton remove, JButton removeAll, JPanel mainPanel) {
	    JPanel jPanel = new JPanel();
	    jPanel.setLayout(new GridLayout(1,3));
	    jPanel.add(browse);
	    jPanel.add(removeAll);
	    jPanel.add(remove);
		return jPanel;
	}


	/**
	 * Displays a message. If the status panel is set, the message is displayed on it.
	 * If not, the System.out.println function is used.
	 * 
	 * @param msg The message to display.
	 */
	private void displayMsg (String msg) {
		if (status == null) {
			System.out.println(msg);
		} else {
			status.append(msg);
			status.append("\n");
		}
	}
	public void displayErr (Exception e) {
		displayErr (e.getClass().getName() + ": " + e.getLocalizedMessage());
	}
	public void displayErr (String err) {
		displayMsg ("-ERROR- " + err);
	}
	public void displayInfo (String info){
		displayMsg ("-INFO- " + info);
	}
	public void displayWarn (String warn){
		displayMsg ("-WARNING- " + warn);
	}
	public void displayDebug (String debug, int minDebugLevel){
		if (debugLevel >= minDebugLevel) {
			displayMsg ("-DEBUG- " + debug);
		}
	}

	/**
	 * @return Returns the status.
	 */
	public JTextArea getStatus() {
		return status;
	}

	/**
	 * @see wjhk.jupload2.policies.UploadPolicy#addHeader(java.lang.String)
	 */
	public void addHeader(String header) {
		headers.add(header);		
	}


	/**
	 * @see UploadPolicy#onAppendHeader(StringBuffer)
	 */
	public StringBuffer onAppendHeader(StringBuffer sb) {
		Iterator it = headers.iterator();
		String header;
		while (it.hasNext()) {
			header = (String)it.next();
			displayDebug (header, 90);
			sb.append(header);
			sb.append("\r\n");
		}
		return sb;
	}//appendHeader

	/**
	 * @see UploadPolicy#createFileData(File)
	 */
	public FileData createFileData(File file) {
		return new FileData(file);
	}

	/**
	 * @see wjhk.jupload2.policies.UploadPolicy#setProperty(java.lang.String, java.lang.String)
	 */
	public void setProperty(String prop, String value) {
		// Default : nothing to do.	
	}
	
	/**
	 * @see wjhk.jupload2.policies.UploadPolicy#isUploadReady()
	 */
	public boolean isUploadReady() {
		// Default :  nothing to do before upload, so we're ready.
		return true;
	}
	
	/**
	 * @see wjhk.jupload2.policies.UploadPolicy#afterUpload(FilePanel, Exception, String)
	 */
	public void afterUpload(FilePanel filePanel, Exception e, String serverOutput) {
        if(null != e){
          	status.append("ERROR  : " + e.toString() + "\n");          
        } else {
          	status.append("INFO   : " + filePanel.getFilesLength() + " Files uploaded.\n");
          	filePanel.removeAll();
            displayMsg("INFO   : -------- Server Output Start --------\n");
            displayMsg(serverOutput + "\n");
            displayMsg("INFO   : --------- Server Output End ---------\n");
        }
	}

	/**
	 * @see UploadPolicy#getUploadFilename(FileData, int)
	 */
	public String getUploadFilename (FileData fileData, int index) {
		//This is the original way of working of JUpload.
		//It can easily be modified, by using another UploadPolicy.
		return "File" + index;
	}

	/**
	 * A protected getter, that returns the current Applet.
	 * 
	 * @return Reference to the applet.
	 */
	Applet getApplet() {
		return theApplet;
	}
	/**
	 * @see UploadPolicy#getString(String)
	 */
	public String getString(String key) {
		String ret = resourceBundle.getString(key);
		return ret;
	}
	
	/**
	 * @see UploadPolicy#getString(String,String)
	 */
	public String getString(String key, String value1) {
		String ret = resourceBundle.getString(key).replaceAll("\\{1\\}", value1);
		return ret;
	}
	
	/**
	 * @see UploadPolicy#getString(String,int)
	 */
	public String getString(String key, int value1) {
		return getString(key, Integer.toString(value1));
	}
	
	/**
	 * Default implementation of the {@link wjhk.jupload2.policies.UploadPolicy#onSelectFile(wjhk.jupload2.filedata.FileData)}.
	 * Nothing's done.
	 */
	public void onSelectFile(FileData fileData) {
		// Default implementation : no action	
	}
	
	/**
	 * @see UploadPolicy#setDebugLevel(int)
	 */
	public void setDebugLevel(int debugLevel) {
		this.debugLevel = debugLevel;
	}
	
	/**
	 * @see UploadPolicy#getDebugLevel()
	 */
	public int getDebugLevel() {
		return debugLevel;
	}

	/**
	 * @see wjhk.jupload2.policies.UploadPolicy#getServerProtocol()
	 */
	public String getServerProtocol() {
		return serverProtocol;
	}

}
