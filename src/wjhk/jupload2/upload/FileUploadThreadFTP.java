package wjhk.jupload2.upload;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.util.MissingResourceException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JProgressBar;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

import wjhk.jupload2.exception.JUploadException;
import wjhk.jupload2.exception.JUploadExceptionUploadFailed;
import wjhk.jupload2.filedata.FileData;
import wjhk.jupload2.policies.UploadPolicy;

/** The FileUploadThreadFTP class is intended to extend the functionality of 
 *  the JUpload applet and allow it to handle ftp:// addresses.
 *  <br>
 *  Note: this class is not a V4 of the FTP upload. It is named V4, as it inherits from the 
 *  {@link FileUploadThreadV4} class.
 *  
 *  <br>
 *  <br>
 *  In order to use it, simply change the postURL argument to the applet to 
 *  contain the approperiate ftp:// link.  The format is:
 *  
 *  <pre>
 *    ftp://username:password@myhost.com:21/directory
 *  </pre>
 *  Where everything but the host is optional.
 *  
 *  There is another parameter that can be passed to the applet named 'binary'
 *  which will set the file transfer mode based on the value.  The possible 
 *  values here are 'true' or 'false'.  It was intended to be somewhat intelligent
 *  by looking at the file extension and basing the transfer mode on that, however,
 *  it was never implemented.  Feel free to!
 *  
 *  Also, there is a 'passive' parameter which also has a value of 'true' or 
 *  'false' which sets the connection type to either active or passive mode.
 *  
 * 
 * @author Evin Callahan (inheritance from DefaultUploadThread built by Etienne Gauthier) 
 * @author Daystar Computer Services
 * @see FileUploadThread
 * @see DefaultFileUploadThread
 * @version 1.0, 01 Jan 2007
 *
 **
 *Update march 2007, Etienne Gauthier
 *Adaptation to match all JUpload functions:
 *<DIR>
 *<LI>Inheritance from the {@link FileUploadThreadV4} class, 
 *<LI>Use of the UploadFileData class,
 *<LI>Before upload file preparation,
 *<LI>Upload stop by the user.
 *<LI>
 *</DIR>  
 */
public class FileUploadThreadFTP extends DefaultFileUploadThread {

	//////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////  PRIVATE ATTRIBUTES  ///////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////


	//////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////  PRIVATE ATTRIBUTES  ///////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////	
	
	/**
	 * The output stream, where the current file should be written. This output stream should not be used. 
	 * The buffered one is much faster.
	 */
	private OutputStream ftpOutputStream = null;
	
	/**
	 * The buffered stream, that the application should use for upload.
	 */
	private BufferedOutputStream bufferedOutputStream = null;
	
	
	private Matcher uriMatch;
	
	// the client that does the actual connecting to the server
	private FTPClient ftp = new FTPClient();
	
	// info taken from the ftp string
	private String user;
	private String pass;
	private String host;
	private String port;
	private String dir;	
	
	/**
	 * Indicates whether the connexion to the FTP server is open or not. This allows to connect once on
	 * the FTP server, for multiple file upload.
	 */
	private boolean bConnected = false;
	
	
	// this pattern defines the groups and pattern of the ftp syntax
	public final Pattern ftpPattern = Pattern.compile( "^ftp://(([^:]+):([^\\@]+)\\@)?([^/:]+):?([0-9]+)?(/(.*))?$" );

	//////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////    CONSTRUCTOR       ///////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////
	/** 
	 * Does the connection to the server based on the matcher created in the main.
	 * 
	 * @throws IllegalArgumentException  if any error occurs.  message is error
	 */ 
	public FileUploadThreadFTP( FileData[] filesDataParam, UploadPolicy uploadPolicy, JProgressBar progress) {
		super(filesDataParam, uploadPolicy, progress);		
	}

	
	//////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////  IMPLEMENTATION OF INHERITED METHODS ///////////////////////
	//////////////////////////////////////////////////////////////////////////////////////

	/** @see DefaultFileUploadThread#beforeRequest(int, int)  */
	void beforeRequest(int firstFileToUploadParam, int nbFilesToUploadParam) throws JUploadException {
		
		//If not already connected ... we connect to the server.
		if (!bConnected) { 
			//Let's connect to the FTP server.
			String url = uploadPolicy.getPostURL();
			uriMatch = ftpPattern.matcher(url);
			if( !uriMatch.matches() ) {
				throw new JUploadException("invalid URI: " + url);
			}
			user = uriMatch.group( 2 ) == null ? "anonymous"  : uriMatch.group( 2 );
			pass = uriMatch.group( 3 ) == null ? "JUpload"    : uriMatch.group( 3 );
			host = uriMatch.group( 4 );  // no default server
			port = uriMatch.group( 5 ) == null ? "21"         : uriMatch.group( 5 );
			dir  = uriMatch.group( 7 ) == null ? "/"          : uriMatch.group( 7 ); 
			
			// do connect.. any error will be thrown up the chain
			try {
				ftp.setDefaultPort( Integer.parseInt( port ) );
				ftp.connect( host );
				uploadPolicy.displayDebug( "Connected to " + host, 3 );
				uploadPolicy.displayDebug( ftp.getReplyString(), 20 );
				
				if( !FTPReply.isPositiveCompletion( ftp.getReplyCode() ) )
					throw new JUploadException( "FTP server refused connection." );
				
				// given the login information, do the login
				ftp.login( user, pass );
				uploadPolicy.displayDebug( ftp.getReplyString(), 20 );
				
				if( !FTPReply.isPositiveCompletion( ftp.getReplyCode() ) )
					throw new JUploadException("Invalid username / password");
				
				ftp.changeWorkingDirectory( dir );
				uploadPolicy.displayDebug(ftp.getReplyString(), 20);
				
				if( !FTPReply.isPositiveCompletion( ftp.getReplyCode() ) )
					throw new JUploadException("Invalid directory specified");

				bConnected = true;				
			} catch( Exception e ) {
				uploadException = e;
				throw new JUploadException("Could not connect to server (" + e.getMessage() + ")");
			}
		} //if(!bConnected)
	}
	

	/** @see DefaultFileUploadThread#afterFile(int) */
	void afterFile(int index)  throws JUploadException {
		//Nothing to do
	}

	/** @see DefaultFileUploadThread#beforeFile(int) */
	void beforeFile(int index)  throws JUploadException {
		try {
			setTransferType(index);
			// just in case, delete anything that exists
			
			//No delete, as the user may not have the right for that. We use, later, the store command:
			//If the file already exists, it will be replaced.			
			//ftp.deleteFile(filesToUpload[index].getFileName());
			
			//Let's open the stream for this file.
	        ftpOutputStream = ftp.storeFileStream(filesToUpload[index].getFileName());
	        //The upload is done through a BufferedOutputStream. This speed up the upload in an unbelivable way ...
	        bufferedOutputStream = new BufferedOutputStream(ftpOutputStream);
		} catch (IOException e) {
			throw new JUploadException(e, getClass().getName() + ".beforeFile(" + index + ")");
		}
	}

	/** @see DefaultFileUploadThread#cleanAll() */
	void cleanAll()  throws JUploadException {
		try {
			if( ftp.isConnected() ) {
				ftp.disconnect();
				uploadPolicy.displayDebug( "disconnected", 20 );
			}			
		} catch( IOException e ) {
			// then we arent connected
			uploadPolicy.displayDebug( "Not connected", 20 );
		} finally {
			ftpOutputStream = null;
			bufferedOutputStream = null;
		}
	}

	/** @see DefaultFileUploadThread#cleanRequest() */
	void cleanRequest() throws JUploadException {
		try {
			bufferedOutputStream.close();			
			ftpOutputStream.close();
			if( !ftp.completePendingCommand() ) {
				throw new JUploadExceptionUploadFailed("ftp.completePendingCommand() returned false");
			}
		} catch (IOException e) {
			throw new JUploadException(e, getClass().getName() + ".beforeFile()");
		} finally {
			bufferedOutputStream = null;
		}
	}

	/** @see DefaultFileUploadThread#finishRequest() */
	void finishRequest()  throws JUploadException {
		//Nothing to do
	}

	/** @see DefaultFileUploadThread#getAdditionnalBytesForUpload(int) */
	long getAdditionnalBytesForUpload(int indexFile) {
		//Default: no additional byte.
		return 0;
	}
	/** @see DefaultFileUploadThread#getResponseBody() */
	String getResponseBody() throws JUploadException {
		return "";
	}
	
	/** @see DefaultFileUploadThread#getOutputStream() */
	OutputStream getOutputStream()  throws JUploadException {
		return bufferedOutputStream;
	}

	/** @see DefaultFileUploadThread#startRequest(long, boolean, int, boolean) */
	void startRequest(long contentLength, boolean bChunkEnabled, int chunkPart, boolean bLastChunk) throws JUploadException {
		//Nothing to do
	}

	
	//////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////   PRIVATE METHODS    ///////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////
	/** Will set the binary/ascii value based on the parameters to the applet.
	 *  This could be done by file extension too but it is not implemented.
	 * 
	 * @param index The index of the file that we want to upload, in the array of files to upload.
	 * @throws IOException    if an error occurs while setting mode data
	 */
	private void setTransferType(int index) throws IOException {
		//FileData file
		try {
			String binVal = uploadPolicy.getString( "binary" );
			
			// read the value given from the user
			if( Boolean.getBoolean( binVal ) )
				ftp.setFileType( FTP.BINARY_FILE_TYPE );
			else
				ftp.setFileType( FTP.ASCII_FILE_TYPE );

		} catch( MissingResourceException e ) {
			// should set based on extension (not implemented)
			ftp.setFileType( FTP.BINARY_FILE_TYPE );
		}
		
		// now do the same for the passive/active parameter
		try {
			String pasVal = uploadPolicy.getString( "passive" );
			
			if( Boolean.getBoolean( pasVal ) ) {
				ftp.enterRemotePassiveMode();
				ftp.enterLocalPassiveMode();
			} else {
				ftp.enterLocalActiveMode();
				ftp.enterRemoteActiveMode( InetAddress.getByName( host ), Integer.parseInt( port ) );
			}
		} catch( MissingResourceException e ) {
			ftp.enterRemotePassiveMode();
			ftp.enterLocalPassiveMode();
		}		
	}//setTransferType
}
