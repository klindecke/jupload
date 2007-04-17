package wjhk.jupload2.upload;

/**
 * 
 * This class is based on the {@link FileUploadThreadV4} class. It's an abstract class that contains
 * the default implementation for the {@link FileUploadThread} interface.
 * <BR>
 * It contains the following abstract methods, which must be implemented in the children classes. These methods
 * are called in this order:
 * <DIR>
 * <LI>For each upload request (for instance, upload of 3 files with nbFilesPerRequest to 2, makes 2 request:
 *   2 files, then the last one):
 *   <DIR>
 *   <LI><I>try</I>
 *   <LI>{@link startRequest}: start of the UploadRequest.
 *   <LI>Then, for each file to upload (according to the nbFilesPerRequest and maxChunkSize applet parameters)
 *     <DIR>
 *     <LI>{@link beforeFile(int)} is called before writting the bytes for this file (or this chunk)
 *     <LI>{@link afterFile(int)} is called after writting the bytes for this file (or this chunk)
 *     </DIR>
 *   <LI>{@link #finishRequest()}
 *   </DIR>
 *   </LI><I>finally</I> {@link #cleanRequest()}
 * <LI>Call of {@link #cleanAll()}, to clean up any used resources, common to the whole upload.
 * </DIR>
 * 
 * 
 */


import java.io.OutputStream;

import javax.swing.JProgressBar;

import wjhk.jupload2.exception.JUploadException;
import wjhk.jupload2.exception.JUploadExceptionUploadFailed;
import wjhk.jupload2.filedata.FileData;
import wjhk.jupload2.policies.DefaultUploadPolicy;
import wjhk.jupload2.policies.UploadPolicy;

public abstract class DefaultFileUploadThread extends Thread implements FileUploadThread  {
	
	
	//////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////    CONSTANTS         ///////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////

	/**
	 *  number of bytes to send at a time before updating progress bar.
	 */
	private static final int NUM_BYTES = 4096;

	
	//////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////    VARIABLES         ///////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * This array will contain a 'copy' of the relevant element of the filesDataParam array (see the constructor). 
	 * After filling the filesToUpload array, the UploadThread will only manipulate it: all access to the file 
	 * contained by the filesDataParam given to the constructor will be done through the {@link UploadFileData} 
	 * contained by this array. 
	 */
	UploadFileData[] filesToUpload=null;
	
	/**
	 * The upload policy contains all parameters needed to define the way files should be uploaded, including the URL. 
	 */
	UploadPolicy uploadPolicy = null;
		
	/**
	 * The value of the applet parameter maxChunkSize, or its default value. 
	 */
	//TODO to be moved to HTTP ????
	long maxChunkSize;
	
	/**
	 * Maximum number of files for FTP upload.
	 */
	int nbMaxFilesPerUpload;
	
	/**
	 * If set to 'true', the thread will stop the crrent upload. This attribute is not private as the
	 * {@link UploadFileData} class us it.
	 * 
	 *  @see UploadFileData#uploadFile(java.io.OutputStream, long)
	 */ 
	boolean stop = false;
		
	/**
	 * Thread Exception, if any occured during upload.
	 */ 	
	Exception uploadException = null;

	//////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////  PRIVATE ATTRIBUTES  ///////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////

	/**
	 * Server Output. It can then be displayed in the status bar, if debug is enabled. It is stored 
	 * by {@link DefaultUploadPolicy} in a string buffer, so that all debug output can be sent to 
	 * the webmaster, if an error occurs.
	 */ 
	private StringBuffer sbServerOutput = new StringBuffer();
	
	/** The progress bar, that will indicate to the user the upload state (0 to 100%). */
	private JProgressBar progress = null;
	
	/** The total number of bytes to be sent. This allows the calculation of the progress bar */
	private long totalFilesLength = 0;
	
	/** Current number of bytes that have been uploaded. */
	private long uploadedLength = 0;

	/** Number of bytes that  must be uploaded before updating the progress bar.
	 * See {@link #nbBytesUploaded(long)}
	 */
	private int nbBytesBeforeUpdatingProgressBar = NUM_BYTES;
	
	//////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////    CONSTRUCTOR       ///////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////

	public DefaultFileUploadThread(FileData[] filesDataParam, UploadPolicy uploadPolicy, JProgressBar progress) {
		this.uploadPolicy = uploadPolicy;
		this.progress = progress;
		
		filesToUpload = new UploadFileData[filesDataParam.length];
		maxChunkSize = uploadPolicy.getMaxChunkSize();
		nbMaxFilesPerUpload = uploadPolicy.getNbFilesPerRequest();
		
		for (int i=0; i<filesDataParam.length; i+=1) {
			filesToUpload[i] = new UploadFileData(filesDataParam[i], this, uploadPolicy);
		}
	}
	
	//////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////  PUBLIC FUNCTIONS   ////////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////

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
	
	/**
	 * Used by the {@link UploadFileData#uploadFile(java.io.OutputStream, long)} for each 
	 * uploaded byte ! 
	 * 
	 * @see wjhk.jupload2.upload.FileUploadThread#nbBytesUploaded(long)
	 */
	public void nbBytesUploaded(long nbBytes) {
		uploadedLength += nbBytes;

		//We update the progress bar from time to time:
		nbBytesBeforeUpdatingProgressBar -= nbBytes;
		if (nbBytesBeforeUpdatingProgressBar<0) {
			nbBytesBeforeUpdatingProgressBar = NUM_BYTES;
			if (null != progress) {
				progress.setValue((int)uploadedLength);
			}
		}
	}
	
	//////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////  PROTECTED FUNCTIONS   /////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////

	/**
	 * This method is called before the upload. It calls the {@link FileData#beforeUpload()} method
	 * for all files to upload, and prepares the progress bar (if any), with total number of bytes to upload.
	 */
	final private void beforeUpload() throws JUploadException  {

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
	}
	
	/**
	 * This methods upload overhead for the file number indexFile in the filesDataParam given to the constructor.
	 * For instance, in HTTP, the upload contains a head and a tail for each files.
	 * 
	 * @param indexFile The index of the file in the filesDataParam array, whose addtional length is asked.
	 * @return The additional number of bytes for this file.
	 */
	abstract long getAdditionnalBytesForUpload(int indexFile);
	
	
	/**
	 * This method is called before starting of each request. It can be used to prepare any work, before 
	 * starting the request. For instance, in HTTP, the tail must be properly calculated, as the last one
	 * must be different from the others. 
	 * 
	 * @param firstFileToUploadParam
	 * @param nbFilesToUploadParam
	 */
	abstract void beforeRequest(int firstFileToUploadParam, int nbFilesToUploadParam) throws JUploadException ;
	
	/**
	 * 
	 * This method is called for each upload request to the server. The number of request to the server depends on:
	 * <DIR>
	 * <LI>The total number of files to upload.
	 * <LI>The value of the nbFilesPerRequest applet parameter.
	 * <LI>The value of the maxChunkSize applet parameter.
	 * </DIR>
	 * 
	 * The main objective of this method is to open the connection to the server, where the files to upload will be 
	 * written. It should also send any header necessary for this upload request. The {@link #getOutputStream()} 
	 * methods is then called to know where the uploaded files should be written.
	 * <BR>Note: it's up to the class containing this method to internally manage the connection.
	 * 
	 * @param contentLength The total number of bytes for the files (or the chunk) to upload in this query.
	 * @param bChunkEnabled True if this upload is part of a file (can occurs only if the maxChunkSize applet 
	 * parameter is set). False otherwise.
	 * @param chunkPart The chunk number. Should be ignored if bChunkEnabled is false.
	 * @param bLastChunk True if in chunk mode, and this upload is the last one. Should be ignored if 
	 * bChunkEnabled is false.  
	 */
	abstract void startRequest(long contentLength, boolean bChunkEnabled, int chunkPart, boolean bLastChunk) throws JUploadException;
	
	
	/**
	 * This method is called at the end of each request. 
	 * 
	 * @see #startRequest(long, boolean, int, boolean)
	 */
	abstract void finishRequest() throws JUploadException;

	/**
	 * This method is called before sending the bytes corresponding to the file whose index is given
	 * in argument. If the file is splitted in chunks (see the maxChunkSize applet parameter), this method is called
	 * before each chunk for this file. 
	 * 
	 * @param index The index of the file that will be sent just after
	 */
	abstract void beforeFile(int index) throws JUploadException;
	
	/**
	 * Idem as {@link #beforeFile(int)}, but is called after each file (and each chunks for each file).
	 * 
	 * @param index The index of the file that was just sent.
	 */
	abstract void afterFile(int index) throws JUploadException;
		
	
	/**
	 * Clean any used resource of the last executed request. In HTTP mode, the output stream, input stream and the
	 * socket should be cleaned here.
	 */
	abstract void cleanRequest() throws JUploadException;
	
	/**
	 * Clean any used resource, like a 'permanent' connection. This method is called after the end of the
	 * last request (see on the top of this page for details).
	 */
	abstract void cleanAll() throws JUploadException;
	
	
	/**
	 * Get the output stream where the files should be written for upload.
	 * 
	 * @return The target output stream for upload.
	 */
	abstract OutputStream getOutputStream() throws JUploadException;

	/**
	 * Return the the body for the server response. That is: the server response without the
	 * http header. This the real functionnal response from the server application, that would be outputed,
	 * for instance, by any 'echo' PHP command. 
	 */
	abstract String getResponseBody() throws JUploadException;
	
	//////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////  PRIVATE   FUNCTIONS   /////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////
	
	
	/** Clear the StringBuffer that contains the serverOutput. Called before each HTTP request. */
	void clearServerOutPut(){
		sbServerOutput.setLength(0);
	}
	
	/**
	 * Add a String that has been read from the server response.
	 * @param s
	 */
	void addServerOutPut(String s){
		if (0 < sbServerOutput.length() || !s.equals("")){
			sbServerOutput.append(s);
		}
	}
	

	//------------- THE HEART OF THE PROGRAM ------------------------------

	/**
	 * The heart of the program. This method prepare the upload, then calls doUpload for each HTTP request.
	 */
	final public void run() {
		boolean bUploadOk = true;

		uploadedLength = 0;
		totalFilesLength = 0;
		
		try {
			if(null != progress)  {
				progress.setValue(0);
				progress.setMaximum(filesToUpload.length);
			}
			
			//Prepare upload, for all files to be uploaded.
			beforeUpload();
			beforeRequest(0, filesToUpload.length);

			//Let's go through all files.			
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
					filesToUpload[currentFile].getRemainingLength() +
					getAdditionnalBytesForUpload(currentFile);
				
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
				
				//If the current file is bigger than the maxChunkSize:
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
			bUploadOk = false;
			uploadException = e;
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
	 * @param firstFileToUploadParam The index of the first file to upload, in the {@link #filesToUpload} area.
	 * @param nbFilesToUploadParam Number of file to upload, in the next HTTP upload request. These files are taken from 
	 * the {@link #filesToUpload} area 
	 */
	final private boolean doUpload (int firstFileToUploadParam, int nbFilesToUploadParam) {
		boolean bReturn = true;
		boolean bLastChunk = false;
		boolean bChunkEnabled = false;
		int chunkPart = 0;
		int nbFilesToUpload = 0;
		long totalContentLength = 0;
		long totalFileLength = 0;
		long contentLength = 0;
		long thisChunkSize = 0;
		int firstFileToUpload = 0;
		String msg;
		String action = "init (DefaultFileUploadThread)";

		
		if (nbFilesToUploadParam == 1) {
			msg = (firstFileToUploadParam + 1) + "/" + (filesToUpload.length);
		} else {
			msg = (firstFileToUploadParam + 1) + "-" + (firstFileToUploadParam + nbFilesToUploadParam) + "/" + (filesToUpload.length);
		}
		
		if(!stop && (null != progress)) {
			progress.setString(uploadPolicy.getString("infoUploading", msg));
		}


		//Let's be optimistic: we calculate the total upload length. Then, we'll test that this is less that the
		//maximum chunk size ... if any is defined.
		try {
			//Prepare upload, for all files to be uploaded.
			//We have to do it again, in case we don't upload all files at once. In HTTP header the last tail
			//is different from the other one.
			beforeRequest(firstFileToUploadParam, nbFilesToUploadParam);
			
			for(int i=0; i < nbFilesToUploadParam && !stop; i++){
				totalContentLength += filesToUpload[firstFileToUploadParam+i].getUploadLength();
				totalContentLength += getAdditionnalBytesForUpload(firstFileToUploadParam+i);
				totalFileLength += filesToUpload[firstFileToUploadParam+i].getUploadLength();
				
				if (uploadPolicy.getDebugLevel() >= 80) {
					uploadPolicy.displayDebug("file " + (firstFileToUploadParam+i)
						+ ": content=" + filesToUpload[firstFileToUploadParam+i].getUploadLength()
						+ " bytes, getAdditionnalBytesForUpload=" + getAdditionnalBytesForUpload(firstFileToUploadParam+i)
						+ " bytes"
						, 80);
				}
			}
		} catch (JUploadException e) {
			uploadPolicy.displayErr(e);
			uploadException = e;
		}
		
		//Ok, now we check that the totalContentLength is less than the chunk size.
		if (totalFileLength >= maxChunkSize) {
			//hum, hum, we have to download file by file, with chunk enabled. This a prerequisite of this method.
			if (nbFilesToUploadParam > 1) {
				uploadException = new JUploadException("totalContentLength >= chunkSize: nbFilesToUploadParam should be more than 1 (doUpload)");
			}
			bChunkEnabled = true;
		}
		
		//This while enables the chunk management:
		// In chunk mode, it loops until the last chunk is uploaded. This works only because, in chunk mode,
		//		files are uploaded one y one (the for loop within the while loops through ... 1 unique file).
		// In normal mode, it does nothing, as the bLastChunk is set to true in the first test, within the while.
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
					
					//Is this the last chunk ?
					if (bLastChunk) {
						thisChunkSize = filesToUpload[firstFileToUploadParam].getRemainingLength();
					} else {
						thisChunkSize = maxChunkSize;
					}
	
					contentLength = thisChunkSize + getAdditionnalBytesForUpload(firstFileToUploadParam);
				} else { 
					//Chunk not activate. We upload all files at once.
					bLastChunk = true;
					contentLength = totalContentLength;
					firstFileToUpload = firstFileToUploadParam;
					nbFilesToUpload = nbFilesToUploadParam;
				}
				clearServerOutPut();

				//Ok, we've prepare the job for chunk upload. Let's do it!
				action = "openConnection (DefaultFileUploadThread)";
				startRequest(contentLength, bChunkEnabled, chunkPart, bLastChunk);
								
				
				for(int i=0; i < nbFilesToUpload && !stop; i++){
					// Write to Server the head(4 Lines), a File and the tail.
					action = "send bytes (20)" + (firstFileToUpload+i);
					
					//Let's add any file-specific header.
					beforeFile(firstFileToUpload+i);

					//In chunk mode, we already calculate the correct chunkSize.
					if (!bChunkEnabled) {
						//If not chunk mode, then the file is uploaded in one shot.
						thisChunkSize = filesToUpload[firstFileToUpload+i].getUploadLength();
					}
					
					//Actual upload of the file:
					action = "send bytes (30)" + (firstFileToUpload+i);
					filesToUpload[firstFileToUpload+i].uploadFile(getOutputStream(), thisChunkSize);
					action = "send bytes (31)" + (firstFileToUpload+i);

					//If we are not in chunk mode, or if it was the last chunk, upload should be finished.
					if (!bChunkEnabled  ||  bLastChunk) {
						if(!stop && (null != progress))
							progress.setString(uploadPolicy.getString("infoUploaded", msg));
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

					//Let's add any file-specific header.
					afterFile(firstFileToUpload+i);
				}
				action = "flush";
				getOutputStream().flush ();
				
				
				//Let's finish the request, and wait for the server Output, if any (not applicable in FTP)
				action = "finishRequest";
				finishRequest();
				
				//We now ask to the uploadPolicy, if it was a success.
				//If not, the isUploadSuccessful should raise an exception.
				uploadPolicy.checkUploadSuccess(getServerOutput(), getResponseBody());

			}catch(Exception e){
				uploadException = e;
				bReturn = false;
				uploadPolicy.displayErr(uploadPolicy.getString("errDuringUpload") + " (main | " + action + ") (" + e.getClass() + ".doUpload()) : " + e.getMessage());
			}finally{
				//We force the progress bar refresh
				nbBytesBeforeUpdatingProgressBar = -1;
				nbBytesUploaded(0);

				try {
					cleanRequest();
				} catch (JUploadException e) {
					uploadException = e;
					bReturn = false;
				}
			}
			
	    	if (uploadPolicy.getDebugLevel() > 80) {
	          	uploadPolicy.displayDebug("-------- Server Output Start --------\n", 80);
	            uploadPolicy.displayDebug(getServerOutput() + "\n", 80);
	            uploadPolicy.displayDebug("--------- Server Output End ---------\n", 80);
	    	}
		}//while(!bLastChunk && uploadException==null)
		

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
	
	/** @see FileUploadThread#close() */
	public void close() {
		try {
			cleanAll();
		} catch (JUploadException e) {
			uploadPolicy.displayErr(e);
		}
	}
}
