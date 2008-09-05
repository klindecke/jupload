//
// $Id: FileUploadThreadHTTP.java 488 2008-07-06 20:21:43Z etienne_sf $
//
// jupload - A file upload applet.
// Copyright 2007 The JUpload Team
//
// Created: 2007-03-07
// Creator: etienne_sf
// Last modified: $Date: 2008-07-06 22:21:43 +0200 (dim., 06 juil. 2008) $
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

package wjhk.jupload2.upload.helper;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PushbackInputStream;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.Socket;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import wjhk.jupload2.exception.JUploadException;
import wjhk.jupload2.exception.JUploadIOException;
import wjhk.jupload2.policies.UploadPolicy;
import wjhk.jupload2.upload.FileUploadThread;
import wjhk.jupload2.upload.HttpConnect;

/**
 * 
 * This class contains utilities to delegate network manipulation. It hides the
 * management for the current upload policy connection parameters.
 * 
 * @author etienne_sf
 * 
 */
public class HTTPConnectionHelper {

    // ////////////////////////////////////////////////////////////////////////////////////
    // /////////////////// ATTRIBUTE USED TO CONTROL THE OUTPUT TO THE SERVER
    // ////////////////////////////////////////////////////////////////////////////////////
    /**
     * http boundary, for the posting multipart post.
     */
    private String boundary = "-----------------------------"
            + getRandomString();

    /**
     * Is chunk upload on for this request ?
     */
    private boolean bChunkEnabled;

    /**
     * Is it the last chunk ? If yes, we'll try to keep the connection open,
     * according to the current applet configuration.
     */
    private boolean bLastChunk;

    /**
     * The encoder that will contain the HTTP request.
     */
    private ByteArrayEncoder byteArrayEncoder = null;

    /**
     * This stream is open by {@link #startRequest(long, boolean, int, boolean)}.
     * It is closed by the {@link #cleanRequest()} method.
     * 
     * @see #startRequest(long, boolean, int, boolean)
     * @see #cleanRequest()
     * @see #getOutputStream()
     */
    private DataOutputStream httpDataOut = null;

    /**
     * Contains the HTTP reader. All data coming from the server response are
     * read from it. If this attribute is null, it means that the server
     * response has not been read.
     */
    private HTTPInputStreamReader httpInputStreamReader = null;

    /**
     * This stream allows the applet to get the server response. It is opened
     * and closed as the {@link #httpDataOut}.
     */
    private PushbackInputStream httpDataIn = null;

    /**
     * The current proxy, if any
     */
    private Proxy proxy = null;

    /**
     * The network socket where the bytes should be written.
     */
    private Socket socket = null;

    /**
     * Indicates if the user request to stop the upload.
     */
    private boolean stop = false;

    /**
     * The current upload policy
     */
    private UploadPolicy uploadPolicy = null;

    /**
     * Should we use a proxy
     */
    private boolean useProxy;

    /**
     * Is SSL mode on ?
     */
    private boolean useSSL;

    /**
     * The current URL
     */
    private URL url = null;

    // ////////////////////////////////////////////////////////////////////////////////////
    // /////////////////////// PUBLIC METHODS
    // ////////////////////////////////////////////////////////////////////////////////////

    /**
     * The standard constructor for this class.
     * 
     * @param uploadPolicy The current upload policy.
     */
    public HTTPConnectionHelper(UploadPolicy uploadPolicy) {
        this.uploadPolicy = uploadPolicy;
    }

    /**
     * The standard constructor for this class.
     * 
     * @param url The target URL
     * @param bChunkEnabled Indicates if chunkUpload is enabled for this query.
     *            Put false, if non chunked request or if it is not relevant.
     * @param bLastChunk Indicates whether this chunk is the last one. Put true,
     *            if non chunked request or if it is not relevant.
     * @param uploadPolicy The current upload policy.
     * @throws JUploadIOException
     */
    public HTTPConnectionHelper(URL url, boolean bChunkEnabled,
            boolean bLastChunk, UploadPolicy uploadPolicy)
            throws JUploadIOException {
        this.uploadPolicy = uploadPolicy;
        initRequest(url, bChunkEnabled, bLastChunk);
    }

    /**
     * The standard constructor for this class.
     * 
     * @param url The target URL
     * @param bChunkEnabled Indicates if chunkUpload is enabled for this query.
     *            Put false, if non chunked request or if it is not relevant.
     * @param bLastChunk Indicates whether this chunk is the last one. Put true,
     *            if non chunked request or if it is not relevant.
     * @throws JUploadIOException
     */
    public void initRequest(URL url, boolean bChunkEnabled, boolean bLastChunk)
            throws JUploadIOException {
        // Clean any current request.
        if (isKeepAlive()) {
            dispose();
        }

        // Load the new parameters.
        this.url = url;
        this.bChunkEnabled = bChunkEnabled;
        this.bLastChunk = bLastChunk;
    }

    /**
     * Return the current {@link ByteArrayEncoder}. If it was not created, it
     * is initialized.
     * 
     * @return The current {@link ByteArrayEncoder}, initialized if it didn't
     *         exist.
     * 
     * @throws JUploadIOException
     */
    public ByteArrayEncoder getByteArrayEncoder() throws JUploadIOException {
        if (this.byteArrayEncoder == null) {
            initByteArrayEncoder();
        }

        return this.byteArrayEncoder;
    }

    /**
     * @return Returns the boundary for this HTTP request.
     * 
     */
    public String getBoundary() {
        return this.boundary;
    }

    /**
     * Closes the byteArrayEncoder, create the socket (or not, depending on the
     * current uploadPolicy, and upload history), send the request, and create
     * the InputStream to read the server response.
     * 
     * @throws JUploadIOException
     */
    public void sendRequest() throws JUploadIOException {

        try {
            // We've finished with the current encoder.
            if (!this.byteArrayEncoder.isClosed()) {
                this.byteArrayEncoder.close();
            }
            
            //Let's clear any field that could have been read in a previous step:
            this.httpInputStreamReader = null;

            // Only connect, if sock is null!!
            // ... or if we don't persist HTTP connections (patch for IIS, based
            // on Marc Reidy's patch)
            if (this.socket == null
                    || !this.uploadPolicy.getAllowHttpPersistent()) {
                this.socket = new HttpConnect(this.uploadPolicy).Connect(
                        this.url, this.proxy);
                this.httpDataOut = new DataOutputStream(
                        new BufferedOutputStream(this.socket.getOutputStream()));
                this.httpDataIn = new PushbackInputStream(this.socket
                        .getInputStream(), 1);
            }

            // Send http request to server
            this.httpDataOut.write(this.byteArrayEncoder.getEncodedByteArray());
            
            //The request has been sent. The current ByteArrayEncoder is now useless. A new one is to be created for the next request.
            this.byteArrayEncoder = null;
            
        } catch (IOException e) {
            throw new JUploadIOException("Unable to open socket", e);
        } catch (KeyManagementException e) {
            throw new JUploadIOException("Unable to open socket", e);
        } catch (UnrecoverableKeyException e) {
            throw new JUploadIOException("Unable to open socket", e);
        } catch (NoSuchAlgorithmException e) {
            throw new JUploadIOException("Unable to open socket", e);
        } catch (KeyStoreException e) {
            throw new JUploadIOException("Unable to open socket", e);
        } catch (CertificateException e) {
            throw new JUploadIOException("Unable to open socket", e);
        } catch (IllegalArgumentException e) {
            throw new JUploadIOException("Unable to open socket", e);
        }

    }

    /**
     * Releases all reserved resources.
     * 
     * @throws JUploadIOException
     */
    public void dispose() throws JUploadIOException {

        try {
            // Throws java.io.IOException
            this.httpDataOut.close();
        } catch (NullPointerException e) {
            // httpDataOut is already null ...
        } catch (IOException e) {
            throw new JUploadIOException(e);
        } finally {
            this.httpDataOut = null;
        }

        try {
            // Throws java.io.IOException
            this.httpDataIn.close();
        } catch (NullPointerException e) {
            // httpDataIn is already null ...
        } catch (IOException e) {
            throw new JUploadIOException(e);
        } finally {
            this.httpDataIn = null;
        }

        try {
            // Throws java.io.IOException
            this.socket.close();
        } catch (NullPointerException e) {
            // sock is already null ...
        } catch (IOException e) {
            throw new JUploadIOException(e);
        } finally {
            this.socket = null;
        }
    }

    /**
     * Return the current socket. If the byteArrayEncoder is not closed: close
     * it, and send the request to the server.
     * 
     * @return
     * 
     * public Socket getSocket() { }
     */

    /**
     * get the output stream, where HTTP data can be written.
     * 
     * @return The current output stream to the server, where things can be
     *         written, event after the socket is open, if the byteArrayEncoder
     *         did not contain the full request.
     */
    public DataOutputStream getHttpDataOut() {
        return this.httpDataOut;
    }

    /**
     * get the input stream, where HTTP server response can be read.
     * 
     * @return The current input stream of the socket.
     */
    public PushbackInputStream getHttpDataIn() {
        return this.httpDataIn;
    }

    /**
     * Get the last response body.
     * 
     * @return The full response body, that is: the HTTP body of the server
     *         response.
     */
    public String getResponseBody() {
        return this.httpInputStreamReader.getResponseBody();
    }

    /**
     * Get the last response message.
     * 
     * @return the response message, like "200 OK"
     */
    public String getResponseMsg() {
        return this.httpInputStreamReader.getResponseMsg();
    }

    /**
     * Get the current socket.
     * 
     * @return
     */
    Socket getSocket() {
        return this.socket;
    }

    /**
     * Return true is the upload is stopped.
     * 
     * @return Current value of the stop attribute.
     */
    public boolean gotStopped() {
        return this.stop;
    }

    /**
     * Write bytes to the httpDataout
     * 
     * @param bytes
     * @throws JUploadIOException
     */
    public void write(byte[] bytes) throws JUploadIOException {
        try {
            this.httpDataOut.write(bytes);
        } catch (IOException e) {
            throw new JUploadIOException(e.getClass().getName()
                    + " while writing to httpDataOut", e);
        }
    }

    /**
     * Read the response of the server. This method delegates the work to the
     * HTTPInputStreamReader. handles the chunk HTTP response.
     * 
     * @return The HTTP status. Should be 200, when everything is right.
     * @throws JUploadException
     */
    public int readHttpResponse() throws JUploadException {
        if (this.httpInputStreamReader == null) {
            this.httpInputStreamReader = new HTTPInputStreamReader(this,
                    this.uploadPolicy);
        }

        // Let's do the job
        this.httpInputStreamReader.readHttpResponse();

        if (this.httpInputStreamReader.gotClose) {
            // RFC 2868, section 8.1.2.1
            dispose();
        }

        return this.httpInputStreamReader.gethttpStatusCode();
    }

    /** @see FileUploadThread#stopUpload() */
    public void stopUpload() {
        this.stop = true;
    }

    // ////////////////////////////////////////////////////////////////////////////////////
    // /////////////////////// PRIVATE METHODS
    // ////////////////////////////////////////////////////////////////////////////////////
    /**
     * Construction of a random string, to separate the uploaded files, in the
     * HTTP upload request.
     */
    private final String getRandomString() {
        StringBuffer sbRan = new StringBuffer(11);
        String alphaNum = "1234567890abcdefghijklmnopqrstuvwxyz";
        int num;
        for (int i = 0; i < 11; i++) {
            num = (int) (Math.random() * (alphaNum.length() - 1));
            sbRan.append(alphaNum.charAt(num));
        }
        return sbRan.toString();
    }

    /**
     * creating of a new {@link ByteArrayEncoderHTTP}, and initialising of the
     * following header items: First line (POST currentProtocol URI), Host,
     * Connection, Keep-Alive, Proxy-Connection.
     * 
     * @throws JUploadIOException
     */
    private void initByteArrayEncoder() throws JUploadIOException {
        if (this.byteArrayEncoder != null && !this.byteArrayEncoder.isClosed()) {
            this.byteArrayEncoder.close();
            this.byteArrayEncoder = null;
        }
        this.byteArrayEncoder = new ByteArrayEncoderHTTP(this.uploadPolicy,
                this.boundary);
        this.proxy = null;
        try {
            this.proxy = ProxySelector.getDefault().select(this.url.toURI())
                    .get(0);
        } catch (URISyntaxException e) {
            throw new JUploadIOException("Error while managing url "
                    + this.url.toExternalForm(), e);
        }
        this.useProxy = ((this.proxy != null) && (this.proxy.type() != Proxy.Type.DIRECT));
        this.useSSL = this.url.getProtocol().equals("https");

        // Header: Request line
        // Let's clear it. Useful only for chunked uploads.
        this.byteArrayEncoder.append("POST ");
        if (this.useProxy && (!this.useSSL)) {
            // with a proxy we need the absolute URL, but only if not
            // using SSL. (with SSL, we first use the proxy CONNECT method,
            // and then a plain request.)
            this.byteArrayEncoder.append(this.url.getProtocol()).append("://")
                    .append(this.url.getHost());
        }
        this.byteArrayEncoder.append(this.url.getPath());

        // Append the query params.
        // TODO: This probably can be removed as we now
        // have everything in POST data. However in order to be
        // backwards-compatible, it stays here for now. So we now provide
        // *both* GET and POST params.
        if (null != this.url.getQuery() && !"".equals(this.url.getQuery()))
            this.byteArrayEncoder.append("?").append(this.url.getQuery());

        this.byteArrayEncoder.append(" ").append(
                this.uploadPolicy.getServerProtocol()).append("\r\n");

        // Header: General
        this.byteArrayEncoder.append("Host: ").append(this.url.getHost())
                .append("\r\nAccept: */*\r\n");
        // We do not want gzipped or compressed responses, so we must
        // specify that here (RFC 2616, Section 14.3)
        this.byteArrayEncoder.append("Accept-Encoding: identity\r\n");

        // Seems like the Keep-alive doesn't work properly, at least on my
        // local dev (Etienne).
        if (!this.uploadPolicy.getAllowHttpPersistent()) {
            this.byteArrayEncoder.append("Connection: close\r\n");
        } else {
            if (!this.bChunkEnabled
                    || this.bLastChunk
                    || this.useProxy
                    || !this.uploadPolicy.getServerProtocol()
                            .equals("HTTP/1.1")) { // RFC 2086, section 19.7.1
                this.byteArrayEncoder.append("Connection: close\r\n");
            } else {
                this.byteArrayEncoder.append("Keep-Alive: 300\r\n");
                if (this.useProxy)
                    this.byteArrayEncoder
                            .append("Proxy-Connection: keep-alive\r\n");
                else
                    this.byteArrayEncoder.append("Connection: keep-alive\r\n");
            }
        }

        // Get specific headers for this upload.
        this.uploadPolicy.onAppendHeader(this.byteArrayEncoder);
    }

    /**
     * Indicates whether the current socket should be reused ... if any.
     */
    private boolean isKeepAlive() {
        if (this.socket == null) {
            return true;
        } else if (!this.uploadPolicy.getAllowHttpPersistent()) {
            return false;
        } else {
            if (!this.bChunkEnabled
                    || this.bLastChunk
                    || this.useProxy
                    || !this.uploadPolicy.getServerProtocol()
                            .equals("HTTP/1.1")) { // RFC 2086, section 19.7.1
                return false;
            } else {
                return true;
            }
        }
    }
}
