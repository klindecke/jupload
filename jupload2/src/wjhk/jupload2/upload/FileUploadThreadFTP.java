//
// $Id: FileUploadThreadFTP.java 136 2007-05-12 20:15:36 +0000 (sam., 12 mai
// 2007) felfert $
//
// jupload - A file upload applet.
// Copyright 2007 The JUpload Team
//
// Created: 2007-01-01
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

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPConnectionClosedException;
import org.apache.commons.net.ftp.FTPReply;

import wjhk.jupload2.exception.JUploadException;
import wjhk.jupload2.exception.JUploadExceptionUploadFailed;
import wjhk.jupload2.exception.JUploadIOException;
import wjhk.jupload2.policies.UploadPolicy;

/**
 * The FileUploadThreadFTP class is intended to extend the functionality of the
 * JUpload applet and allow it to handle ftp:// addresses. <br>
 * Note: this class is not a V4 of the FTP upload. It is named V4, as it
 * inherits from the {@link FileUploadThread} class. <br>
 * <br>
 * In order to use it, simply change the postURL argument to the applet to
 * contain the appropriate ftp:// link. The format is:
 * 
 * <pre>
 *         ftp://username:password@myhost.com:21/directory
 * </pre>
 * 
 * Where everything but the host is optional. There is another parameter that
 * can be passed to the applet named 'binary' which will set the file transfer
 * mode based on the value. The possible values here are 'true' or 'false'. It
 * was intended to be somewhat intelligent by looking at the file extension and
 * basing the transfer mode on that, however, it was never implemented. Feel
 * free to! Also, there is a 'passive' parameter which also has a value of
 * 'true' or 'false' which sets the connection type to either active or passive
 * mode.
 * 
 * @author Evin Callahan (inheritance from DefaultUploadThread built by
 *         etienne_sf)
 * @author Daystar Computer Services
 * @see FileUploadThread
 * @see DefaultFileUploadThread
 * @version 1.0, 01 Jan 2007 * Update march 2007, etienne_sf Adaptation to match
 *          all JUpload functions: <DIR> <LI>Inheritance from the
 *          {@link FileUploadThread} class, <LI>Use of the UploadFileData class,
 *          <LI>Before upload file preparation, <LI>Upload stop by the user. <LI>
 *          </DIR>
 */
public class FileUploadThreadFTP extends DefaultFileUploadThread {

    // ////////////////////////////////////////////////////////////////////////////////////
    // /////////////////////// PRIVATE ATTRIBUTES
    // ///////////////////////////////////////
    // ////////////////////////////////////////////////////////////////////////////////////

    // ////////////////////////////////////////////////////////////////////////////////////
    // /////////////////////// PRIVATE ATTRIBUTES
    // ///////////////////////////////////////
    // ////////////////////////////////////////////////////////////////////////////////////

    /**
     * The output stream, where the current file should be written. This output
     * stream should not be used. The buffered one is much faster.
     */
    private OutputStream ftpOutputStream = null;

    /**
     * The buffered stream, that the application should use for upload.
     */
    private BufferedOutputStream bufferedOutputStream = null;

    private Matcher uriMatch;

    // the client that does the actual connecting to the server
    private FTPClient ftp = new FTPClient();

    /** FTP user, taken from the postURL applet parameter */
    private String user;

    /** FTP password, taken from the postURL applet parameter */
    private String pass;

    /** FTP target host, taken from the postURL applet parameter */
    private String host;

    /** FTP target port, taken from the postURL applet parameter */
    private String port;

    /**
     * FTP target root folder for the upload, taken from the postURL applet
     * parameter
     */
    private String ftpRootFolder;

    /**
     * Indicates whether the connection to the FTP server is open or not. This
     * allows to connect once on the FTP server, for multiple file upload.
     */
    private boolean bConnected = false;

    /**
     * This pattern defines the groups and pattern of the ftp syntax.
     */
    public final Pattern ftpPattern = Pattern
            .compile("^ftp://(([^:]+):([^\\@]+)\\@)?([^/:]+):?([0-9]+)?(/(.*))?$");

    /**
     * Creates a new instance. Performs the connection to the server based on
     * the matcher created in the main.
     * 
     * @param uploadPolicy
     * @param fileUploadManagerThread
     * 
     * @throws JUploadException
     * @throws IllegalArgumentException if any error occurs. message is error
     */
    public FileUploadThreadFTP(UploadPolicy uploadPolicy,
            FileUploadManagerThread fileUploadManagerThread)
            throws JUploadException {
        super(uploadPolicy, fileUploadManagerThread);
        this.uploadPolicy.displayDebug("  Using " + this.getClass().getName(),
                30);

        // Some coherence checks, for parameter given to the applet.

        // stringUploadSuccess: unused in FTP mode. Must be null.
        if (uploadPolicy.getStringUploadSuccess() != null) {
            uploadPolicy
                    .displayWarn("FTP mode: stringUploadSuccess parameter ignored (forced to null)");
            uploadPolicy.setProperty(UploadPolicy.PROP_STRING_UPLOAD_SUCCESS,
                    null);
        }

        // nbFilesPerRequest: must be 1 in FTP mode.
        if (uploadPolicy.getNbFilesPerRequest() != 1) {
            uploadPolicy
                    .displayWarn("FTP mode: nbFilesPerRequest parameter ignored (forced to 1)");
            uploadPolicy.setProperty(UploadPolicy.PROP_NB_FILES_PER_REQUEST,
                    "1");
        }

        // maxChunkSize: must be unlimited (no chunk management in FTP mode).
        if (uploadPolicy.getMaxChunkSize() != Long.MAX_VALUE) {
            uploadPolicy
                    .displayWarn("FTP mode: maxChunkSize parameter ignored (forced to Long.MAX_VALUE)");
            uploadPolicy.setProperty(UploadPolicy.PROP_MAX_CHUNK_SIZE, Long
                    .toString(Long.MAX_VALUE));
        }
    }

    /** @see DefaultFileUploadThread#beforeRequest() */
    @Override
    void beforeRequest() throws JUploadException {

        // If we're connected, we need to check the connection.
        if (this.bConnected) {
            // Let's check the connection is still Ok.
            try {
                this.ftp.sendNoOp();
            } catch (FTPConnectionClosedException eClosed) {
                // Let's forget this connection.
                bConnected = false;
            } catch (IOException e) {
                throw new JUploadIOException(e.getClass().getName()
                        + " while checking FTP connection to the server", e);
            }
        }

        // If not already connected ... we connect to the server.
        if (!this.bConnected) {
            // Let's connect to the FTP server.
            String url = this.uploadPolicy.getPostURL();
            this.uriMatch = this.ftpPattern.matcher(url);
            if (!this.uriMatch.matches()) {
                throw new JUploadException("invalid URI: " + url);
            }
            this.user = this.uriMatch.group(2) == null ? "anonymous"
                    : this.uriMatch.group(2);
            this.pass = this.uriMatch.group(3) == null ? "JUpload"
                    : this.uriMatch.group(3);
            this.host = this.uriMatch.group(4); // no default server
            this.port = this.uriMatch.group(5) == null ? "21" : this.uriMatch
                    .group(5);
            this.ftpRootFolder = (this.uriMatch.group(7) == null) ? "/" : "/"
                    + this.uriMatch.group(7);
            // The last character must be a slash
            if (!this.ftpRootFolder.endsWith("/")) {
                this.ftpRootFolder += "/";
            }

            // do connect.. any error will be thrown up the chain
            try {
                this.ftp.setDefaultPort(Integer.parseInt(this.port));
                this.ftp.connect(this.host);
                this.uploadPolicy.displayDebug("Connected to " + this.host, 10);
                this.uploadPolicy.displayDebug(this.ftp.getReplyString(), 80);

                if (!FTPReply.isPositiveCompletion(this.ftp.getReplyCode()))
                    throw new JUploadException("FTP server refused connection.");

                // given the login information, do the login
                this.ftp.login(this.user, this.pass);
                this.uploadPolicy.displayDebug(this.ftp.getReplyString(), 80);

                if (!FTPReply.isPositiveCompletion(this.ftp.getReplyCode()))
                    throw new JUploadException("Invalid username / password");

                // if configured to, we create all target subfolders, on the
                // server side.
                if (uploadPolicy.getFtpCreateDirectoryStructure()) {
                    createDirectoryStructure();
                }

                if (!FTPReply.isPositiveCompletion(this.ftp.getReplyCode()))
                    throw new JUploadException("Invalid directory specified");

                this.bConnected = true;
            } catch (JUploadException jue) {
                // No special action, we keep the exception untouched
                throw jue;
            } catch (IOException ioe) {
                throw new JUploadIOException(ioe.getClass().getName()
                        + "Could not connect to server (" + ioe.getMessage()
                        + ")", ioe);
            } catch (Exception e) {
                throw new JUploadException(e.getClass().getName()
                        + "Could not connect to server (" + e.getMessage()
                        + ")", e);
            }

            // now do the same for the passive/active parameter
            if (this.uploadPolicy.getFtpTransfertPassive()) {
                this.ftp.enterLocalPassiveMode();
            } else {
                this.ftp.enterLocalActiveMode();
            }

        } // if(!bConnected)
    }

    /** @see DefaultFileUploadThread#afterFile(int) */
    @Override
    void afterFile(int index) {
        // Nothing to do
    }

    /** @see DefaultFileUploadThread#beforeFile(int) */
    @Override
    void beforeFile(int index) throws JUploadException {
        try {
            // if configured to, we go to the relative sub-folder of the current
            // file, or on the root of the postURL.
            if (uploadPolicy.getFtpCreateDirectoryStructure()) {
                this.ftp.changeWorkingDirectory(this.ftpRootFolder
                        + this.filesToUpload[index].getRelativeDir());
                this.uploadPolicy.displayDebug(this.ftp.getReplyString(), 80);
            } else {
                this.ftp.changeWorkingDirectory(this.ftpRootFolder);
                this.uploadPolicy.displayDebug(this.ftp.getReplyString(), 80);
            }

            // FIXME To in beforeRequest, just after connection.

            setTransferType(index);
            // just in case, delete anything that exists

            // No delete, as the user may not have the right for that. We use,
            // later, the store command:
            // If the file already exists, it will be replaced.
            // ftp.deleteFile(filesToUpload[index].getFileName());

            // Let's open the stream for this file.
            this.ftpOutputStream = this.ftp
                    .storeFileStream(this.filesToUpload[index].getFileName());
            // The upload is done through a BufferedOutputStream. This speed up
            // the upload in an unbelievable way ...
            this.bufferedOutputStream = new BufferedOutputStream(
                    this.ftpOutputStream);
        } catch (IOException e) {
            throw new JUploadException(e);
        }
    }

    /** @see DefaultFileUploadThread#cleanAll() */
    @Override
    void cleanAll() {
        try {
            if (this.ftp.isConnected()) {
                this.ftp.disconnect();
                this.uploadPolicy.displayDebug("disconnected", 50);
            }
        } catch (IOException e) {
            // then we arent connected
            this.uploadPolicy.displayDebug("Not connected", 50);
        } finally {
            this.ftpOutputStream = null;
            this.bufferedOutputStream = null;
        }
    }

    /** @see DefaultFileUploadThread#cleanRequest() */
    @Override
    void cleanRequest() throws JUploadException {
        if (this.bufferedOutputStream != null) {
            try {
                this.bufferedOutputStream.close();
                this.ftpOutputStream.close();
                if (!this.ftp.completePendingCommand()) {
                    throw new JUploadExceptionUploadFailed(
                            "ftp.completePendingCommand() returned false");
                }
            } catch (IOException e) {
                throw new JUploadException(e);
            } finally {
                this.bufferedOutputStream = null;
            }
        }
    }

    /**
     * @throws JUploadIOException
     * @see DefaultFileUploadThread#finishRequest()
     */
    @Override
    int finishRequest() throws JUploadException {
        try {
            getOutputStream().flush();
            return 200;
        } catch (IOException ioe) {
            throw new JUploadIOException("FileUploadThreadFTP.finishRequest()",
                    ioe);
        } catch (Exception e) {
            // When the user may not override an existing file, I got a
            // NullPointerException. Let's trap all errors here.
            throw new JUploadException(
                    "FileUploadThreadFTP.finishRequest()  (check the user permission on the server)",
                    e);
        }
    }

    /** @see DefaultFileUploadThread#getAdditionnalBytesForUpload(int) */
    @Override
    long getAdditionnalBytesForUpload(int indexFile) {
        // Default: no additional byte.
        return 0;
    }

    /** @see DefaultFileUploadThread#getOutputStream() */
    @Override
    OutputStream getOutputStream() {
        return this.bufferedOutputStream;
    }

    /** @see DefaultFileUploadThread#startRequest(long, boolean, int, boolean) */
    @Override
    void startRequest(long contentLength, boolean bChunkEnabled, int chunkPart,
            boolean bLastChunk) {
        // Nothing to do
    }

    /**
     * Will set the binary/ascii value based on the parameters to the applet.
     * This could be done by file extension too but it is not implemented.
     * 
     * @param index The index of the file that we want to upload, in the array
     *            of files to upload.
     * @throws IOException if an error occurs while setting mode data
     */
    private void setTransferType(int index) throws JUploadIOException {
        try {
            // read the value given from the user
            if (this.uploadPolicy.getFtpTransfertBinary()) {
                this.ftp.setFileType(FTP.BINARY_FILE_TYPE);
            } else {
                this.ftp.setFileType(FTP.ASCII_FILE_TYPE);
            }
        } catch (IOException ioe) {
            throw new JUploadIOException(
                    "Cannot set transfert binary or ascii mode (binary: "
                            + this.uploadPolicy.getFtpTransfertBinary() + ")",
                    ioe);
        }
    }

    /**
     * Create all relative sub-directories, so the structure on the server
     * reflects the structure of the uploaded files.
     * 
     * @throws JUploadIOException When an error occurs during folder creation
     */
    // A tester
    private void createDirectoryStructure() throws JUploadIOException {
        SortedSet<String> foldersToCreate = new TreeSet<String>();
        String folderName;
        String intermediateFolderName;
        StringTokenizer st;

        // 1) Let's find all folders and sub-folders we'll have to create.
        for (int i = 0; i < this.filesToUpload.length
                && !this.fileUploadManagerThread.isUploadStopped(); i++) {
            folderName = this.filesToUpload[i].getRelativeDir();
            folderName = folderName.replaceAll("\\\\", "/");
            // Do we already have this folder ?
            if (!foldersToCreate.contains(folderName)) {
                // We add this folder, and all missing intermediate ones
                st = new StringTokenizer(folderName, "/");
                intermediateFolderName = this.ftpRootFolder;
                while (st.hasMoreTokens()) {
                    intermediateFolderName += st.nextToken() + "/";
                    if (!foldersToCreate.contains(intermediateFolderName)) {
                        this.uploadPolicy.displayDebug(
                                "FTP structure identification: Adding subfolder "
                                        + intermediateFolderName, 80);
                        foldersToCreate.add(intermediateFolderName);
                    }
                }
            }
        }

        // 2) Let's create theses folders.
        try {
            String folder;
            for (Iterator<String> it = foldersToCreate.iterator(); it.hasNext();) {
                folder = it.next();

                // The folder is in the list of folder to create, created from
                // the file list.
                // We first check if the folder already exist.
                this.ftp.changeWorkingDirectory(folder);
                if (FTPReply.isPositiveCompletion(this.ftp.getReplyCode())) {
                    this.uploadPolicy.displayDebug("Folder " + folder
                            + " already exist", 80);
                } else {
                    // We can not guess if it's because the folder
                    // doesn't exist, or if it's a 'real' error.
                    // Let's try to create the folder.
                    this.ftp.mkd(folder);
                    this.uploadPolicy.displayDebug("Folder " + folder
                            + " created", 80);
                    if (!FTPReply.isPositiveCompletion(this.ftp.getReplyCode())) {
                        throw new JUploadIOException(
                                "Error while creating folder '"
                                        + folder
                                        + "' ("
                                        + this.ftp.getReplyString().replaceAll(
                                                "\r\n", "") + ")");
                    }
                }
            }
        } catch (IOException ioe) {
            throw new JUploadIOException(ioe.getClass().getName()
                    + " in FileUploadThreadFTP.createDirectoryStructure()", ioe);
        }
    }
}
