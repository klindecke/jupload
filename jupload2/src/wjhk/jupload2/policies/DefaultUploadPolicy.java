/*
 * Created on 4 mai 2006
 */
package wjhk.jupload2.policies;

	
	
import java.applet.Applet;
import java.awt.GridLayout;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import netscape.javascript.JSException;
import netscape.javascript.JSObject;

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
	 * @see UploadPolicy#getStringUploadSuccess()
	 */
	String stringUploadSuccess;
	
	/**
	 * @see UploadPolicy#sendDebugInformation(String)
	 */
	String webmasterMail;
	
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

	/**
	 * This StringBuffer is used to store all information that could be useful, in case a problem occurs.
	 * Is content can then be sent to the webmaster. 
	 */
	private StringBuffer debugBufferString = new StringBuffer(); 
	//////////////   Constructors
	
	
	/**
	 * The main constructor : use default values, and the given postURL.
	 * 
	 * @param postURL The URL where files should be uploaded. 
	 */
	protected DefaultUploadPolicy(String postURL, Applet theApplet, int debugLevel, JTextArea status) {
		//Call default constructor for all default initialization;.
		this.postURL = postURL;
		this.theApplet = theApplet;
		this.debugLevel = debugLevel;
		this.status = status;
		
		displayInfo("JUpload applet, version " + JUploadApplet.VERSION + " (" + JUploadApplet.LAST_MODIFIED + "), available at http://jupload.sourceforge.net/");
		

		///////////////////////////////////////////////////////////////////////////////
		//Load session data read from the navigator: 
		// - cookies. 
		// - User-Agent : re
		//
		//cookie is the value of the javascript <I>document.cookie</I> property.
		String cookie;
		//userAgent is the value of the javascript <I>navigator.userAgent</I> property.
		String userAgent;

		try {
			JSObject applet = JSObject.getWindow(getApplet());
		    JSObject doc = (JSObject) applet.getMember("document");
		    cookie = (String) doc.getMember("cookie");
	
		    JSObject nav = (JSObject) applet.getMember("navigator");
		    userAgent = (String) nav.getMember("userAgent");
		    
		    displayDebug("cookie: " + cookie, 10);
		    displayDebug("userAgent: " + userAgent, 10);
		} catch (JSException e) {
			displayWarn("JSException (" + e.getMessage()+ ") in CoppermineUploadPolicy, trying default values.");
			//If we can't have access to the JS objects, we're in development :
			//Let's put some 'hard value', to test the applet from the development tool (mine is eclipse).
			cookie = "cpg146_data=YTozOntzOjI6IklEIjtzOjMyOiJkOTk0NzRhMzlkZjBjZDAxM2EwYTc2ZGMwZjNhNDI4NCI7czoyOiJhbSI7aToxO3M6NDoibGFuZyI7czo2OiJmcmVuY2giO30%3D; b5de201130bd138db614bab4c3a1c4a3=f46dcd4f6a8c025614325024311a2fd0";
			userAgent = "Mozilla/5.0 (Windows; U; Windows NT 5.0; fr-FR; rv:1.7.12) Gecko/20050915";
		}
		//The cookies and user-agent will be added to the header sent by the applet:
	    addHeader("Cookie: " + cookie);
	    addHeader("User-Agent: " + userAgent);

		///////////////////////////////////////////////////////////////////////////////
	    //get the maximum number of files to upload in one HTTP request. 
		maxFilesPerUpload = UploadPolicyFactory.getParameter(theApplet, PROP_NB_FILES_PER_REQUEST, DEFAULT_NB_FILES_PER_REQUEST);

		///////////////////////////////////////////////////////////////////////////////
	    //get the mail of the webmaster. It can be used to send debug information, if
		//problems occurs. 
		webmasterMail = UploadPolicyFactory.getParameter(theApplet, PROP_WEBMASTER_MAIL, DEFAULT_WEBMASTER_MAIL);

		///////////////////////////////////////////////////////////////////////////////
	    //get the server protocol. 
		// It is used by Coppermine Picture Gallery (nice tool) to control that the user
	    // sending the cookie uses the same http protocol that the original connexion.
	    // Please have a look tp the UploadPolicy.serverProtocol attribute.
		serverProtocol = UploadPolicyFactory.getParameter(theApplet, PROP_SERVER_PROTOCOL, DEFAULT_SERVER_PROTOCOL);

		///////////////////////////////////////////////////////////////////////////////
	    //get the upload String Success. See Uploadolicy#getStringUploadSuccess 
		// It is used by Coppermine Picture Gallery (nice tool) to control that the user
	    // sending the cookie uses the same http protocol that the original connexion.
	    // Please have a look tp the UploadPolicy.serverProtocol attribute.
		stringUploadSuccess = UploadPolicyFactory.getParameter(theApplet, PROP_STRING_UPLOAD_SUCCESS, DEFAULT_STRING_UPLOAD_SUCCESS);		
		
		///////////////////////////////////////////////////////////////////////////////
		//Get resource file.
		String lang = UploadPolicyFactory.getParameter(theApplet,PROP_LANG, DEFAULT_LANG);
		Locale locale;
		if (lang == null) {
			displayInfo("lang = null, taking default language");
			locale = Locale.getDefault();
		} else {
			locale = new Locale(lang);
		}

		//Let's handle the language:
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
	 * This method allows the applet to send debug information to the webmaster. The default implementation is
	 * to open the user's mailer, by using a mailto link.  
	 * 
	 * @param reason A string describing briefly the problem. The mail subject will be somethin like: Jupload Error (reason)
	 * @see UploadPolicy#sendDebugInformation(String)  
	 *
	 */
	public void sendDebugInformation(String reason) {
		StringBuffer href = new StringBuffer();
		
		href.append("mailto:");
		if (webmasterMail.length() > 0) {
			href.append(webmasterMail);
		}
		//TODO finish the 'building' of this mail.
		try {
			URL url = new URL(href.toString());
			theApplet.getAppletContext().showDocument(url);
		}  catch (MalformedURLException e) {
		   System.err.println("Invalid URL");
		}
			     
	}//sendDebugInformation

	/**
	 * This methods allows the applet to store all messages (debug, warning, info, errors...) into a StringBuffer.
	 * If any problem occurs, the whole output (displayed or not by the displayDebug, for instance) can be stored
	 * in a file, or sent to the webmaster. This can help to identify and correct problems that can occurs on the
	 * various computer configurations. 
	 * 
	 * @param msg
	 */	
	private void addMsgToDebugBufferString (String msg) {
		debugBufferString.append(msg);
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
		//Let's store all text in the debug BufferString
		addMsgToDebugBufferString(msg);
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
		String msg = "-DEBUG- " + debug;
		if (debugLevel >= minDebugLevel) {
			//displayMsg will add the message to the debugStrignBuffer.
			displayMsg (msg);
		} else {
			//Let's store all text in the debug BufferString
			addMsgToDebugBufferString(msg);
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

	/**
	 * @see wjhk.jupload2.policies.UploadPolicy#getStringUploadSuccess()
	 */
	public String getStringUploadSuccess() {
		return stringUploadSuccess;
	}

}
