/**
 * 
 * This class defines the methods of the various FileUploadThreadV1 classes. These classes are kept in the 
 * CVS, as people often update them for their needs: I don't want to remove them, when I do a 'big bang' within
 * them.
 * 
 * Created on 21 nov. 06
 */
package wjhk.jupload2.upload;

public interface FileUploadThread {
		
	/**
	 * Stopping the Thread
	 */
	public void stopUpload();
	
	/**
	 * Returns true if someone asks the thread to stop.
	 * 
	 * @see #stopUpload()
	 */
	public boolean isUploadStopped();
	
	/**
	 *  Get the server Output.
	 *  
	 * @return The StringBuffer that contains the full server HTTP response.
	 */
	public String getServerOutput();
	
	/**
	 * Get the exception that occurs during upload.
	 * 
	 * @return The exception, or null if no exception were thrown.
	 */
	public Exception getException();
	
	/**
	 * Indicate to the UploadThread that nbBytes bytes have been uploaded to the server. It's up to this method
	 * to change the display on the progress bar (or whatever other information displayed to the user) 
	 * 
	 * @param nbBytes
	 */
	public void nbBytesUploaded(long nbBytes); 

	//////////////////////////////////////////////////////////////////////////////////////////////////
	//////////////////////     METHODS DEFINED BY THE Thread CLASS   /////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////////////

	public void close();
	public boolean isAlive();
	public void join() throws InterruptedException;
	public void join(long millisec) throws InterruptedException;
	public void start();
	
}
