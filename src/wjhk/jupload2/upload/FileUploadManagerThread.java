package wjhk.jupload2.upload;

import javax.swing.JProgressBar;

import wjhk.jupload2.exception.JUploadException;
import wjhk.jupload2.filedata.FileData;
import wjhk.jupload2.gui.FilePanel;
import wjhk.jupload2.gui.JUploadPanel;
import wjhk.jupload2.policies.UploadPolicy;

/**
 * This class is responsible for managing the upload. At the end of the upload,
 * the {@link JUploadPanel#updateButtonState()} is called, to refresh the button
 * state. Its job is to: <DIR>
 * <LI>Prepare upload for the file (calls to {@link FileData#beforeUpload()}
 * for each file in the file list.
 * <LI>Create the thread to send a packet of files.
 * <LI>Prepare the packets, that will be red by the upload thread.
 * <LI>Manage the end of upload: trigger the call to
 * {@link JUploadPanel#updateButtonState()} and the call to
 * {@link UploadPolicy#afterUpload(Exception, String)}.
 * <LI>Manage the 'stop' button reaction. </DIR> This class is created by
 * {@link JUploadPanel}, when the user clicks on the upload button.
 * 
 * @author etienne_sf
 */
public class FileUploadManagerThread extends Thread {

    /** The current file list. */
    private FilePanel filePanel = null;

    /**
     * The upload thread, that will wait for the next file packet to be ready,
     * then send it.
     */
    private FileUploadThread fileUploadThread = null;

    /** @see UploadPolicy#getMaxChunkSize() */
    long maxChunkSize = -1;

    /** Contains the sum of the files, ready for upload, and not uploaded yet */
    private long nbBytesReadyForUpload = 0;

    /**
     * Number of files that has been read by the {@link FileUploadThread}
     * thread. These files have been read by the {@link #getNextPacket()}
     * method.
     */
    private int nbFilesBeingUploaded = 0;

    /** @see UploadPolicy#getNbFilesPerRequest() */
    int nbFilesPerRequest = -1;

    /**
     * Number of files that are prepared for upload. A file is prepared for
     * upload, if the {@link FileData#beforeUpload()} has been called.
     */
    private int nbPreparedFiles = 0;

    /**
     * Number of files that have already been uploaded. Include the success
     * server response.
     */
    private int nbUploadedFiles = 0;

    /**
     * Indicated the number of bytes that have currently been sent for the
     * current file. This allows the management of the progress bar.
     */
    private long nbBytesUploadedForCurrentFile = 0;

    /**
     * Contains the next packet to upload.
     * 
     * @see #isNextPacketReady()
     * @see #getNextPacket()
     */
    private UploadFileData[] nextPacket = null;

    /** The {@link JUploadPanel} progress bar. */
    private JProgressBar progressBar = null;

    /**
     * Contains the time of the actual start of upload. Doesn't take into
     * account the time for preparing files.
     */
    private long uploadStartTime;

    /**
     * If set to 'true', the thread will stop the crrent upload. This attribute
     * is not private as the {@link UploadFileData} class us it.
     * 
     * @see UploadFileData#uploadFile(java.io.OutputStream, long)
     */
    private boolean stop = false;

    /**
     * Contains an estimation of the total number of bytes to upload. It's the
     * average upload file size, divided by the total number of files. It is
     * calculated in {@link  #anotherFileIsReady(FileData)}.
     */
    private long estimatedTotalLength = 0;

    /** Thread Exception, if any occurred during upload. */
    private JUploadException uploadException = null;

    /** Current number of bytes that have been uploaded. */
    private long uploadedLength = 0;

    /** A shortcut to the upload panel */
    private JUploadPanel uploadPanel = null;

    /** The current upload policy. */
    private UploadPolicy uploadPolicy = null;

    /** The list of files to upload */
    private UploadFileData[] uploadFileDataArray = null;

    /**
     * Standard constructor of the class.
     * 
     * @param uploadPolicy
     */
    public FileUploadManagerThread(UploadPolicy uploadPolicy) {
        // General shortcuts on the current applet.
        this.uploadPolicy = uploadPolicy;
        this.uploadPanel = uploadPolicy.getApplet().getUploadPanel();

        this.filePanel = this.uploadPanel.getFilePanel();
        this.progressBar = this.uploadPanel.getProgressBar();
        this.nbFilesPerRequest = this.uploadPolicy.getNbFilesPerRequest();
        this.maxChunkSize = this.uploadPolicy.getMaxChunkSize();

        // Prepare the list of files to upload. We do this here, to minimize the
        // risk of concurrency, if the user drops or pastes files on the applet
        // while uploading.
        FileData[] fileDataArray = this.uploadPanel.getFilePanel().getFiles();
        uploadFileDataArray = new UploadFileData[fileDataArray.length];
        for (int i = 0; i < uploadFileDataArray.length; i += 1) {
            uploadFileDataArray[i] = new UploadFileData(fileDataArray[i], this,
                    this.uploadPolicy);
        }
    }

    /**
     * The heart of the program. This method prepare the upload, then calls
     * doUpload for each HTTP request.
     * 
     * @see java.lang.Thread#run()
     */
    @Override
    final public void run() {
        boolean bUploadOk = true;

        // The upload is started. Let's change the button state.
        this.uploadPanel.updateButtonState();

        // Let's prepare the progress bar, to display the current upload stage.
        initProgressBar();

        // Let's start the upload thread. It will wait until the first packet
        // is ready.
        createUploadThread();

        // We have to prepare the files, then to create the upload thread for
        // each file packet.
        try {
            for (int i = 0; i < uploadFileDataArray.length; i += 1) {
                this.uploadPolicy.displayDebug(
                        "============== Start of file preparation ("
                                + uploadFileDataArray[i].getFileName() + ")",
                        30);
                // If the upload has not started, nothing has been sent. We
                // display the progress message to the user.
                this.progressBar.setString(String.format(this.uploadPolicy
                        .getString("preparingFile"), new Integer(i + 1),
                        new Integer(this.uploadFileDataArray.length)));
                // Then, we work
                uploadFileDataArray[i].beforeUpload();
                this.uploadPolicy.displayDebug(
                        "============== End of file preparation ("
                                + uploadFileDataArray[i].getFileName() + ")",
                        30);
                anotherFileIsReady(uploadFileDataArray[i]);
            }

            // Wait for the end of upload.
            while (this.fileUploadThread != null
                    && this.fileUploadThread.isAlive()) {
                try {
                    this.fileUploadThread.join();
                } catch (InterruptedException ie) {
                    // Nothing to do.
                }
            }
        } catch (JUploadException e) {
            bUploadOk = false;
            this.uploadException = e;
            this.uploadPolicy.displayErr(e);
            this.progressBar.setString(e.getMessage());
            stopUpload();
        }
        // The upload is finished. Let's restore the button state.
        this.uploadPanel.updateButtonState();

        // TODO after upload reaction
        if (bUploadOk) {
            afterUploadOk();
        }

        // The thread upload may need some information about the current one,
        // like ... knowing that upload is actually finished (no more file to
        // send).
        try {
            while (this.fileUploadThread != null
                    && this.fileUploadThread.isAlive()) {
                this.fileUploadThread.join();
            }
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * Returns the estimated length of the whole upload. This is an estimation,
     * as file are prepared in a thread and uploaded in another. The more file
     * are prepared, the most accurate this estimation is. When all files are
     * prepared, this is no more an estimation, but the real count. This value
     * is calculated in the anotherFileIsReady(FileData) method.
     * 
     * @return The estimation of the total number of bytes to upload, based on
     *         the average size of the already prepared files.
     */
    public long getEstimatedTotalLength() {
        return this.estimatedTotalLength;

    }

    /**
     * Get the exception that occurs during upload.
     * 
     * @return The exception, or null if no exception were thrown.
     */
    public Exception getException() {
        return this.uploadException;
    }

    /**
     * Get the total number of files which have been successfully uploaded.
     * 
     * @return Total number of uploaded files.
     */
    public int getNbUploadedFiles() {
        return this.nbUploadedFiles;
    }

    /**
     * Retrieve the start time of the upload. That is: when the first upload
     * starts. It can be some delay after this thread creation, as it first need
     * to prepare files to upload.
     * 
     * @return The time this thread was started in ms.
     */
    public final long getUploadStartTime() {
        return this.uploadStartTime;
    }

    /**
     * Return the number of bytes for the files that have been successfully
     * uploaded. This doesn't count the additional information, like HTTP
     * headers, form data...
     * 
     * @return Total number of uploaded bytes.
     * 
     */
    public long getUploadedLength() {
        return this.uploadedLength;
    }

    /**
     * Stores the last upload exception that occurs. This method won't write to
     * the log file.
     * 
     * @param uploadException
     */
    public void setUploadException(JUploadException uploadException) {
        this.uploadException = uploadException;
    }

    /**
     * Get the last upload exception that occurs.
     * 
     * @return The last upload exception, or null if no exception occurs.
     */
    public JUploadException getUploadException() {
        return this.uploadException;
    }

    /**
     * Indicates if all files have been sent to the server.
     * 
     * @return true if all files have been uploaded. False otherwise.
     */
    public boolean isUploadFinished() {
        // If there is no files ready, and all files have been prepared ...
        // Then, it means that all files have been uploaded.
        return nbUploadedFiles == uploadFileDataArray.length;
    }

    /**
     * Indicates if the upload has been stopped by the user, or by any upload
     * error.
     * 
     * @return true if the current upload has been asked to stop by the user,
     *         false otherwise.
     */
    public boolean isUploadStopped() {
        return this.stop;
    }

    /**
     * Used by the UploadFileData#uploadFile(java.io.OutputStream, long) for
     * each uploaded buffer
     * 
     * @param nbBytes Number of additional bytes that where uploaded.
     * @throws JUploadException
     */
    public void nbBytesUploaded(long nbBytes) throws JUploadException {
        this.uploadedLength += nbBytes;
        this.nbBytesUploadedForCurrentFile += nbBytes;
        // Let's display some information
        updateProgressBar();
    }

    /**
     * Reaction to the user click on the 'Stop' button, or any action from the
     * user asking to stop the upload. The upload should go on for the current
     * file, and stop before starting the next upload request to the server, to
     * avoid strange problems on the server.
     */
    public void stopUpload() {
        this.stop = true;
    }

    // //////////////////////////////////////////////////////////////////////////////////////////////
    // /////////////////// SYNCHRONIZATION METHODS
    // //////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Check if a new packet is ready. A packet is ready if enough file are
     * prepared for upload (compared to the nbFilesPerRequest applet parameter)
     * or if the sum of bytes for the prepared files are more than the
     * maxChunkSize applet parameter (if it was given as an applet parameter).
     * The result is stored in the {@link #isNextPacketReady} attribute. <BR>
     * Note: Take care that the result of this method (isNextPacketReady)
     * doesn't take into account the number of files that are actually being
     * uploaded.
     * 
     * @throws JUploadException
     */
    private synchronized boolean checkIfNextPacketIsReady()
            throws JUploadException {

        if (this.nextPacket != null) {
            // Nothing to do: the next packet is already prepared
        } else if (this.nbUploadedFiles + this.nbFilesBeingUploaded == this.nbPreparedFiles) {
            // No file is ready to sent: Upload is finished or all files are
            // been sent to the FileUploadThread.
        } else {
            // Some new files are ready, let's look if the packet is ready. We
            // add at least the first one.
            FileData[] tempFileData = new FileData[this.nbFilesPerRequest];
            int nbFilesInPacket = 0;
            long packetLength = 0;
            boolean isPacketFinished = false;

            // We'll add the files, up to :
            // 1) The number of new files prepared and not uploaded (or being
            // uploaded by another upload thread),
            // 2) The number of files per request is no more than the
            // nbFilesPerRequest applet parameter.
            while (nbFilesInPacket < this.nbPreparedFiles
                    - this.nbUploadedFiles - this.nbFilesBeingUploaded
                    && nbFilesInPacket < this.nbFilesPerRequest) {
                // If the packet is not empty, we don't allow to add a new file,
                // if this new file make the total upload size be more than a
                // chunk size, as chunk upload expects that files are sent file
                // by file.
                if (nbFilesInPacket > 0
                        && packetLength
                                + uploadFileDataArray[nbFilesInPacket]
                                        .getUploadLength() > this.maxChunkSize) {
                    // The packet is full, now.
                    isPacketFinished = true;
                    break;
                }
                // Let's add this file.
                tempFileData[nbFilesInPacket] = uploadFileDataArray[this.nbUploadedFiles
                        + this.nbFilesBeingUploaded + nbFilesInPacket];

                nbFilesInPacket += 1;
            }

            // We've extracted some files into the tempFileData array. The
            // question is: is this packet full ?
            if (this.nbUploadedFiles + this.nbFilesBeingUploaded
                    + nbFilesInPacket == this.uploadFileDataArray.length) {
                // Ok, we've up to the last file.
                isPacketFinished = true;
            } else if (nbFilesInPacket == this.nbFilesPerRequest) {
                isPacketFinished = true;
            }

            if (isPacketFinished) {
                // The packet is full. Let's copy the temp data to the next
                // packet data.
                this.nextPacket = new UploadFileData[nbFilesInPacket];
                System.arraycopy(tempFileData, 0, this.nextPacket, 0,
                        nbFilesInPacket);
            }
        }

        return this.nextPacket != null;
    }

    /**
     * This method is called each time a new file is ready to upload. It
     * calculates if a new packet of files is ready to upload. It is private, as
     * it may be called only from this class.
     * 
     * @throws JUploadException
     */
    private synchronized void anotherFileIsReady(FileData newlyPreparedFileData)
            throws JUploadException {
        nbPreparedFiles += 1;
        nbBytesReadyForUpload += newlyPreparedFileData.getUploadLength();

        if (!isNextPacketReady()) {
            // perhaps it's ready now, with this file ?
            checkIfNextPacketIsReady();
        }

        // Let's estimate the average size;
        long nbBytesPrepared = 0;
        for (int i = 0; i < this.uploadFileDataArray.length; i += 1) {
            nbBytesPrepared += this.uploadFileDataArray[i].getUploadLength();
        }
        this.estimatedTotalLength = nbBytesPrepared / nbPreparedFiles;
    }

    /**
     * This method is called each time a new file is successfully uploaded. It
     * calculates if a new packet of files is ready to upload. It is public, as
     * upload is done in another thread, whose class maybe in another package.
     * 
     * @param newlyUploadedFileData
     * @throws JUploadException
     */
    public synchronized void anotherFileHasBeenUploaded(
            FileData newlyUploadedFileData) throws JUploadException {
        nbUploadedFiles += 1;
        this.nbFilesBeingUploaded -= 1;
        nbBytesUploadedForCurrentFile = 0;
        nbBytesReadyForUpload -= newlyUploadedFileData.getUploadLength();

        // Let's display some information
        updateProgressBar();

        if (!isNextPacketReady()) {
            // Perhaps conditions for next upload are not ready now that this
            // file is removed from the pool of files to upload?
            checkIfNextPacketIsReady();
        }

        // We should now remove this file from the list of files to upload, to
        // show the user that there is less and less work to do.
        this.filePanel.remove(newlyUploadedFileData);
    }

    /**
     * Returns the next packet of files, for upload, according to the current
     * upload policy.
     * 
     * @return The array of files to upload.
     */
    public synchronized UploadFileData[] getNextPacket() {
        final String infoUploading = this.uploadPolicy
                .getString("infoUploading");

        // If it's the first packet, we noted the current time as the upload
        // start time.
        if (this.nbUploadedFiles == 0) {
            this.uploadStartTime = System.currentTimeMillis();
        }

        if (this.nextPacket == null) {
            return null;
        } else {
            // As the packet has been prepared, we expect ... it's almost being
            // uploaded.Let's write this to the progress bar...
            String msg;
            if (this.nextPacket.length == 1) {
                msg = (this.nbUploadedFiles + 1) + "/"
                        + (this.nextPacket.length);
            } else {
                msg = (this.nbUploadedFiles + 1) + "-"
                        + (this.nbUploadedFiles + this.nextPacket.length) + "/"
                        + (this.nextPacket.length);
            }
            if (!this.stop && (null != this.progressBar)) {
                this.progressBar.setString(String.format(infoUploading, msg));
            }

            UploadFileData[] fileDataTmp = this.nextPacket;
            this.nextPacket = null;
            this.nbFilesBeingUploaded += fileDataTmp.length;

            return fileDataTmp;
        }

    }

    /**
     * Returns the indicator, whether a new packet can be sent to the server or
     * not.
     * 
     * @return Whether or not a new upload request to the server can be done.
     */
    public synchronized boolean isNextPacketReady() {
        return (this.nextPacket != null);
    }

    // //////////////////////////////////////////////////////////////////////////////////////////////
    // /////////////////// PRIVATE METHODS
    // //////////////////////////////////////////////////////////////////////////////////////////////

    private void afterUploadOk() {
        this.uploadPolicy.displayDebug("FileUploadManagerThread: in afterUploadOk()",
                10);
        String svrRet = this.fileUploadThread.getResponseMsg();

        // Let's indicate that upload is finished to the upload panel, so that
        // it refreshes itself.
        this.uploadPanel.onUploadFinished();

        try {
            this.uploadPolicy.afterUpload(this.getUploadException(), svrRet);
        } catch (JUploadException e1) {
            this.uploadPolicy.displayErr(
                    "error in uploadPolicy.afterUpload (JUploadPanel)", e1);
        }

    }

    /**
     * Creates and starts the upload thread. It will wait until the first packet
     * is ready.
     */
    private void createUploadThread() {
        try {
            if (this.uploadPolicy.getPostURL().substring(0, 4).equals("ftp:")) {
                this.fileUploadThread = new FileUploadThreadFTP(
                        this.uploadPolicy, this);
            } else {
                this.fileUploadThread = new FileUploadThreadHTTP(
                        this.uploadPolicy, this);
            }
        } catch (JUploadException e1) {
            // Too bad !
            this.uploadPolicy.displayErr(e1);
        }
        this.fileUploadThread.start();
    }

    /**
     * Initialize the maximum value for the progress bar: 100*the number of
     * files to upload.
     * 
     * @see #updateProgressBar()
     */
    private void initProgressBar() {
        this.progressBar.setMaximum(100 * this.uploadFileDataArray.length);
        this.progressBar.setString("");
    }

    /**
     * Update the progress bar, based on the following data: <DIR>
     * <LI>{@link #nbUploadedFiles}: number of files that have already been
     * updated.
     * <LI>{@link #nbBytesUploadedForCurrentFile}: allows calculation of the
     * upload progress for the current file, based on it total upload length.
     * </DIR>
     * 
     * @throws JUploadException
     */
    private void updateProgressBar() throws JUploadException {
        float percent = 0;

        if (this.nbUploadedFiles < this.uploadFileDataArray.length) {
            percent = (float) this.nbBytesUploadedForCurrentFile
                    / this.uploadFileDataArray[this.nbUploadedFiles]
                            .getUploadLength();
        }
        this.progressBar.setValue(100 * this.nbUploadedFiles
                + (int) (100 * percent));
    }

}
