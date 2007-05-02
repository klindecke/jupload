package wjhk.jupload2.upload;

/**
 * This interface defines the methods of the various FileUploadThread classes. These
 * classes are kept in the CVS, as people often update them for their needs: I
 * don't want to remove them, when I do a 'big bang' within them. Created on 21
 * nov. 06
 */
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
     * Get the server Output.
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
     * Indicate to the UploadThread that nbBytes bytes have been uploaded to the
     * server. It's up to this method to change the display on the progress bar
     * (or whatever other information displayed to the user)
     * 
     * @param nbBytes
     */
    public void nbBytesUploaded(long nbBytes);

    /**
     * 
     */
    public void close();

    /**
     * @see java.lang.Thread#isAlive()
     */
    public boolean isAlive();

    /**
     * @see java.lang.Thread#join()
     */
    public void join() throws InterruptedException;

    /**
     * @see java.lang.Thread#join(long)
     */
    public void join(long millisec) throws InterruptedException;

    /**
     * @see java.lang.Thread#start()
     */
    public void start();

}
