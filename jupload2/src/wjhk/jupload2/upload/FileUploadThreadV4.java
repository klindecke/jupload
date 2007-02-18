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
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.X509TrustManager;
import javax.swing.JProgressBar;

import wjhk.jupload2.exception.JUploadException;
import wjhk.jupload2.exception.JUploadExceptionUploadFailed;
import wjhk.jupload2.filedata.FileData;
import wjhk.jupload2.policies.DefaultUploadPolicy;
import wjhk.jupload2.policies.UploadPolicy;


public class FileUploadThreadV4 extends Thread implements FileUploadThread  {
	//	 TrustManager to allow all certificates
	private final class TM implements X509TrustManager {
		public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
		}

		public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
		}

		public X509Certificate[] getAcceptedIssuers() {
			return new X509Certificate[0];
		}
	}
	
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
	 * The value of the applet parameter maxChunkSize, or its default value. 
	 */
	private long maxChunkSize;
	
	/**
	 * If set to 'true', the thread will stop the crrent upload. This attribute is not private as the
	 * {@link UploadFileData} class us it.
	 * 
	 *  @see UploadFileData#uploadFile(java.io.OutputStream)
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
	
	
	/**
	 * http boundary, for the posting multipart post.
	 */
	String boundary = "-----------------------------" + getRandomString();

	/**
	 * local head within the multipart post, for each file. This is precalculated for all files, in case 
	 * the upload is not chunked. The heads length are counted in the total upload size, to check that
	 * it is less than the maxChunkSize.
	 * tails are calculated once, as they depend not of the file position in the upload.
	 */
	String[] heads = null;
	
	/**
	 * same as heads, for the ... tail in the multipart post, for each file.
	 * But tails depend on the file position (the boundary is added to the last tail). So it's to be
	 * calculated by each function.
	 */
	//String[] tails = null;

	
	//------------- CONSTRUCTOR --------------------------------------------
	public FileUploadThreadV4(FileData[] filesDataParam, UploadPolicy uploadPolicy, JProgressBar progress) {
		this.uploadPolicy = uploadPolicy;
		this.progress = progress;
		uploadPolicy.displayDebug("Upload done by using the " + getClass().getName() + " class", 40);
		
		filesToUpload = new UploadFileData[filesDataParam.length];
		maxChunkSize = uploadPolicy.getMaxChunkSize();
		
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
	 * @param firstFileToUpload The index of the first file to upload, in the {@link #filesToUpload} area.
	 * @param nbFilesToUpload Number of file to upload, in the next HTTP upload request. These files are taken from 
	 * the {@link #filesToUpload} area 
	 * @param bound The String boundary between the post data in the HTTP request.
	 * @return HTTP header for each file, within the multipart HTTP request.
	 * 
	 * @throws JUploadException
	 */
	private String[] setAllHead(int firstFileToUpload, int nbFilesToUpload, String bound) throws JUploadException {
		String[] heads = new String[nbFilesToUpload];
		for(int i=0; i < nbFilesToUpload; i++){
			heads[i] = filesToUpload[firstFileToUpload+i].getFileHeader(i, bound, -1);
		}
		return heads;
	}
	
	/**
 	 * Construction of the tail for each file.
 	 * 
	 * @param firstFileToUpload The index of the first file to upload, in the {@link #filesToUpload} area.
	 * @param nbFilesToUpload Number of file to upload, in the next HTTP upload request. These files are taken from 
	 * the {@link #filesToUpload} area 
	 * @param bound
	 * @return Returns an array containing the HTTP tails for al files of the current HTTP request.
	 */
	private String[] setAllTail(int firstFileToUpload, int nbFilesToUpload, String bound){
		String[] tails = new String[firstFileToUpload + nbFilesToUpload];
		for(int i=0; i < nbFilesToUpload; i++){
			tails[firstFileToUpload+i] = ("\r\n");
		}
		// Telling the Server we have Finished.
		tails[firstFileToUpload+nbFilesToUpload-1] += bound + "--\r\n";
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
			
			heads = setAllHead(0, filesToUpload.length, boundary);
			String[] tails = setAllTail(0, filesToUpload.length, boundary);

			for(int i=0; i < this.filesToUpload.length && !stop; i++){
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
			
			int iFirstFileForThisUpload = 0;
			int iNbFilesForThisUpload = 0;
			int currentFile = 0;
			long nextUploadContentLength = 0;	//The contentLength of file managed byt the current loop.
			long currentUploadContentLength = 0;//The current contentLength of files between iFirstFileForThisUpload and iNbFilesForThisUpload   
			//////////////////////////////////////////////////////////////////////////////////////////
			//We upload files, according to the current upload policy.
			while (iFirstFileForThisUpload+iNbFilesForThisUpload < filesToUpload.length  &&  bUploadOk  && !stop) {
				currentFile = iFirstFileForThisUpload+iNbFilesForThisUpload;
				//Calculate the size of this file upload
				nextUploadContentLength = 
					heads[currentFile].length() +
					filesToUpload[currentFile].getRemainingLength() +
					tails[currentFile].length();
				
				//If we already had one or more files to upload, and the new upload content length is more
				//the the maxChunkSize, we upload what we already have to.
				if (iNbFilesForThisUpload>0 && currentUploadContentLength+nextUploadContentLength>maxChunkSize) {
					//Let's do an upload.
					bUploadOk = doUpload (iFirstFileForThisUpload, iNbFilesForThisUpload);
					iFirstFileForThisUpload += iNbFilesForThisUpload;
					iNbFilesForThisUpload = 0;
					currentUploadContentLength = 0;
				}
				
				//Let's add the current file to the list of files for the next upload.
				currentUploadContentLength += nextUploadContentLength;
				iNbFilesForThisUpload += 1;
				
				//If the current file is bigger than the maxChunkSize :
				//a) We did an upload in the previous 'if'.
				//b) We upload this file alone, and it will use chunks (see doUpload).
				if (currentUploadContentLength > maxChunkSize) {
					//Let's do an upload.
					bUploadOk = doUpload (iFirstFileForThisUpload, iNbFilesForThisUpload);
					iFirstFileForThisUpload += iNbFilesForThisUpload;
					iNbFilesForThisUpload = 0;
					currentUploadContentLength = 0;
				}
				//Do we attain the maximum number of files in one upload ?					
				if (iNbFilesForThisUpload == nbMaxFilesPerUpload) {
					//Let's do an upload.
					bUploadOk = doUpload (iFirstFileForThisUpload, iNbFilesForThisUpload);
					iFirstFileForThisUpload += iNbFilesForThisUpload;
					iNbFilesForThisUpload = 0;
					currentUploadContentLength = 0;
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
			uploadPolicy.displayDebug("FileUploadThread: within run().finally", 70);
			try {
				UploadFileData f;
				for(int i=0; i < filesToUpload.length; i++){
					f = filesToUpload[i];
					if (f != null) {
						f.afterUpload();
					}
				}
			} catch (Exception e) {
				uploadPolicy.displayWarn(e.getClass().getName() + " in " + getClass().getName() + ".run() (finally)");
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
	 * <BR>
	 * This method is called by the run() method. The prerequisite are :
	 * <DIR>
	 * <LI>If the contentLength for the nbFilesToUploadParam is more than the maxChunkSize, then 
	 * nbFilesToUploadParam is one.
	 * <LI>nbFilesToUploadParam is less (or equal) than the nbMaxFilesPerUpload.
	 * </DIR>
	 * 
	 * @param firstFileToUpload The index of the first file to upload, in the {@link #filesToUpload} area.
	 * @param nbFilesToUpload Number of file to upload, in the next HTTP upload request. These files are taken from 
	 * the {@link #filesToUpload} area 
	 */
	private boolean doUpload (int firstFileToUploadParam, int nbFilesToUploadParam) {
		boolean bReturn = true;
		boolean bLastChunk = false;
		boolean bChunkEnabled = false;
		int chunkPart = 0;
		int nbFilesToUpload = 0;
		String chunkHttpParam = null;
		Socket sock = null;
		long totalContentLength = 0;
		long contentLength = 0;
		long thisChunkSize = 0;
		int firstFileToUpload = 0;
		DataOutputStream dataout = null;
		BufferedReader datain = null;
		String msg;
		String action = "init";
		StringBuffer header = new StringBuffer();

		
		if (nbFilesToUploadParam == 1) {
			msg = (firstFileToUploadParam + 1) + "/" + (filesToUpload.length);
		} else {
			msg = (firstFileToUploadParam + 1) + "-" + (firstFileToUploadParam + nbFilesToUploadParam) + "/" + (filesToUpload.length);
		}
		
		if(!stop && (null != progress)) {
			progress.setString(uploadPolicy.getString("infoUploading", msg));
		}
		
		//The tails must recalclated for each upload, as the last one contains the boundary.
		String[] tails = setAllTail(firstFileToUploadParam, nbFilesToUploadParam, boundary);
		
		//Let's be optimistic: we calculate the total upload length. Then, we'll test that this is less that the
		//maximum chunk size ... if any is defined.
		try {
			
			for(int i=0; i < nbFilesToUploadParam && !stop; i++){
				totalContentLength += heads[firstFileToUploadParam+i].length();
				totalContentLength += filesToUpload[firstFileToUploadParam+i].getUploadLength();
				totalContentLength += tails[firstFileToUploadParam+i].length();
				uploadPolicy.displayDebug("file " + (firstFileToUploadParam+i)
					+ ": heads=" + heads[firstFileToUploadParam+i].length()
					+ " bytes, content=" + filesToUpload[firstFileToUploadParam+i].getUploadLength()
					+ " bytes, tail=" + tails[firstFileToUploadParam+i].length()
					+ " bytes"
					, 80);
			}
		} catch (JUploadException e) {
			uploadPolicy.displayErr(e);
			uploadException = e;
		}
		
		//Ok, now we check that the totalContentLength is less than the chunk size.
		if (totalContentLength >= maxChunkSize) {
			//hum, hum, we have to download file by file, with chunk enabled. This a prerequisite of this method.
			if (nbFilesToUploadParam > 1) {
				uploadException = new JUploadException("totalContentLength >= chunkSize: nbFilesToUploadParam should be more than 1 (doUpload)");
			}
			bChunkEnabled = true;
		}
		
		while (!bLastChunk && uploadException == null) {
			try {
				//First: chunk management.
				if (bChunkEnabled) {
					//Let's manage chunk:
					//Files are uploaded one by one. This is checked just above.
					firstFileToUpload = firstFileToUploadParam;
					nbFilesToUpload = 1;
					chunkPart += 1;
					bLastChunk = (contentLength > filesToUpload[firstFileToUploadParam].getRemainingLength());
					chunkHttpParam = "jupart=" +  chunkPart + "&jufinal=" + (bLastChunk ? "1" : "0");
					uploadPolicy.displayDebug("chunkHttpParam: " + chunkHttpParam, 40);
					
					//Is this the last chunk ?
					if (bLastChunk) {
						thisChunkSize = filesToUpload[firstFileToUploadParam].getRemainingLength();
					} else {
						thisChunkSize = maxChunkSize;
					}
					//Overriding of the head, for the current chunk of the current file, based on the contentLength 
					//we just calculated.
					heads[firstFileToUploadParam] = filesToUpload[firstFileToUpload].getFileHeader(1, boundary, chunkPart);
	
					contentLength = thisChunkSize + heads[firstFileToUploadParam].length() + tails[firstFileToUploadParam].length();
				} else { 
					//Chunk not activate. We upload all files at once.
					bLastChunk = true;
					contentLength = totalContentLength;
					firstFileToUpload = firstFileToUploadParam;
					nbFilesToUpload = nbFilesToUploadParam;
				}
				clearServerOutPut();

				//Ok, we've prepare the job for chunk upload. Let's do it!
				
				action = "get URL";
				URL url = new URL(uploadPolicy.getPostURL());
				
				// Header: Request line
				action = "append headers";
				header.setLength(0);  //Let's clear it. Useful only for chunked uploads.
				header.append("POST ");header.append(url.getPath());
				
				if(null != url.getQuery() && !"".equals(url.getQuery())){
					header
						.append("?")
						.append(url.getQuery());
					//In case we divided the current upload in chunks, we have to give some information about it
					//to the server:
					if (bChunkEnabled) {
						header.append("&").append(chunkHttpParam);
					}
				} else if (bChunkEnabled) {
					header.append("?").append(chunkHttpParam);
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
				//sock = new Socket(url.getHost(), (-1 == url.getPort())?80:url.getPort());
				
				//////////////////////////////////////////////////////////////////////////////////////////////////
				//Management of SSL, thanks to David Gnedt
				// Check if SSL connection is needed
				if (url.getProtocol().equals("https")) {
					SSLContext context = SSLContext.getInstance("SSL");
					// Allow all certificates
					context.init(null, new X509TrustManager[] {new TM()}, null);
					// If port not specified then use default https port 443.
					uploadPolicy.displayDebug("Using SSL socket", 20);
					sock = (Socket) context.getSocketFactory().createSocket(url.getHost(), (-1 == url.getPort())?443:url.getPort());
				} else {
					// If we are not in SSL, just use the old code.
					sock = new Socket(url.getHost(), (-1 == url.getPort())?80:url.getPort());
					uploadPolicy.displayDebug("Using non SSL socket", 20);
				}
				//////////////////////////////////////////////////////////////////////////////////////////////////
				
				dataout = new DataOutputStream(new BufferedOutputStream(sock.getOutputStream()));
				datain  = new BufferedReader(new InputStreamReader(sock.getInputStream()));
				//DataInputStream datain  = new DataInputStream(new BufferedInputStream(sock.getInputStream()));
				
				// Send http request to server
				action = "send bytes (1)";
				uploadPolicy.displayDebug(header.toString(), 100);
				dataout.writeBytes(header.toString());
				
				for(int i=0; i < nbFilesToUpload && !stop; i++){
					// Write to Server the head(4 Lines), a File and the tail.
					action = "send bytes (20)" + (firstFileToUpload+i);
					//heads[i] contains the header specific for the file, in the multipart content.
					//It is initialized at the beginning of the run() method. It can be override at the beginning
					//of this loop, if in chunk mode.
					dataout.writeBytes(heads[firstFileToUpload+i]);
					action = "send bytes (30)" + (firstFileToUpload+i);

					//In chunk mode, we already calculate the correct chunkSize.
					if (!bChunkEnabled) {
						//If not chunk mode, then the file is uploaded in one shot.
						thisChunkSize = filesToUpload[firstFileToUpload+i].getUploadLength();
					}
					
					//Actual upload of the file:
					filesToUpload[firstFileToUpload+i].uploadFile(dataout, thisChunkSize);
					
					//If we are not in chunk mode, or if it was the last chunk, upload should be finished.
					if (!bChunkEnabled  ||  bLastChunk) {
						if (filesToUpload[firstFileToUpload+i].getRemainingLength() > 0) {
							uploadException = new JUploadExceptionUploadFailed(
									"Files has not be entirely uploaded. The remaining size is "
									+ filesToUpload[firstFileToUpload+i].getRemainingLength()
									+ " bytes. File size was: "
									+ filesToUpload[firstFileToUpload+i].getUploadLength()
									+ " bytes."
									);
						}
					}
					action = "send bytes (40)" + (firstFileToUpload+i);
					dataout.writeBytes(tails[firstFileToUpload+i]);
				}
				action = "flush";
				dataout.flush ();
				if(!stop && (null != progress))
					progress.setString(uploadPolicy.getString("infoUploaded", msg));
				
				
				action = "wait for server answer";
				boolean readingHttpBody = false;
				StringBuffer sbHttpResponseBody = new StringBuffer(); 
				String line;
	
				while ((line = datain.readLine()) != null   && !stop) {
					this.addServerOutPut(line);
					this.addServerOutPut("\n");
	
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
				
				//We now ask to the uploadPolicy, if it was a success.
				//If not, the isUploadSuccessful should raise an exception.
				uploadPolicy.checkUploadSuccess(getServerOutput(), sbHttpResponseBody.toString());

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
				uploadPolicy.displayDebug ("Sent to server : \n" + header.toString(), 80);
	          	uploadPolicy.displayDebug("-------- Server Output Start --------\n", 80);
	            uploadPolicy.displayDebug(getServerOutput() + "\n", 80);
	            uploadPolicy.displayDebug("--------- Server Output End ---------\n", 80);
	    	}
		}//while (uploadException==null)
		
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
		uploadPolicy.displayDebug("FileUploadThread: within close()", 70);
		filesToUpload = null;
		uploadException = null;
		sbServerOutput = null;
	}
}
