package wjhk.jupload2.upload;

/**
 * Current version of FileUploadThreadV1: uses the {@link UploadPolicy} interface.
 * 
 * This class is a copy of the original FileUploadThreadV2 : it uses an array of
 * FileData, instead of an array of Files. This allow any default initialization or
 * work on the file, before upload. For instance : resize a picture, check an xml,..
 * 
 * This class should now be easily resusable, as it uses the FileData.getInputStream
 * to read what ever result of these transformation ... or the file data, if no
 * transformation.
 * 
 * <HR>
 * <B>Orginal FileUploadThreadV2 comment :</B><BR> 
 * 
 * URLConnection instance given by the URL class openConnection() function
 * can't handle uploading of large files.
 *
 * The reason being? URLConnection only does a post to the server after the
 * getInputStream() function is called. So anything you write to the Output
 * Stream before the getInputStream() is called will be written to memory.
 * For large files this will caused the JVM to throw an Out of Memory exception.
 *
 * With the above reason I have decided to replace the use of URLConnection
 * with sockets.
 */

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.URL;
import java.util.regex.Pattern;

import javax.swing.JProgressBar;

import wjhk.jupload2.exception.JUploadException;
import wjhk.jupload2.exception.JUploadExceptionUploadFailed;
import wjhk.jupload2.filedata.FileData;
import wjhk.jupload2.policies.DefaultUploadPolicy;
import wjhk.jupload2.policies.UploadPolicy;

public class FileUploadThreadV4 extends Thread implements FileUploadThread  {
	
	//------------- INFORMATION --------------------------------------------
	public static final String TITLE = "JUpload FileUploadThreadV4";
	public static final String DESCRIPTION =
		"Java Thread to upload files into a web server (based on FileUploadThreadV2).";
	public static final String AUTHOR = "Etienne Gauthier";
	
	public static final double VERSION = 2.2;
	public static final String LAST_MODIFIED = "20 may 2006";
	
	//------------- CONSTANTS ----------------------------------------------
		
	
	//------------- VARIABLES ----------------------------------------------
	
	/**
	 * This array will contain a 'copy' of the relevant element of the filesDataParam array (see the constructor). 
	 * After filling the filesToUpload array, the UploadThread will only manipulate it: all access to the file 
	 * contained by the filesDataParam given to the constructor will be done through the {@link UploadFileData} 
	 * contained by this array. 
	 */
	private UploadFileData[] filesToUpload=null;
	
	/**
	 * The upload policy contains all parameters needed to define the way files should be uploaded, including the URL. 
	 */
	private UploadPolicy uploadPolicy = null;
	
	/**
	 * The progress bar, that will indicate to the user the upload state (0 to 100%). 
	 */
	private JProgressBar progress = null;
	
	/**
	 * The total number of bytes to be sent. This allows the calculation of the progress bar
	 */
	private long totalFilesLength = 0;
	
	/**
	 * Current number of byts that have been uploaded.
	 */
	private long uploadedLength = 0;
	
	/**
	 * If set to 'true', the thread will stop the crrent upload. This attribute is not private as the
	 * {@link UploadFileData} class us it.
	 * 
	 *  @see UploadFileData#uploadFile(DataOutputStream)
	 */ 
	boolean stop = false;
	
	/**
	 * Server Output. It can then be displayed in the status bar, if debug is enabled. It is stored 
	 * by {@link DefaultUploadPolicy} in a string buffer, so that all debug output can be sent to 
	 * the webmaster, if an error occurs.
	 */ 
	private StringBuffer sbServerOutput = new StringBuffer();
	
	/**
	 * Thread Exception, if any occured during upload.
	 */ 
	
	private Exception uploadException = null;
	
	//------------- CONSTRUCTOR --------------------------------------------
	public FileUploadThreadV4(FileData[] filesDataParam, UploadPolicy uploadPolicy, JProgressBar progress) {
		this.uploadPolicy = uploadPolicy;
		this.progress = progress;
		uploadPolicy.displayDebug("Upload done by using the " + getClass().getName() + " class", 40);
		
		filesToUpload = new UploadFileData[filesDataParam.length];
		
		for (int i=0; i<filesDataParam.length; i+=1) {
			filesToUpload[i] = new UploadFileData(filesDataParam[i], this, uploadPolicy);
		}
	}
	
	//------------- Public Functions ---------------------------------------
	
	/** @see FileUploadThread#stopUpload() */
	public void stopUpload(){
		this.stop = true;
	}
	
	/** @see FileUploadThread#isUploadStopped() */
	public boolean isUploadStopped() {
		return stop;
	}

	
	/**
	 *  Get the server Output.
	 *  
	 * @return The StringBuffer that contains the full server HTTP response.
	 */
	public String getServerOutput(){
		return sbServerOutput.toString();
	}
	
	/**
	 * Get the exception that occurs during upload.
	 * 
	 * @return The exception, or null if no exception were thrown.
	 */
	public Exception getException(){
		return uploadException;
	}
	
	//Unused, but necessary to verify the FileUploadThread interface.
	public void nbBytesUploaded(long nbBytes) {
		uploadedLength += nbBytes;
		if(null != progress) progress.setValue((int)uploadedLength);
	}
	
	//------------- Private Functions --------------------------------------
	
	
	/**
	 * Clear the StringBuffer that contains the serverOutput. Called before each HTTP request.
	 *
	 */
	private void clearServerOutPut(){
		sbServerOutput.setLength(0);
		sbServerOutput.append("\n");
	}
	
	/**
	 * Add a String that has been read from the server response.
	 * @param s
	 */
	private void addServerOutPut(String s){
		if (0 < sbServerOutput.length() || !s.equals("")){
			sbServerOutput.append(s);
		}
	}
	
	/**
	 * Construction of a random string, to separate the uploaded files, in the HTTP upload request.
	 */
	private String getRandomString(){
		StringBuffer sbRan = new StringBuffer(11);
		String alphaNum= "1234567890abcdefghijklmnopqrstuvwxyz";
		int num;
		for(int i = 0; i < 11; i++){
			num = (int)(Math.random()* (alphaNum.length() - 1));
			sbRan.append(alphaNum.charAt(num));
		}
		return sbRan.toString();
	}

	/**
	 * Construction of the head for each file.
	 * 
	 * @param files array of UploadFileData classes, that contains all files to upload.
	 * @param nbFilesToUpload actual number of files to manage, within files.
	 * @param bound
	 * @return HTTP header for each file, within the multipart HTTP request.
	 * 
	 * @throws JUploadException
	 */
	private String[] setAllHead(int firstFileToUpload, int nbFilesToUpload, String bound) throws JUploadException {
		String[] heads = new String[nbFilesToUpload];
		for(int i=0; i < nbFilesToUpload; i++){
			heads[i] = filesToUpload[firstFileToUpload+i].getFileHeader(i, bound);
		}
		return heads;
	}
	
	/**
 	 * Construction of the tail for each file.
 	 * 
	 * @param fileLength
	 * @param bound
	 * @return Returns an array containing the HTTP tails for al files of the current HTTP request.
	 */
	private String[] setAllTail(int firstFileToUpload, int nbFilesToUpload, String bound){
		String[] tails = new String[nbFilesToUpload];
		for(int i=0; i < nbFilesToUpload; i++){
			tails[i] = ("\r\n");
		}
		// Telling the Server we have Finished.
		tails[nbFilesToUpload-1] = tails[nbFilesToUpload-1] + bound + "--\r\n";
		return tails;
	}
	//------------- THE HEART OF THE PROGRAM ------------------------------

	/**
	 * The heart of the program. This method prepare the upload, then calls doUpload for each HTTP request.
	 */
	public void run() {
		boolean bUploadOk = true;
		uploadedLength = 0;
		totalFilesLength = 0;
		
		try {
			if(null != progress)  {
				progress.setValue(0);
				progress.setMaximum(filesToUpload.length);
			}
			for(int i=0; i < this.filesToUpload.length; i++){
				if(null != progress)  {
					progress.setValue(i);
					progress.setString(uploadPolicy.getString("preparingFile", (i+1) + "/" + (filesToUpload.length) ));
				}
				filesToUpload[i].beforeUpload();
				//totalFilesLength is used to correctly displays the progress bar.
				totalFilesLength += filesToUpload[i].getRemainingLength();
			}
			
			if(null != progress)  {
				progress.setValue(0);
				progress.setMaximum((int)totalFilesLength);
				progress.setString("");
			}
			//Prepare upload
			//Let's take the upload policy into account  : how many files at a time ?
			int nbMaxFilesPerUpload = uploadPolicy.getNbFilesPerRequest();
			
			//We upload files, according to the current upload policy.
			int iFirstFileForThisUpload = 0;
			int iNbFilesForThisUpload = 0;
			while (iFirstFileForThisUpload+iNbFilesForThisUpload < filesToUpload.length  &&  bUploadOk  && !stop) {
				iNbFilesForThisUpload += 1;
				if (iNbFilesForThisUpload == nbMaxFilesPerUpload) {
					//Let's do an upload.
					bUploadOk = doUpload (iFirstFileForThisUpload, iNbFilesForThisUpload);
					iFirstFileForThisUpload += iNbFilesForThisUpload;
					iNbFilesForThisUpload = 0;
				}
			}//while
			
			if (iNbFilesForThisUpload > 0  &&  bUploadOk  &&  !stop) {
				//Some files are still to upload. Let's finish the job.
				bUploadOk = doUpload (iFirstFileForThisUpload, iNbFilesForThisUpload);
			}

			//Let's show everything is Ok
			if(null != progress)  {
				if (bUploadOk) {
					progress.setString(uploadPolicy.getString("nbUploadedFiles", iFirstFileForThisUpload+iNbFilesForThisUpload));
				} else {
					progress.setString("errDuringUpload");
				}
			}
		} catch (JUploadException e) {
			uploadPolicy.displayErr(e);
			progress.setString(e.getMessage());
		} finally {
			//In all cases, we try to free all reserved resources.
			for(int i=0; i < filesToUpload.length; i++){
				filesToUpload[i].afterUpload();
			}
		}
		
		//If the upload was unsuccessful, we try to alert the webmaster.
		if (!bUploadOk) {
			uploadPolicy.sendDebugInformation("Error in Upload");
		}
		
		//Enf of thread.
		
	}//run
	
	/**
	 * Actual execution file upload. It's called by the run methods, once for all files, or file by file, 
	 * depending on the UploadPolicy.
	 * 
	 * @param filesA An array of FileData, that contains all files to upload in this HTTP request.
	 * @param nbFilesToUpload The number of files in filesA to use (indice 0 to nbFilesToUpload-1).
	 * @param iTotalFileCount The total number of files that are to upload. It is used to generate the "file 1 out of 4 " message, on the progress bar. 
	 *
	 */
	private boolean doUpload (int firstFileToUpload, int nbFilesToUpload) {
		boolean bReturn = true;
		Socket sock = null;
		DataOutputStream dataout = null;
		BufferedReader datain = null;
		String msg;
		String action = "init";
		StringBuffer header = new StringBuffer();
		
		clearServerOutPut();
		
		if (nbFilesToUpload == 1) {
			msg = (firstFileToUpload + 1) + "/" + (filesToUpload.length);
		} else {
			msg = (firstFileToUpload + 1) + "-" + (firstFileToUpload + nbFilesToUpload) + "/" + (filesToUpload.length);
		}
		
		if(!stop && (null != progress))
			progress.setString(uploadPolicy.getString("infoUploading", msg));
		
		try{
			action = "get URL";
			URL url = new URL(uploadPolicy.getPostURL());
			
			String boundary = "-----------------------------" + getRandomString();
			
			String[] heads = setAllHead(firstFileToUpload, nbFilesToUpload, boundary);
			String[] tails = setAllTail(firstFileToUpload, nbFilesToUpload, boundary);
			
			long contentLength = 0;
			for(int i=0; i < nbFilesToUpload && !stop; i++){
				contentLength += heads[i].length();
				contentLength += filesToUpload[firstFileToUpload+i].getUploadLength();
				contentLength += tails[i].length();
			}
			
			// Header: Request line
			action = "append headers";
			header.append("POST ");header.append(url.getPath());
			if(null != url.getQuery() && !"".equals(url.getQuery())){
				header.append("?");header.append(url.getQuery());
			}
			header.append(" ").append(uploadPolicy.getServerProtocol()).append("\r\n");
			// Header: General
			header.append("Host: ");
			header.append(url.getHost());header.append("\r\n");
			header.append("Accept: */*\r\n");
			header.append("Content-type: multipart/form-data; boundary=");
			header.append(boundary.substring(2, boundary.length()) +"\r\n");
			header.append("Connection: close\r\n");
			header.append("Content-length: ")
				  .append(contentLength-2)
				  .append("\r\n");
			
			//Get specific headers for this upload.
			uploadPolicy.onAppendHeader(header);
			
			// Blank line (end of header)
			header.append("\r\n");
			
			// If port not specified then use default http port 80.
			sock = new Socket(url.getHost(), (-1 == url.getPort())?80:url.getPort());
			dataout = new DataOutputStream(new BufferedOutputStream(sock.getOutputStream()));
			datain  = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			//DataInputStream datain  = new DataInputStream(new BufferedInputStream(sock.getInputStream()));
			
			// Send http request to server
			action = "send bytes (1)";
			String headerStr = header.toString(); 
			//uploadPolicy.displayDebug(headerStr, 100);

			//Send the header
			dataout.writeBytes(headerStr);
			
			for(int i=0; i < nbFilesToUpload && !stop; i++){
				// Write to Server the head(4 Lines), a File and the tail.
				action = "send bytes (20)" + (firstFileToUpload+i);
				dataout.writeBytes(heads[i]);
				action = "send bytes (30)" + (firstFileToUpload+i);
				filesToUpload[firstFileToUpload+i].uploadFile(dataout);
				action = "send bytes (40)" + (firstFileToUpload+i);
				dataout.writeBytes(tails[i]);
			}
			action = "flush";
			dataout.flush ();
			if(!stop && (null != progress))
				progress.setString(uploadPolicy.getString("infoUploaded", msg));
			
			
			action = "wait for server answer";
			String strUploadSuccess = uploadPolicy.getStringUploadSuccess();
			boolean uploadSuccess = false;
			boolean upload_200_OK = false;
			boolean readingHttpBody = false;
			boolean uploadTransferEncodingChunked = false;
			StringBuffer sbHttpResponseBody = new StringBuffer(); 
			String line;

			Pattern patternSuccess = Pattern.compile(strUploadSuccess);
			Pattern pattern_200_OK = Pattern.compile("HTTP(.*) 200 OK");
			Pattern patternTransferEncodingChunked = Pattern.compile("Transfer-Encoding: chunked", Pattern.CASE_INSENSITIVE);

			while ((line = datain.readLine()) != null) {
				this.addServerOutPut(line);
				this.addServerOutPut("\n");
				
				//Is this upload a success ?
				action ="test success";
				if (patternSuccess.matcher(line).find()) {
					uploadSuccess = true;
				}
				
				//Is this a HTTP OK response (which means that the upload is technically a success, but he
				//may have been refused by the webserver, for instance because the user is not allowed
				//to upload files, or because the session is lost.
				action = "test 200 OK";
				if (pattern_200_OK.matcher(line).find()) {
				//if (line.equals("HTTP/1.1 200 OK")) {
					upload_200_OK = true;
					uploadPolicy.displayDebug("upload_200_OK = true", 30);
				}
				
				//Check, if we get the "Transfer-Encoding: chunked" header, which means that the content
				//may be split. Then, looking for a string within the body response will fail.
				action = "test Transfer-Encoding: chunked";
				if (patternTransferEncodingChunked.matcher(line).find()) {
					uploadTransferEncodingChunked = true;
					uploadPolicy.displayDebug("uploadTransferEncodingChunked = true", 30);
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
			}//while
				
			//Is our upload a success ?
			if (! uploadSuccess) {
				//If the upload is 200 OK, and the encoding is chunked, the success string may not be found
				//within the reponse body. We log a warning ... and consider it as being a success.
				//FIXME correctly decode chunked encoding, instead of hoping it's Ok.
				if (upload_200_OK) {
					if (uploadTransferEncodingChunked) {
						uploadPolicy.displayWarn("The HTTP response is Ok. The success string was not found, but it may be split, as the transfer-encoding is chunked.");
					} else {
						uploadPolicy.displayWarn("The HTTP response is Ok, but the success string was not found. The server output is logged below, if debugLevel is 80 or more.");						
					}
				} else {
					throw new JUploadExceptionUploadFailed(uploadPolicy.getString("errHttpResponse"));
				}
			}

		}catch(Exception e){
			uploadException = e;
			bReturn = false;
			uploadPolicy.displayErr(uploadPolicy.getString("errDuringUpload") + " (main | " + action + ") (" + e.getClass() + ".doUpload()) : " + e.getMessage());
		}finally{
			try{
				// Throws java.io.IOException
				dataout.close();
			} catch(Exception e) {
				uploadException = e;
				bReturn = false;
				uploadPolicy.displayErr(uploadPolicy.getString("errDuringUpload") + " (dataout.close) (" + e.getClass() + ".doUpload()) : " + e.getMessage());
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
				bReturn = false;
				uploadPolicy.displayErr(uploadPolicy.getString("errDuringUpload") + " (sock.close)(" + e.getClass() + ".doUpload()) : " + e.getMessage());
			}
			sock = null;
		}
		
    	if (uploadPolicy.getDebugLevel() > 80) {
			uploadPolicy.displayDebug ("Sent to server : " + header.toString(), 80);
          	uploadPolicy.displayDebug("-------- Server Output Start --------\n", 80);
            uploadPolicy.displayDebug(getServerOutput() + "\n", 80);
            uploadPolicy.displayDebug("--------- Server Output End ---------\n", 80);
    	}
		
    	if (uploadException == null) {
    		//The upload was Ok, we remove the uploaded files from the filePanel.
			for(int i=0; i < nbFilesToUpload && !stop; i++){
				uploadPolicy.getApplet().getFilePanel().remove(filesToUpload[firstFileToUpload+i]);
			}
    	} else {
        	uploadPolicy.displayErr(uploadException.toString() + "\n");          
        } 
		
		return bReturn;
	}
	
	//------------- CLEAN UP -----------------------------------------------
	
	/**
	 * Some internal attributes are set to null.
	 */
	public void close(){
		filesToUpload = null;
		uploadException = null;
		sbServerOutput = null;
	}
}
