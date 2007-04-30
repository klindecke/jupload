/*
 * Created on 20 nov. 06
 */
package wjhk.jupload2.filedata;

import java.io.File;
import java.io.InputStream;
import java.util.Date;

import wjhk.jupload2.exception.JUploadException;
import wjhk.jupload2.policies.UploadPolicy;

/**
*
* This class contains all data and methods for a file to upload. The current {@link wjhk.jupload2.policies.UploadPolicy}
* contains the necessary parameters to personalize the way files must be handled.
* <BR><BR>
* This class is the interface that all FileData must implement. The {@link DefaultFileData} class contains the 
* default implementation for this interface. The {@link  PictureFileData} contains another implementation of this
* interface, adapted to manage pictures (rotation, resizing...). 
* <BR>
* The instance of FileData is created by the {@link UploadPolicy#createFileData(File)} method. This method can be 
* overrided in a new upoad policy, to create an instance of another FileData. See {@link  PictureFileData} for an
* example of that. 
*  
* @author Etienne Gauthier
*/

public interface FileData {

	/**
	 * Prepare the fileData to upload. For instance, picture data can be resized before upload 
	 * (see {@link PictureFileData}. This method is called before the upload of this file.
	 * 
	 * @see FileUploadThread
	 *
	 */
	public void beforeUpload () throws JUploadException;
	
	/**
	 * Get size of upload, which may be different from th actual file length.
	 * 
	 * @return The length of upload. In this class, this is the size of the file, as it isn't transformed for upload.
	 * This size may change if encoding is necessary (needs a new FileData class), or if picture is to be resized 
	 * or rotated.
	 * 
	 * @see PictureFileData 
	 */
	public long getUploadLength() throws JUploadException;

	/**
	 * This function is called after upload, whether it is successful or not. It allows fileData to
	 * free any resssource created for the upload. For instance, {@link PictureFileData#afterUpload()} removes the 
	 * temporary file, if any was created. 
	 *
	 */
	public void afterUpload ();
	
	/**
	 * This function create an input stream for this file. The {@link FileUploadThread} class can then read
	 * bytes from it, to transfert them to the webserver. The caller is responsible for closing this input stream.
	 * 
	 * @return An inputStream 
	 */
	public InputStream getInputStream () throws JUploadException;
	
	/**
	 * Get the original filename. This is the name of the file, into the local hardrive
	 * 
	 * @return The original filename 
	 * 
	 */
	public String getFileName();
		
	/**
	 * 
	 * @return The extension for the original file.
	 */
	public String getFileExtension ();
	
	/**
	 * @return The length of the original file. 
	 */
	public long getFileLength();

	/**
	 * @return The original file date.
	 */
	public Date getLastModified();
	
	/**
	 * Get the directory of the file. 
	 * 
	 * @return The directory where this file is stored.
	 */
	public String getDirectory ();
	
	/**
	 * This function return the FileData content type. 
	 * 
	 * @return The mimeType for the file.
	 */
	public String getMimeType();
	
	/**
	 * Indicate if this file can be read.
	 * 
	 */
	public boolean canRead();
	
	/**
	 * @return the File instance associated with this row.
	 */
	public File getFile();

}
