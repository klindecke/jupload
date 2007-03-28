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
 * 
 * @deprecated Replaced by {@link wjhk.jupload2.upload.FileUploadThreadHTTP}
 */

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;

import javax.swing.JProgressBar;

import wjhk.jupload2.exception.JUploadException;
import wjhk.jupload2.exception.JUploadExceptionUploadFailed;
import wjhk.jupload2.filedata.FileData;
import wjhk.jupload2.gui.FilePanel;
import wjhk.jupload2.policies.DefaultUploadPolicy;
import wjhk.jupload2.policies.UploadPolicy;

public class FileUploadThreadV3 extends Thread implements FileUploadThread  {
	
	//------------- INFORMATION --------------------------------------------
	public static final String TITLE = "JUpload FileUploadThreadV3";
	public static final String DESCRIPTION =
		"Java Thread to upload files into a web server (based ont FileUploadThreadV2).";
	public static final String AUTHOR = "Etienne Gauthier";
	
	public static final double VERSION = 2.2;
	public static final String LAST_MODIFIED = "20 may 2006";
	
	//------------- CONSTANTS ----------------------------------------------
	
	/**
	 * MAX_WAIT is the longuer time that the thread should wait for pictures to 
	 * be ready.
	 */
	public static final long MAX_WAIT = 20 * 1000;
	
	
	//------------- VARIABLES ----------------------------------------------
	/**
	 * Files asked to be uploaded into the server.
	 */ 
	private FileData[] allFiles;
	
	/**
	 * The upload policy contains all parameters needed to define the way files should be uploaded, including the URL. 
	 */
	private UploadPolicy uploadPolicy;
	
	/**
	 * The progress bar, that will indicate to the user the upload state (0 to 100%). 
	 */
	private JProgressBar progress;
	
	/**
	 * The total number of bytes to be sent. This allows the calculation of the progress bar
	 */
	private long totalFilesLength;
	
	/**
	 * Current number of byts that have been uploaded.
	 */
	private long uploadedLength;
	
	/**
	 * If set to 'true', the thread will stop the crrent upload.
	 */ 
	private boolean stop = false;
	
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
	public FileUploadThreadV3(FileData[] allFiles, UploadPolicy uploadPolicy, JProgressBar progress){
		this.allFiles = allFiles;
		this.uploadPolicy = uploadPolicy;
		this.progress = progress;
		
		totalFilesLength = 0;
		
		uploadPolicy.displayDebug("Upload done by using the " + getClass().getName() + " class", 40);
	}
	
	//------------- Public Functions ---------------------------------------
	
	/**
	 *  Setting Progress Panel.
	 *
	public void setProgressPanel(JProgressBar pgrBar){
		progress = pgrBar;
	}
	*/
	
	/**
	 * Stopping the Thread
	 */
	public void stopUpload(){
		this.stop = true;
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
	
	//------------- Private Functions --------------------------------------
	
	/**
	 * Construction of a random string, to separate the uploaded files, in the HTTP upload request.
	 */
	private StringBuffer getRandomString(){
		StringBuffer sbRan = new StringBuffer(11);
		StringBuffer alphaNum= new StringBuffer();
		alphaNum.append("1234567890abcdefghijklmnopqrstuvwxyz");
		int num;
		for(int i = 0; i < 11; i++){
			num = (int)(Math.random()* (alphaNum.length() - 1));
			sbRan.append(alphaNum.charAt(num));
		}
		return sbRan;
	}
	
	/**
	 * 
	 * This methods reads an InputStream (containing the file data to upload), and write the content
	 * to an outputStream (the output toward the HTTP server).
	 * 
	 * @param is
	 * @param dOut
	 * @throws IOException
	 */
	private void uploadFileStream(InputStream is, DataOutputStream dOut) throws IOException {
		byte[] byteBuff = null;
		try{
			int numBytes = 0;
			byteBuff = new byte[1024];
			while(-1 != (numBytes = is.read(byteBuff)) && !stop){
				dOut.write(byteBuff, 0, numBytes);
				uploadedLength += numBytes;
				if(null != progress) progress.setValue((int)uploadedLength);
			}
		}finally{
			try{
				is.close();
			} catch (Exception e) {
				//An error occurs during the closing of the fileinputstream. This is not 
				//an upload error: we just log it.
				uploadPolicy.displayErr(e);
			}
			byteBuff = null;
		}
	}
	
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
	 * Construction of the head for each file.
	 * 
	 * @param fileA
	 * @param bound
	 * @return HTTP header for each file, within the multipart HTTP request.
	 * @throws JUploadException
	 */
	private StringBuffer[] setAllHead(FileData[] fileA, int nbFilesToUpload, StringBuffer bound) throws JUploadException {
		StringBuffer[] sbArray = new StringBuffer[fileA.length];
		FileData fileData;
		StringBuffer sb;
		for(int i=0; i < nbFilesToUpload; i++){
			fileData = fileA[i];
			sbArray[i] = new StringBuffer();
			sb = sbArray[i];
			// Line 1.
			sb.append(bound.toString());sb.append("\r\n");
			// Line 2.
			//try {
				sb	.append("Content-Disposition: form-data; name=\"")
					.append(uploadPolicy.getUploadName(fileData, i))
					.append("\"; filename=\"")
					.append(uploadPolicy.getUploadFilename(fileData, i))
					//.append(URLEncoder.encode(fileData.getFileName(), "UTF-8"))
					.append("\"\r\n");
			/*	
			} catch (UnsupportedEncodingException e) {
				throw new JUploadException(e, "setAllHead");
			}
			*/
			// Line 3 & Empty Line 4.
			sb	.append("Content-Type: ")
				.append(fileData.getMimeType())
				//Encoding tests (begin)
				//.append("; charset=utf-8")
				//Encoding tests (end)
				.append("\r\n");
			sb.append("\r\n");
			uploadPolicy.displayDebug("head : '" +  sb.toString() + "'", 70);
		}
		return sbArray;
	}
	
	/**
 	 * Construction of the tail for each file.
 	 * 
	 * @param fileLength
	 * @param bound
	 * @return Returns an array containing the HTTP tails for al files of the current HTTP request.
	 */
	private StringBuffer[] setAllTail(int fileLength, StringBuffer bound){
		StringBuffer[] sbArray = new StringBuffer[fileLength];
		for(int i=0; i < fileLength; i++){
			sbArray[i] = new StringBuffer("\r\n");
		}
		// Telling the Server we have Finished.
		sbArray[sbArray.length-1].append(bound.toString());
		sbArray[sbArray.length-1].append("--\r\n");
		return sbArray;
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
				progress.setMaximum(this.allFiles.length);
			}
			for(int i=0; i < this.allFiles.length; i++){
				if(null != progress)  {
					progress.setValue(i);
					progress.setString(uploadPolicy.getString("preparingFile", (i+1) + "/" + (this.allFiles.length) ));
				}
				this.allFiles[i].beforeUpload();
				//totalFilesLength is used to correctly displays the progressBar.
				totalFilesLength += this.allFiles[i].getUploadLength();
			}
			
			if(null != progress)  {
				progress.setValue(0);
				progress.setMaximum((int)totalFilesLength);
				progress.setString("");
			}
			//Prepare upload
			//Let's take the upload policy into accound  : how many files at a time ?
			int nbMaxFilesPerUpload = uploadPolicy.getNbFilesPerRequest();
			FileData[] filesToUpload;
			if (nbMaxFilesPerUpload <= 0) {
				nbMaxFilesPerUpload = Integer.MAX_VALUE;
				filesToUpload = new FileData[allFiles.length];
			} else {
				filesToUpload = new FileData[nbMaxFilesPerUpload];
			}
			
			//We upload files, according to the current upload policy.
			int iPerUploadCount = 0;
			int iTotalFileCount = 0;
			while (iTotalFileCount < allFiles.length  &&  bUploadOk) {
				filesToUpload[iPerUploadCount] = allFiles[iTotalFileCount]; 
				iPerUploadCount += 1;
				iTotalFileCount += 1;
				if (iPerUploadCount == nbMaxFilesPerUpload) {
					//Let's do an upload.
					bUploadOk = doUpload (filesToUpload, iPerUploadCount, iTotalFileCount);
					iPerUploadCount = 0;
				}
			}//while
			
			if (iPerUploadCount > 0  &&  bUploadOk) {
				//Some files are still to upload. Let's finish the job.
				bUploadOk = doUpload (filesToUpload, iPerUploadCount, iTotalFileCount);
			}

			//Let's show everything is Ok
			if(null != progress)  {
				if (bUploadOk) {
					progress.setString(uploadPolicy.getString("nbUploadedFiles", iTotalFileCount));
				} else {
					progress.setString("errDuringUpload");
				}
			}
		} catch (JUploadException e) {
			uploadPolicy.displayErr(e);
			progress.setString(e.getMessage());
		} finally {
			//In all cases, we try to free all reserved resources.
			for(int i=0; i < allFiles.length; i++){
				allFiles[i].afterUpload();
			}
		}
		
		//If the upload was unsuccessful, we try to alert the webmaster.
		if (!bUploadOk) {
			uploadPolicy.sendDebugInformation("Error in Upload");
		}
		
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
	private boolean doUpload (FileData[] filesA, int nbFilesToUpload, int iTotalFileCount) {
		boolean bReturn = true;
		Socket sock = null;
		DataOutputStream dataout = null;
		BufferedReader datain = null;
		String msg;
		String action = "init";
		StringBuffer header = new StringBuffer();
		
		clearServerOutPut();
		
		if (nbFilesToUpload == 1) {
			msg = iTotalFileCount + "/" + allFiles.length;
		} else {
			msg = (iTotalFileCount - nbFilesToUpload + 1) + "-" + iTotalFileCount + "/" + allFiles.length;
		}
		
		if(!stop && (null != progress))
			progress.setString(uploadPolicy.getString("infoUploading", msg));
		
		try{
			action = "get URL";
			URL url = new URL(uploadPolicy.getPostURL());
			
			StringBuffer boundary = new StringBuffer();
			boundary.append("-----------------------------");
			boundary.append(getRandomString().toString());
			
			StringBuffer[] head = setAllHead(filesA, nbFilesToUpload, boundary);
			StringBuffer[] tail = setAllTail(nbFilesToUpload, boundary);
			
			long contentLength = 0;
			for(int i = 0; i < nbFilesToUpload; i++){
				contentLength += head[i].length();
				contentLength += filesA[i].getUploadLength();
				contentLength += tail[i].length();
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
				action = "send bytes (20)" + i;
				dataout.writeBytes(head[i].toString());
				action = "send bytes (30)" + i;
				uploadFileStream(filesA[i].getInputStream(),dataout);
				action = "send bytes (40)" + i;
				dataout.writeBytes(tail[i].toString());
			}
			action = "flush";
			dataout.flush ();
			if(!stop && (null != progress))
				progress.setString(uploadPolicy.getString("infoUploaded", msg));
			
			
			action = "wait for server answer";
			String strUploadSuccess = uploadPolicy.getStringUploadSuccess();
			boolean uploadSuccess = false;
			boolean upload200OK = false;
			boolean readingHttpBody = false;
			boolean uploadTransferEncodingChunked = false;
			StringBuffer sbHttpResponseBody = new StringBuffer(); 
			String line;

			while ((line = datain.readLine()) != null) {
				this.addServerOutPut(line);
				this.addServerOutPut("\n");
				
				//Is this upload a success ?
				action ="test success";
				if (line.matches(strUploadSuccess)) {
					uploadSuccess = true;
				}
				
				//Is this a HTTP OK response (which means that the upload is technically a success, but he
				//may have been refused by the webserver, for instance because the user is not allowed
				//to upload files, or because the session is lost.
				action = "test 200 OK";
				if (line.matches("200 OK$")) {
					upload200OK = true;
				}
				
				//Check, if we get the "Transfer-Encoding: chunked" header, which means that the content
				//may be split. Then, looking for a string within the body response will fail.
				action = "test Transfer-Encoding: chunked";
				if (line.equalsIgnoreCase("Transfer-Encoding: chunked")) {
					uploadTransferEncodingChunked = true;
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
				//If the upload is 200 OK, and the encoding is chunked, the success string may not be found
				//within the reponse body. We log a warning ... and consider it as being a success.
				//FIXME correctly decode chunked encoding, instead of hoping it's Ok.
				if (upload200OK && uploadTransferEncodingChunked) {
					uploadPolicy.displayWarn("The success string was not found, but it may be split, as the transfer-encoding is chunked.");
				} else {
					throw new JUploadExceptionUploadFailed(uploadPolicy.getString("errHttpResponse"));
				}
			}

		}catch(Exception e){
			this.uploadException = e;
			bReturn = false;
			uploadPolicy.displayErr(uploadPolicy.getString("errDuringUpload") + " (main | " + action + ") (" + e.getClass() + ".doUpload()) : " + e.getMessage());
		}finally{
			try{
				// Throws java.io.IOException
				dataout.close();
			} catch(Exception e) {
				this.uploadException = e;
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
			uploadPolicy.displayDebug ("Sent to server : " + header.toString(), 40);
			uploadPolicy.displayDebug ("Serveur output : " + getServerOutput().toString(), 10);
		}
		
		//If the upload was Ok, we remove the uploaded files from the filePanel.
		FilePanel filePanel = uploadPolicy.getApplet().getFilePanel();
        if(uploadException != null){
        	uploadPolicy.displayErr(uploadException.toString() + "\n");          
        } else {
        	if (uploadPolicy.getDebugLevel() > 80) {
	          	uploadPolicy.displayDebug("-------- Server Output Start --------\n", 80);
	            uploadPolicy.displayDebug(getServerOutput() + "\n", 80);
	            uploadPolicy.displayDebug("--------- Server Output End ---------\n", 80);
        	}
        	for(int i=0; i < nbFilesToUpload; i++){
        		filePanel.remove(filesA[i]);
        	}
        }
		
		return bReturn;
	}
	
	//------------- CLEAN UP -----------------------------------------------
	
	/**
	 * Some internal attributes are set to null.
	 */
	public void close(){
		allFiles = null;
		uploadException = null;
		sbServerOutput = null;
	}

	  /** @see FileUploadThread#nbBytesUploaded(long) */
	  public void nbBytesUploaded(long nbBytes) {
			uploadedLength += nbBytes;
			if(null != progress) progress.setValue((int)uploadedLength);
	  }
		
		/** @see FileUploadThread#isUploadStopped() */
		public boolean isUploadStopped() {
			return stop;
		}
	}