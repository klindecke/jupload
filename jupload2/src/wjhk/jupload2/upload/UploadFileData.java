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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

import wjhk.jupload2.exception.JUploadException;
import wjhk.jupload2.exception.JUploadExceptionTooBigFile;
import wjhk.jupload2.exception.JUploadIOException;
import wjhk.jupload2.filedata.FileData;
import wjhk.jupload2.policies.UploadPolicy;

class UploadFileData implements FileData {

    /**
     * The {@link FileData} instance that contains all information about the
     * file to upload.
     */
    private FileData fileData = null;

    /**
     * Instance of the fileUploadThread. This allow this class to send feedback
     * to the thread.
     * 
     * @see FileUploadThread#nbBytesUploaded(long)
     */
    private FileUploadThread fileUploadThread = null;

    /**
     * inputStream contains the stream that read from the file to upload. This
     * may be a transformed version of the file (for instance, a compressed
     * one).
     * 
     * @see FileData#getInputStream()
     */
    private InputStream inputStream = null;

    /**
     * The number of bytes to upload, for this file (without the head and tail
     * defined for the HTTP multipart body).
     */
    private long uploadRemainingLength = -1;

    /**
     * The current {@link UploadPolicy}
     */
    private UploadPolicy uploadPolicy = null;

    private MessageDigest digest = null;

    private static final int BUFLEN = 4096;

    private static final byte buffer[] = new byte[BUFLEN];

    // /////////////////////////////////////////////////////////////////////////////////////////////////////
    // //////////////////////////////////// CONSTRUCTOR
    // ///////////////////////////////////////////
    // /////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Standard constructor for the UploadFileData class.
     */
    public UploadFileData(FileData fileDataParam,
            FileUploadThread fileUploadThreadParam,
            UploadPolicy uploadPolicyParam) {
        this.fileData = fileDataParam;
        this.fileUploadThread = fileUploadThreadParam;
        this.uploadPolicy = uploadPolicyParam;
    }

    // /////////////////////////////////////////////////////////////////////////////////////////////////////
    // //////////////////////////////////// PROTECTED METHODS
    // ///////////////////////////////////////////
    // /////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Returns the header for this file, within the http multipart body.
     * 
     * @param fileIndex Index of the file in the array that contains all files
     *            to upload.
     * @param boundary The boundary that separate files in the http multipart
     *            post body.
     * @param chunkPart The numero of the current chunk (from 1 to n)
     * @return The header for this file.
     */
    String getFileHeader(int fileIndex, String boundary,
            @SuppressWarnings("unused")
            int chunkPart) throws JUploadException {
        String filenameEncoding = this.uploadPolicy.getFilenameEncoding();
        String uploadFilename = this.uploadPolicy.getUploadFilename(
                this.fileData, fileIndex);
        StringBuffer sb = new StringBuffer();

        // boundary, POST-variable "mimetype"
        sb.append(boundary);
        sb.append("\r\n");
        sb.append("Content-Disposition: form-data; name=\"mimetype\"");
        sb.append("\r\n");
        sb.append("\r\n");
        sb.append(this.fileData.getMimeType());
        sb.append("\r\n");

        // boundary.
        sb.append(boundary);
        sb.append("\r\n");

        // Content-Disposition.
        sb.append("Content-Disposition: form-data; name=\"").append(
                this.uploadPolicy.getUploadName(this.fileData, fileIndex))
                .append("\"; filename=\"");
        if (filenameEncoding == null) {
            sb.append(uploadFilename);
        } else {
            try {
                this.uploadPolicy.displayDebug("Encoded filename: "
                        + URLEncoder.encode(uploadFilename, filenameEncoding),
                        99);
                sb.append(URLEncoder.encode(uploadFilename, filenameEncoding));
            } catch (UnsupportedEncodingException e) {
                this.uploadPolicy
                        .displayWarn(e.getClass().getName() + ": "
                                + e.getMessage()
                                + " (in UploadFileData.getFileHeader)");
                sb.append(uploadFilename);
            }
        }
        sb.append("\"\r\n");

        // Line 3: Content-Type.
        if (false) // will be a configurable e.g.: transfer-binary
            sb.append("Content-Type: application/octet-stream");
        else
            sb.append("Content-Type: ").append(this.fileData.getMimeType());
        sb.append("\r\n");

        // An empty line to finish the header.
        sb.append("\r\n");
        return sb.toString();
    }

    /**
     * Get the number of files that are still to upload. It is initialized at
     * the creation of the file, by a call to the
     * {@link FileData#getUploadLength()}. <BR>
     * <B>Note:</B> When the upload for this file is finish and you want to
     * send it again (for instance the upload failed, and you want to do a
     * retry), you should not reuse this instance, but, instead, create a new
     * UploadFileData instance.
     * 
     * @return Number of bytes still to upload.
     * @see #getInputStream()
     */
    long getRemainingLength() {
        return this.uploadRemainingLength;
    }

    /**
     * Retrieves the MD5 sum of the recently transfered chunk.
     * @return The corresponding MD5 sum.
     */
    String getMD5() {
        StringBuffer ret = new StringBuffer();
        byte md5sum[] = new byte[32];
        if (this.digest != null)
            md5sum = this.digest.digest();
        for (int i = 0; i < md5sum.length; i++) {
            ret.append(Integer.toHexString((md5sum[i] >> 4) & 0x0f));
            ret.append(Integer.toHexString(md5sum[i] & 0x0f));
        }
        return ret.toString();
    }

    /**
     * This methods writes the file data (see {@link FileData#getInputStream()}
     * to the given outputStream (the output toward the HTTP server).
     * 
     * @param outputStream The stream on which the data is to be written.
     * @param amount The number of bytes to write.
     * @throws JUploadException if an I/O error occurs.
     */
    void uploadFile(OutputStream outputStream, long amount)
            throws JUploadException {

        // We'l update the progess bar every NUM_BYTES bytes, instead of every
        // byte.

        this.uploadPolicy.displayDebug("in UploadFileData.uploadFile (amount:"
                + amount + ", getUploadLength(): " + getUploadLength() + ")",
                30);

        // getInputStream will put a new fileInput in the inputStream attribute,
        // or leave it unchanged if it
        // is not null.
        getInputStream();
        try {
            this.digest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new JUploadException(e);
        }
        while (!this.fileUploadThread.isUploadStopped() && (0 < amount)) {
            int toread = (amount > BUFLEN) ? BUFLEN : (int) amount;
            int towrite = 0;
            try {
                towrite = this.inputStream.read(buffer, 0, toread);
            } catch (IOException e) {
                e.printStackTrace();
                throw new JUploadIOException(e);
            }
            if (towrite > 0) {
                this.digest.update(buffer, 0, towrite);
                try {
                    outputStream.write(buffer, 0, towrite);
                    this.fileUploadThread.nbBytesUploaded(towrite);
                    amount -= towrite;
                    this.uploadRemainingLength -= towrite;
                } catch (IOException e) {
                    e.printStackTrace();
                    throw new JUploadIOException(e);
                }
            }
        }
    }

    /**
     * This method closes the inputstream, and remove the file from the
     * filepanel. Then it calls {@link FileData#afterUpload()}.
     * 
     * @see FileData#afterUpload()
     */
    public void afterUpload() {
        // 1. Close the InputStream
        if (this.inputStream != null) {
            try {
                this.inputStream.close();
            } catch (IOException e) {
                this.uploadPolicy.displayWarn(e.getClass().getName() + ": "
                        + e.getMessage() + " (in UploadFileData.afterUpload()");
            }
            this.inputStream = null;
        }

        // 2. Ask the FileData to release any other locked resource.
        this.fileData.afterUpload();
    }

    /** @see FileData#beforeUpload() */
    public void beforeUpload() throws JUploadException {
        this.fileData.beforeUpload();

        // Calculation of some internal variables.
        this.uploadRemainingLength = this.fileData.getUploadLength();
    }

    /** @see FileData#canRead() */
    public boolean canRead() {
        return this.fileData.canRead();
    }

    /** @see FileData#getDirectory() */
    public String getDirectory() {
        return this.fileData.getDirectory();
    }

    /** @see FileData#getFile() */
    public File getFile() {
        return this.fileData.getFile();
    }

    /** @see FileData#getFileExtension() */
    public String getFileExtension() {
        return this.fileData.getFileExtension();
    }

    /** @see FileData#getFileLength() */
    public long getFileLength() {
        return this.fileData.getFileLength();
    }

    /** @see FileData#getFileName() */
    public String getFileName() {
        return this.fileData.getFileName();
    }

    /** @see FileData#getInputStream() */
    public InputStream getInputStream() throws JUploadException {
        // If you didn't already open the input stream, the remaining length
        // should be non 0.
        if (this.inputStream == null) {
            if (this.uploadRemainingLength <= 0) {
                // Too bad: we already uploaded this file. Perhaps its Ok (a
                // second try?)
                // To avoid this warning, just create a new UploadFileData
                // instance, and not use an already existing one.
                this.uploadPolicy
                        .displayWarn("["
                                + getFileName()
                                + "] UploadFileData.getInputStream(): uploadRemainingLength is <= 0. Trying a new upload ?");
                this.uploadRemainingLength = this.fileData.getUploadLength();
            }
            // Ok, this is the start of upload for this file. Let's get its
            // InputStream.
            this.inputStream = this.fileData.getInputStream();
        }
        return this.inputStream;
    }

    /** @see FileData#getLastModified() */
    public Date getLastModified() {
        return this.fileData.getLastModified();
    }

    /** @see FileData#getMimeType() */
    public String getMimeType() {
        return this.fileData.getMimeType();
    }

    /** @see FileData#getUploadLength() */
    public long getUploadLength() throws JUploadException {
        long uploadLength = this.fileData.getUploadLength();

        // We check the filesize only now: the file to upload may be different
        // from the original file. For instance,
        // a selected picture on the local hard drive may be bigger than
        // maxFileSize, but, as the picture can be
        // resized before upload, the picture to upload may be still be smaller
        // than maxFileSize.
        if (uploadLength > this.uploadPolicy.getMaxFileSize()) {
            throw new JUploadExceptionTooBigFile(this.fileData.getFileName(),
                    this.fileData.getUploadLength(), this.uploadPolicy);
        }

        return uploadLength;
    }

}
