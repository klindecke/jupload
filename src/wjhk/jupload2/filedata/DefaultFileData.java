/*
 * Created on 21 avr. 2006
 */
package wjhk.jupload2.filedata;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.Date;
import java.util.Properties;

import wjhk.jupload2.exception.JUploadException;
import wjhk.jupload2.exception.JUploadExceptionTooBigFile;
import wjhk.jupload2.exception.JUploadIOException;
import wjhk.jupload2.policies.DefaultUploadPolicy;
import wjhk.jupload2.policies.UploadPolicy;

/**
 * This class contains all data and methods for a file to upload. The current
 * {@link wjhk.jupload2.policies.UploadPolicy} contains the necessary parameters
 * to personalize the way files must be handled. <BR>
 * <BR>
 * This class is the default FileData implementation. It gives the default
 * behaviour, and is used by {@link DefaultUploadPolicy}. It provides standard
 * control on the files choosen for upload.
 * 
 * @see FileData
 * @author Etienne Gauthier
 */
public class DefaultFileData implements FileData {

    /**
     * The current upload policy.
     */
    UploadPolicy uploadPolicy;

    /**
     * the mime type list, coming from: http://www.mimetype.org/ Thanks to them!
     */
    public static Properties mimeTypes = null;

    // ///////////////////////////////////////////////////////////////////////////////////////////////////////
    // /////////////////////// Protected attributes
    // /////////////////////////////////////////////////////
    // ///////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Mime type of the file. It will be written in the upload HTTP request.
     */
    protected String mimeType = "application/octet-stream";

    // ///////////////////////////////////////////////////////////////////////////////////////////////////////
    // /////////////////////// Private attributes
    // ////////////////////////////////////////////////////////
    // ///////////////////////////////////////////////////////////////////////////////////////////////////////

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

        // Let's load the mime types list.
        if (mimeTypes == null) {
            mimeTypes = new Properties();
            try {
                mimeTypes.load(getClass().getResourceAsStream(
                        "/conf/mimetypes.properties"));
            } catch (IOException e) {
                uploadPolicy.displayWarn("Unable to load the mime types list: "
                        + e.getMessage());
                mimeTypes = null;
            }
        }

        // Let
        this.mimeType = mimeTypes.getProperty(getFileExtension().toLowerCase());
        if (this.mimeType == null) {
            this.mimeType = "application/octet-stream";
        }
    }

    /** @see FileData#beforeUpload() */
    public void beforeUpload() throws JUploadException {
        // Default : we check that the file is smalled than the maximum upload
        // size.
        if (getUploadLength() > this.uploadPolicy.getMaxFileSize()) {
            throw new JUploadExceptionTooBigFile(getFileName(),
                    getUploadLength(), this.uploadPolicy);
        }
    }

    /** @see FileData#getUploadLength() */
    @SuppressWarnings("unused")
    public long getUploadLength() throws JUploadException {
        return this.file.length();
    }

    /** @see FileData#afterUpload() */
    public void afterUpload() {
        // Nothing to do here
    }

    /** @see FileData#getInputStream() */
    public InputStream getInputStream() throws JUploadException {
        // Standard FileData : we read the file.
        try {
            return new FileInputStream(this.file);
        } catch (FileNotFoundException e) {
            throw new JUploadIOException(e);
        }
    }

    /** @see FileData#getFileName() */
    public String getFileName() {
        return this.file.getName();
    }

    /** @see FileData#getFileExtension() */
    public String getFileExtension() {
        String name = this.file.getName();
        return name.substring(name.lastIndexOf('.') + 1);
    }

    /** @see FileData#getFileLength() */
    public long getFileLength() {
        return this.file.length();
    }

    /** @see FileData#getLastModified() */
    public Date getLastModified() {
        return new Date(this.file.lastModified());
    }

    /** @see FileData#getDirectory() */
    public String getDirectory() {
        return this.file.getAbsoluteFile().getParent();
    }

    /** @see FileData#getMimeType() */
    public String getMimeType() {
        return this.mimeType;
    }

    /** @see FileData#canRead() */
    public boolean canRead() {
        return this.file.canRead();
    }

    /** @see FileData#getFile() */
    public File getFile() {
        return this.file;
    }
}
