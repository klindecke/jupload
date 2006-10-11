/*
 * Created on 21 avr. 2006
 */
package wjhk.jupload2.filedata;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import wjhk.jupload2.exception.JUploadException;
import wjhk.jupload2.policies.UploadPolicy;




/**
 *
 * This class contains all data for a file to upload. The current {@link wjhk.jupload2.policies.UploadPolicy}
 * contains the necessary parameters to personalize the way files must be handled. 
 *  
 * @author Etienne Gauthier
 */
public class FileData  {

	/**
	 * The current upload policy.
	 */
	UploadPolicy uploadPolicy;

	private File file;
	private Date lastModified;
	private String mimeType = "application/octet-stream";


	
	/**
	 * Standard constructor
	 * 
	 * @param file The file whose data this instance will give.
	 */
	public FileData(File file, UploadPolicy uploadPolicy) {
		this.file = file;
		lastModified = new Date(file.lastModified());
		this.uploadPolicy = uploadPolicy;
	}

	/**
	 * Prepare the fileData to upload. For instance, picture data can be 
	 * resized before upload. This method is called before upload.<BR>
	 * Default: do nothing.
	 * 
	 * @see #isUploadReady()
	 *
	 */
	public void beforeUpload () {
		//Default : nothing to do. 
	}
	
	/**
	 * Indicate if everything is ready for upload. This method is called for each file
	 * by the 'heart' of the program, before upload, until it returns true.
	 * 
	 * @return ready or not for upload.
	 * @see #beforeUpload()
	 */
	public boolean isUploadReady() {
		//Default : nothing to do before upload, so ... we're always ready !
		return true;
	}

	/**
	 * This function create an input stream for this file. The caller is responsible
	 * for closing this input stream.
	 * 
	 * @return An inputStream 
	 */
	public InputStream getInputStream () throws IOException, JUploadException {
		//Standard FileData : we read the file.
		return new FileInputStream(file);
	}
	
	/**
	 * This function is called after upload, whether it is successful or not. It allows fileData to
	 * free any resssource created for the upload. 
	 *
	 */
	public void afterUpload () {
		//Nothing to do here
	}
	
	/**
	 * Get the original filename. This is the name of the file, into the local hardrive
	 * 
	 * @return The original filename 
	 * 
	 */
	public String getFileName() {
		return file.getName();
	}
	
	/**
	 * 
	 * @return The extension for this file.
	 */
	public String getFileExtension () {
		String name = file.getName();
		return name.substring(name.lastIndexOf('.')+1);
	}
	
	/**
	 * @return The length of the original file. 
	 */
	public long getFileLength() {
		return file.length();
	}

	/**
	 * @return The length of upload. In this class, this is ... the size of the file !
	 * 
	 * @see PictureFileData 
	 */
	public long getUploadLength() throws JUploadException {
		return file.length();
	}

	/**
	 * File date.
	 */
	public Date getLastModified() {
		return lastModified;
	}
	
	/**
	 * Get the absolute path of the file. 
	 * 
	 * @return Get the directory where is stored this file.
	 */
	public String getDirectory () {
		return file.getAbsolutePath();
	}
	
	/**
	 * This function return the FileData content type. Default is 
	 * 
	 * @return The mimeType for the file.
	 */
	public String getMimeType () {
		return mimeType;
	}
	
	/**
	 * Indicate if this file can be read.
	 */
	public boolean canRead () {
		return file.canRead();
	}
	
	/**
	 * 
	 * @return the File instance associated with this row.
	 */
	public File getFile() {
		return file;
	}
}
