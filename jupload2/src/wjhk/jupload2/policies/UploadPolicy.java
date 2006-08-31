/*
 * Created on 4 mai 2006
 */
package wjhk.jupload2.policies;


import java.io.File;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import wjhk.jupload2.filedata.FileData;
import wjhk.jupload2.gui.FilePanel;

/**
 * This interface contains all methods that should be given by any UploadPolicy. 
 * The abstract class DefaultUploadPolicy contains all default policy.
 * <BR>
 * It allows to specify :
 * <DIR>
 * <LI> The way files should be managed.
 * <LI> The target URL.
 * <LI> The way files are uploaded :
 *    <DIR>
 * 	  <LI> File by file
 *    <LI> All files at once
 *    </DIR>
 * </DIR>
 * 
 * The current implemented upload policies are :
 * <DIR>
 * <LI> {@link wjhk.jupload2.policies.DefaultUploadPolicy}. It's a 'simple' instanciation of each UploadPolicy methods. 
 * It makes JUpload work the same way as the original JUpload (v1).
 * <LI> {@link wjhk.jupload2.policies.CustomizedNbFilesPerRequestUploadPolicy} is a DefaultUploadPolicy, which allows 
 * to control how many files are to be uploaded for each HTTP request.
 * <LI> {@link wjhk.jupload2.policies.FileByFileUploadPolicy} is CustomizedNbFilesPerRequestUploadPolicy, where the
 * number of files to upload for each HTTP request is ... guess ?!?    ;-)
 * <LI>{@link wjhk.jupload2.policies.PictureUploadPolicy} adds picture handling the the applet. The main 
 * functionnalities are :
 * 		<DIR>
 * 			<LI> Preview picture: The look of the applet changes, to allow display of the selected picture.
 * 			<LI> Rotation: you can rotate the picture by quarter of turn.
 * 			<LI> Resizing: the applet can resize picture before upload, to lower network (and time) transfert. Just 
 * 				specify a maximum width and/or height, in pixels. 
 * 		</DIR>
 * <LI> {@link wjhk.jupload2.policies.CoppermineUploadPolicy} is a special PictureUploadPolicy: it allows upload
 * to the <a href="coppermine.sourceforge.net">coppermine picture gallery</a>.
 * </DIR>
 * 
 * <BR><BR>
 * From the application, the {@link wjhk.jupload2.policies.UploadPolicyFactory} allows easy instanciation of
 * the needed UploadPolicy.<BR>
 * All constructors for class inherited from UploadPolicy should have a <B>protected contructor</B>: all class 
 * creations are controled by the UploadPolicyFactory, which is the only class that should be used to create upload
 * policies. You can:
 * <DIR>
 * <LI>Create a new UploadPolicy, by using the getUploadPolicy methods.
 * <LI>Get the previously created UploadPolicy, by using the {@link wjhk.jupload2.policies.UploadPolicyFactory#getCurrentUploadPolicy()} method.
 * </DIR>
 * 
 * <BR><BR>
 * 
 * The {link #progress} component is to be updated while uploading.<BR>
 * The {link #status} component is to be used to display informations.<BR>
 * <BR>
 * To allow the easiest possible change of upload, all default upload code is embbeded into the 
 * {@link wjhk.jupload2.policies.DefaultUploadPolicy} class. 
 * 
 * @author Etienne Gauthier
 * 
 * @see wjhk.jupload2.policies.DefaultUploadPolicy
 *
 */
public interface UploadPolicy {

	/*
	 * Available parameters for the applet. New parameters (for instance for new policies) should all
	 * be added here, in alphabetic order. These ensure that all tags are unique
	 */
	final static String PROP_ALBUM_ID 				= "albumId";
	final static String PROP_CREATE_BUFFERED_IMAGE	= "createBufferedImage";
	final static String PROP_DEBUG_LEVEL			= "debugLevel";
	final static String PROP_LANG	 				= "lang";
	final static String PROP_MAX_HEIGHT				= "maxPicHeight";
	final static String PROP_MAX_WIDTH				= "maxPicWidth";
	final static String PROP_NB_FILES_PER_REQUEST 	= "nbFilesPerRequest";
	final static String PROP_POST_URL 				= "postURL";
	final static String PROP_SERVER_PROTOCOL		= "serverProtocol";
	final static String PROP_TARGET_PICTURE_FORMAT	= "targetPictureFormat";
	final static String PROP_UPLOAD_POLICY 			= "uploadPolicy";
	
	final static String DEFAULT_POST_URL = 
		//"http://localhost:8080/jupload/pages/writeOut.jsp?URLParam=URL+Parameter+Value";
		"http://localhost/coppermine/xp_publish.php";
	final static int     DEFAULT_ALBUM_ID				= 0;
	final static boolean DEFAULT_CREATE_BUFFERED_IMAGE	= false;
	final static int     DEFAULT_DEBUG_LEVEL			= 0;
	final static String  DEFAULT_LANG	 				= null;
	final static int     DEFAULT_MAX_WIDTH				= -1;
	final static int     DEFAULT_MAX_HEIGHT				= -1;
	final static int     DEFAULT_NB_FILES_PER_REQUEST	= 1;
	final static String  DEFAULT_SERVER_PROTOCOL		= "HTTP/1.1";
	final static String  DEFAULT_TARGET_PICTURE_FORMAT	= null;
	final static String  DEFAULT_UPLOAD_POLICY			= "DefaultUploadPolicy";

	/**
	 * This method is called to create the top panel. The default implementation is defined
	 * in {@link DefaultUploadPolicy#createTopPanel(JButton, JButton, JButton, JPanel)}.
	 * 
	 * @param browse The default browse button. 
	 * @param remove The default removeSelected button. 
	 * @param removeAll The default removeAll button. 
	 * @param mainPanel The panel that contains all objects. It can be used to change the cursor (to a WAIT_CURSOR for instance).
	 * 
	 * @return the topPanel, that will be displayed on the top of the Applet.
	 */
	public JPanel createTopPanel(JButton browse, JButton remove, JButton removeAll, JPanel mainPanel);	
	
	/**
	 * This methods creates a new FileData instance (or one of its inherited classes), 
	 * and return it to the caller.
	 * 
	 * @param file The file used to create the FileData instance.
	 * @return A FileData instance. The exact class depends on the currentUploadPolicy.
	 */
	public FileData createFileData(File file);
	
	/**
	 * Get the target URL for upload.
	 * 
	 * @return Should be valid URL...
	 */
	public String getPostURL();
	
	/**
	 * The URL can change during the life of our policy ...
	 * 
	 * @param postURL
	 */
	public void setPostURL(String postURL);

	/**
	 * Get an upload filename, that is to be send in the HTTP upload request. 
	 * 
	 * @param index index of the file within upload (can be4, ou of 10 for instance).
	 * @return The upload filename
	 */
	public String getUploadFilename (FileData fileData, int index);
	
	/**
	 * This function returns the number of files should be uploaded at a time. If negative or 0, all files are to 
	 * be uploaded in one HTTP request. If positive, each HTTP upload contains this number of files. The last upload 
	 * request may contain less files.
	 * <BR>
	 * Examples :
	 * <UL>
	 * <LI>If 1 : files are uploaded file by file.
	 * <LI>If 5 : files are uploaded 5 files by 5 files. If 12 files are uploaded, 3 HTTP upload are done, containing
	 * 5, 5 and 2 files.
	 * </UL>
	 * 
	 * @return Returns the maximum number of files, to download in one HTTP request.
	 */
	public int getMaxFilesPerUpload();
		
	/**
	 * log an error message, based on an exception. Will be logged in the status bar, if defined.
	 * 
	 * @param e The exception to report
	 */
	public void displayErr (Exception e);

	/**
	 * log an error message. Will be logged in the status bar, if defined.
	 * 
	 * @param err The erreur text to be displayed.
	 */
	public void displayErr (String err);

	/**
	 * log an info message. Will be logged in the status bar, if defined.
	 * 
	 * @param info The information message that will be displayed.
	 */
	public void displayInfo (String info);
	
	/**
	 * log a warning message. Will be logged in the status bar, if defined.
	 * 
	 * @param warn The warning message that will be displayed.
	 */
	public void displayWarn (String warn);

	/**
	 * log a debug message. Will be logged in the status bar, if defined.
	 * 
	 * @param debug The message to display.
	 * @param minDebugLevel If the current debug level is superior or equals to minDebugLevel, 
	 * the message will be displayed. Otherwise, it will be ignored. 
	 */
	public void displayDebug (String debug, int minDebugLevel);
	
	/**
	 * Add an header to the list of headers that will be added to ech HTTP upload request.
	 * This method is called from specific uploadPolicies, which would need headers to be
	 * added to all uploads. These headers are used in {@link DefaultUploadPolicy}.
	 * 
	 * @param header
	 * @see #onAppendHeader(StringBuffer)
	 */
	public void addHeader(String header);
	
	/**
	 * Append specific headers for this upload (session cookies, for instance). This method
	 * is called while building each upload HTTP request.
	 * 
	 * @param sb The header StringBuffer where spécific headers should be appended.
	 * @return The StringBuffer given in parameters. This is conform to the StringBuffer.append method.
	 * @see #addHeader(String)
	 * @see wjhk.jupload2.upload.FileUploadThreadV3#doUpload(FileData[], int, int)
	 */
	public StringBuffer onAppendHeader(StringBuffer sb);
	
	/**
	 * This method is called each time a file is selected in the panel files. It allows, for instance, to preview
	 * a picture {@link PictureUploadPolicy}.
	 * 
	 * @param fileData
	 */
	public void onSelectFile (FileData fileData);
	
	/**
	 * This allow runtime modifications of properties. Currently, this is only user after
	 * full initialization.
	 * 
	 * @param prop
	 * @param value
	 */
	public void setProperty(String prop, String value);
	
	/**
	 * Indicate if everything is ready for upload.
	 * 
	 * @return indicate if everything is ready for upload.
	 */
	public boolean isUploadReady();
	
	/**
	 * This method is called after a upload, whether it is successful or not.
	 *
	 * @param e null if success, or the exception indicating the problem. 
	 */
	public void afterUpload(FilePanel filePanel, Exception e, String serverOutput);
	
	/**
	 * 
	 * HTTP protocol that should be used to send the HTTP request. Currently, this is mainly used by
	 * {@link CoppermineUploadPolicy}, as the coppermine control that the protocol used for each HTTP request 
	 * is the same as the one used during the session creation. It is used in the default policy, as it could 
	 * be used elsewhere.
	 * <BR>
	 * Default is : HTTP/1.1
	 * 
	 * @return The selected server protocol.
	 * 
	 */
	public String getServerProtocol();
	
	/**
	 * Retrive a local property. Mainly used for localization.
	 * 
	 * @param key The key, whose associated text is to retrieve.
	 * @return The associated text.
	 * @see DefaultUploadPolicy#DefaultUploadPolicy(String, Applet, int, JTextArea)
	 */
	public String getString(String key);

	/**
	 * Retrive a local property. Mainly used for localization. 
	 * <BR>
	 * All occurences of {1} in the value (corresponding to key) are replaced by value1.
	 * <BR>
	 * Sample : <BR>
	 * Love=Oh {1}, I love you so much ...
	 * <BR>
	 * Call it by getString("Love", "John Smith") ...  ;-) 
	 * 
	 * @param key The key, whose associated text is to retrieve.
	 * @param value1 The value, which will replace all occurence of {1}
	 * @return The associated text.
	 * @see DefaultUploadPolicy#DefaultUploadPolicy(String, int, Applet, int, JTextArea)
	 */
	public String getString(String key, String value1);
	
	/**
	 * Same as {@link #getString(String,String)}, but the given value is an integer.
	 * @param key The key, whose associated text is to retrieve.
	 * @param value1 The value, which will replace all occurence of {1}
	 * @return The associated text.
	 */
	public String getString(String key, int value1);


	/**
	 * This method indicate whether or not the debug messages must be displayed. Default is no debug (0).
	 * <BR>
	 * To activate the debug, add a 'debug' parameter to the applet (with 1 to n value), or call this method.  
	 * Currently, level used in the code are between 0 (no debug) and 100 (max debug).
	 * <BR>
	 * With a 0 value, no debug messages will be displayed.
	 * 
	 * @param debugLevel The new debugLevel.
	 */
	public void setDebugLevel(int debugLevel);

	/**
	 * This method returns the current debug level.
	 * 
	 * @return The current debug level
	 * @see #setDebugLevel(int)
	 */
	public int getDebugLevel();
}

