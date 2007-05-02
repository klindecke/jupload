/*
 * Created on 4 mai 2006
 */
package wjhk.jupload2.policies;

import java.io.File;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JPanel;

import wjhk.jupload2.JUploadApplet;
import wjhk.jupload2.exception.JUploadException;
import wjhk.jupload2.filedata.FileData;
import wjhk.jupload2.gui.JUploadFileFilter;
import wjhk.jupload2.gui.JUploadFileView;
import wjhk.jupload2.gui.JUploadPanel;

/**
 * This package contains upload policies, which allow easy configuration of the
 * applet behaviour. <BR>
 * <BR>
 * The class {@link DefaultUploadPolicy} contains a default implementation for
 * all UploadPolicy methods. <BR>
 * <BR>
 * <A NAME="parameters">
 * <H4>Parameters</H4>
 * </A> Here is the list of all parameters available in the current package,
 * that is: available in available upload policies. These are applet parameters
 * that should be 'given' to the applet, with <PARAM> tags, as precised below in
 * the <A href="#example">example</A>. <TABLE border=1>
 * <TR>
 * <TH>Parameter name</TH>
 * <TH>Default value / <BR>
 * Implemented in</TH>
 * <TH>Description</TH>
 * </TR>
 * <TR>
 * <TD>afterUploadURL</TD>
 * <TD><I>null</I><BR>
 * since 2.9.0<BR>
 * {@link wjhk.jupload2.policies.DefaultUploadPolicy}</TD>
 * <TD>This parameter is used by all policies. It allows the applet to change
 * the current page to another one after a successful upload. <BR>
 * This allows, for instance, to display a page containing the file description
 * of the newly uploaded page. </TD>
 * </TR>
 * <TR>
 * <TD>allowedFileExtensions</TD>
 * <TD><I>empty string</I><BR>
 * since 2.9.0<BR>
 * {@link wjhk.jupload2.policies.DefaultUploadPolicy}</TD>
 * <TD>This parameter allows the caller to specify a list of file extension. If
 * this parameter is specified, only file with this extension can be selected in
 * the applet.<BR>
 * This parameter must contains a list of extensions, in lower case, separated
 * by slashes. eg: jpg/jpeg/gif </TD>
 * </TR>
 * <TR>
 * <TD>albumId</TD>
 * <TD>-1 <BR>
 * <BR>
 * {@link wjhk.jupload2.policies.CoppermineUploadPolicy}</TD>
 * <TD>This parameter is only used by CoppermineUploadPolicy. So it is to be
 * used to upload into a <a href="http://coppermine.sourceforge.net/">coppermine
 * picture gallery</a>. This parameter contains the identifier of the album,
 * where pictures should be used. See CoppermineUploadPolicy for an example.
 * <BR>
 * Before upload, CoppermineUploadPolicy.{@link wjhk.jupload2.policies.CoppermineUploadPolicy#isUploadReady()}
 * checks that the albumId is correct, that is: >=1. </TD>
 * </TR>
 * <TR>
 * <TD>debugLevel</TD>
 * <TD>0 <BR>
 * <BR>
 * {@link wjhk.jupload2.policies.DefaultUploadPolicy}</TD>
 * <TD>With 0, you get the normal production output. The higher the number is,
 * the more information is displayed in the status bar. <BR>
 * Note: the whole debug messages is stored in the
 * {@link wjhk.jupload2.policies.DefaultUploadPolicy#debugBufferString}. It can
 * be used to display more information, if needed. See also the 'webmasterMail'
 * parameter. </TD>
 * </TR>
 * <TR>
 * <TD>filenameEncoding</TD>
 * <TD><I>null</I><BR>
 * <BR>
 * {@link wjhk.jupload2.policies.DefaultUploadPolicy}</TD>
 * <TD>With null, the filename in the <I>Content-Disposition</I> header is not
 * encoded. If not null, the applet tries to encode this filename with the given
 * encoding. It's up to the receiver (the web site) to decode this encoding (see
 * {@link #getUploadFilename(FileData, int)}. <BR>
 * Example: if the "UTF8" encoding is choosen, the PHP function urldecode can be
 * used to decode the filename. </TD>
 * </TR>
 * <TR>
 * <TD>highQualityPreview</TD>
 * <TD>false<BR>
 * <BR>
 * {@link wjhk.jupload2.policies.DefaultUploadPolicy}</TD>
 * <TD>If this parameter is set to <I>true</I>, the applet will call the
 * BufferedImage.getScaledInstance(), instead of doing a basic scale
 * transformation. This consume more CPU: on a PII 500MHz, the full screen go
 * from around 5 seconds to between 12 and 20 seconds, for a picture created by
 * my EOS20D (8,5M pixels). The standard preview (above the file list) seem to
 * be displayed at the same speed, whatever is the value of this parameter. <BR>
 * Note: when resizing is done before upload, the
 * BufferedImage.getScaledInstance() is always called, so that the uploaded
 * picture is of the best available quality. </TD>
 * </TR>
 * <TR>
 * <TD>lang</TD>
 * <TD>Navigator language <BR>
 * <BR>
 * {@link wjhk.jupload2.policies.DefaultUploadPolicy}</TD>
 * <TD>Should be something like <I>en</I>, <I>fr</I>... Currently only french
 * and english are known from the applet. If anyone want to add another language
 * ... Please translate the wjhk.jupload2.lang.lang_en, and send it back to
 * <mailto:etienne_sf@sourceforge.net">. </TD>
 * </TR>
 * <TR>
 * <TD>lookAndFeel <BR>
 * since 2.5</TD>
 * <TD><I>empty</I><BR>
 * <BR>
 * {@link wjhk.jupload2.policies.DefaultUploadPolicy}</TD>
 * <TD>This allows to control the look & feel of the applet. The authorized
 * values are: <DIR>
 * <LI><I>empty</I>: uses the default look & feel. This is the same as java.
 * <LI>java: uses the java default look & feel. Same as <I>empty</I>.
 * <LI>system: uses the current system look and feel. The call will be : <BR>
 * UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
 * <LI>Any valid String argument for UIManager.setLookAndFeel(String). </DIR>
 * </TD>
 * </TR>
 * <TR>
 * <TD>maxChunkSize<BR>
 * Since 2.7.1</TD>
 * <TD>0<BR>
 * <I>Long.MAX_VALUE</I><BR>
 * <BR>
 * {@link wjhk.jupload2.policies.DefaultUploadPolicy}</TD>
 * <TD>This parameters defines the maximum size of an upload. <DIR>
 * <LI>If not set, or set to a value of 0 or less, the chunk mode is disabled.
 * That is: each file will be uploaded within being splitted in pieces.
 * <LI>If set to a value of 1 or more, the upload size will be never be more
 * than maxChunkSize. A file bigger will be split in several part of
 * <I>maxChunkSize</I> size, then the last part will contain the remaining, and
 * will probably be smaller than <I>maxChunkSize</I>. </DIR> <BR>
 * <B>How to build the server part:</B> the server will have to 'guess' that
 * the file is splitted, and then it will have to reconstruct the uploaded file.
 * Here are the necessary informations: <DIR>
 * <LI>When a file is chunked, the <I>jupart</I> and <I>jufinal</I> parameter
 * are given in the URL (get parameters). This identify a chunk upload. If these
 * parameters are not given, the file(s) is(are) uploaded in one piece.
 * <LI><I>jupart</I> identify the part number: from 1 to N for a file being
 * plitted in N pieces. The N-1 chunks should be <I>maxChunkSize</I> bytes
 * long. The last one contains the remaining of the file.
 * <LI><I>jufinal</I> is set to 0 for chunks from 1 to N-1. It is is set to 1
 * only for the last chunk (N, in this 'example').
 * <LI>The uploaded filename is not modified when the upload is chunked.
 * Example: upload of the file <I>bigPicture.jpeg</I>, of 4,5 Mb, with chunk of
 * 2Mb. The upload is splitted in three chunk. Chunk 1 and 2 are 2Mb long. The
 * third one is 0,5Mb long. The uploaded filename for these three uploads is
 * <I>bigPicture.jpeg</I>. It's up to the server part to read the <I>jupart</I>
 * and <I>jufinal</I> get parameters, to understand that the upload is chunked.
 * <LI><B>Important:</B> The server script <U>must</U> check the resulting
 * filesize. If not, the client can send a file of any size, and fill the server
 * hard drive.
 * <LI>The wwwroot/pages/parseRequest.jsp is a java example of a server page
 * that can receive chunk upload. It stores each chunk is <I>filename.partN</I>
 * (where N is the chunk number), then construct the final file, by
 * concatenating all parts together. </DIR> <B>Note: </B> If nbFilesPerRequest
 * is different than 1, the applet will try to upload the files until the sum of
 * their content length is less than maxChunkSize. The upload is triggered just
 * before the sum of their content length is bigger then maxChunkSize.<BR>
 * If one file is bigger than <I>maxChunkSize</I>, all previous files are
 * uploaded (at once or not, depending on nbFilesPerRequest). Then the 'big'
 * file is uploaded alone, splitted in chunk. Then upload goes on, file by file
 * or not, depending on <I>nbFilesPerRequest</I>. </TD>
 * </TR>
 * <TR>
 * <TD>maxFileSize<BR>
 * Since 2.7.1</TD>
 * <TD>0<BR>
 * <I>Long.MAX_VALUE</I><BR>
 * <BR>
 * {@link wjhk.jupload2.policies.DefaultUploadPolicy}</TD>
 * <TD>This parameter identify the maximum size that an uploaded file may have.
 * It prevent the user to upload too big files. It is especially important when
 * chunk upload is activated (see below <I>maxChunkSizew</I>). <DIR>
 * <LI>If <I>maxChunkSize</I> is not set, negative or 0, <I>maxFileSize</I>
 * should be the maximum upload size of the server. In this case, it is useful
 * only to display a message when the user select a file that will be refused by
 * the server.
 * <LI>If chunk upload is activated, this parameter becomes really important:
 * in this case the maximum file size of an uploaded file is ... the available
 * space on the server hard drive! (see below, <I>maxChunkSize</I>). </DIR>
 * </TD>
 * </TR>
 * <TR>
 * <TD>maxPicHeight</TD>
 * <TD>-1 <BR>
 * <BR>
 * {@link wjhk.jupload2.policies.PictureUploadPolicy}</TD>
 * <TD>This parameters allows the HTML page to control the maximum height for
 * pictures. If a picture is to be download, and its height is bigger, the
 * picture will be resized. The proportion between width and height of the
 * resized picture are the same as those of the original picture. If both
 * maxPicHeight and maxPicWidth are given, it can happen that the resized
 * picture has a height lesser than maxPicHeight, so that width is no more than
 * maxPicWidth. <BR>
 * <B>Precisions:</B> <BR>
 * If this parameter value is negative, then no control is done on the picture
 * height. <BR>
 * If the original picture is smaller than the maximum size, the picture is not
 * enlarged. <BR>
 * If the picture is resized, its other characteristics are kept (number of
 * colors, ColorModel...). The picture format is ketp, if targetPictureFormat is
 * empty. If the picture format is a destructive (like jpeg), the maximum
 * available quality is choosed. <BR>
 * <I>See also maxPicWidth, realMaxPicHeight</I> </TD>
 * </TR>
 * <TR>
 * <TD>maxPicWidth</TD>
 * <TD>-1 <BR>
 * <BR>
 * {@link wjhk.jupload2.policies.PictureUploadPolicy}</TD>
 * <TD>Same as maxPicHeight, but for the maximum width of the uploaded picture.
 * <BR>
 * <I>See also maxPicHeight, realMaxPicWidth</I> </TD>
 * </TR>
 * <TR>
 * <TD>nbFilesPerRequest</TD>
 * <TD>-1 <BR>
 * <BR>
 * {@link wjhk.jupload2.policies.DefaultUploadPolicy}</TD>
 * <TD>This allows the control of the maximal number of files that are uploaded
 * in one HTTP upload to the server. <BR>
 * If set to -1, there is no maximum. This means that all files are uploaded in
 * the same HTTP request. <BR>
 * If set to 5, for instance, and there are 6 files to upload, there will be two
 * HTTP upload request to the server : 5 files in the first one, and that last
 * file in a second HTTP request. </TD>
 * </TR>
 * <TR>
 * <TD><B>postURL</B></TD>
 * <TD><I>Mandatory</I> <BR>
 * <BR>
 * {@link wjhk.jupload2.policies.DefaultUploadPolicy}</TD>
 * <TD><B> It contains the target URL toward which the files should be upload.
 * This parameter is mandatory for existing class. It may become optional in new
 * UploadPolicy, that would create this URL from other data. If this URL may
 * change during the applet execution time, you can call the setProperty applet
 * method from javascript, or create a new UploadPolicy class and either : <DIR>
 * <LI>Override the {@link wjhk.jupload2.policies.UploadPolicy#getPostURL()}
 * method, to make the postURL totaly dynamic.
 * <LI>Override the
 * {@link wjhk.jupload2.policies.UploadPolicy#setPostURL(String)} method, to
 * modify the postURL on the fly, when it is changed.
 * <LI>Override the
 * {@link wjhk.jupload2.policies.UploadPolicy#setProperty(String, String)}
 * method. The {@link wjhk.jupload2.policies.CoppermineUploadPolicy} changes the
 * postURL when the albumID property changes.
 * <LI>Find another solution ... </DIR> <U>Note 1:</U> in HTTP, the upload is
 * done in the same user session, as the applet uses the cookies from the
 * navigator. This allows right management during upload, on the server side.<BR>
 * <U>Note 2:</U> FTP URL should looks like:
 * ftp://username:password@myhost.com:21/directory<BR>
 * <U>Note 3:</U> in FTP, you'll have to add the jakarta-commons-oro.jar and
 * jakarta-commons-net.jar jar files in the applet ARCHIVE tag attribute. See
 * the 'advanced_js_demo.html page for a sample. You'll have to put the two
 * files coming from the JUpload distribution in the same directory as the
 * wjhk.jupload.jar. </B> </TD>
 * </TR>
 * <TR>
 * <TD>realMaxPicHeight</TD>
 * <TD>-1 <BR>
 * <BR>
 * {@link wjhk.jupload2.policies.PictureUploadPolicy}<BR>
 * <I>Since v2.8.1</I></TD>
 * <TD>This parameters is about the same as maxPicHeight. It overrides it for
 * pictures that must be transformed (currentlty only when the picture is
 * rotated). <BR>
 * The aim of this parameter, is to prevent the applet to resize picture, and
 * let the server do it: it will be much quicker. <BR>
 * This allows you to: <DIR>
 * <LI>Put a 'big' <I>maxPicHeight</I> (or don't provide the parameter in the
 * APPLET tag), and let the server resize the picture according to the real
 * maxHeight. The <I>maxPicHeight</I> will be used when the picture is not
 * tranformed by the user.
 * <LI>Put this realMaxHeight to the real configured maxHeight. The applet will
 * then directly produce the final file, when it has to tranform the picture
 * (picture rotation, for instance). </DIR> <BR>
 * <I>See also maxPicHeight, realMaxPicWidth, maxChunkSize (to override any
 * server upload size limitation).</I> </TD>
 * </TR>
 * <TR>
 * <TD>realMaxPicWidth</TD>
 * <TD>-1 <BR>
 * <BR>
 * {@link wjhk.jupload2.policies.PictureUploadPolicy}<I>Since v2.8.1</I></TD>
 * <TD>Same as realMaxPicHeight, but for the maximum width of uploaded picture
 * that must be transformed. <BR>
 * <I>See also maxPicWidth, realMaxPicHeight</I> </TD>
 * </TR>
 * <TR>
 * <TD>serverProtocol</TD>
 * <TD>HTTP/1.1 <BR>
 * <BR>
 * {@link wjhk.jupload2.policies.DefaultUploadPolicy}</TD>
 * <TD>This parameter allows the control of the protocol toward the server.
 * Currently, only HTTP is supported, so valid values are HTTP/0.9 (not tested),
 * HTTP/1.0 and HTTP/1.1. <BR>
 * This parameter is really useful only in
 * {@link wjhk.jupload2.policies.CoppermineUploadPolicy}, as the coppermine
 * application also controls that the requests send within an HTTP session uses
 * the same HTTP protocol (as a protection to limit the 'steal' of session
 * cookies). </TD>
 * </TR>
 * <TR>
 * <TD>showStatusBar</TD>
 * <TD>True<BR>
 * <BR>
 * {@link wjhk.jupload2.policies.DefaultUploadPolicy}</TD>
 * <TD>If given with the <I>False</I> value, the status bar will be hidden.
 * The applet will still store all debug information in it. But the user won't
 * see it any more. If a problem occurs, the <I>urlToSendErrorTo</I> can still
 * be used to log all available information. </TD>
 * </TR>
 * <TR>
 * <TD>storeBufferedImage</TD>
 * <TD>false <BR>
 * <BR>
 * {@link wjhk.jupload2.policies.PictureUploadPolicy}</TD>
 * <TD>This parameter indicates that the preview image on the applet is kept in
 * memory. It works really nice under eclise. But, once in the navigator, the
 * applet runs very quickly out of memory. So I add a lot of calls to
 * {@link wjhk.jupload2.filedata.PictureFileData#freeMemory(String)}, but it
 * doesn't change anything. Be careful to this parameter, and let it to the
 * default value, unless you've well tested it under all your target client
 * configurations. </TD>
 * </TR>
 * <TR>
 * <TD>stringUploadSuccess</TD>
 * <TD>empty string ("") since 2.9.0<BR>
 * (was ".* 200 OK$" before) <BR>
 * <BR>
 * {@link wjhk.jupload2.policies.DefaultUploadPolicy}</TD>
 * <TD>This string is a regular expression. It allows the applet to test that
 * the server has accepted the upload. If this parameter is given to the applet,
 * the upload thread will try to match this regular epression to each lines
 * returned from the server.<BR>
 * If the match is successfull once, the upload is considered to be a success.
 * If not, a {@link wjhk.jupload2.exception.JUploadExceptionUploadFailed} is
 * thrown. <BR>
 * The default test expression testes that the web server returns no HTTP error:
 * 200 is the return code for a successfull HTTP request. It actually means that
 * postURL is a valid URL, and that the applet was able to send a request to
 * this URL: there should be no problem with the network configuration, like
 * proxy, password proxy...). <BR>
 * <B>But</B> it doesn't mean that the uploaded files have correctly be managed
 * by the server. For instance, the URL can be http://sourceforge.net, which, of
 * course, would not take your files into account. <BR>
 * So, as soon as you know a regular expression that test the return from the
 * target application (and not just a techical HTTP response code), change the
 * stringUploadSuccess to this value. For instance, the
 * {@link wjhk.jupload2.policies.CoppermineUploadPolicy} changes this value to
 * "^SUCCESS$", as the HTTP body content of the server's answer contain just
 * this exact line. This 'success' means that the pictures have correctly be
 * added to the album, that vignettes have been generated (this I suppose),
 * etc... </TD>
 * </TR>
 * <TR>
 * <TD>targetPictureFormat</TD>
 * <TD><I>Empty String</I> <BR>
 * <BR> (<B>to be</B> implemented in
 * {@link wjhk.jupload2.policies.PictureUploadPolicy})</TD>
 * <TD>This parameter can contain any picture writer known by the JVM. For
 * instance: jpeg, png, gif. All standard formats should be available. More
 * information can be found on the <A
 * href='http://java.sun.com/j2se/1.4.2/docs/guide/imageio/spec/title.fm.html'>java.sun.com</A>
 * web site. </TD>
 * </TR>
 * <TR>
 * <TD><B>uploadPolicy</B></TD>
 * <TD>DefaultUploadPolicy <BR>
 * <BR>
 * see {@link wjhk.jupload2.policies.UploadPolicyFactory}</TD>
 * <TD>This parameter contains the class name for the UploadPolicy that should
 * be used. If it is not set, or if its value is unknown from
 * {@link wjhk.jupload2.policies.UploadPolicyFactory#getUploadPolicy(JUploadApplet)},
 * the {@link wjhk.jupload2.policies.DefaultUploadPolicy} is used. </TD>
 * </TR>
 * <TR>
 * <TD>urlToSendErrorTo</TD>
 * <TD><I>Empty String</I> <BR>
 * <BR>
 * {@link wjhk.jupload2.policies.DefaultUploadPolicy}</TD>
 * <TD>If this url is given, and an upload error occurs, the applet post the
 * all the debug output to this address. It's up to this URL to handle this
 * mail. It is possible to just store the file, or to log the error in a
 * database, or to send a mail (like the mail.php script given with the
 * coppermine pack). <BR>
 * <U>Note:</U> Don't put a mailto link here: it won't be able to manage the
 * debug output, that is too big. The maximum length of a mailto depends on the
 * navigator. With Firefox, it seems to be around 4kb. </TD>
 * </TR>
 * </TABLE> <A NAME="example">
 * <H3>HTML call example</H3>
 * </A> You'll find below an example of how to put the applet into a PHP page:
 * <BR>
 * <XMP> <APPLET NAME="JUpload" CODE="wjhk.jupload2.JUploadApplet"
 * ARCHIVE="plugins/jupload/wjhk.jupload.jar" <!-- Applet display size, on the
 * navigator page --> WIDTH="500" HEIGHT="700" <!-- The applet call some
 * javascript function, so we must allow it : --> MAYSCRIPT > <!-- Only one
 * parameter is mandatory. We don't precise the UploadPolicy, so
 * DefaultUploadPolicy is used. The applet behaves like the original JUpload.
 * (jupload v1) --> <PARAM NAME="postURL"
 * VALUE="http://some.host.com/youruploadpage.php"> Java 1.4 or higher plugin
 * required. </APPLET> </XMP>
 * 
 * @author Etienne Gauthier
 * @see wjhk.jupload2.policies.DefaultUploadPolicy
 */

public interface UploadPolicy {

    /*
     * Available parameters for the applet. New parameters (for instance for new
     * policies) should all be added here, in alphabetic order. This ensures
     * that all tags are unique
     */
    final static String PROP_AFTER_UPLOAD_URL = "afterUploadURL";

    final static String PROP_ALLOWED_FILE_EXTENSIONS = "allowedFileExtensions";

    final static String PROP_ALBUM_ID = "albumId";

    // Be careful: if set to true, you'll probably have memory problems while in
    // a navigator.
    final static String PROP_STORE_BUFFERED_IMAGE = "storeBufferedImage";

    final static String PROP_DEBUG_LEVEL = "debugLevel";

    final static String PROP_LANG = "lang";

    final static String PROP_FILENAME_ENCODING = "filenameEncoding";

    final static String PROP_HIGH_QUALITY_PREVIEW = "highQualityPreview";

    final static String PROP_LOOK_AND_FEEL = "lookAndFeel";

    final static String PROP_MAX_CHUNK_SIZE = "maxChunkSize";

    final static String PROP_MAX_FILE_SIZE = "maxFileSize";

    final static String PROP_MAX_HEIGHT = "maxPicHeight";

    final static String PROP_MAX_WIDTH = "maxPicWidth";

    final static String PROP_NB_FILES_PER_REQUEST = "nbFilesPerRequest";

    final static String PROP_POST_URL = "postURL";

    final static String PROP_REAL_MAX_HEIGHT = "realMaxPicHeight";

    final static String PROP_REAL_MAX_WIDTH = "realMaxPicWidth";

    final static String PROP_SERVER_PROTOCOL = "serverProtocol";

    final static String PROP_SHOW_STATUSBAR = "showStatusBar";

    final static String PROP_STRING_UPLOAD_SUCCESS = "stringUploadSuccess";

    final static String PROP_TARGET_PICTURE_FORMAT = "targetPictureFormat";

    final static String PROP_UPLOAD_POLICY = "uploadPolicy";

    final static String PROP_URL_TO_SEND_ERROR_TO = "urlToSendErrorTo";

    final static String DEFAULT_AFTER_UPLOAD_URL = null;

    final static String DEFAULT_ALLOWED_FILE_EXTENSIONS = "";

    final static int DEFAULT_ALBUM_ID = 0;

    // Be careful: if set to true, you'll probably have memory problems while in
    // a navigator.
    final static boolean DEFAULT_STORE_BUFFERED_IMAGE = false;

    final static int DEFAULT_DEBUG_LEVEL = 0;

    final static String DEFAULT_LANG = null;

    // Note: the CoppermineUploadPolicy forces it to "UTF8".
    final static String DEFAULT_FILENAME_ENCODING = null;

    final static boolean DEFAULT_HIGH_QUALITY_PREVIEW = false;

    final static String DEFAULT_LOOK_AND_FEEL = "";

    final static long DEFAULT_MAX_CHUNK_SIZE = Long.MAX_VALUE;

    // Take care of this parameter if chunk upload is activated!
    // See comment above.
    final static long DEFAULT_MAX_FILE_SIZE = Long.MAX_VALUE;

    final static int DEFAULT_MAX_WIDTH = -1;

    final static int DEFAULT_MAX_HEIGHT = -1;

    // Note: the CoppermineUploadPolicy forces it to 1.
    final static int DEFAULT_NB_FILES_PER_REQUEST = -1;

    final static String DEFAULT_POST_URL = "http://localhost:8080/jupload/pages/parseRequest.jsp";

    final static int DEFAULT_REAL_MAX_WIDTH = -1;

    final static int DEFAULT_REAL_MAX_HEIGHT = -1;

    final static String DEFAULT_SERVER_PROTOCOL = "HTTP/1.1";

    final static boolean DEFAULT_SHOW_STATUSBAR = true;

    // Note: was ".* 200 OK$" before 2.9.0
    final static String DEFAULT_STRING_UPLOAD_SUCCESS = "";

    final static String DEFAULT_TARGET_PICTURE_FORMAT = null;

    final static String DEFAULT_UPLOAD_POLICY = "DefaultUploadPolicy";

    final static String DEFAULT_URL_TO_SEND_ERROR_TO = "";

    /**
     * This method is called to create the top panel. The default implementation
     * is defined in
     * {@link wjhk.jupload2.policies.DefaultUploadPolicy#createTopPanel(JButton, JButton, JButton, JPanel)}.
     * 
     * @param browse The default browse button.
     * @param remove The default removeSelected button.
     * @param removeAll The default removeAll button.
     * @param mainPanel The panel that contains all objects. It can be used to
     *            change the cursor (to a WAIT_CURSOR for instance).
     * @return the topPanel, that will be displayed on the top of the Applet.
     */
    public JPanel createTopPanel(JButton browse, JButton remove,
            JButton removeAll, JPanel mainPanel);

    /**
     * This methods creates a new FileData instance (or one of its inherited
     * classes), and return it to the caller.
     * 
     * @param file The file used to create the FileData instance. This method is
     *            called once for each file selected by the user, even if the
     *            user added several files in one 'shot'.
     * @return A FileData instance. The exact class depends on the
     *         currentUploadPolicy. Can be null, if the policy performs checks,
     *         and the given file is not Ok for these controls. See
     *         {@link PictureUploadPolicy#createFileData(File)} for an example.
     *         It's up to the upload policy to display a message to inform the
     *         user that this file won't be added to the file list.
     */
    public FileData createFileData(File file);

    /**
     * This method displays the applet parameter list, according to the current
     * debugLevel. It is called by the {@link #setDebugLevel(int)} method. It
     * should be override by any subclasses, that should display its own
     * parameters, then call <I>super.displayParameterStatus()</I>.
     */
    public void displayParameterStatus();

    // //////////////////////////////////////////////////////////////////////////////////////////////
    // /////////////////// getters / setters
    // ///////////////////////////////////////////////////
    // //////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * This allow runtime modifications of properties. With this method, you can
     * change any applet parameter after the applet initilization, with
     * JavaScript for instance. If the applet parameters given in <I>prop</I>
     * is not managed by this method, a warning is displayed on the status bar.
     * 
     * @param prop The applet parameter name.
     * @param value The new value for this parameter. If the value is not valid
     *            (for instance <I>aaa</I> for a number), a warning is
     *            displayed in the status bar, and the existing value is not
     *            changed.
     */
    public void setProperty(String prop, String value);

    /**
     * Retrieves the current value for the afterUploadURL applet parameter.
     * 
     * @return The current value for he afterUploadURL applet parameter.
     */
    public String getAfterUploadURL();

    /**
     * Retrieves the current value for allowedFileExtensions *
     * 
     * @return Current value for allowedFileExtensions
     */
    public String getAllowedFileExtensions();

    /**
     * A useful function, that has nothing to do with an upload policy. But it
     * is useful to have it here, as the uploadPolicy is known everywhere in the
     * applet.
     * 
     * @return Reference to the applet.
     */
    public JUploadApplet getApplet();

    /**
     * This method indicate whether or not the debug messages must be displayed.
     * Default is no debug (0). <BR>
     * To activate the debug, add a 'debugLevel' parameter to the applet (with 1
     * to n value), or call this method. Currently, level used in the code are
     * between 0 (no debug) and 100 (max debug). <BR>
     * With a 0 value, no debug messages will be displayed. The
     * {@link DefaultUploadPolicy}.addMsgToDebugBufferString method stores all
     * debug output in a BufferString.
     * 
     * @param debugLevel The new debugLevel.
     * @see DefaultUploadPolicy#sendDebugInformation(String)
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
     * Return the encoding that should be used for the filename. This encoding
     * has no impact on the content of the file that will be uploaded.
     * 
     * @return The encoding name, like UTF-8 (see the Charset JDK
     *         documentation).
     */
    public String getFilenameEncoding();

    /**
     * Returns the value of the applet parameter maxChunkSize (see above for a
     * description of all applet parameters)
     * 
     * @return the current value of maxChunkSize.
     */
    public long getMaxChunkSize();

    /**
     * Returns the value of the applet parameter maxFileSize (see above for a
     * description of all applet parameters)
     * 
     * @return the current value of maxFileSize.
     */
    public long getMaxFileSize();

    /**
     * This function returns the number of files should be uploaded during one
     * access to the server. If negative or 0, all files are to be uploaded in
     * one HTTP request. If positive, each HTTP upload contains this number of
     * files. The last upload request may contain less files. <BR>
     * Examples :
     * <UL>
     * <LI>If 1 : files are uploaded file by file.
     * <LI>If 5 : files are uploaded 5 files by 5 files. If 12 files are
     * uploaded, 3 HTTP upload are done, containing 5, 5 and 2 files.
     * </UL>
     * 
     * @return Returns the maximum number of files, to download in one HTTP
     *         request.
     */
    public int getNbFilesPerRequest();

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
     * HTTP protocol that should be used to send the HTTP request. Currently,
     * this is mainly used by
     * {@link wjhk.jupload2.policies.CoppermineUploadPolicy}, as the coppermine
     * control that the protocol used for each HTTP request is the same as the
     * one used during the session creation. It is used in the default policy,
     * as it could be used elsewhere. <BR>
     * Default is : HTTP/1.1
     * 
     * @return The selected server protocol.
     */
    public String getServerProtocol();

    /**
     * Indicate whether the status bar should be shown. It may be interesting to
     * hide it, as it contains no really text information. But it still is the
     * only place where is displayed the upload status (and upload error if
     * any). <BR>
     * Default is : true
     * 
     * @return The current value for the <I>showStatusBar</I> applet parameter.
     */
    public boolean getShowStatusBar();

    /**
     * Get the original name of the file on the disk. This function can encode
     * the filename (see the filenameEncoding parameter). By default, the
     * original filename is returned.
     * 
     * @param fileData
     * @param index
     * @return The filename the is given in the filename part of the
     *         Content-Disposition header.
     */
    public String getUploadFilename(FileData fileData, int index)
            throws JUploadException;

    /**
     * Get an upload filename, that is to be send in the HTTP upload request.
     * This is the name part of the Content-Disposition header. That is: this is
     * the name under which you can manage the file (for instance in the
     * _FILES[$name] in PHP) and not the filename of the original file.
     * 
     * @param index index of the file within upload (can be4, ou of 10 for
     *            instance).
     * @return The name part of the Content-Disposition header.
     * @see #getUploadFilename(FileData, int)
     */
    public String getUploadName(FileData fileData, int index);

    /**
     * Returns the current URL where error log must be posted. See <a
     * href="#parameters>Parameters</a>
     * 
     * @return the urlToSendErrorTo
     */
    public String getUrlToSendErrorTo();

    /**
     * Get the regular expression that will be tested against each line of the
     * server answer. If one line matches this expression, that upload is marked
     * as successful. <BR>
     * The upload works this way:
     * <OL>
     * <LI>Upload the selected file(s) to the server
     * <LI>Get all the server HTTP response.
     * <LI>The stringUploadSuccess regular expression is tested against each
     * line from the server.
     * <LI>If the above test gives a match, the upload is marked as successful.
     * Else, the upload is marked as unsuccessful, and a
     * JUploadExceptionUploadFailure is thrown.
     * </OL>
     * 
     * @return The regular expression that must be run again each line of the
     *         http answer.
     */
    public String getStringUploadSuccess();

    // //////////////////////////////////////////////////////////////////////////////////////////////
    // /////////////////// miscellanneous methods
    // ////////////////////////////////////////////////
    // //////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * This methods is called by the {@link JUploadFileFilter#accept(File)}. It
     * allows the current upload policy to filter files, according to any
     * choosen applet behaviour.<BR>
     * In the {@link DefaultUploadPolicy} upload policy, this filter is based on
     * the applet parameter: <I>allowedFileExtensions</I>.
     * 
     * @see JUploadPanel#JUploadPanel(java.awt.Container,
     *      wjhk.jupload2.gui.JUploadTextArea, UploadPolicy)
     */
    public boolean fileFilterAccept(File file);

    /**
     * Return a description for the FileFilter, according to the current upload
     * policy.
     */
    public String fileFilterGetDescription();

    /**
     * Response for the {@link JUploadFileView#getIcon(File)}. Default is
     * implemented in {@link DefaultUploadPolicy#fileViewGetIcon(File)}, by
     * returning null, which displays the default icon.
     * 
     * @param file The file from which the icon should represent.
     * @return The resulting icon.
     */
    public Icon fileViewGetIcon(File file);

    /**
     * This method allows the applet to post debug information to the website
     * (see {@link #getUrlToSendErrorTo()}). Then, it is possible to log the
     * error, to send a mail...
     * 
     * @param reason A string describing briefly the problem. The mail subject
     *            will be something like: Jupload Error (reason)
     */
    public void sendDebugInformation(String reason);

    /**
     * log an error message, based on an exception. Will be logged in the status
     * bar, if defined.
     * 
     * @param e The exception to report
     */
    public void displayErr(Exception e);

    /**
     * log an error message. Will be logged in the status bar, if defined.
     * 
     * @param err The erreur message to be displayed.
     */
    public void displayErr(String err);

    /**
     * log an info message. Will be logged in the status bar, if defined.
     * 
     * @param info The information message that will be displayed.
     */
    public void displayInfo(String info);

    /**
     * log a warning message. Will be logged in the status bar, if defined.
     * 
     * @param warn The warning message that will be displayed.
     */
    public void displayWarn(String warn);

    /**
     * log a debug message. Will be logged in the status bar, if defined.
     * 
     * @param debug The message to display.
     * @param minDebugLevel If the current debug level is superior or equals to
     *            minDebugLevel, the message will be displayed. Otherwise, it
     *            will be ignored.
     */
    public void displayDebug(String debug, int minDebugLevel);

    /**
     * Add an header to the list of headers that will be added to each HTTP
     * upload request. This method is called from specific uploadPolicies, which
     * would need headers to be added to all uploads. These headers are used in
     * {@link wjhk.jupload2.policies.DefaultUploadPolicy}.
     * 
     * @param header
     * @see #onAppendHeader(StringBuffer)
     */
    public void addHeader(String header);

    /**
     * Append specific headers for this upload (session cookies, for instance).
     * This method is called while building each upload HTTP request.
     * 
     * @param sb The header StringBuffer where sp�cific headers should be
     *            appended.
     * @return The StringBuffer given in parameters. This is conform to the
     *         StringBuffer.append method.
     * @see #addHeader(String)
     * @see wjhk.jupload2.upload.FileUploadThreadV3#doUpload(FileData[],int,int)
     */
    public StringBuffer onAppendHeader(StringBuffer sb);

    /**
     * This method is called each time a file is selected in the panel files. It
     * allows, for instance, to preview a picture
     * {@link wjhk.jupload2.policies.PictureUploadPolicy}.
     * 
     * @param fileData
     */
    public void onSelectFile(FileData fileData);

    /**
     * Indicate if everything is ready for upload.
     * 
     * @return indicate if everything is ready for upload.
     */
    public boolean isUploadReady();

    /**
     * Enable any action, required before an upload. For instance,
     * {@link PictureUploadPolicy} disable the rotation buttons during buttons.
     * 
     * @see #afterUpload(Exception, String)
     */
    public void beforeUpload();

    /**
     * This method returns true, if upload is a success. A HTTP response of "200
     * OK" indicates that the server response is techically correct. But, it may
     * be a functionnal error. For instance, the server could answer by a proper
     * HTTP page, that the user is no allowed to upload files. It's up to the
     * uploadPolicy to check this, and answer true or false to this method. <BR>
     * This method is called once for each HTTP request toward the server. For
     * instance, if the upload is done file by file, and there are three files
     * to upload, this method will be called three times. <BR>
     * So this method is different from the
     * {@link #afterUpload(Exception, String)}, that will be called only once
     * in this case, after the three calls to the checkUploadSuccess method.
     * 
     * @param serverOutput The full http response, including the http headers.
     * @param serverOutputBody The http body part (that is: the serverOuput
     *            without the http headers and the blank line that follow them).
     * @return true (or an exception is raised, instead of returning false).
     *         This garantees that all cases are handled: the compiler will
     *         indicate an error if the code can come to the end of the method,
     *         without finding a 'return' or a throw exception. This return code
     *         can be ignored by the caller.
     */
    public boolean checkUploadSuccess(String serverOutput,
            String serverOutputBody) throws JUploadException;

    /**
     * This method is called after an upload, whether it is successful or not.
     * This method is called once for each click of the user on the 'upload'
     * button. That is: if the nbFilesPerRequest is 2, and the user selected 5
     * files before clicking on the 'upload' button. Then the afterUpload is
     * called once the 5 files were uploaded to the server.
     * 
     * @param e null if success, or the exception indicating the problem.
     * @param serverOutput The full server output, including the HTTP headers.
     */
    public void afterUpload(Exception e, String serverOutput);

    /**
     * Retrieve a local property. This allows localization. All strings are
     * stored in the property files in the wjhk.jupload2.lang package.
     * 
     * @param key The key, whose associated text is to retrieve.
     * @return The associated text.
     * @see wjhk.jupload2.policies.DefaultUploadPolicy#DefaultUploadPolicy(JUploadApplet)
     */
    public String getString(String key);

    /**
     * Retrive a local property. This allows localization. All strings are
     * stored in the property files in the wjhk.jupload2.lang package. <BR>
     * All occurences of <B>{1}</B> in the value (corresponding to key) are
     * replaced by value1. <BR>
     * Sample : <BR>
     * Love=Oh {1}, I love you so much ... <BR>
     * Call it by <I>getString("Love", "John Smith")</I> ... &nbsp; ;-)
     * 
     * @param key The key, whose associated text is to retrieve.
     * @param value1 The value, which will replace all occurence of {1}
     * @return The associated text.
     * @see wjhk.jupload2.policies.DefaultUploadPolicy#DefaultUploadPolicy(JUploadApplet)
     */
    public String getString(String key, String value1);

    /**
     * Same as {@link #getString(String, String)}, for two parameters.
     * 
     * @param key The key, whose associated text is to retrieve.
     * @param value1 The first value, which will replace all occurence of {1}
     * @param value2 The second value, which will replace all occurence of {2}
     * @return The associated text.
     * @see wjhk.jupload2.policies.DefaultUploadPolicy#DefaultUploadPolicy(JUploadApplet)
     */
    public String getString(String key, String value1, String value2);

    /**
     * Same as {@link #getString(String, String)}, for three parameters.
     * 
     * @param key The key, whose associated text is to retrieve.
     * @param value1 The first value, which will replace all occurence of {1}
     * @param value2 The second value, which will replace all occurence of {2}
     * @param value3 The third value, which will replace all occurence of {3}
     * @return The associated text.
     * @see wjhk.jupload2.policies.DefaultUploadPolicy#DefaultUploadPolicy(JUploadApplet)
     */
    public String getString(String key, String value1, String value2,
            String value3);

    /**
     * Same as {@link #getString(String,String)}, but the given value is an
     * integer.
     * 
     * @param key The key, whose associated text is to retrieve.
     * @param value1 The value, which will replace all occurence of {1}
     * @return The associated text.
     */
    public String getString(String key, int value1);

    /**
     * alert displays a MessageBox with a unique 'Ok' button, like the
     * javascript alert function.
     * 
     * @param key The string identifying the text to display, depending on the
     *            current language.
     * @see #alertStr(String)
     */
    public void alert(String key);

    /**
     * alert displays a MessageBox with a unique 'Ok' button, like the
     * javascript alert function.
     * 
     * @param str The full String that must be displayed to the user.
     * @see #alert(String)
     */
    void alertStr(String str);

    /**
     * alert displays a MessageBox with a unique 'Ok' button, like the
     * javascript alert function.
     * 
     * @param key The string identifying the text to display, depending on the
     *            current language.
     * @param arg1 A string that will replace all {1} in the text corresponding
     *            to property_str. This allows to have dynamic localized text.
     */
    public void alert(String key, String arg1);
}
