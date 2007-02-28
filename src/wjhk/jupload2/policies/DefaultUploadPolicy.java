/*
 * Created on 4 mai 2006
 */
package wjhk.jupload2.policies;


import java.awt.GridLayout;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.UIManager;

//import sun.plugin.javascript.JSObject;

import netscape.javascript.JSException;
import netscape.javascript.JSObject;


import wjhk.jupload2.JUploadApplet;
import wjhk.jupload2.exception.JUploadException;
import wjhk.jupload2.exception.JUploadExceptionUploadFailed;
import wjhk.jupload2.filedata.DefaultFileData;
import wjhk.jupload2.filedata.FileData;
import wjhk.jupload2.gui.JUploadTextArea;

/**
 * This class implements all {@link wjhk.jupload2.policies.UploadPolicy} methods. Its
 * way of working is he same as the JUpload version 1.
 * <BR>
 * The simplest way to use this policy is given in the presentation of {@link UploadPolicy}. The DefaultUploadPolicy 
 * is used when no <I>uploadPolicy</I> parameter is given to the applet, or this parameter has 'DefaultUploadPolicy' 
 * as a value. 
 * <BR>
 * <P>The <U>default behavior</U> is representated below. It can be overrided by adding parameters to the applet. All available
 * parameters are shown in the presentation of {@link UploadPolicy}.</P>
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

	////////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////    APPLET PARAMETERS   ///////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * applet contains the reference of the Applet. It's useful to interact with it.
	 * <BR>
	 * It also allows acccess to the navigator properties, if the html tag MAYSCRIPT is put in the APPLET tag. This
	 * allows this class to get the cookie, userAgent and protocol, to upload files in the current user session on 
	 * the server.   
	 * <BR>
	 * Default : no default value 
	 */
	JUploadApplet applet = null;
	
	/**
	 * The current debug level.
	 */
	private int debugLevel = -1;
	
	/**
	 * This String contains the filenameEncoding parameter. All details about the available applet parameters
	 * are displayed in the <a href="UploadPolicy.html@parameters">Upload Policy javadoc page</a>.
	 */
	String filenameEncoding = null;
	
	/**
	 * The look and feel is used as a parameter of the UIManager.setLookAndFeel(String) method. See the parameters
	 * list on the {@link UploadPolicy} page.
	 */
	String lookAndFeel = "";

	/**
	 * The applet will do as may HTTP requests to upload all files, with the number 
	 * as a maximum number of files for each HTTP request.
	 * <BR>
	 * Default : -1 
	 */
	int nbFilesPerRequest;
	
	/**
	 * Current value (or default value) of the maxChunkSize applet parameter.
	 * <BR>
	 * Default : Long.MAX_VALUE
	 */
	long maxChunkSize;
	
	/**
	 * Current value (or default value) of the maxFileSize applet parameter.
	 * <BR>
	 * Default : Long.MAX_VALUE
	 */
	long maxFileSize;
	
	/**
	 * The URL where files should be posted.
	 * <BR>
	 * Default : no default value. (mandatory) 
	 */
	String postURL = null;	
	
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

	////////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////    INTERNAL ATTRIBUTE  ///////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////

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
	private JUploadTextArea statusArea = null;

	/**
	 * The resourceBundle contains all localized String (and others ??)
	 */
	ResourceBundle resourceBundle = null;

	/**
	 * This StringBuffer is used to store all information that could be useful, in case a problem occurs.
	 * Is content can then be sent to the webmaster. 
	 */
	private StringBuffer debugBufferString = new StringBuffer(); 
	
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////      CONSTRUCTORS      ///////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * The main constructor : use default values, and the given postURL.
	 * 
	 * @param theApplet The current applet. As the reference to the current upload policy exists almost everywhere,
	 *   this parameter allows any access to anyone on the applet... including reading the applet parameters.    
	 */
	public DefaultUploadPolicy(JUploadApplet theApplet) {
		//Call default constructor for all default initialization;.
		this.applet = theApplet;
		this.statusArea = theApplet.getStatusArea();
		

	    //////////////////////////////////////////////////////////////////////////////
	    //get the debug level. This control the level of debug messages that are written 
	    //in the status area (see displayDebugMessage). In all cases, the full output 
	    //is written in the debugBufferString (see also urlToSendErrorTo)
	    debugLevel = UploadPolicyFactory.getParameter(theApplet, PROP_DEBUG_LEVEL, DEFAULT_DEBUG_LEVEL);

	    //////////////////////////////////////////////////////////////////////////////
	    //get the filenameEncoding. If not null, it should be a valid argument for
	    //the URLEncoder.encode method. 
	    filenameEncoding = UploadPolicyFactory.getParameter(theApplet, PROP_FILENAME_ENCODING, DEFAULT_FILENAME_ENCODING);

	    
	    //Force the look and feel of the current system.
	    lookAndFeel = UploadPolicyFactory.getParameter(theApplet, PROP_LOOK_AND_FEEL, DEFAULT_LOOK_AND_FEEL);
	    if (lookAndFeel != null  &&  !lookAndFeel.equals("")  &&  !lookAndFeel.equals("java")) {
	    	//We try to call the UIManager.setLookAndFeel() method. We catch all possible exceptions, to prevent
	    	//that the applet is blocked.
	    	try {
		    	if (! lookAndFeel.equals("system")) {
		    		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		    	} else {
		    		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		    	}
	    	} catch (Exception e) {
	    		displayErr(e);
	    	}
	    }

	    ///////////////////////////////////////////////////////////////////////////////
	    //get the maximum number of files to upload in one HTTP request. 
		nbFilesPerRequest = UploadPolicyFactory.getParameter(theApplet, PROP_NB_FILES_PER_REQUEST, DEFAULT_NB_FILES_PER_REQUEST);

	    ///////////////////////////////////////////////////////////////////////////////
	    //get the maximum size of a file on one HTTP request (indicate if the file
		//must be splitted before upload, see UploadPolicy comment).
		maxChunkSize = UploadPolicyFactory.getParameter(theApplet, PROP_MAX_CHUNK_SIZE, DEFAULT_MAX_CHUNK_SIZE);

	    ///////////////////////////////////////////////////////////////////////////////
	    //get the maximum size of an uploaded file.
		maxFileSize = UploadPolicyFactory.getParameter(theApplet, PROP_MAX_FILE_SIZE, DEFAULT_MAX_FILE_SIZE);

	    ///////////////////////////////////////////////////////////////////////////////
	    //get the URL where files must be posted. 
	    postURL = UploadPolicyFactory.getParameter(theApplet, PROP_POST_URL, DEFAULT_POST_URL);

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
	    //get the URL where the full debug output can be sent when an error occurs. 
		urlToSendErrorTo = UploadPolicyFactory.getParameter(theApplet, PROP_URL_TO_SEND_ERROR_TO, DEFAULT_URL_TO_SEND_ERROR_TO);

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
			cookie = "cpg146_data=YTo1OntzOjI6IklEIjtzOjMyOiI5MWEyMzdiNmYwYmM0MTJjMjRiMTZlNzdiNzlmYzBjMyI7czoyOiJhbSI7aToxO3M6NDoibGFuZyI7czo2OiJmcmVuY2giO3M6MzoibGFwIjtpOjI7czozOiJsaXYiO2E6NDp7aTowO3M6MzoiNDMzIjtpOjE7czozOiI0NDUiO2k6MjtzOjM6IjQ0NiI7aTozO3M6MzoiNDY2Ijt9fQ%3D%3D; 66f77117891c8a8654024874bc0a5d24=56357ed95576afa0d8beb558bdb6c73d";
			userAgent = "Mozilla/5.0 (Windows; U; Windows NT 5.0; fr; rv:1.8.1) Gecko/20061010 Firefox/2.0";
		}
		//The cookies and user-agent will be added to the header sent by the applet:
	    addHeader("Cookie: " + cookie);
	    addHeader("User-Agent: " + userAgent);

		//Let's handle the language:
	    displayDebug("lang (parameter): " + lang, 20);
	    displayDebug("language: " + locale.getLanguage(), 20);
	    displayDebug("country: " + locale.getCountry(), 20);
	    
		///////////////////////////////////////////////////////////////////////////////
		// Let's display some information to the user, about the received parameters.
		displayInfo("JUpload applet, version " + JUploadApplet.VERSION + " (" + JUploadApplet.LAST_MODIFIED + "), available at http://jupload.sourceforge.net/");
	    displayInfo("postURL: " + postURL);

	    if (maxFileSize == Long.MAX_VALUE) {
	    	//If the maxFileSize was not given, we display its value only in debug mode.
	    	displayDebug("maxFileSize  : " + maxFileSize, 20);
	    } else {
	    	//If the maxFileSize was given, we always inform the user.
	    	displayInfo("maxFileSize  : " + maxFileSize);
	    }
	    displayDebug("Java version  : " + System.getProperty("java.version"), 20); 		
	    displayDebug("debug: " + debugLevel, 1); 
	    displayDebug("filenameEncoding: " + filenameEncoding, 20);
	    displayDebug("nbFilesPerRequest: " + nbFilesPerRequest, 20);
	    displayDebug("maxChunkSize: " + maxChunkSize, 20);
	    displayDebug("stringUploadSuccess: " + stringUploadSuccess, 20); 
	    displayDebug("urlToSendErrorTo: " + urlToSendErrorTo, 20);
	    displayDebug("serverProtocol: " + serverProtocol, 20); 
	}
		
	////////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////  UploadPolicy methods  ///////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////
	//getters and setters are sorted below


	/**
	 * @see wjhk.jupload2.policies.UploadPolicy#addHeader(java.lang.String)
	 */
	public void addHeader(String header) {
		headers.add(header);		
	}
	
	/**
	 * @see wjhk.jupload2.policies.UploadPolicy#beforeUpload()
	 */
	public void beforeUpload() {
		//Default: no special action.
	}

	/**
	 * The default behaviour (see {@link DefaultUploadPolicy}) is to check that the stringUploadSuccess applet 
	 * parameter is present in the request. The return is :
	 * <DIR>
	 * <LI>True, if the stringUploadSuccess string is present in the serverOutputBody.
	 * <LI>True, If previous condition is not filled, but the HTTP header "HTTP(.*)200OK$" is present: the test is
	 *   currently non blocking, because I can not test all possible HTTP configurations.<BR>
	 *   Note: If "Transfer-Encoding: chunked" is present, the body may be cut by 'strange' characters, which prevent 
	 *   to find the success string. Then, a warning is displayed.
	 * <LI>False if the previous conditions are not fullfilled.
	 * </DIR>     
	 * 
	 * @param serverOutput The full HTTP answer, including the http headers. 
	 * @param serverOutputBody The body of the HTTP answer.
	 * @return True or False, indicating if the upload is a success or not.
	 * 
	 * @see UploadPolicy#checkUploadSuccess(String, String)
	 */
	public boolean checkUploadSuccess(String serverOutput, String serverOutputBody) throws JUploadException {
		final Pattern patternSuccess = Pattern.compile(stringUploadSuccess);
		final Pattern patternTransferEncodingChunked = Pattern.compile("^Transfer-Encoding: chunked", Pattern.CASE_INSENSITIVE);
		//La première ligne est de la forme "HTTP/1.1 NNN Texte", où NNN et le code HTTP de retour (200, 404, 500...)
		final Pattern patternHttpStatus = Pattern.compile("HTTP[^ ]* ([^ ]*) .*", Pattern.DOTALL);
		
		//The success string should be in the http body
		boolean uploadSuccess = patternSuccess.matcher(serverOutputBody).find();
		//The transfert encoding may be present in the serverOutput (that contains the http headers)
		boolean uploadTransferEncodingChunked = patternTransferEncodingChunked.matcher(serverOutput).find();
		
		//And have a match, to search for the http return code (200 for Ok)
		Matcher matcherUploadHttpStatus = patternHttpStatus.matcher(serverOutput);
		if (!matcherUploadHttpStatus.matches()) {
			throw new JUploadException("Can't find the HTTP status in serverOutput!");
		} else {
			int httpStatus = Integer.parseInt(matcherUploadHttpStatus.group(1));
			boolean upload_200_OK = (httpStatus == 200);
			
			displayDebug("HTTP return code: " + httpStatus, 40);
	
			//Let's find what we should answer:
			if (uploadSuccess) {
				return true;
			} else if (uploadTransferEncodingChunked && upload_200_OK) {
				//Hum, as the transfert encoding is chuncked, the success string may be splitted. We display 
				//an info message, and expect everything is Ok.
				//FIXME The chunked encoding should be correctly handled, instead of the current 'expectations' below. 
				displayInfo("The transertEncoding is chunked, and http upload is technically Ok, but the success string was not found. Suspicion is that upload was Ok...let's go on");
				return true;
			} else if (upload_200_OK) {
				//This method is currently non blocking.
				displayWarn("The http upload is technically Ok, but the success string was not found. Suspicion is that upload was Ok...let's go on");
				//We raise no exception (= success)
				return true;
			} else {
				//The upload is not successful: here, we know it!
				throw new JUploadExceptionUploadFailed (getClass().getName() + ".checkUploadSuccess(): The http return code is : " + httpStatus + " (should be 200)");
			}
		}
	}//isUploadSuccessful

	/**
	 * @see wjhk.jupload2.policies.UploadPolicy#afterUpload(Exception, String)
	 */
	public void afterUpload(Exception e, String serverOutput) {
		//Default: no special action.
	}

	
	/** @see UploadPolicy#alertStr(String) */
	public void alertStr(String str) {
		JSObject applet = (JSObject) JSObject.getWindow(getApplet());
	    JSObject win    = (JSObject) applet.getMember("window");
	    Object[] args   = {str};
	    win.call("alert", args);
	}
	/** @see UploadPolicy#alert(String) */
	public void alert(String key) {
		alertStr(getString(key));
	}
	/** @see UploadPolicy#alert(String,String) */
	public void alert(String key, String arg1) {
		alertStr(getString(key, arg1));
	}

	/**
	 * The DefaultUpload accepts all file types: we just return an instance of FileData, without any test.
	 * 
	 * @see UploadPolicy#createFileData(File)
	 */
	public FileData createFileData(File file) {
		return new DefaultFileData(file, this);
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
	 * @see UploadPolicy#displayErr(Exception)
	 */
	public void displayErr (Exception e) {
		displayErr (e.getClass().getName() + ": " + e.getLocalizedMessage());
	}
	/**
	 * @see UploadPolicy#displayErr(String)
	 */
	public void displayErr (String err) {
		displayMsg ("-ERROR- " + err);
	}
	/**
	 * @see UploadPolicy#displayInfo(String)
	 */
	public void displayInfo (String info){
		displayMsg ("-INFO- " + info);
	}
	/**
	 * @see UploadPolicy#displayWarn(String)
	 */
	public void displayWarn (String warn){
		displayMsg ("-WARNING- " + warn);
	}

	/**
	 * @see UploadPolicy#displayDebug(String, int)
	 */
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

	/** @see UploadPolicy#getString(String) */
	public String getString(String key) {
		String ret = resourceBundle.getString(key);
		return ret;
	}	
	/** @see UploadPolicy#getString(String,String) */
	public String getString(String key, String value1) {
		String ret = resourceBundle.getString(key).replaceAll("\\{1\\}", value1);
		return ret;
	}
	/** @see UploadPolicy#getString(String,String,String) */
	public String getString(String key, String value1, String value2) {
		String ret = resourceBundle.getString(key).
		replaceAll("\\{1\\}", value1).
		replaceAll("\\{2\\}", value2);
		return ret;
	}
	/** @see UploadPolicy#getString(String,String,String,String) */
	public String getString(String key, String value1, String value2, String value3) {
		String ret = resourceBundle.getString(key).
		replaceAll("\\{1\\}", value1).
		replaceAll("\\{2\\}", value2).
		replaceAll("\\{3\\}", value3);
		return ret;
	}
	
	/**
	 * @see UploadPolicy#getString(String,int)
	 */
	public String getString(String key, int value1) {
		return getString(key, Integer.toString(value1));
	}
	
	/**
	 * @see UploadPolicy#getUploadFilename(FileData, int)
	 */
	public String getUploadFilename (FileData fileData, int index) throws JUploadException {
		if (filenameEncoding == null || filenameEncoding.equals("")) {
			return fileData.getFileName();
		} else {
			try {
				return URLEncoder.encode(fileData.getFileName(), filenameEncoding);
			} catch (UnsupportedEncodingException e) {
				throw new JUploadException (e, this.getClass().getName() + ".getUploadFilename");
			}
		}
	}

	/** @see UploadPolicy#getUploadName(FileData, int) */
	public String getUploadName (FileData fileData, int index) {
		//This is the original way of working of JUpload.
		//It can easily be modified, by using another UploadPolicy.
		return "File" + index;
	}

	/** @see wjhk.jupload2.policies.UploadPolicy#isUploadReady() */
	public boolean isUploadReady() {
		// Default :  nothing to do before upload, so we're ready.
		return true;
	}
	
	/** @see UploadPolicy#onAppendHeader(StringBuffer) */
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
	 * Default implementation of the {@link wjhk.jupload2.policies.UploadPolicy#onSelectFile(wjhk.jupload2.filedata.FileData)}.
	 * Nothing's done.
	 */
	public void onSelectFile(FileData fileData) {
		// Default implementation : no action	
	}

	/** @see UploadPolicy#sendDebugInformation(String) */
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
				
				//The message is written in english, as it is not sure that the webmaster speaks the same
				//language as the current user.
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
		    }
		}
	}//sendDebugInformation

	/**
	 * @see wjhk.jupload2.policies.UploadPolicy#setProperty(java.lang.String, java.lang.String)
	 */
	public void setProperty(String prop, String value) {
		// Default : nothing to do.	
	}

	////////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////    getters / setters   ///////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** @see UploadPolicy#getApplet() */
	public JUploadApplet getApplet() {
		return applet;
	}

	/** @see UploadPolicy#getDebugLevel() */
	public int getDebugLevel() {
		return debugLevel;
	}
	/** @see UploadPolicy#setDebugLevel(int) */
	public void setDebugLevel(int debugLevel) {
		//If the debugLevel was previously set, we inform the user of this change.
		if (this.debugLevel >= 0) {
			displayInfo("Debug level set to " + debugLevel);
		}
		this.debugLevel = debugLevel;
	}	

	/** @see wjhk.jupload2.policies.UploadPolicy#getMaxChunkSize() */
	public long getMaxChunkSize() {
		return maxChunkSize;
	}

	/** @see wjhk.jupload2.policies.UploadPolicy#getMaxFileSize() */
	public long getMaxFileSize() {
		return maxFileSize;
	}

	/** @see wjhk.jupload2.policies.UploadPolicy#getNbFilesPerRequest() */
	public int getNbFilesPerRequest() {
		return nbFilesPerRequest;
	}

	/** @see UploadPolicy#getFilenameEncoding() */
	public String getFilenameEncoding() {
		return filenameEncoding;
	}

	/** @see wjhk.jupload2.policies.UploadPolicy#getPostURL() */
	public String getPostURL() {
		return postURL;
	}
	/** @see wjhk.jupload2.policies.UploadPolicy#setPostURL(String) */
	public void setPostURL(String postURL) {
		this.postURL = postURL;
	}

	/** @see wjhk.jupload2.policies.UploadPolicy#getServerProtocol() */
	public String getServerProtocol() {
		return serverProtocol;
	}

	/** @see wjhk.jupload2.policies.UploadPolicy#getStringUploadSuccess() */
	public String getStringUploadSuccess() {
		return stringUploadSuccess;
	}

	/** @see wjhk.jupload2.policies.UploadPolicy#getUrlToSendErrorTo() */
	public String getUrlToSendErrorTo() {
		return urlToSendErrorTo;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////    Internal methods    ///////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////


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
	 * Displays a message. If the statusArea panel is set, the message is displayed on it.
	 * If not, the System.out.println function is used.
	 * 
	 * @param msg The message to display.
	 */
	private void displayMsg (String msg) {
		if (statusArea == null) {
			System.out.println(msg);
		} else {
			statusArea.append(msg);
			statusArea.append("\r\n");
		}
		//Let's store all text in the debug BufferString
		addMsgToDebugBufferString(msg + "\r\n");
	}
	
}
