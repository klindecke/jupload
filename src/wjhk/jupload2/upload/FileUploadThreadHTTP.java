//
// $Id$
//
// jupload - A file upload applet.
// Copyright 2007 The JUpload Team
//
// Created: 2007-03-07
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

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;

import wjhk.jupload2.exception.JUploadException;
import wjhk.jupload2.exception.JUploadIOException;
import wjhk.jupload2.policies.UploadPolicy;
import wjhk.jupload2.upload.helper.ByteArrayEncoder;
import wjhk.jupload2.upload.helper.ByteArrayEncoderHTTP;
import wjhk.jupload2.upload.helper.HTTPConnectionHelper;

/**
 * This class implements the file upload via HTTP POST request.
 * 
 * @author etienne_sf
 * @version $Revision$
 */
public class FileUploadThreadHTTP extends DefaultFileUploadThread {

    /**
     * The current connection helper. No initialization now: we need to wait for
     * the startRequest method, to have all needed information.
     */
    private HTTPConnectionHelper connectionHelper = null;

    /**
     * local head within the multipart post, for each file. This is
     * precalculated for all files, in case the upload is not chunked. The heads
     * length are counted in the total upload size, to check that it is less
     * than the maxChunkSize. tails are calculated once, as they depend not of
     * the file position in the upload.
     */
    private ByteArrayEncoder heads[] = null;

    /**
     * same as heads, for the ... tail in the multipart post, for each file. But
     * tails depend on the file position (the boundary is added to the last
     * tail). So it's to be calculated for each upload.
     */
    private ByteArrayEncoder tails[] = null;

    /**
     * Creates a new instance.
     * 
     * @param uploadPolicy The policy to be applied.
     * @param fileUploadManagerThread
     */
    public FileUploadThreadHTTP(UploadPolicy uploadPolicy,
            FileUploadManagerThread fileUploadManagerThread) {
        super(uploadPolicy, fileUploadManagerThread);
        this.uploadPolicy.displayDebug("  Using " + this.getClass().getName(), 30);

        uploadPolicy.displayDebug("Upload done by using the "
                + getClass().getName() + " class", 30);
        // Name the thread (useful for debugging)
        setName("FileUploadThreadHTTP");
        this.connectionHelper = new HTTPConnectionHelper(uploadPolicy);
    }

    /** @see DefaultFileUploadThread#beforeRequest(int, int) */
    @Override
    void beforeRequest() throws JUploadException {
        setAllHead(this.connectionHelper.getBoundary());
        setAllTail(this.connectionHelper.getBoundary());
    }

    /** @see DefaultFileUploadThread#getAdditionnalBytesForUpload(int) */
    @Override
    long getAdditionnalBytesForUpload(int index) throws JUploadIOException {
        return this.heads[index].getEncodedLength()
                + this.tails[index].getEncodedLength();
    }

    /** @see DefaultFileUploadThread#afterFile(int) */
    @Override
    void afterFile(int index) throws JUploadIOException {
        this.connectionHelper.append(this.tails[index].getEncodedByteArray());
        this.uploadPolicy.displayDebug("--- filetail start (len="
                + this.tails[index].getEncodedLength() + "):", 70);
        this.uploadPolicy.displayDebug(
                quoteCRLF(this.tails[index].getString()), 70);
        this.uploadPolicy.displayDebug("--- filetail end", 70);
    }

    /** @see DefaultFileUploadThread#beforeFile(int) */
    @Override
    void beforeFile(int index) throws JUploadException {
        // heads[i] contains the header specific for the file, in the multipart
        // content.
        // It is initialized at the beginning of the run() method. It can be
        // override at the beginning of this loop, if in chunk mode.
        try {
            this.connectionHelper.append(this.heads[index]
                    .getEncodedByteArray());

            // Debug output: always called, so that the debug file is correctly
            // filled.
            this.uploadPolicy.displayDebug("--- fileheader start (len="
                    + this.heads[index].getEncodedLength() + "):", 70);
            this.uploadPolicy.displayDebug(quoteCRLF(this.heads[index]
                    .getString()), 70);
            this.uploadPolicy.displayDebug("--- fileheader end", 70);
        } catch (Exception e) {
            throw new JUploadException(e);
        }
    }

    /** @see DefaultFileUploadThread#cleanAll() */
    @SuppressWarnings("unused")
    @Override
    void cleanAll() throws JUploadException {
        // Nothing to do in HTTP mode.
    }

    /** @see DefaultFileUploadThread#cleanRequest() */
    @Override
    void cleanRequest() throws JUploadException {
        try {
            this.connectionHelper.dispose();
        } catch (JUploadIOException e) {
            this.uploadPolicy.displayErr(this.uploadPolicy
                    .getString("errDuringUpload"), e);
            throw e;
        }
    }

    @Override
    int finishRequest() throws JUploadException {
        if (this.uploadPolicy.getDebugLevel() > 100) {
            // Let's have a little time to check the upload messages written on
            // the progress bar.
            try {
                Thread.sleep(400);
            } catch (InterruptedException e) {
            }
        }
        int status = this.connectionHelper.readHttpResponse();
        setResponseMsg(this.connectionHelper.getResponseMsg());
        setResponseBody(this.connectionHelper.getResponseBody());
        return status;
    }

    /**
     * @see DefaultFileUploadThread#getResponseBody()
     * @Override String getResponseBody() { return
     *           this.sbHttpResponseBody.toString(); }
     */

    /** @see DefaultFileUploadThread#getOutputStream() */
    @SuppressWarnings("unused")
    @Override
    OutputStream getOutputStream() throws JUploadException {
        return this.connectionHelper.getOutputStream();
    }

    /** @see DefaultFileUploadThread#startRequest(long, boolean, int, boolean) */
    @Override
    void startRequest(long contentLength, boolean bChunkEnabled, int chunkPart,
            boolean bLastChunk) throws JUploadException {

        try {
            String chunkHttpParam = "jupart=" + chunkPart + "&jufinal="
                    + (bLastChunk ? "1" : "0");
            this.uploadPolicy.displayDebug("chunkHttpParam: " + chunkHttpParam,
                    30);

            URL url = new URL(this.uploadPolicy.getPostURL());

            // Add the chunking query params to the URL if there are any
            if (bChunkEnabled) {
                if (null != url.getQuery() && !"".equals(url.getQuery())) {
                    url = new URL(url.toExternalForm() + "&" + chunkHttpParam);
                } else {
                    url = new URL(url.toExternalForm() + "?" + chunkHttpParam);
                }
            }

            this.connectionHelper.initRequest(url, "POST", bChunkEnabled,
                    bLastChunk);

            // Get the GET parameters from the URL and convert them to
            // post form params
            ByteArrayEncoder formParams = getFormParamsForPostRequest(url);
            contentLength += formParams.getEncodedLength();

            this.connectionHelper.append(
                    "Content-Type: multipart/form-data; boundary=").append(
                    this.connectionHelper.getBoundary().substring(2)).append(
                    "\r\n");
            this.connectionHelper.append("Content-Length: ").append(
                    String.valueOf(contentLength)).append("\r\n");

            // Blank line (end of header)
            this.connectionHelper.append("\r\n");

            // formParams are not really part of the main header, but we add
            // them here anyway. We write directly into the
            // ByteArrayOutputStream, as we already encoded them, to get the
            // encoded length. We need to flush the writer first, before
            // directly writing to the ByteArrayOutputStream.
            this.connectionHelper.append(formParams);

            // Let's call the server
            this.connectionHelper.sendRequest();

            // Debug output: always called, so that the debug file is correctly
            // filled.
            this.uploadPolicy.displayDebug("=== main header (len="
                    + this.connectionHelper.getByteArrayEncoder()
                            .getEncodedLength()
                    + "):\n"
                    + quoteCRLF(this.connectionHelper.getByteArrayEncoder()
                            .getString()), 70);
            this.uploadPolicy.displayDebug("=== main header end", 70);
        } catch (IOException e) {
            throw new JUploadIOException(e);
        } catch (IllegalArgumentException e) {
            throw new JUploadException(e);
        }
    }

    // ////////////////////////////////////////////////////////////////////////////////////
    // /////////////////////// PRIVATE METHODS
    // ////////////////////////////////////////////////////////////////////////////////////

    /**
     * Returns the header for this file, within the http multipart body.
     * 
     * @param index Index of the file in the array that contains all files to
     *            upload.
     * @param bound The boundary that separate files in the http multipart post
     *            body.
     * @param chunkPart The numero of the current chunk (from 1 to n)
     * @return The encoded header for this file. The {@link ByteArrayEncoder} is
     *         closed within this method.
     * @throws JUploadException
     */
    private final ByteArrayEncoder getFileHeader(int index, String bound,
            @SuppressWarnings("unused")
            int chunkPart) throws JUploadException {
        String filenameEncoding = this.uploadPolicy.getFilenameEncoding();
        String mimetype = this.filesToUpload[index].getMimeType();
        String uploadFilename = this.filesToUpload[index]
                .getUploadFilename(index);
        ByteArrayEncoder bae = new ByteArrayEncoderHTTP(this.uploadPolicy,
                bound);

        // We'll encode the output stream into UTF-8.
        String form = this.uploadPolicy.getFormdata();
        if (null != form) {
            bae.appendFormVariables(form);
        }
        // We ask the current FileData to add itself its properties.
        this.filesToUpload[index].appendFileProperties(bae);

        // boundary.
        bae.append(bound).append("\r\n");

        // Content-Disposition.
        bae.append("Content-Disposition: form-data; name=\"");
        bae.append(this.filesToUpload[index].getUploadName(index)).append(
                "\"; filename=\"");
        if (filenameEncoding == null) {
            bae.append(uploadFilename);
        } else {
            try {
                this.uploadPolicy.displayDebug("Encoded filename: "
                        + URLEncoder.encode(uploadFilename, filenameEncoding),
                        70);
                bae.append(URLEncoder.encode(uploadFilename, filenameEncoding));
            } catch (UnsupportedEncodingException e) {
                this.uploadPolicy
                        .displayWarn(e.getClass().getName() + ": "
                                + e.getMessage()
                                + " (in UploadFileData.getFileHeader)");
                bae.append(uploadFilename);
            }
        }
        bae.append("\"\r\n");

        // Line 3: Content-Type.
        bae.append("Content-Type: ").append(mimetype).append("\r\n");

        // An empty line to finish the header.
        bae.append("\r\n");

        // The ByteArrayEncoder is now filled.
        bae.close();
        return bae;
    }// getFileHeader

    /**
     * Construction of the head for each file.
     * 
     * @param firstFileToUpload The index of the first file to upload, in the
     *            {@link #filesToUpload} area.
     * @param nbFilesToUpload Number of file to upload, in the next HTTP upload
     *            request. These files are taken from the {@link #filesToUpload}
     *            area
     * @param bound The String boundary between the post data in the HTTP
     *            request.
     * @throws JUploadException
     */
    private final void setAllHead(String bound) throws JUploadException {
        this.heads = new ByteArrayEncoder[this.filesToUpload.length];
        for (int i = 0; i < this.filesToUpload.length; i++) {
            this.heads[i] = getFileHeader(i, bound, -1);
        }
    }

    /**
     * Construction of the tail for each file.
     * 
     * @param firstFileToUpload The index of the first file to upload, in the
     *            {@link #filesToUpload} area.
     * @param nbFilesToUpload Number of file to upload, in the next HTTP upload
     *            request. These files are taken from the {@link #filesToUpload}
     *            area
     * @param bound Current boundary, to apply for these tails.
     */
    private final void setAllTail(String bound) throws JUploadException {
        this.tails = new ByteArrayEncoder[this.filesToUpload.length];
        for (int i = 0; i < this.filesToUpload.length; i++) {
            // We'll encode the output stream into UTF-8.
            ByteArrayEncoder bae = new ByteArrayEncoderHTTP(this.uploadPolicy,
                    bound);

            bae.append("\r\n");
            bae.appendTextProperty("md5sum[]", this.filesToUpload[i].getMD5());

            // The last tail gets an additional "--" in order to tell the
            // server we have finished.
            if (i == this.filesToUpload.length - 1) {
                bae.append(bound).append("--\r\n");
            }

            // Let's store this tail.
            bae.close();

            this.tails[i] = bae;
        }

    }

    /**
     * Converts the parameters in GET form to post form
     * 
     * @param url the <code>URL</code> containing the query parameters
     * @return the parameters in a string in the correct form for a POST request
     * @throws JUploadIOException
     */
    private final ByteArrayEncoder getFormParamsForPostRequest(final URL url)
            throws JUploadIOException {

        // Use a string buffer
        // We'll encode the output stream into UTF-8.
        ByteArrayEncoder bae = new ByteArrayEncoderHTTP(this.uploadPolicy,
                this.connectionHelper.getBoundary());

        // Get the query string
        String query = url.getQuery();

        if (null != query) {
            // Split this into parameters
            HashMap<String, String> requestParameters = new HashMap<String, String>();
            String[] paramPairs = query.split("&");
            String[] oneParamArray;

            // Put the parameters correctly to the Hashmap
            for (String param : paramPairs) {
                if (param.contains("=")) {
                    oneParamArray = param.split("=");
                    if (oneParamArray.length > 1) {
                        // There is a value for this parameter
                        requestParameters.put(oneParamArray[0],
                                oneParamArray[1]);
                    } else {
                        // There is no value for this parameter
                        requestParameters.put(oneParamArray[0], "");
                    }
                }
            }

            // Now add one multipart segment for each
            for (String key : requestParameters.keySet())
                bae.appendTextProperty(key, requestParameters.get(key));
        }
        // Return the body content
        bae.close();

        return bae;
    }// getFormParamsForPostRequest

}
