/*
 * Created on 4 mai 2006
 */
package wjhk.jupload2.policies;

	
	
import java.applet.Applet;
import java.awt.GridLayout;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextArea;

//import sun.plugin.javascript.JSObject;

import netscape.javascript.JSException;
import netscape.javascript.JSObject;


import wjhk.jupload2.JUploadApplet;
import wjhk.jupload2.exception.JUploadExceptionUploadFailed;
import wjhk.jupload2.filedata.FileData;
import wjhk.jupload2.gui.FilePanel;

/**
 * This class implements all {@link wjhk.jupload2.policies.UploadPolicy} methods. Its
 * way of working is he same as the JUpload version 1.
 * <BR>
 * The simplest way to use this policy is given in the presentation of {@link UploadPolicy}. The DefaultUploadPolicy 
 * is used when no <I>uploadPolicy</I> parameter is given to the applet, or this parameter has 'DefaultUploadPolicy' 
 * as a value. 
 * <BR>
 * The default behavior is representated below. It can be overrided by adding parameters to the applet. All available
 * parameters are shown in the presentation of {@link UploadPolicy}.
 * <UL>
 * <LI>Default implementation for all {@link wjhk.jupload2.policies.UploadPolicy} methods.
 * <LI>Files are uploaded all in one HTTP request.
 * <LI>No handling for particular kind of files: files are transmitted without any transformation.
 * <LI>The file are transmitted to the server with the navigator cookies, userAgent and Protocol. This 
 * make upload occurs within the current user session on the server. So, it allows right management and context
 * during the management of uploaded files, on the server. 
 * </UL>
 * 
 * @author Etienne Gauthier
 */

public class DefaultUploadPolicy implements UploadPolicy {

	/**
	 * theApplet contains the reference of the Applet. It's useful to interact with it.
	 * <BR>
	 * It also allows acccess to the navigator properties, if the html tag MAYSCRIPT is put in the APPLET tag. This
	 * allows this class to get the cookie, userAgent and protocol, to upload files in the current user session on 
	 * the server.   
	 * <BR>
	 * Default : no default value 
	 */
	Applet theApplet = null;
	
	/**
	 * The URL where files should be posted.
	 * <BR>
	 * Default : no default value. (mandatory) 
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
	 * If an error occurs during upload, and this attribute is not null, the applet asks the user if wants to send 
	 * the debug ouput to the administrator. If yes, the full debug information is POSTed to this URL. It's a little
	 * development on the server side to send a mail to the webmaster, or just log this error into a log file.  
	 * 
	 * @see UploadPolicy#sendDebugInformation(String)
	 */
	String urlToSendErrorTo;
	
	/**
	 * This Vector contains headers that will be added for each upload. It may contains
	 * specific cookies, for instance.
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
	protected DefaultUploadPolicy(Applet theApplet, JTextArea status) {
		//Call default constructor for all default initialization;.
		this.theApplet = theApplet;
		this.status = status;
		

	    ///////////////////////////////////////////////////////////////////////////////
	    //get the URL where files must be posted. 
	    postURL = UploadPolicyFactory.getParameter(theApplet, PROP_POST_URL, DEFAULT_POST_URL);

	    //////////////////////////////////////////////////////////////////////////////
	    //get the debug level. This control the level of debug messages that are written 
	    //in the status area (see displayDebugMessage). In all cases, the full output 
	    //is written in the debugBufferString (see also urlToSendErrorTo)
	    debugLevel = UploadPolicyFactory.getParameter(theApplet, PROP_DEBUG_LEVEL, DEFAULT_DEBUG_LEVEL);

	    ///////////////////////////////////////////////////////////////////////////////
	    //get the maximum number of files to upload in one HTTP request. 
		maxFilesPerUpload = UploadPolicyFactory.getParameter(theApplet, PROP_NB_FILES_PER_REQUEST, DEFAULT_NB_FILES_PER_REQUEST);

		///////////////////////////////////////////////////////////////////////////////
	    //get the URL where the full debug output can be sent when an error occurs. 
		urlToSendErrorTo = UploadPolicyFactory.getParameter(theApplet, PROP_URL_TO_SEND_ERROR_TO, DEFAULT_URL_TO_SEND_ERROR_TO);

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
		resourceBundle = ResourceBundle.getBundle("wjhk.jupload2.lang.lang", locale);

	    
		displayInfo("JUpload applet, version " + JUploadApplet.VERSION + " (" + JUploadApplet.LAST_MODIFIED + "), available at http://jupload.sourceforge.net/");
	    displayInfo("Java version  : " + System.getProperty("java.version")); 
		

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
			//Test, to avoid a crash under linux
			JSObject applet = (JSObject) JSObject.getWindow(getApplet());
		    JSObject doc = (JSObject) applet.getMember("document");
		    cookie = (String) doc.getMember("cookie");
	
		    JSObject nav = (JSObject) applet.getMember("navigator");
		    userAgent = (String) nav.getMember("userAgent");
		    
		    displayDebug("cookie: " + cookie, 10);
		    displayDebug("userAgent: " + userAgent, 10);
		} catch (JSException e) {
			displayWarn("JSException (" + e.getClass() + ": " + e.getMessage()+ ") in DefaultUploadPolicy, trying default values.");
			
			//If we can't have access to the JS objects, we're in development :
			//Let's put some 'hard value', to test the applet from the development tool (mine is eclipse).
			cookie = "cpg146_data=YTozOntzOjI6IklEIjtzOjMyOiJkOTk0NzRhMzlkZjBjZDAxM2EwYTc2ZGMwZjNhNDI4NCI7czoyOiJhbSI7aToxO3M6NDoibGFuZyI7czo2OiJmcmVuY2giO30%3D; b5de201130bd138db614bab4c3a1c4a3=f46dcd4f6a8c025614325024311a2fd0";
			userAgent = "Mozilla/5.0 (Windows; U; Windows NT 5.0; fr-FR; rv:1.7.12) Gecko/20050915";
		}
		//The cookies and user-agent will be added to the header sent by the applet:
	    addHeader("Cookie: " + cookie);
	    addHeader("User-Agent: " + userAgent);

		//Let's handle the language:
	    displayDebug("lang (parameter) : " + lang, 20);
	    displayDebug("language : " + locale.getLanguage(), 20);
	    displayDebug("country : " + locale.getCountry(), 20);
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
	 * creates a JPanel, containing the three given JButton. It creates the same panel as the original JUpload.  
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
	 * @param description A string describing briefly the problem. The mail subject will be somethin like: Jupload Error (reason)
	 * @see UploadPolicy#sendDebugInformation(String)  
	 *
	 */
	public void sendDebugInformation(String description) {
		
		if (urlToSendErrorTo.length() > 0) {
			Object response = null;
			//TODO Put a java MessageBox instead of a javascript one.
			try {
				//This works Ok within an applet, but won't work within a java application.
				JSObject applet = (JSObject) JSObject.getWindow(getApplet());
			    JSObject win =  (JSObject) applet.getMember("window");
			    Object[] args = {getString("questionSendMailOnError")};
			    response = win.call("confirm", args);
			} catch (Exception e) {
				//We're probably not in a navigator. Let's send the mail.
				response = "true";
			}
		    
	    	displayDebug("Answer to " + getString("questionSendMailOnError") + ": " + response, 60);
		    if (response.toString().equals("true")) {
		    	displayDebug("Within response == true", 60);
				StringBuffer href = new StringBuffer();
				
				//The message is written in english, as it is not sure that the webmaster speaks the same
				//language as the current user.
			
				
				switch (3) {
				case 1:
					//TODO finish the 'building' of this mail.
					try {
						href
						.append("mailto:etiennegauthier@free.fr")
						.append("?subject=[JUpload] ")
						.append(description)
						.append("&body=salut")
						//.append("\n\nAn error occured during upload, in JUpload\nAll debuginformation is available below\n\n\n\n")
						//.append(debugBufferString)
						//.append("&attachment=e:/temp2/t.log")    //KO
						.append("&filename=e:/temp2/t.log")
						.append("&filename1=e:/temp2/t.log")
						.append("&file=e:/temp2/t.log")  //KO
						.append("&file1=e:/temp2/t.log") //KO
						;
						URL url = new URL(href.toString());
						theApplet.getAppletContext().showDocument(url);
					}  catch (MalformedURLException e) {
						   displayErr(e);
					}
					break;
					/*
				case 2:
					try {
						displayDebug("Within PostMethod", 60);
						HttpClient client = new HttpClient();
						PostMethod post = new PostMethod(urlToSendErrorTo);
						
						//FIXME Headers needs to be added, so that Coppermine recognize the session !
						
						//FIXME Put constants here, instead of hard coded strings.
						post.addParameter("description", description);
						post.addParameter("log", debugBufferString.toString());
						int statusCode = client.executeMethod (post);
						if( statusCode == -1 ) {
							displayWarn("Error during log management (statusCode=" + statusCode + ")");
						} else {
							String body = post.getResponseBodyAsString();
							post.releaseConnection();
							displayDebug("body returned from urlToSendErrorTo : \n" + body, 100);
							if (! body.equals("SUCCESS")) {
								//Dommage 
								displayWarn("Error during log management: \n" + body);
							}
						}
					}  catch (Exception e) {
					   UploadPolicyFactory.getCurrentUploadPolicy().displayErr(e);
					}
					break;
					*/
				case 3:
					String query = null;
					String action = null;
					Socket sock = null;
					DataOutputStream dataout = null;
					BufferedReader datain  = null;
					StringBuffer sbHttpResponseBody = null;
					StringBuffer request = null;
					
					try {
						query = "description=" + URLEncoder.encode(description, "UTF-8")
								+ "&log="  + URLEncoder.encode(
									"\n\nAn error occured during upload, in JUpload\n"
									+ "All debug information is available below\n\n\n\n" 
									+ debugBufferString.toString()
									, "UTF-8");
						request = new StringBuffer();
						URL url = new URL(urlToSendErrorTo);
						request
							.append("POST ")
								.append(url)
								.append(" ")
								.append(getServerProtocol())
								.append("\r\n")
							.append("Host: ")
								.append(url.getHost())
								.append("\r\n")
							.append("Accept: */*\r\n")
							.append("Content-type: application/x-www-form-urlencoded\r\n")
							.append("Connection: close\r\n")
							.append("Content-length: ")
							  .append(query.length())
							  .append("\r\n");						
						//Get specific headers for this upload.
						onAppendHeader(request);						
						// Blank line (end of header)
						request
							.append("\r\n")
							.append(query)
							;
						
						// If port not specified then use default http port 80.
						sock = new Socket(url.getHost(), (-1 == url.getPort())?80:url.getPort());
						dataout = new DataOutputStream(new BufferedOutputStream(sock.getOutputStream()));
						datain  = new BufferedReader(new InputStreamReader(sock.getInputStream()));
						//DataInputStream datain  = new DataInputStream(new BufferedInputStream(sock.getInputStream()));
						
						// Send http request to server
						action = "send bytes (1)";
						dataout.writeBytes(request.toString());
						dataout.writeBytes(query);
						action = "flush";
						dataout.flush ();
						action = "wait for server answer";
						String strUploadSuccess = getStringUploadSuccess();
						boolean uploadSuccess = false;
						boolean readingHttpBody = false;
						sbHttpResponseBody = new StringBuffer(); 
						String line;
						//Now, we wait for the full answer (which should mean that the uploaded files
						//has been treated on the server)
						while ((line = datain.readLine()) != null) {
							
							//Is this upload a success ?
							action ="test success";
							if (line.matches(strUploadSuccess)) {
								uploadSuccess = true;
							}
							
							//Store the http body 
							if (readingHttpBody) {
								action = "sbHttpResponseBody";
								sbHttpResponseBody.append(line).append("\n");
							}
							if (line.length() == 0) {
								//Next lines will be the http body (or perhaps we already are in the body, but it's Ok anyway) 
								action = "readingHttpBody";
								readingHttpBody = true;
							}
						}
						//Is our upload a success ?
						if (! uploadSuccess) {
							throw new JUploadExceptionUploadFailed(getString("errHttpResponse"));
						}
	
					}catch(Exception e){
						displayErr(getString("errDuringLogManagement") + " (" + action + ") (" + e.getClass() + ") : " + e.getMessage());
					}finally{
						try{
							dataout.close();
						} catch(Exception e) {
							displayErr(getString("errDuringLogManagement") + " (dataout.close) (" + e.getClass() + ") : " + e.getMessage());
						}
						dataout = null;
						try{
							// Throws java.io.IOException
							datain.close();
						} catch(Exception e){}
						datain = null;
						try{
							// Throws java.io.IOException
							sock.close();
						} catch(Exception e) {
							displayErr(getString("errDuringLogManagement") + " (sock.close)(" + e.getClass() + ") : " + e.getMessage());
						}
						sock = null;
						displayDebug ("Sent to server: " + request.toString(), 100);
						displayDebug ("Body received: " + sbHttpResponseBody.toString(), 100);
						
					}
					break;
				} //switch
		    }
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
			status.append("\r\n");
		}
		//Let's store all text in the debug BufferString
		addMsgToDebugBufferString(msg + "\r\n");
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
	 * The DefaultUpload accepts all file types: we just return an instance of FileData, without any test.
	 * 
	 * @see UploadPolicy#createFileData(File)
	 */
	public FileData createFileData(File file) {
		return new FileData(file, this);
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
	public Applet getApplet() {
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
	 * alert displays a MessageBox with a unique 'Ok' button, like the javascript alert function. 
	 * 
	 * @param str The full String that must be displayed to the user.
	 * @see #alert(String)
	 */
	void alertStr(String str) {
		JSObject applet = (JSObject) JSObject.getWindow(getApplet());
	    JSObject win    = (JSObject) applet.getMember("window");
	    Object[] args   = {str};
	    win.call("alert", args);
	}

	public void alert(String key) {
		alertStr(getString(key));
	}
	public void alert(String key, String arg1) {
		alertStr(getString(key, arg1));
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

	/**
	 * @see wjhk.jupload2.policies.UploadPolicy#getUrlToSendErrorTo()
	 */
	public String getUrlToSendErrorTo() {
		return urlToSendErrorTo;
	}

}
