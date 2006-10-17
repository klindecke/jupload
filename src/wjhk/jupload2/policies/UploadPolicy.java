/*
 * Created on 4 mai 2006
 */
package wjhk.jupload2.policies;


import java.applet.Applet;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import wjhk.jupload2.filedata.FileData;
import wjhk.jupload2.gui.FilePanel;

/**
 * 
This package contains upload policies, which allow easy configuration of the applet behaviour.

The abstract class DefaultUploadPolicy contains all default policy.
<BR>
It allows to specify :
<DIR>
<LI> The way files should be managed.
<LI> The target URL.
<LI> The way files are uploaded :
   <DIR>
	  <LI> File by file
   <LI> All files at once
   </DIR>
<LI> Any specific behaviour. For instance, the {@link  wjhk.jupload2.policies.PictureUploadPolicy} allows picture management. This contains: 
</DIR>

<A NAME="policies">The current implemented upload policies are :</A>
<DIR>
<LI> {@link wjhk.jupload2.policies.DefaultUploadPolicy}. It's a 'simple' instanciation of each UploadPolicy methods. 
It makes JUpload work the same way as the original JUpload (v1).
<LI> <B><I>(deprecated)</I></B>{@link wjhk.jupload2.policies.CustomizedNbFilesPerRequestUploadPolicy} is a DefaultUploadPolicy, which allows 
to control how many files are to be uploaded for each HTTP request. 
<LI> {@link wjhk.jupload2.policies.FileByFileUploadPolicy} is CustomizedNbFilesPerRequestUploadPolicy, where the
number of files to upload for each HTTP request is ... one! This policy behaves as the DefaultUploadPolicy, when
nbFilesPerRequest parameter (see below) is 1.
<LI>{@link wjhk.jupload2.policies.PictureUploadPolicy} adds picture handling the the applet. The main 
functionnalities are :
		<DIR>
			<LI> Preview picture: The look of the applet changes, to allow display of the selected picture.
			<LI> Rotation: you can rotate the picture by quarter of turn.
			<LI> Resizing: the applet can resize picture before upload, to lower network (and time) transfert. Just 
				specify a maximum width and/or height, in pixels. 
		</DIR>
<LI> {@link wjhk.jupload2.policies.CoppermineUploadPolicy} is a special PictureUploadPolicy: it allows upload
to the <a href="coppermine.sourceforge.net">coppermine picture gallery</a>.
</DIR>

<BR><BR>
From the application, the {@link wjhk.jupload2.policies.UploadPolicyFactory} allows easy instanciation of
the needed UploadPolicy.<BR>
All constructors for class inherited from UploadPolicy should have a <B>protected contructor</B>: all class 
creations are controled by the UploadPolicyFactory, which is the only class that should be used to create upload
policies. You can:
<DIR>
<LI>Create a new UploadPolicy, by using the getUploadPolicy methods.
<LI>Get the previously created UploadPolicy, by using the {@link wjhk.jupload2.policies.UploadPolicyFactory#getCurrentUploadPolicy()} method.
</DIR>

<BR><BR>

The {link #progress} component is to be updated while uploading.<BR>
The {link #status} component is to be used to display informations.<BR>
<BR>
To allow the easiest possible change of upload, all default upload code is embbeded into the 
{@link wjhk.jupload2.policies.DefaultUploadPolicy} class. 

<BR><BR>

<A NAME="parameters"><H3>Parameters</H3></A>
Here is the list of all parameters available in the current package. These are applet parameters that should be 
'given' to the applet, with <PARAM> tags, as precised below in the <A href="#example">example</A>.

<TABLE border=1>
<TR>
  <TH>Parameter name</TH>
  <TH>Default value / <BR> Implemented in</TH>
  <TH>Description</TH>
</TR>
<TR>
  <TD>uploadPolicy</TD>
  <TD>DefaultUploadPolicy <BR><BR> see {@link wjhk.jupload2.policies.UploadPolicyFactory}</TD>
  <TD>This parameter contains the class name for the UploadPolicy that should be used. If it is not 
      set, or if its value is unknown from {@link wjhk.jupload2.policies.UploadPolicyFactory#getUploadPolicy(Applet, JTextArea, String)},
      the {@link wjhk.jupload2.policies.DefaultUploadPolicy} is used.
  </TD>
</TR>
<TR>
  <TD>postURL</TD>
  <TD><I>Mandatory</I> <BR><BR> {@link wjhk.jupload2.policies.DefaultUploadPolicy}</TD>
  <TD>
		It contains the target URL toward which the files should be upload. This parameter is mandatory for existing class. It may
     become optional in new UploadPolicy, that would create this URL from other data.
		If the this URL may change during the applet execution time, you can create a new UploadPolicy class, 
		and either :
		<DIR>
			<LI>Override the {@link wjhk.jupload2.policies.UploadPolicy#getPostURL()} method, to make the postURL totaly dynamic.  
			<LI>Override the {@link wjhk.jupload2.policies.UploadPolicy#setPostURL(String)} method, to modify the postURL on the fly, when it is changed. 
			<LI>Override the {@link wjhk.jupload2.policies.UploadPolicy#setProperty(String, String)} method. The 
				{@link wjhk.jupload2.policies.CoppermineUploadPolicy} changes the postURL when the albumID property changes.  
			<LI>Find another solution ... 
	    </DIR> 
	 </TD>
</TR>
<TR>
  <TD>debugLevel</TD>
  <TD>0 <BR><BR> {@link wjhk.jupload2.policies.DefaultUploadPolicy}</TD>
  <TD>With 0, you get the normal production output. The higher the number is, the more information is displayed
      in the status bar.
	     <BR>Note: the whole debug messages is stored in the {@link wjhk.jupload2.policies.DefaultUploadPolicy#debugBufferString}.
      It can be used to display more information, if needed. See also the 'webmasterMail' parameter.
  </TD>
</TR>
<TR>
  <TD>lang</TD>
  <TD>Navigator language <BR><BR> {@link wjhk.jupload2.policies.DefaultUploadPolicy}</TD>
  <TD>Should be something like <I>en</I>, <I>fr</I>... Currently only french and english are known 
      from the applet. If anyone want to add another language ... Please translate the
      wjhk.jupload2.lang.lang_en, and send it back to <mailto:etienne_sf@sourceforge.net>.
  </TD>
</TR>
<TR>
  <TD>urlToSendErrorTo</TD>
  <TD><I>Empty String</I> <BR><BR> {@link wjhk.jupload2.policies.DefaultUploadPolicy}</TD>
  <TD>If this url is given, and an upload error occurs, the applet post the all the debug output to this
    address. It's up to this URL to handle this mail. It is possible to just store the file, or to log the 
    error in a database, or to send a mail (like the mail.php script given with the coppermine pack).
  </TD>
</TR>
<TR>
  <TD>nbFilesPerRequest</TD>
  <TD>-1 <BR><BR> {@link wjhk.jupload2.policies.DefaultUploadPolicy}</TD>
  <TD>This allows the control of the maximal number of files that are uploaded in one HTTP upload to the server.
       <BR>
       If set to -1, there is no maximum. This means that all files are uploaded in the same HTTP request.
       <BR>
       If set to 5, for instance, and there are 6 files to upload, there will be two HTTP upload request to the 
       server : 5 files in the first one, and that last file in a second HTTP request.   
  </TD>
</TR>
<TR>
  <TD>serverProtocol</TD>
  <TD>HTTP/1.1 <BR><BR> {@link wjhk.jupload2.policies.DefaultUploadPolicy}</TD>
  <TD>This parameter allows the control of the protocol toward the server. Currently, only HTTP is supported, so 
		valid values are HTTP/0.9 (not tested), HTTP/1.0 and HTTP/1.1.
     <BR>This parameter is really useful only in {@link wjhk.jupload2.policies.CoppermineUploadPolicy}, 
		as the coppermine application also controls that the requests send within an HTTP session uses the same 
     HTTP protocol (as a protection to limit the 'steal' of session cookies).  
	 </TD>
</TR>
<TR>
  <TD>stringUploadSuccess</TD>
  <TD>.* 200 OK$ <BR><BR> {@link wjhk.jupload2.policies.DefaultUploadPolicy}</TD>
  <TD>This string is a regular expression. The upload thread will try to match this regular epression to each 
     lines returned from the server.
		If the match is successfull, the upload is considered to be a success. If not, a 
     {@link wjhk.jupload2.exception.JUploadExceptionUploadFailed} is thrown.
    <BR>
    The default test expression testes that the web server returns no HTTP error: 200 is the return code for a 
    successfull HTTP request. It actually means that postURL is a valid URL, and that the applet was able to send 
    a request to this URL: there should be no problem with the network configuration, like proxy, password proxy...). 
    <BR>
    <B>But</B> it doesn't mean that the uploaded files have correctly be managed by the server. For instance, the
    URL can be http://sourceforge.net, which, of course, would not take your files into account.
    <BR>
    So, as soon as you know a regular expression that test the return from the target application (and not just
    a techical HTTP response code), change the stringUploadSuccess to this value. For instance, the 
    {@link wjhk.jupload2.policies.CoppermineUploadPolicy}
    changes this value to "^SUCCESS$", as the HTTP body content of the server's answer contain just this exact 
    line. This 'success' means that the pictures have correctly be added to the album, that vignettes have been 
    generated (this I suppose), etc...
  </TD>
</TR>
<TR>
  <TD>maxPicHeight</TD>
  <TD>-1 <BR><BR> {@link wjhk.jupload2.policies.PictureUploadPolicy}</TD>
  <TD>This parameters allows the PHP script to control the maximum width for pictures. If a picture is to be 
     download, and its height is bigger, the picture will be resized. The proportion between width and height
     of the resized picture are the same as those of the original picture. If both maxPicHeight and maxPicWidth
     are given, it can happen that the resized picture has a height lesser than maxPicHeight, so that width 
     is no more than maxPicWidth.
     <BR>
     <B>Precisions:</B>
     <BR>
     If this parameter value is negative, then no control is done on the picture height.
     <BR>
     If the original picture is smaller than the maximum size, the picture is not enlarged.
     <BR>
     If the picture is resized, its other characteristics are kept (number of colors, ColorModel...). The picture 
     format is ketp, if targetPictureFormat is empty. If the picture format is a destructive (like jpeg), the 
     maximum available quality is choosed.
  </TD>
</TR>
<TR>
  <TD>maxPicWidth</TD>
  <TD>-1 <BR><BR> {@link wjhk.jupload2.policies.PictureUploadPolicy}</TD>
  <TD>Same as maxPicHeight, but for the maximum width of the uploaded picture.</TD>
</TR>
<TR>
  <TD>targetPictureFormat</TD>
  <TD><I>Empty String</I> <BR><BR>  (<B>to be</B> implemented in {@link wjhk.jupload2.policies.PictureUploadPolicy})</TD>
  <TD>This parameter can contain any picture writer known by the JVM. For instance: jpeg, png, gif. All standard 
      formats should be available. More information can be found on the  
      <A href='http://java.sun.com/j2se/1.4.2/docs/guide/imageio/spec/title.fm.html'>java.sun.com</A> web site.
  </TD>
</TR>
<TR>
  <TD>albumId</TD>
  <TD>-1 <BR><BR> {@link wjhk.jupload2.policies.CoppermineUploadPolicy}</TD>
  <TD>This parameter is only used by CoppermineUploadPolicy. So it is to be used to upload into a 
      <a href="http://coppermine.sourceforge.net/">coppermine picture gallery</a>. This parameter 
      contains the identifier of the album, where pictures should be used. See CoppermineUploadPolicy 
      for an example.
      <BR>
      Before upload, CoppermineUploadPolicy.{@link wjhk.jupload2.policies.CoppermineUploadPolicy#isUploadReady()}
      checks that the albumId is correct, that is: >=1.		
  </TD>
</TR>
<TR>
  <TD>storeBufferedImage</TD>
  <TD>false <BR><BR> {@link wjhk.jupload2.policies.PictureUploadPolicy}</TD>
  <TD>This parameter indicates that the preview image on the applet is kept in memory. It works really
      nice under eclise.  But, once in the navigator, the applet runs very quickly out of memory. So I add a lot
      of calls to {@link wjhk.jupload2.filedata.PictureFileData#freeMemory(String)}, but it doesn't change 
      anything. Be careful to this parameter, and let it to the default value, unless you've well tested it
      under all your target client configurations. 
   </TD>
</TR>
</TABLE>

<A NAME="example"><H3>HTML call example</H3></A>
You'll find below an example of how to put the applet into a PHP page:
<BR>
<XMP>
      <APPLET  
          NAME="JUpload"
          CODE="wjhk.jupload2.JUploadApplet" 
          ARCHIVE="plugins/jupload/wjhk.jupload.jar" 
          <!-- Applet display size, on the navigator page -->
          WIDTH="500" 
          HEIGHT="700"
          <!-- The applet call some javascript function, so we must allow it : -->
          MAYSCRIPT
          >
          <!-- 
          		Only one parameter is mandatory. We don't precise the UploadPolicy, so DefaultUploadPolicy is used.
          		The applet behaves like the original JUpload. (jupload v1) 
          -->
          <PARAM NAME="postURL"      VALUE="http://some.host.com/youruploadpage.php">
                
      Java 1.4 or higher plugin required.
      </APPLET>

</XMP>

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
	final static String PROP_STORE_BUFFERED_IMAGE	= "storeBufferedImage";   //Be careful: if set to true, you'll probably have memory problems whil in a navigator.
	final static String PROP_DEBUG_LEVEL			= "debugLevel";
	final static String PROP_LANG	 				= "lang";
	final static String PROP_MAX_HEIGHT				= "maxPicHeight";
	final static String PROP_MAX_WIDTH				= "maxPicWidth";
	final static String PROP_NB_FILES_PER_REQUEST 	= "nbFilesPerRequest";
	final static String PROP_POST_URL 				= "postURL";
	final static String PROP_SERVER_PROTOCOL		= "serverProtocol";
	final static String PROP_STRING_UPLOAD_SUCCESS	= "stringUploadSuccess";
	final static String PROP_TARGET_PICTURE_FORMAT	= "targetPictureFormat";
	final static String PROP_UPLOAD_POLICY 			= "uploadPolicy";
	final static String PROP_URL_TO_SEND_ERROR_TO	= "urlToSendErrorTo";
	
	final static String DEFAULT_POST_URL = 
		//"http://localhost:8080/jupload/pages/writeOut.jsp?URLParam=URL+Parameter+Value";
		"http://localhost/coppermine/xp_publish.php";
	final static int     DEFAULT_ALBUM_ID				= 0;
	final static boolean DEFAULT_STORE_BUFFERED_IMAGE	= false;   //Be careful: if set to true, you'll probably have memory problems whil in a navigator.
	final static int     DEFAULT_DEBUG_LEVEL			= 0;
	final static String  DEFAULT_LANG	 				= null;
	final static int     DEFAULT_MAX_WIDTH				= -1;
	final static int     DEFAULT_MAX_HEIGHT				= -1;
	final static int     DEFAULT_NB_FILES_PER_REQUEST	= -1;
	final static String  DEFAULT_SERVER_PROTOCOL		= "HTTP/1.1";
	final static String  DEFAULT_STRING_UPLOAD_SUCCESS	= ".* 200 OK$";
	final static String  DEFAULT_TARGET_PICTURE_FORMAT	= null;
	final static String  DEFAULT_UPLOAD_POLICY			= "DefaultUploadPolicy";
	final static String  DEFAULT_URL_TO_SEND_ERROR_TO	= "";

	/**
	 * This method is called to create the top panel. The default implementation is defined
	 * in {@link wjhk.jupload2.policies.DefaultUploadPolicy#createTopPanel(JButton, JButton, JButton, JPanel)}.
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
	 * @param file The file used to create the FileData instance. Can be null, if the policy performs checks, and the 
	 *             given file is not Ok for these controls. See {@link PictureUploadPolicy#createFileData(File)}
	 *             for an example. It's up to the upload policy to display a message to inform the user that this 
	 *             file won't be added to the file list.
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
	 * This method allows the applet to send debug information to the webmaster.
	 * 
	 * @param reason A string describing briefly the problem. The mail subject will be somethin like: Jupload Error (reason)
	 * @see wjhk.jupload2.policies.DefaultUploadPolicy#sendDebugInformation(String)
	 */
	public void sendDebugInformation(String reason);
	
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
//TODO commentaire à compléter : STring = lang.properties
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
	 * added to all uploads. These headers are used in {@link wjhk.jupload2.policies.DefaultUploadPolicy}.
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
	 * @see wjhk.jupload2.upload.FileUploadThreadV3#doUpload(FileData[],int,int)
	 */
	public StringBuffer onAppendHeader(StringBuffer sb);
	
	/**
	 * This method is called each time a file is selected in the panel files. It allows, for instance, to preview
	 * a picture {@link wjhk.jupload2.policies.PictureUploadPolicy}.
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
	 * {@link wjhk.jupload2.policies.CoppermineUploadPolicy}, as the coppermine control that the protocol used for each HTTP request 
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
	 * @see wjhk.jupload2.policies.DefaultUploadPolicy#DefaultUploadPolicy(String, Applet, int, JTextArea)
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
	 * @see wjhk.jupload2.policies.DefaultUploadPolicy#DefaultUploadPolicy(String, Applet, int, JTextArea)
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
	 * Get the regular expression that will be tested against each line of the server answer. If one line matches this
	 * expression, that upload is marked as successful. 
	 * <BR>
	 * The upload works this way:
	 * <OL>
	 * <LI>Upload the selected file(s) to the server
	 * <LI>Get all the server HTTP response. 
	 * <LI>The stringUploadSuccess regular expression is tested against each line from the server.
	 * <LI>If the above test gives a match, the upload is marked as successful. Else, the upload is marked
	 *     as unsuccessful, and a JUploadExceptionUploadFailure is thrown. 
	 * </OL>
	 * 
	 * @return The regular expression that must be run again each line of the http answer.
	 */
	public String getStringUploadSuccess();
	
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
	
	/**
	 * A useful function, that has nothing to do with an upload policy. But it
	 * is rather eay to have it here.
	 * 
	 * @return Reference to the applet.
	 */
	public Applet getApplet();
	
	/**
	 * alert displays a MessageBox with a unique 'Ok' button, like the javascript alert function. 
	 * 
	 * @param property_str The string identifying the text to display, depending on the current language.
	 */
	public void alert(String property_str);

	/**
	 * alert displays a MessageBox with a unique 'Ok' button, like the javascript alert function. 
	 * 
	 * @param property_str The string identifying the text to display, depending on the current language.
	 * @param arg1 A string that will replace all {1} in the text corresponding to property_str. This allows 
	 *             to have dynamic localized text.
	 */
	public void alert(String property_str, String arg1);
}

