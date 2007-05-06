//
// $Id$
// 
// jupload - A file upload applet.
// Copyright 2007 The JUpload Team
// 
// Created: 2007-03-07
// Creator: Etienne Gauthier
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
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.Socket;
import java.net.URL;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;

import javax.net.ssl.SSLContext;
import javax.net.ssl.X509TrustManager;
import javax.swing.JProgressBar;

import netscape.javascript.JSException;
import netscape.javascript.JSObject;

import wjhk.jupload2.exception.JUploadException;
import wjhk.jupload2.filedata.FileData;
import wjhk.jupload2.policies.UploadPolicy;

/**
 * This class implements the file upload via HTTP POST request.
 * 
 * @author Etienne Gauthier
 * @version $Revision$
 */
public class FileUploadThreadHTTP extends DefaultFileUploadThread {

    private final static String DUMMYMD5 = "DUMMYMD5DUMMYMD5DUMMYMD5DUMMYMD5";

    /**
     * An implementation of {@link javax.net.ssl.X509TrustManager} which accepts
     * any certificate.
     */
    protected final class TM implements X509TrustManager {
        /**
         * @see javax.net.ssl.X509TrustManager#checkClientTrusted(java.security.cert.X509Certificate[],
         *      java.lang.String)
         */
        @SuppressWarnings("unused")
        public void checkClientTrusted(@SuppressWarnings("unused")
        X509Certificate[] arg0, @SuppressWarnings("unused")
        String arg1) throws CertificateException {
            // Nothing to do.
        }

        /**
         * @see javax.net.ssl.X509TrustManager#checkServerTrusted(java.security.cert.X509Certificate[],
         *      java.lang.String)
         */
        @SuppressWarnings("unused")
        public void checkServerTrusted(@SuppressWarnings("unused")
        X509Certificate[] chain, @SuppressWarnings("unused")
        String authType) throws CertificateException {
            // Nothing to do.
        }

        /**
         * @see javax.net.ssl.X509TrustManager#getAcceptedIssuers()
         */
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    }

    /**
     * http boundary, for the posting multipart post.
     */
    private String boundary = "-----------------------------"
            + getRandomString();

    /**
     * local head within the multipart post, for each file. This is
     * precalculated for all files, in case the upload is not chunked. The heads
     * length are counted in the total upload size, to check that it is less
     * than the maxChunkSize. tails are calculated once, as they depend not of
     * the file position in the upload.
     */
    private String[] heads = null;

    /**
     * same as heads, for the ... tail in the multipart post, for each file. But
     * tails depend on the file position (the boundary is added to the last
     * tail). So it's to be calculated by each function.
     */
    private String[] tails = null;

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
     * The network socket where the bytes should be written.
     */
    private Socket sock = null;

    /**
     * This stream allows the applet to get the server response. It is opened
     * and closed as the {@link #httpDataOut}.
     */
    private BufferedReader httpDataIn = null;

    /**
     * This StringBuffer contains the body for the server response. That is: the
     * server response without the http header. This the real functionnal
     * response from the server application, that would be outputed, for
     * instance, by any 'echo' PHP command.
     */
    private StringBuffer sbHttpResponseBody = null;

    /**
     * Creates a new instance.
     * 
     * @param filesDataParam The files to upload.
     * @param uploadPolicy The policy to be applied.
     * @param progress The progress bar to be updated.
     */
    public FileUploadThreadHTTP(FileData[] filesDataParam,
            UploadPolicy uploadPolicy, JProgressBar progress) {
        super(filesDataParam, uploadPolicy, progress);
        uploadPolicy.displayDebug("Upload done by using the "
                + getClass().getName() + " class", 40);
        // Name the thread (useful for debugging)
        setName("FileUploadThreadHTTP");
        this.heads = new String[filesDataParam.length];
        this.tails = new String[filesDataParam.length];
    }

    /** @see DefaultFileUploadThread#beforeRequest(int, int) */
    @Override
    void beforeRequest(int firstFileToUploadParam, int nbFilesToUploadParam)
            throws JUploadException {
        setAllHead(firstFileToUploadParam, nbFilesToUploadParam, this.boundary);
        setAllTail(firstFileToUploadParam, nbFilesToUploadParam, this.boundary);
    }

    /** @see DefaultFileUploadThread#getAdditionnalBytesForUpload(int) */
    @Override
    long getAdditionnalBytesForUpload(int index) {
        return this.heads[index].length() + this.tails[index].length();
    }

    /** @see DefaultFileUploadThread#afterFile(int) */
    @Override
    void afterFile(int index) throws JUploadException {
        try {
            String tail = this.tails[index].replaceFirst(DUMMYMD5,
                    this.filesToUpload[index].getMD5());
            this.httpDataOut.writeBytes(tail);
            this.uploadPolicy.displayDebug("--- filetail start (len="
                    + tail.length() + "):", 80);
            this.uploadPolicy.displayDebug(quoteCRLF(tail), 80);
            this.uploadPolicy.displayDebug("--- filetail end", 80);
        } catch (Exception e) {
            throw new JUploadException(e);
        }
    }

    /** @see DefaultFileUploadThread#beforeFile(int) */
    @Override
    void beforeFile(int index) throws JUploadException {
        // heads[i] contains the header specific for the file, in the multipart
        // content.
        // It is initialized at the beginning of the run() method. It can be
        // override at the beginning
        // of this loop, if in chunk mode.
        try {
            this.httpDataOut.writeBytes(this.heads[index]);
            this.uploadPolicy.displayDebug("--- fileheader start (len="
                    + this.heads[index].length() + "):", 80);
            this.uploadPolicy.displayDebug(quoteCRLF(this.heads[index]), 80);
            this.uploadPolicy.displayDebug("--- fileheader end", 80);
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
        JUploadException localException = null;

        try {
            // Throws java.io.IOException
            this.httpDataOut.close();
        } catch (NullPointerException e) {
            // httpDataOut is already null ...
        } catch (IOException e) {
            localException = new JUploadException(e);
            this.uploadPolicy.displayErr(this.uploadPolicy
                    .getString("errDuringUpload")
                    + " (httpDataOut.close) ("
                    + e.getClass()
                    + ".doUpload()) : " + localException.getMessage());
        } finally {
            this.httpDataOut = null;
        }

        try {
            // Throws java.io.IOException
            this.httpDataIn.close();
        } catch (NullPointerException e) {
            // httpDataIn is already null ...
        } catch (IOException e) {
            if (localException != null) {
                localException = new JUploadException(e);
                this.uploadPolicy.displayErr(this.uploadPolicy
                        .getString("errDuringUpload")
                        + " (httpDataIn.close) ("
                        + e.getClass()
                        + ".doUpload()) : " + localException.getMessage());
            }
        } finally {
            this.httpDataIn = null;
        }

        try {
            // Throws java.io.IOException
            this.sock.close();
        } catch (NullPointerException e) {
            // sock is already null ...
        } catch (IOException e) {
            if (localException != null) {
                localException = new JUploadException(e);
                this.uploadPolicy.displayErr(this.uploadPolicy
                        .getString("errDuringUpload")
                        + " (sock.close)("
                        + e.getClass()
                        + ".doUpload()) : "
                        + e.getMessage());
            }
        } finally {
            this.sock = null;
        }

        if (localException != null) {
            throw localException;
        }
    }

    @Override
    void finishRequest() throws JUploadException {
        boolean readingHttpBody = false;
        boolean gotClose = false;
        String line;

        this.sbHttpResponseBody = new StringBuffer();
        try {
            // If the user requested abort, we are not going to send
            // anymore, so shutdown the outgoing half of the socket.
            // This helps the server to speed up with it's response.
            if (this.stop)
                this.sock.shutdownOutput();
            // && is evaluated from left to right so !stop must come first!
            while (!this.stop && (line = this.httpDataIn.readLine()) != null) {
                this.addServerOutPut(line);
                this.addServerOutPut("\n");

                // Store the http body
                if (readingHttpBody) {
                    this.sbHttpResponseBody.append(line).append("\n");
                } else {
                    if (line.matches("^Connection:\\sclose"))
                        gotClose = true;
                    if (line.matches("^Proxy-Connection:\\sclose"))
                        gotClose = true;
                }
                if (line.length() == 0) {
                    // Next lines will be the http body (or perhaps we already
                    // are in the body, but it's Ok anyway)
                    readingHttpBody = true;
                }
            }// while

            if (gotClose) {
                // RFC 2868, section 8.1.2.1
                cleanRequest();
            }
        } catch (Exception e) {
            throw new JUploadException(e);
        }
    }

    /** @see DefaultFileUploadThread#getResponseBody() */
    @SuppressWarnings("unused")
    @Override
    String getResponseBody() throws JUploadException {
        return this.sbHttpResponseBody.toString();
    }

    /** @see DefaultFileUploadThread#getOutputStream() */
    @SuppressWarnings("unused")
    @Override
    OutputStream getOutputStream() throws JUploadException {
        return this.httpDataOut;
    }

    /**
     * Helper function for perforing a proxy CONNECT request.
     * 
     * @param proxy The proxy to use.
     * @param host The destination's hostname.
     * @param port The destination's port
     * @return An established socket connection to the proxy.
     * @throws ConnectException if the proxy response code is not 200
     * @throws UnknownHostException
     * @throws IOException
     */
    private Socket HttpProxyConnect(Proxy proxy, String host, int port)
            throws UnknownHostException, IOException, ConnectException {
        InetSocketAddress sa = (InetSocketAddress) proxy.address();
        String phost = (sa.isUnresolved()) ? sa.getHostName() : sa.getAddress()
                .getHostAddress();
        int pport = sa.getPort();
        // 
        Socket proxysock = new Socket(phost, pport);
        String req = "CONNECT " + host + ":" + port + " HTTP/1.1\r\n\r\n";
        proxysock.getOutputStream().write(req.getBytes());
        BufferedReader proxyIn = new BufferedReader(new InputStreamReader(
                proxysock.getInputStream()));
        // We expect exactly one line: the proxy response
        String line = proxyIn.readLine();
        if (!line.matches("^HTTP/\\d\\.\\d\\s200\\s.*"))
            throw new ConnectException("Proxy response: " + line);
        this.uploadPolicy.displayDebug("Proxy response: " + line, 40);
        proxyIn.readLine(); // eat the header delimiter
        // we now are connected ...
        return proxysock;
    }

    /** @see DefaultFileUploadThread#startRequest(long, boolean, int, boolean) */
    @Override
    void startRequest(long contentLength, boolean bChunkEnabled, int chunkPart,
            boolean bLastChunk) throws JUploadException {
        StringBuffer header = new StringBuffer();

        try {
            String chunkHttpParam = "jupart=" + chunkPart + "&jufinal="
                    + (bLastChunk ? "1" : "0");
            this.uploadPolicy.displayDebug("chunkHttpParam: " + chunkHttpParam,
                    40);

            URL url = new URL(this.uploadPolicy.getPostURL());

            // Add the chunking query params to the URL if there are any
            if (bChunkEnabled) {
                if (null != url.getQuery() && !"".equals(url.getQuery())) {
                    url = new URL(url.toExternalForm() + "&" + chunkHttpParam);
                } else {
                    url = new URL(url.toExternalForm() + "?" + chunkHttpParam);
                }
            }

            Proxy proxy = null;
            proxy = ProxySelector.getDefault().select(url.toURI()).get(0);
            boolean useProxy = ((proxy != null) && (proxy.type() != Proxy.Type.DIRECT));
            boolean useSSL = url.getProtocol().equals("https");

            // Header: Request line
            // Let's clear it. Useful only for chunked uploads.
            header.setLength(0);
            header.append("POST ");
            if (useProxy && (!useSSL)) {
                // with a proxy we need the absolute URL, but only if not
                // using SSL. (with SSL, we first use the proxy CONNECT method,
                // and then a plain request.)
                header.append(url.getProtocol()).append("://").append(
                        url.getHost());
            }
            header.append(url.getPath());

            // Append the query params.
            // TODO: This probably can be removed as we now
            // have everything in POST data. However in order to be
            // backwards-compatible, it stays here for now. So we now provide
            // *both* GET and POST params.
            if (null != url.getQuery() && !"".equals(url.getQuery()))
                header.append("?").append(url.getQuery());

            header.append(" ").append(this.uploadPolicy.getServerProtocol())
                    .append("\r\n");

            // Header: General
            header.append("Host: ").append(url.getHost()).append(
                    "\r\nAccept: */*\r\n");
            if (!bChunkEnabled
                    || bLastChunk
                    || useProxy
                    || !this.uploadPolicy.getServerProtocol()
                            .equals("HTTP/1.1")) {
                // RFC 2086, section 19.7.1
                header.append("Connection: close\r\n");
            } else {
                header.append("Keep-Alive: 300\r\n");
                if (useProxy)
                    header.append("Proxy-Connection: keep-alive\r\n");
                else
                    header.append("Connection: keep-alive\r\n");
            }
            // Get the GET parameters from the URL and convert them to
            // post form params
            String formParams = getFormParamsForPostRequest(url);
            contentLength += formParams.length();

            header.append("Content-Type: multipart/form-data; boundary=")
                    .append(this.boundary.substring(2)).append("\r\n").append(
                            "Content-Length: ").append(contentLength).append(
                            "\r\n");

            // Get specific headers for this upload.
            this.uploadPolicy.onAppendHeader(header);

            // Blank line (end of header)
            header.append("\r\n");

            // formParams are not really part of the main header, but we add
            // them here anyway.
            header.append(formParams);

            // Only connect, if sock is null!!
            if (this.sock == null) {
                // Temporary socket for SOCKS support
                Socket tsock;
                String host = url.getHost();
                int port;

                // Check if SSL connection is needed
                if (url.getProtocol().equals("https")) {
                    port = (-1 == url.getPort()) ? 443 : url.getPort();
                    SSLContext context = SSLContext.getInstance("SSL");
                    // Allow all certificates
                    context.init(null, new X509TrustManager[] {
                        new TM()
                    }, null);
                    if (useProxy) {
                        if (proxy.type() == Proxy.Type.HTTP) {
                            // First establish a CONNECT, then do a normal SSL
                            // thru that connection.
                            this.uploadPolicy.displayDebug(
                                    "Using SSL socket, via HTTP proxy", 20);
                            this.sock = context
                                    .getSocketFactory()
                                    .createSocket(
                                            HttpProxyConnect(proxy, host, port),
                                            host, port, true);
                        } else if (proxy.type() == Proxy.Type.SOCKS) {
                            this.uploadPolicy.displayDebug(
                                    "Using SSL socket, via SOCKS proxy", 20);
                            tsock = new Socket(proxy);
                            tsock.connect(new InetSocketAddress(host, port));
                            this.sock = context.getSocketFactory()
                                    .createSocket(tsock, host, port, true);
                        } else
                            throw new ConnectException("Unkown proxy type "
                                    + proxy.type());
                    } else {
                        // If port not specified then use default https port
                        // 443.
                        this.uploadPolicy.displayDebug(
                                "Using SSL socket, direct connection", 20);
                        this.sock = context.getSocketFactory().createSocket(
                                host, port);
                    }
                } else {
                    // If we are not in SSL, just use the old code.
                    port = (-1 == url.getPort()) ? 80 : url.getPort();
                    if (useProxy) {
                        if (proxy.type() == Proxy.Type.HTTP) {
                            InetSocketAddress sa = (InetSocketAddress) proxy
                                    .address();
                            host = (sa.isUnresolved()) ? sa.getHostName() : sa
                                    .getAddress().getHostAddress();
                            port = sa.getPort();
                            this.uploadPolicy.displayDebug(
                                    "Using non SSL socket, proxy=" + host + ":"
                                            + port, 20);
                            this.sock = new Socket(host, port);
                        } else if (proxy.type() == Proxy.Type.SOCKS) {
                            this.uploadPolicy
                                    .displayDebug(
                                            "Using non SSL socket, via SOCKS proxy",
                                            20);
                            tsock = new Socket(proxy);
                            tsock.connect(new InetSocketAddress(host, port));
                            this.sock = tsock;
                        } else
                            throw new ConnectException("Unkown proxy type "
                                    + proxy.type());
                    } else {
                        this.uploadPolicy.displayDebug(
                                "Using non SSL socket, direct connection", 20);
                        this.sock = new Socket(host, port);
                    }
                }

                this.httpDataOut = new DataOutputStream(
                        new BufferedOutputStream(this.sock.getOutputStream()));
                this.httpDataIn = new BufferedReader(new InputStreamReader(
                        this.sock.getInputStream()));
            } // sock == null

            // Send http request to server
            this.httpDataOut.writeBytes(header.toString());
        } catch (Exception e) {
            throw new JUploadException(e);
        }

        if (this.uploadPolicy.getDebugLevel() >= 80) {
            this.uploadPolicy.displayDebug("=== main header (len="
                    + header.length() + "):\n" + quoteCRLF(header.toString()),
                    80);
            this.uploadPolicy.displayDebug("=== main header end", 80);
        }
    }

    // ////////////////////////////////////////////////////////////////////////////////////
    // /////////////////////// PRIVATE METHODS
    // ///////////////////////////////////////
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
     * Creates a mime multipart string snippet, representing a POST variable.
     * 
     * @param boundary The multipart boundary to use.
     * @param name The name of the POST variable
     * @param value The value of the POST variable
     * @return A StringBuffer, suitable for appending to the multipart content.
     */
    private final StringBuffer addPostVariable(String boundary, String name,
            String value) {
        StringBuffer sb = new StringBuffer();
        return sb.append(boundary).append("\r\n").append(
                "Content-Disposition: form-data; name=\"").append(name).append(
                "\"\r\nContent-Transfer-Encoding: 8bit\r\n\r\n").append(value)
                .append("\r\n");
    }

    /**
     * Creates a mime multipart string snippet, representing a FORM. Extracts
     * all form elements of a given HTML form and assembles a StringBuffer which
     * contains a sequence of mime multipart messages which represent the
     * elements of that form.
     * 
     * @param boundary The multipart boundary to use.
     * @param formname The name of the form to evaluate.
     * @return A StringBuffer, suitable for appending to the multipart content.
     * @throws JUploadException
     */
    private final StringBuffer addFormVariables(String boundary, String formname)
            throws JUploadException {
        StringBuffer sb = new StringBuffer();
        try {
            JSObject win = JSObject.getWindow(this.uploadPolicy.getApplet());
            Object o = win.eval("document." + formname + ".elements.length");
            if (o instanceof Number) {
                int len = ((Number) o).intValue();
                int i;
                for (i = 0; i < len; i++) {
                    try {
                        Object name = win.eval("document." + formname + "[" + i
                                + "].name");
                        Object value = win.eval("document." + formname + "["
                                + i + "].value");
                        if (name instanceof String) {
                            if (value instanceof String) {
                                sb.append(addPostVariable(boundary,
                                        (String) name, (String) value));
                            }
                        }
                    } catch (Exception e1) {
                        if (e1 instanceof JSException) {
                            this.uploadPolicy.displayDebug(
                                    e1.getStackTrace()[1]
                                            + ": got JSException, bailing out",
                                    80);
                        } else
                            throw new JUploadException(e1);
                        i = len;
                    }
                }
            }
        } catch (Exception e) {
            if (e instanceof JSException) {
                this.uploadPolicy.displayDebug(e.getStackTrace()[1]
                        + ": No JavaScript availabe", 80);
            } else
                throw new JUploadException(e);
        }
        return sb;
    }

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
    private final String getFileHeader(int index, String boundary,
            @SuppressWarnings("unused")
            int chunkPart) throws JUploadException {
        String filenameEncoding = this.uploadPolicy.getFilenameEncoding();
        String mimetype = this.filesToUpload[index].getMimeType();
        String uploadFilename = this.filesToUpload[index]
                .getUploadFilename(index);
        StringBuffer sb = new StringBuffer();

        String form = this.uploadPolicy.getFormdata();
        if (null != form)
            sb.append(addFormVariables(boundary, form));
        sb.append(addPostVariable(boundary, "mimetype", mimetype));

        // boundary.
        sb.append(boundary).append("\r\n");

        // Content-Disposition.
        sb.append("Content-Disposition: form-data; name=\"").append(
                this.filesToUpload[index].getUploadName(index)).append(
                "\"; filename=\"");
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
            sb.append("Content-Type: ").append(mimetype);
        sb.append("\r\n");

        // An empty line to finish the header.
        sb.append("\r\n");
        return sb.toString();
    }

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
    private final void setAllHead(int firstFileToUpload, int nbFilesToUpload,
            String bound) throws JUploadException {
        for (int i = 0; i < nbFilesToUpload; i++) {
            this.heads[i] = getFileHeader(firstFileToUpload + i, bound, -1);
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
    private final void setAllTail(int firstFileToUpload, int nbFilesToUpload,
            String bound) {

        for (int i = 0; i < nbFilesToUpload; i++) {
            this.tails[firstFileToUpload + i] = "\r\n"
                    + addPostVariable(bound, "md5sum", DUMMYMD5);
        }
        // The last tail gets an additional "--" in order to tell the Server we
        // have finished.
        this.tails[firstFileToUpload + nbFilesToUpload - 1] += bound + "--\r\n";

    }

    private final String quoteCRLF(String s) {
        return s.replaceAll("\r", "\\\\r").replaceAll("\n", "\\\\n\n");
    }

    /**
     * Converts the parameters in GET form to post form
     * 
     * @param url the <code>URL</code> containing the query parameters
     * @return the parameters in a string in the correct form for a POST request
     */
    private final String getFormParamsForPostRequest(final URL url) {

        // Use a string buffer
        StringBuffer formParams = new StringBuffer();

        // Get the query string
        String query = url.getQuery();

        // Split this into parameters
        HashMap<String, String> requestParameters = new HashMap<String, String>();
        String[] paramPairs = query.split("&");

        // Put the parameters correctly to the Hashmap
        for (String param : paramPairs) {
            if (param.contains("=")) {
                requestParameters.put(param.split("=")[0], param.split("=")[1]);
            }
        }

        // Now add one multipart segment for each
        for (String key : requestParameters.keySet())
            formParams.append(addPostVariable(this.boundary, key,
                    requestParameters.get(key)));

        // Return the body content
        return formParams.toString();
    }
}
