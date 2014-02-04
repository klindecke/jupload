//
// $Id$
//
// jupload - A file upload applet.
// Copyright 2007 The JUpload Team
//
// Created: 2006-11-20
// Creator: etienne_sf
// Last modified: $Date$
//
// This program is free software; you can redistribute it and/or modify it under
// the terms of the GNU General Public License as published by the Free Software
// Foundation; either version 2 of the License, or (at your option) any later
// version. This program is distributed in the hope that it will be useful, but
// WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
// FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
// details. You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software Foundation, Inc.,
// 675 Mass Ave, Cambridge, MA 02139, USA.

package wjhk.jupload2.upload;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

import wjhk.jupload2.exception.JUploadException;
import wjhk.jupload2.exception.JUploadExceptionTooBigFile;
import wjhk.jupload2.exception.JUploadIOException;
import wjhk.jupload2.exception.JUploadInterrupted;
import wjhk.jupload2.filedata.FileData;
import wjhk.jupload2.policies.UploadPolicy;
import wjhk.jupload2.upload.helper.ByteArrayEncoder;

/**
 * This class implements the FileData interface, and is responsible to do the
 * actual upload of the files.
 *
 * @author etienne_sf
 *
 */
public class UploadFileData implements FileData {

    /**
     * The {@link FileData} instance that contains all information about the
     * file to upload.
     */
    private FileData fileData = null;

    /**
     * Instance of the fileUploadManagerThread. This allow this class to send
     * feedback to the thread.
     *
     * @see FileUploadManagerThread#nbBytesUploaded(long)
     */
    private FileUploadManagerThread fileUploadManagerThread = null;

    /**
     * inputStream contains the stream that read from the file to upload. This
     * may be a transformed version of the file (for instance, a compressed
     * one).
     *
     * @see FileData#getInputStream()
     */
    private InputStream inputStream = null;

    /**
     * The upload lenght of the current file. It is stored to: <DIR> <LI>Avoid
     * multiple calls to the underlying FileData.getUploadLength() method <LI>
     * Keep the upload length, after the call to {@link #afterUpload()} </DIR>
     */
    private long uploadLength = -1;

    /**
     * The number of bytes to upload, for this file (without the head and tail
     * defined for the HTTP multipart body).
     */
    private long uploadRemainingLength = -1;

    /**
     * The current {@link UploadPolicy}
     */
    private UploadPolicy uploadPolicy = null;

    private final static int BUFLEN = 4096;

    /**
     * This field is no more static, as we could decide to upload two files
     * simultaneously.
     */
    private final byte readBuffer[] = new byte[BUFLEN];

    // /////////////////////////////////////////////////////////////////////////////////////////////////////
    // //////////////////////////////////// CONSTRUCTOR
    // ///////////////////////////////////////////
    // /////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Standard constructor for the UploadFileData class.
     *
     * @param fileDataParam The file data the this instance must transmist.
     * @param fileUploadThreadParam The current instance of
     *            {@link FileUploadThread}
     * @param uploadPolicyParam The current upload policy, instance of
     *            {@link UploadPolicy}
     */
    public UploadFileData(FileData fileDataParam,
            FileUploadManagerThread fileUploadThreadParam,
            UploadPolicy uploadPolicyParam) {
        if (fileDataParam == null) {
            throw new NullPointerException(
                    "fileData is null in UploadFileData constructor");
        }
        this.fileData = fileDataParam;
        this.fileUploadManagerThread = fileUploadThreadParam;
        this.uploadPolicy = uploadPolicyParam;
    }

    /**
     * Get the number of files that are still to upload. It is initialized at
     * the creation of the file, by a call to the
     * {@link FileData#getUploadLength()}. <BR>
     * <B>Note:</B> When the upload for this file is finish and you want to send
     * it again (for instance the upload failed, and you want to do a retry),
     * you should not reuse this instance, but, instead, create a new
     * UploadFileData instance.
     *
     * @return Number of bytes still to upload.
     * @see #getInputStream()
     */
    long getRemainingLength() {
        return this.uploadRemainingLength;
    }

    /**
     * Retrieves the MD5 sum of the file.<BR>
     * <U>Caution:</U> since 3.3.0, this method has been rewrited. The file is
     * now parsed once within this method. This allows proper calculation of the
     * file head and tail, before upload.
     *
     * @return The corresponding MD5 sum.
     */
    String getMD5() throws JUploadException {
        StringBuffer ret = new StringBuffer();
        MessageDigest digest = null;
        byte md5Buffer[] = new byte[BUFLEN];
        int nbBytes;

        // Calculation of the MD5 sum. Now done before upload, to prepare the
        // file head.
        // This makes the file being parsed two times: once before upload, and
        // once for the actual upload
        InputStream md5InputStream = this.fileData.getInputStream();
        try {
            digest = MessageDigest.getInstance("MD5");
            while ((nbBytes = md5InputStream.read(md5Buffer, 0, BUFLEN)) > 0) {
                digest.update(md5Buffer, 0, nbBytes);
            }
            md5InputStream.close();
        } catch (IOException e) {
            throw new JUploadIOException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new JUploadException(e);
        }

        // Now properly format the md5 sum.
        byte md5sum[] = new byte[32];
        if (digest != null)
            md5sum = digest.digest();
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
     * @throws JUploadInterrupted Thrown when an interruption of the thread is
     *             detected.
     */
    void uploadFile(OutputStream outputStream, long amount)
            throws JUploadException, JUploadInterrupted {
        this.uploadPolicy.displayDebug("in UploadFileData.uploadFile (amount:"
                + amount + ", getUploadLength(): " + getUploadLength() + ")",
                30);

        // getInputStream will put a new fileInput in the inputStream attribute,
        // or leave it unchanged if it is not null.
        getInputStream();

        while (amount > 0 && !this.fileUploadManagerThread.isUploadStopped()) {
            // Are we interrupted ?
            if (Thread.interrupted()) {
                throw new JUploadInterrupted(getClass().getName()
                        + ".uploadFile [" + this.getFileName() + "]",
                        this.uploadPolicy);
            }

            int toread = (amount > BUFLEN) ? BUFLEN : (int) amount;
            int towrite = 0;

            try {
                towrite = this.inputStream.read(this.readBuffer, 0, toread);
            } catch (IOException e) {
                throw new JUploadIOException(e);
            }
            if (towrite > 0) {
                try {
                    outputStream.write(this.readBuffer, 0, towrite);
                    this.fileUploadManagerThread.nbBytesUploaded(towrite);
                    amount -= towrite;
                    this.uploadRemainingLength -= towrite;

                    // For debug reason, I may need to simulate upload, that are
                    // on a real network. We then slow down the upload. This can
                    // occurs only when given a 'high' debugLevel (higher than
                    // what can be set with the applet GUI.
                    if (this.uploadPolicy.getDebugLevel() > 100) {
                        try {
                            Thread.sleep(20);
                        } catch (InterruptedException e) {
                            // Nothing to do. We'll just take a look at the loop
                            // condition.
                        }
                    }
                } catch (IOException ioe) {
                    throw new JUploadIOException(this.getClass().getName()
                            + ".uploadFile()", ioe);
                } catch (Exception e) {
                    // When the user may not override an existing file, I got a
                    // NullPointerException. Let's trap all errors here.
                    throw new JUploadException(
                            this.getClass().getName()
                                    + ".uploadFile()  (check the user permission on the server)",
                            e);
                }
            }
        }// while
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

    /** {@inheritDoc} */
    public void appendFileProperties(ByteArrayEncoder bae, int index)
            throws JUploadIOException {
        this.fileData.appendFileProperties(bae, index);
    }

    /** {@inheritDoc} */
    public void beforeUpload() throws JUploadException {
        this.fileData.beforeUpload();

        // Calculation of some internal variables.
        this.uploadRemainingLength = this.fileData.getUploadLength();
    }

    /** {@inheritDoc} */
    public boolean canRead() {
        return this.fileData.canRead();
    }

    /** {@inheritDoc} */
    public String getDirectory() {
        return this.fileData.getDirectory();
    }

    /** {@inheritDoc} */
    public File getFile() {
        return this.fileData.getFile();
    }

    /** {@inheritDoc} */
    public String getFileExtension() {
        return this.fileData.getFileExtension();
    }

    /** {@inheritDoc} */
    public long getFileLength() {
        return this.fileData.getFileLength();
    }

    /** {@inheritDoc} */
    public String getFileName() {
        return this.fileData.getFileName();
    }

    /** {@inheritDoc} */
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

    /** {@inheritDoc} */
    public Date getLastModified() {
        return this.fileData.getLastModified();
    }

    /** {@inheritDoc} */
    public String getMimeType() {
        return this.fileData.getMimeType();
    }

    /** {@inheritDoc} */
    public String getRelativeDir() {
        return this.fileData.getRelativeDir();
    }

    /**
     * Retrieves the file name, that should be used in the server application.
     * Default is to send the original filename.
     *
     * @param index The index of this file in the current request to the server.
     * @return The real file name. Not used in FTP upload.
     * @throws JUploadException Thrown when an error occurs.
     *
     * @see UploadPolicy#getUploadFilename(FileData, int)
     */
    public String getUploadFilename(int index) throws JUploadException {
        return this.uploadPolicy.getUploadFilename(this.fileData, index);
    }

    /**
     * Retrieves the upload file name, that should be sent to the server. It's
     * the technical name used to retrieve the file content. Default is File0,
     * File1... This method just calls the
     * {@link UploadPolicy#getUploadFilename(FileData, int)} method.
     *
     * @param index The index of this file in the current request to the server.
     * @return The technical upload file name. Not used in FTP upload.
     * @throws JUploadException
     *
     * @see UploadPolicy#getUploadName(FileData, int)
     */
    public String getUploadName(int index) throws JUploadException {
        return this.uploadPolicy.getUploadName(this.fileData, index);
    }

    /**
     * This methods stores locally the upload length. So, on the contrary of the
     * {@link FileData} interface, this method may be called after
     * {@link #afterUpload()}, at one condition: that it has been called once
     * before {@link #afterUpload()} is called.
     *
     * @see FileData#getUploadLength()
     */
    public long getUploadLength() throws JUploadException {
        if (this.uploadLength < 0) {
            this.uploadLength = this.fileData.getUploadLength();
        }
        // We check the filesize only now: the file to upload may be different
        // from the original file. For instance,
        // a selected picture on the local hard drive may be bigger than
        // maxFileSize, but, as the picture can be
        // resized before upload, the picture to upload may be still be smaller
        // than maxFileSize.
        if (this.uploadLength > this.uploadPolicy.getMaxFileSize()) {
            throw new JUploadExceptionTooBigFile(this.fileData.getFileName(),
                    this.fileData.getUploadLength(), this.uploadPolicy);
        }

        return this.uploadLength;
    }

    /** {@inheritDoc} */
    public boolean isPreparedForUpload() {
        return this.fileData.isPreparedForUpload();
    }
}
