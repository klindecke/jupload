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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.SSLSocket;

import wjhk.jupload2.exception.JUploadException;
import wjhk.jupload2.exception.JUploadIOException;
import wjhk.jupload2.policies.UploadPolicy;
import wjhk.jupload2.upload.CookieJar;
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
    // /////////////////// ATTRIBUTE CONTAINING DATA COMING FROM THE RESPONSE
    // ////////////////////////////////////////////////////////////////////////////////////
    /**
     * The status message from the first line of the response (e.g. "200 OK").
     */
    String responseMsg = null;

    /**
     * The string buffer that will contain the HTTP response body, that is: the
     * server response, without the headers.
     */
    String responseBody = null;

    private CookieJar cookies = new CookieJar();

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
    // /////////////////// CONSTANTS USED TO CONTROL THE OUTPUT
    // ////////////////////////////////////////////////////////////////////////////////////
    private final static int CHUNKBUF_SIZE = 4096;

    private final byte chunkbuf[] = new byte[CHUNKBUF_SIZE];

    private final static Pattern pChunked = Pattern.compile(
            "^Transfer-Encoding:\\s+chunked", Pattern.CASE_INSENSITIVE);

    private final static Pattern pClose = Pattern.compile(
            "^Connection:\\s+close", Pattern.CASE_INSENSITIVE);

    private final static Pattern pProxyClose = Pattern.compile(
            "^Proxy-Connection:\\s+close", Pattern.CASE_INSENSITIVE);

    private final static Pattern pHttpStatus = Pattern
            .compile("^HTTP/\\d\\.\\d\\s+((\\d+)\\s+.*)$");

    private final static Pattern pContentLen = Pattern.compile(
            "^Content-Length:\\s+(\\d+)$", Pattern.CASE_INSENSITIVE);

    private final static Pattern pContentTypeCs = Pattern.compile(
            "^Content-Type:\\s+.*;\\s*charset=([^;\\s]+).*$",
            Pattern.CASE_INSENSITIVE);

    private final static Pattern pSetCookie = Pattern.compile(
            "^Set-Cookie:\\s+(.*)$", Pattern.CASE_INSENSITIVE);

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
        if (byteArrayEncoder.isClosed()) {
            throw new JUploadIOException("byteArrayEncoder is already closed");
        }

        return byteArrayEncoder;
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

            // Only connect, if sock is null!!
            // ... or if we don't persist HTTP connections (patch for IIS, based
            // on Marc Reidy's patch)
            if (this.socket == null || !uploadPolicy.getAllowHttpPersistent()) {
                this.socket = new HttpConnect(this.uploadPolicy).Connect(url,
                        proxy);
                this.httpDataOut = new DataOutputStream(
                        new BufferedOutputStream(this.socket.getOutputStream()));
                this.httpDataIn = new PushbackInputStream(this.socket
                        .getInputStream(), 1);
            }

            // Send http request to server
            this.httpDataOut.write(this.byteArrayEncoder.getEncodedByteArray());
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
        return httpDataOut;
    }

    /**
     * get the input stream, where HTTP server response can be read.
     * 
     * @return The current input stream of the socket.
     */
    public PushbackInputStream getHttpDataIn() {
        return httpDataIn;
    }

    /**
     * Get the last response body.
     * 
     * @return
     */
    public String getResponseBody() {
        return responseBody;
    }

    /**
     * Get the last response body.
     * 
     * @return
     */
    public String getResponseMsg() {
        return responseMsg;
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
     * Read the response of the server. This method handles the chunk HTTP
     * response.
     * 
     * @param sbHttpResponseBody
     * @return The HTTP status. Should be 200, when everything is right.
     * @throws JUploadException
     */
    public int readHttpResponse() throws JUploadException {
        boolean readingHttpBody = false;
        boolean gotClose = false;
        boolean gotChunked = false;
        boolean gotContentLength = false;
        int status = 0;
        int clen = 0;
        String line = "";
        byte[] body = new byte[0];
        String charset = "ISO-8859-1";

        try {
            // If the user requested abort, we are not going to send
            // anymore, so shutdown the outgoing half of the socket.
            // This helps the server to speed up with it's response.
            if (this.stop && !(this.socket instanceof SSLSocket))
                this.socket.shutdownOutput();

            // && is evaluated from left to right so !stop must come first!
            while (!this.stop && ((!gotContentLength) || (clen > 0))) {
                if (readingHttpBody) {
                    // Read the http body
                    if (gotChunked) {
                        // Read the chunk header.
                        // This is US-ASCII! (See RFC 2616, Section 2.2)
                        line = readLine(httpDataIn, "US-ASCII", false);
                        if (null == line)
                            throw new JUploadException(
                                    "unexpected EOF (in HTTP Body, chunked mode)");
                        // Handle a single chunk of the response
                        // We cut off possible chunk extensions and ignore them.
                        // The length is hex-encoded (RFC 2616, Section 3.6.1)
                        int len = Integer.parseInt(line.replaceFirst(";.*", "")
                                .trim(), 16);
                        this.uploadPolicy.displayDebug("Chunk: " + line
                                + " dec: " + len, 80);
                        if (len == 0) {
                            // RFC 2616, Section 3.6.1: A length of 0 denotes
                            // the last chunk of the body.

                            // This code wrong if the server sends chunks
                            // with trailers! (trailers are HTTP Headers that
                            // are send *after* the body. These are announced
                            // in the regular HTTP header "Trailer".
                            // Fritz: Never seen them so far ...
                            // TODO: Implement trailer-handling.
                            break;
                        }

                        // Loop over the chunk (len == length of the chunk)
                        while (len > 0) {
                            int rlen = (len > CHUNKBUF_SIZE) ? CHUNKBUF_SIZE
                                    : len;
                            int ofs = 0;
                            if (rlen > 0) {
                                while (ofs < rlen) {
                                    int res = this.httpDataIn.read(
                                            this.chunkbuf, ofs, rlen - ofs);
                                    if (res < 0)
                                        throw new JUploadException(
                                                "unexpected EOF");
                                    len -= res;
                                    ofs += res;
                                }
                                if (ofs < rlen)
                                    throw new JUploadException("short read");
                                if (rlen < CHUNKBUF_SIZE)
                                    body = byteAppend(body, this.chunkbuf, rlen);
                                else
                                    body = byteAppend(body, this.chunkbuf);
                            }
                        }
                        // Got the whole chunk, read the trailing CRLF.
                        readLine(httpDataIn, false);
                    } else {
                        // Not chunked. Use either content-length (if available)
                        // or read until EOF.
                        if (gotContentLength) {
                            // Got a Content-Length. Read exactly that amount of
                            // bytes.
                            while (clen > 0) {
                                int rlen = (clen > CHUNKBUF_SIZE) ? CHUNKBUF_SIZE
                                        : clen;
                                int ofs = 0;
                                if (rlen > 0) {
                                    while (ofs < rlen) {
                                        int res = this.httpDataIn.read(
                                                this.chunkbuf, ofs, rlen - ofs);
                                        if (res < 0)
                                            throw new JUploadException(
                                                    "unexpected EOF (in HTTP body, not chunked mode)");
                                        clen -= res;
                                        ofs += res;
                                    }
                                    if (ofs < rlen)
                                        throw new JUploadException("short read");
                                    if (rlen < CHUNKBUF_SIZE)
                                        body = byteAppend(body, this.chunkbuf,
                                                rlen);
                                    else
                                        body = byteAppend(body, this.chunkbuf);
                                }
                            }
                        } else {
                            // No Content-length available, read until EOF
                            // 
                            while (true) {
                                byte[] lbuf = readLine(httpDataIn, true);
                                if (null == lbuf)
                                    break;
                                body = byteAppend(body, lbuf);
                            }
                            break;
                        }
                    }
                } else { // (! readingHttpBody)
                    // readingHttpBody is false, so we are still in headers.
                    // Headers are US-ASCII (See RFC 2616, Section 2.2)
                    String tmp = readLine(httpDataIn, "US-ASCII", false);
                    if (null == tmp)
                        throw new JUploadException("unexpected EOF (in header)");
                    if (status == 0) {
                        // We must be reading the first line of the HTTP header.
                        this.uploadPolicy.displayDebug(
                                "-------- Response Headers Start --------", 80);
                        Matcher m = pHttpStatus.matcher(tmp);
                        if (m.matches()) {
                            status = Integer.parseInt(m.group(2));
                            responseMsg = m.group(1);
                        } else {
                            // The status line must be the first line of the
                            // response. (See RFC 2616, Section 6.1) so this
                            // is an error.

                            // We first display the wrong line.
                            this.uploadPolicy
                                    .displayDebug("First line of response: '"
                                            + tmp + "'", 80);
                            // Then, we throw the exception.
                            throw new JUploadException(
                                    "HTTP response did not begin with status line.");
                        }
                    }
                    // Handle folded headers (RFC 2616, Section 2.2). This is
                    // handled after the status line, because that line may
                    // not be folded (RFC 2616, Section 6.1).
                    if (tmp.startsWith(" ") || tmp.startsWith("\t"))
                        line += " " + tmp.trim();
                    else
                        line = tmp;
                    this.uploadPolicy.displayDebug(line, 80);
                    if (pClose.matcher(line).matches())
                        gotClose = true;
                    if (pProxyClose.matcher(line).matches())
                        gotClose = true;
                    if (pChunked.matcher(line).matches())
                        gotChunked = true;
                    Matcher m = pContentLen.matcher(line);
                    if (m.matches()) {
                        gotContentLength = true;
                        clen = Integer.parseInt(m.group(1));
                    }
                    m = pContentTypeCs.matcher(line);
                    if (m.matches())
                        charset = m.group(1);
                    m = pSetCookie.matcher(line);
                    if (m.matches())
                        this.cookies.parseCookieHeader(m.group(1));
                    if (line.length() == 0) {
                        // RFC 2616, Section 6. Body is separated by the
                        // header with an empty line.
                        readingHttpBody = true;
                        this.uploadPolicy.displayDebug(
                                "--------- Response Headers End ---------", 80);
                    }
                }
            } // while

            if (gotClose) {
                // RFC 2868, section 8.1.2.1
                dispose();
            }
            // Convert the whole body according to the charset.
            // The default for charset ISO-8859-1, but overridden by
            // the charset attribute of the Content-Type header (if any).
            // See RFC 2616, Sections 3.4.1 and 3.7.1.
            responseBody = new String(body, charset);
        } catch (Exception e) {
            e.printStackTrace();
            throw new JUploadException(e);
        }

        return status;
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
        if (byteArrayEncoder != null && !byteArrayEncoder.isClosed()) {
            byteArrayEncoder.close();
            byteArrayEncoder = null;
        }
        this.byteArrayEncoder = new ByteArrayEncoderHTTP(uploadPolicy, boundary);
        Proxy proxy = null;
        try {
            proxy = ProxySelector.getDefault().select(url.toURI()).get(0);
        } catch (URISyntaxException e) {
            throw new JUploadIOException("Error while managing url "
                    + url.toExternalForm(), e);
        }
        boolean useProxy = ((proxy != null) && (proxy.type() != Proxy.Type.DIRECT));
        boolean useSSL = url.getProtocol().equals("https");

        // Header: Request line
        // Let's clear it. Useful only for chunked uploads.
        this.byteArrayEncoder.append("POST ");
        if (useProxy && (!useSSL)) {
            // with a proxy we need the absolute URL, but only if not
            // using SSL. (with SSL, we first use the proxy CONNECT method,
            // and then a plain request.)
            this.byteArrayEncoder.append(url.getProtocol()).append("://")
                    .append(url.getHost());
        }
        this.byteArrayEncoder.append(url.getPath());

        // Append the query params.
        // TODO: This probably can be removed as we now
        // have everything in POST data. However in order to be
        // backwards-compatible, it stays here for now. So we now provide
        // *both* GET and POST params.
        if (null != url.getQuery() && !"".equals(url.getQuery()))
            this.byteArrayEncoder.append("?").append(url.getQuery());

        this.byteArrayEncoder.append(" ").append(
                this.uploadPolicy.getServerProtocol()).append("\r\n");

        // Header: General
        this.byteArrayEncoder.append("Host: ").append(url.getHost()).append(
                "\r\nAccept: */*\r\n");
        // We do not want gzipped or compressed responses, so we must
        // specify that here (RFC 2616, Section 14.3)
        this.byteArrayEncoder.append("Accept-Encoding: identity\r\n");

        // Seems like the Keep-alive doesn't work properly, at least on my
        // local dev (Etienne).
        if (!this.uploadPolicy.getAllowHttpPersistent()) {
            this.byteArrayEncoder.append("Connection: close\r\n");
        } else {
            if (!bChunkEnabled
                    || bLastChunk
                    || useProxy
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
            if (!bChunkEnabled
                    || bLastChunk
                    || useProxy
                    || !this.uploadPolicy.getServerProtocol()
                            .equals("HTTP/1.1")) { // RFC 2086, section 19.7.1
                return false;
            } else {
                return true;
            }
        }
    }

    // //////////////////////////////////////////////////////////////////////////////////////
    // //////////////////// Various utilities
    // //////////////////////////////////////////////////////////////////////////////////////

    /**
     * Concatenates two byte arrays.
     * 
     * @param buf1 The first array
     * @param buf2 The second array
     * @return A byte array, containing buf2 appended to buf2
     */
    static byte[] byteAppend(byte[] buf1, byte[] buf2) {
        byte[] ret = new byte[buf1.length + buf2.length];
        System.arraycopy(buf1, 0, ret, 0, buf1.length);
        System.arraycopy(buf2, 0, ret, buf1.length, buf2.length);
        return ret;
    }

    /**
     * Concatenates two byte arrays.
     * 
     * @param buf1 The first array
     * @param buf2 The second array
     * @param len Number of bytes to copy from buf2
     * @return A byte array, containing buf2 appended to buf2
     */
    static byte[] byteAppend(byte[] buf1, byte[] buf2, int len) {
        if (len > buf2.length)
            len = buf2.length;
        byte[] ret = new byte[buf1.length + len];
        System.arraycopy(buf1, 0, ret, 0, buf1.length);
        System.arraycopy(buf2, 0, ret, buf1.length, len);
        return ret;
    }

    /**
     * Similar like BufferedInputStream#readLine() but operates on raw bytes.
     * Line-Ending is <b>always</b> "\r\n".
     * 
     * @param charset The input charset of the stream.
     * @param includeCR Set to true, if the terminating CR/LF should be included
     *            in the returned byte array.
     */
    public static String readLine(PushbackInputStream inputStream,
            String charset, boolean includeCR) throws IOException {
        byte[] line = readLine(inputStream, includeCR);
        return (null == line) ? null : new String(line, charset);
    }

    /**
     * Similar like BufferedInputStream#readLine() but operates on raw bytes.
     * According to RFC 2616, and of line may be CR (13), LF (10) or CRLF.
     * Line-Ending is <b>always</b> "\r\n" in header, but not in text bodies.
     * Update done by TedA (sourceforge account: tedaaa). Allows to manage
     * response from web server that send LF instead of CRLF ! Here is a part of
     * the RFC: <I>"we recommend that applications, when parsing such headers,
     * recognize a single LF as a line terminator and ignore the leading CR"</I>.
     * <BR>
     * Corrected again to manage line finished by CR only. This is not allowed
     * in headers, but this method is also used to read lines in the body.
     * 
     * @param includeCR Set to true, if the terminating CR/LF should be included
     *            in the returned byte array. In this case, CR/LF is always
     *            returned to the caller, wether the input stream got CR, LF or
     *            CRLF.
     */
    public static byte[] readLine(PushbackInputStream inputStream,
            boolean includeCR) throws IOException {
        final byte EOS = -1;
        final byte CR = 13;
        final byte LF = 10;
        int len = 0;
        int buflen = 128; // average line length
        byte[] buf = new byte[buflen];
        byte[] ret = null;
        int b;
        boolean lineRead = false;

        while (!lineRead) {
            b = inputStream.read();
            switch (b) {
                case EOS:
                    // We've finished reading the stream, and so the line is
                    // finished too.
                    if (len == 0) {
                        return null;
                    }
                    lineRead = true;
                    break;
                /*
                 * if (len > 0) { ret = new byte[len]; System.arraycopy(buf, 0,
                 * ret, 0, len); return ret; } return null;
                 */
                case LF:
                    // We found the end of the current line.
                    lineRead = true;
                    break;
                case CR:
                    // We got a CR. It can be the end of line.
                    // Is it followed by a LF ? (not mandatory in RFC 2616)
                    b = inputStream.read();

                    if (b != LF) {
                        // The end of line was a simple LF: the next one blongs
                        // to the next line.
                        inputStream.unread(b);
                    }
                    lineRead = true;
                    break;
                default:
                    buf[len++] = (byte) b;
                    // If the buffer is too small, we let enough space to add CR
                    // and LF, in case of ...
                    if (len + 2 >= buflen) {
                        buflen *= 2;
                        byte[] tmp = new byte[buflen];
                        System.arraycopy(buf, 0, tmp, 0, len);
                        buf = tmp;
                    }
            }
        } // while

        // Let's go back to before any CR and LF.
        while (len > 0 && (buf[len] == CR || buf[len] == LF)) {
            len -= 1;
        }

        // Ok, now len indicates the end of the actual line.
        // Should we add a proper CRLF, or nothing ?
        if (includeCR) {
            // We have enough space to add these two characters (see the default
            // here above)
            buf[len++] = CR;
            buf[len++] = LF;
        }

        if (len > 0) {
            ret = new byte[len];
            if (len > 0)
                System.arraycopy(buf, 0, ret, 0, len);
        } else {
            // line feed for empty line between headers and body, or within the
            // body.
            ret = new byte[0];
        }
        return ret;
    }

}
