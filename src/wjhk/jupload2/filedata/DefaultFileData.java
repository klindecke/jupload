/*
 * Created on 21 avr. 2006
 */
package wjhk.jupload2.filedata;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Date;

import wjhk.jupload2.exception.JUploadException;
import wjhk.jupload2.exception.JUploadIOException;
import wjhk.jupload2.policies.DefaultUploadPolicy;
import wjhk.jupload2.policies.UploadPolicy;




/**
 *
 * This class contains all data and methods for a file to upload. The current 
 * {@link wjhk.jupload2.policies.UploadPolicy} contains the necessary parameters to personalize the way files 
 * must be handled.
 * <BR><BR>
 * This class is the default FileData implementation. It gives the default behaviour, and is used by 
 * {@link DefaultUploadPolicy}. It provides standard control on the files choosen for upload.
 * 
 * @see FileData
 *  
 * @author Etienne Gauthier
 */
public class DefaultFileData  implements FileData {

	/**
	 * The current upload policy.
	 */
	UploadPolicy uploadPolicy;

	/////////////////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////   Prrotected attributes   /////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * Mime type of the file. It will be written in the upload HTTP request.
	 */
	protected String mimeType = "application/octet-stream";

	/////////////////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////   Private attributes   ////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * file is the file about which this FileData contains data.
	 */
	private File file;
	

	
	/**
	 * Standard constructor
	 * 
	 * @param file The file whose data this instance will give.
	 */
	public DefaultFileData(File file, UploadPolicy uploadPolicy) {
		this.file = file;
		this.uploadPolicy = uploadPolicy;
	}

	/** @see FileData#beforeUpload() */
	public void beforeUpload () throws JUploadException {
		//Default : nothing to do. 
	}
	
	/** @see FileData#getUploadLength() */
	public long getUploadLength() throws JUploadException {
		return file.length();
	}

	/** @see FileData#afterUpload() */
	public void afterUpload () {
		//Nothing to do here
	}

	/** @see FileData#getInputStream() */
	public InputStream getInputStream () throws JUploadException {
		//Standard FileData : we read the file.
		try {
			return new FileInputStream(file);
		} catch (FileNotFoundException e) {
			throw new JUploadIOException(e, "DefaultFileData.getInputStream()");
		}
	}
	
	/** @see FileData#getFileName() */
	public String getFileName() {
		return file.getName();
	}
		
	/** @see FileData#getFileExtension() */
	public String getFileExtension () {
		String name = file.getName();
		return name.substring(name.lastIndexOf('.')+1);
	}
	
	/** @see FileData#getFileLength() */
	public long getFileLength() {
		return file.length();
	}

	/** @see FileData#getLastModified() */
	public Date getLastModified() {
		return new Date(file.lastModified());
	}
	
	/** @see FileData#getDirectory() */
	public String getDirectory () {
		return file.getAbsolutePath();
	}
	
	/** @see FileData#getMimeType() */
	public String getMimeType () {
		return mimeType;
	}
	
	/** @see FileData#canRead() */
	public boolean canRead () {
		return file.canRead();
	}
	
	/** @see FileData#getFile() */
	public File getFile() {
		return file;
	}
}
