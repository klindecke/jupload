package wjhk.jupload2.test;

import wjhk.jupload2.exception.JUploadException;
import wjhk.jupload2.policies.UploadPolicy;
import wjhk.jupload2.upload.FileUploadManagerThread;
import wjhk.jupload2.upload.FileUploadThread;
import wjhk.jupload2.upload.UploadFileData;

/**
 * This class allows easy construction of non-active instances of
 * FileUploadThread. It is used to execute unit tests on
 * {@link FileUploadManagerThread}
 * 
 * @author etienne_sf
 */
public class FileUploadThreadTestSuccess extends Thread implements
        FileUploadThread {

    UploadPolicy uploadPolicy = null;

    FileUploadManagerThread fileUploadManagerThread = null;

    UploadFileData[] filesToUpload = null;

    /**
     * @param uploadPolicy
     */
    public FileUploadThreadTestSuccess(UploadPolicy uploadPolicy) {
        this.uploadPolicy = uploadPolicy;
    }

    /**
     * @see java.lang.Thread#run()
     */
    /**
     * This method loops on the {@link FileUploadManagerThread#getNextPacket()}
     * method, until a set of files is ready. Then, it calls the doUpload()
     * method, to send these files to the server.
     */
    @Override
    final public void run() {
        try {
            // We'll stop the upload if an error occurs. So the try/catch is
            // outside the while.
            while (!this.fileUploadManagerThread.isUploadFinished()) {
                // If a packet is ready, we take it into account. Otherwise, we
                // wait for a new packet.
                this.filesToUpload = this.fileUploadManagerThread
                        .getNextPacket();
                if (this.filesToUpload != null) {
                    // Let's simulate the upload.
                    // Then, upload each file.
                    for (int i = 0; i < this.filesToUpload.length; i++) {
                        // We simulate the UploadFileData.uploadFile() behavior.
                        this.fileUploadManagerThread
                                .nbBytesUploaded(this.filesToUpload[i]
                                        .getFileLength());
                        // Ok, the file has been sent (hum, almost!)
                        this.fileUploadManagerThread
                                .anotherFileHasBeenSent(this.filesToUpload[i]);
                    }
                    this.fileUploadManagerThread
                            .currentRequestIsFinished(this.filesToUpload);
                } else {
                    try {
                        // We wait a little. If a file is prepared in the
                        // meantime, this thread is notified. The wait duration,
                        // is just to be sure to go and see if there is still
                        // some work from time to time.
                        sleep(200);
                    } catch (InterruptedException e) {
                        // Nothing to do. We'll just take a look at the loop
                        // condition.
                    }
                }
            }// while

        } catch (JUploadException e) {
            this.fileUploadManagerThread.setUploadException(e);
        }
    }// run

    /** {@inheritDoc} */
    public void close() {
        // No action
    }

    /** {@inheritDoc} */
    public String getResponseMsg() {
        return this.uploadPolicy.getStringUploadSuccess();
    }

    /** {@inheritDoc} */
    public void setFileUploadThreadManager(
            FileUploadManagerThread fileUploadManagerThread) {
        this.fileUploadManagerThread = fileUploadManagerThread;
    }

}
