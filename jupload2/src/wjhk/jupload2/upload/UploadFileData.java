/*
 * Created on 20 nov. 06
 */
package wjhk.jupload2.upload;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Date;

import wjhk.jupload2.exception.JUploadException;
import wjhk.jupload2.exception.JUploadExceptionUploadFailed;
import wjhk.jupload2.exception.JUploadIOException;
import wjhk.jupload2.filedata.FileData;
import wjhk.jupload2.policies.UploadPolicy;

class UploadFileData implements FileData {
		
	/**
	 * The {@link FileData} instance that contains all information about the file to upload.
	 */
	private FileData fileData = null;
			
	/**
	 * Instance of the fileUploadThread. This allow this class to send feedback to the thread.
	 * @see FileUploadThread#nbBytesUploaded(long)
	 */
	private FileUploadThread fileUploadThread = null;
	
	/**
	 * inputStream contains the stream that read from the file to upload. This may be a transformed version of the
	 * file (for instance, a compressed one).
	 * @see FileData#getInputStream()
	 */
	private InputStream inputStream = null;

	/**
	 * The number of bytes to upload, for this file (without the head and tail defined for the HTTP multipart body).
	 */
	private long uploadRemainingLength = -1;
	
	/**
	 * The current {@link UploadPolicy}
	 */
	private UploadPolicy uploadPolicy = null;
	
	
	///////////////////////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////////////    CONSTRUCTOR       ///////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////////////////
	
	
	/**
	 * Standard constructor for the UploadFileData class. 
	 */
	public UploadFileData(FileData fileDataParam, FileUploadThread fileUploadThreadParam, UploadPolicy uploadPolicyParam){
		this.fileData = fileDataParam;
		this.fileUploadThread = fileUploadThreadParam;
		this.uploadPolicy = uploadPolicyParam;		
	}

	

	
	///////////////////////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////////////  PROTECTED METHODS   ///////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * Returns the header for this file, within the http multipart body.
	 * 
	 * @param fileIndex Index of the file in the array that contains all files to upload.
	 * @param boundary The boundary that separate files in the http multipart post body.
	 * @param chunkPart The numero of the current chunk (from 1 to n)
	 * @return The header for this file.
	 */
	String getFileHeader(int fileIndex, String boundary, int chunkPart) throws JUploadException {
		String fileHead;
		String filenameEncoding = uploadPolicy.getFilenameEncoding();
		String uploadFilename = uploadPolicy.getUploadFilename(fileData, fileIndex);
		StringBuffer sb = new StringBuffer();
		
		// Line 1: boundary.
		sb.append(boundary.toString());sb.append("\r\n");
		// Line 2: Content-Disposition.
		//try {
		sb	.append("Content-Disposition: form-data; name=\"")
			.append(uploadPolicy.getUploadName(fileData, fileIndex))
			.append("\"; filename=\"")
			;
		if (filenameEncoding == null) {
			sb.append(uploadFilename);
		} else {
			try {
				uploadPolicy.displayDebug("Encoded filename: " + URLEncoder.encode(uploadFilename, filenameEncoding), 99);
				sb.append(URLEncoder.encode(uploadFilename, filenameEncoding));
			} catch (UnsupportedEncodingException e) {
				uploadPolicy.displayWarn(e.getClass().getName() + ": " + e.getMessage() + " (in UploadFileData.getFileHeader)");
				sb.append(uploadFilename);
			}
		}
		//In chunk mode, we add 'partN' at the end of the filename (where N is the part number, from 1 to n)
		if (chunkPart >= 0) {
			sb.append(".part").append(chunkPart);
		}
		//Let's finish the header.
		sb.append("\"\r\n");

		// Line 3: Content-Type.
		sb	.append("Content-Type: ")
			.append(fileData.getMimeType());
		if (filenameEncoding != null) {
			sb	.append("; charset=")
				.append(filenameEncoding);
		}
		sb.append("\r\n");
		
		//An empty line to finish the header.
		sb.append("\r\n");
		
		fileHead = sb.toString();		
		uploadPolicy.displayDebug("head : <<" +  fileHead + ">>", 70);
		
		return fileHead;
	}
	
	/**
	 * Get the number of files that are still to upload. It is initialized at the creation of the file, by 
	 * a call to the {@link FileData#getUploadLength()}.
	 * <BR>
	 * <B>Note:</B> When the upload for this file is finish and you want to send it again (for instance the upload
	 * failed, and you want to do a retry), you should not reuse this instance, but, instead, create a new 
	 * UploadFileData instance. 
	 * 
	 * @return Number of bytes still to upload.
	 * @see #getInputStream() 
	 */
	long getRemainingLength() {
		return uploadRemainingLength;
	}

	/**
	 * This methods upload write the file data to upload (see {@link FileData#getInputStream()}
	 * to the given outputStream (the output toward the HTTP server).
	 * 
	 * @param outputStream The stream on which the file is to be written.
	 * @throws JUploadException Any troubles that could occurs during upload.
	 */
	void uploadFile(OutputStream outputStream, long nbBytesToWrite) throws JUploadException {
		long nbBytesTransmitted = 0;
		byte[] byteBuff = null;
		//getInputStream will put a new fileInput in the inputStream attribute, or leave it unchanged if it 
		//is not null.
		getInputStream(); 
		
		
		int nbBytes = 0;
		byteBuff = new byte[1024];
		try {
			while(!fileUploadThread.isUploadStopped() && nbBytesTransmitted < nbBytesToWrite) {
				nbBytes = inputStream.read(byteBuff);
				if (nbBytes < 1 || nbBytes >1024) {
					throw new JUploadExceptionUploadFailed("nbBytes=" + nbBytes + " in UploadFileData.uploadFile (should be 1).");
				}
				fileUploadThread.nbBytesUploaded(nbBytes);
				outputStream.write(byteBuff, 0, nbBytes);
				nbBytesTransmitted += nbBytes;
				uploadRemainingLength -= nbBytes;
			}
		} catch (IOException e) {
			throw new JUploadIOException (e, "UploadFileData.uploadFile(OutputStream)");
		}
	}

	
	///////////////////////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////////////  PRIVATE METHODS   /////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////////////////


	
	///////////////////////////////////////////////////////////////////////////////////////////////////////
	//////////////////////  IMPLEMENTATION OF METHODS COMING FROM THE FileData INTERFACE //////////////////
	///////////////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * This method closes the inputstream, and remove the file from the filepanel. Then it calls
	 * {@link FileData#afterUpload()}.
	 *  
	 * @see FileData#afterUpload() 
	 */
	public void afterUpload() {
		//1. Close the InputStream
		if (inputStream != null) {
			try {
				inputStream.close();
			} catch (IOException e) {
				uploadPolicy.displayWarn(e.getClass().getName() + ": " + e.getMessage() + " (in UploadFileData.afterUpload()");
			}
			inputStream  = null;
		}

		//2. Ask the FileData to release any other locked resource.
		fileData.afterUpload();		
	}

	/** @see FileData#beforeUpload() */
	public void beforeUpload() throws JUploadException {
		fileData.beforeUpload();
		
		//Calculation of some internal variables.
		uploadRemainingLength = fileData.getUploadLength();		
	}

	/** @see FileData#canRead() */
	public boolean canRead() {
		return fileData.canRead();
	}

	/** @see FileData#getDirectory() */
	public String getDirectory() {
		return fileData.getDirectory();
	}

	/** @see FileData#getFile() */
	public File getFile() {
		return fileData.getFile();
	}

	/** @see FileData#getFileExtension() */
	public String getFileExtension() {
		return fileData.getFileExtension();
	}

	/** @see FileData#getFileLength() */
	public long getFileLength() {
		return fileData.getFileLength();
	}

	/** @see FileData#getFileName() */
	public String getFileName() {
		return fileData.getFileName();
	}

	/** @see FileData#getInputStream() */
	public InputStream getInputStream() throws JUploadException {
		//If you didn't already open the input stream, the remaining length should be non 0.
		if (inputStream == null) {
			if (uploadRemainingLength <= 0) {
				//Too bad: we already uploaded this file. Perhaps its Ok (a second try?)
				//To avoid this warning, just create a new UploadFileData instance, and not use an already existing one.
				uploadPolicy.displayWarn("[" + getFileName() + "] UploadFileData.getInputStream(): uploadRemainingLength is <= 0. Trying a new upload ?");
				uploadRemainingLength = fileData.getUploadLength();
			}
			//Ok, this is the start of upload for this file. Let's get its InputStream.
			inputStream = fileData.getInputStream(); 
		}
		return inputStream;
	}

	/** @see FileData#getLastModified() */
	public Date getLastModified() {
		return fileData.getLastModified();
	}

	/** @see FileData#getMimeType() */
	public String getMimeType() {
		return fileData.getMimeType();
	}

	/** @see FileData#getUploadLength() */
	public long getUploadLength() throws JUploadException {
		return fileData.getUploadLength();
	}

}
