/*
 * Created on 9 mars 07
 */
package wjhk.jupload2.upload;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.X509TrustManager;
import javax.swing.JProgressBar;

import wjhk.jupload2.exception.JUploadException;
import wjhk.jupload2.filedata.FileData;
import wjhk.jupload2.policies.UploadPolicy;

public class FileUploadThreadHTTP extends DefaultFileUploadThread {

    // TrustManager to allow all certificates
    protected final class TM implements X509TrustManager {
        public void checkClientTrusted(@SuppressWarnings("unused")
        X509Certificate[] arg0, @SuppressWarnings("unused")
        String arg1) throws CertificateException {
        }

        public void checkServerTrusted(@SuppressWarnings("unused")
        X509Certificate[] chain, @SuppressWarnings("unused")
        String authType) throws CertificateException {
        }

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
     * 
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

    // ////////////////////////////////////////////////////////////////////////////////////
    // /////////////////////// CONSTRUCTOR
    // ///////////////////////////////////////
    // ////////////////////////////////////////////////////////////////////////////////////

    public FileUploadThreadHTTP(FileData[] filesDataParam,
            UploadPolicy uploadPolicy, JProgressBar progress) {
        super(filesDataParam, uploadPolicy, progress);
        uploadPolicy.displayDebug("Upload done by using the "
                + getClass().getName() + " class", 40);
        heads = new String[filesDataParam.length];
        tails = new String[filesDataParam.length];
    }

    // ////////////////////////////////////////////////////////////////////////////////////
    // /////////////////////// PUBLIC FUNCTIONS
    // ////////////////////////////////////////
    // ////////////////////////////////////////////////////////////////////////////////////

    // ////////////////////////////////////////////////////////////////////////////////////
    // /////////////////////// IMPLEMENTATION OF INHERITED METHODS
    // ///////////////////////
    // ////////////////////////////////////////////////////////////////////////////////////

    /** @see DefaultFileUploadThread#beforeRequest(int, int) */
    @Override
    void beforeRequest(int firstFileToUploadParam, int nbFilesToUploadParam)
            throws JUploadException {
        setAllHead(firstFileToUploadParam, nbFilesToUploadParam, boundary);
        setAllTail(firstFileToUploadParam, nbFilesToUploadParam, boundary);
    }

    /** @see DefaultFileUploadThread#getAdditionnalBytesForUpload(int) */
    @Override
    long getAdditionnalBytesForUpload(int indexFile) {
        return heads[indexFile].length() + tails[indexFile].length();
    }

    /** @see DefaultFileUploadThread#afterFile(int) */
    @Override
    void afterFile(int index) throws JUploadException {
        try {
            httpDataOut.writeBytes(tails[index]);
        } catch (Exception e) {
            throw new JUploadException(e, this.getClass().getName()
                    + ".afterFile(index)");
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
            httpDataOut.writeBytes(heads[index]);
        } catch (Exception e) {
            throw new JUploadException(e, this.getClass().getName()
                    + ".beforeFile(index)");
        }
    }

    /** @see DefaultFileUploadThread#cleanAll() */
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
            httpDataOut.close();
        } catch (NullPointerException e) {
            // httpDataOut is already null ...
        } catch (IOException e) {
            localException = new JUploadException(e, getClass().getName()
                    + ".cleanRequest() (10)");
            uploadPolicy.displayErr(uploadPolicy.getString("errDuringUpload")
                    + " (httpDataOut.close) (" + e.getClass()
                    + ".doUpload()) : " + localException.getMessage());
        } finally {
            httpDataOut = null;
        }

        try {
            // Throws java.io.IOException
            httpDataIn.close();
        } catch (NullPointerException e) {
            // httpDataIn is already null ...
        } catch (IOException e) {
            if (localException != null) {
                localException = new JUploadException(e, getClass().getName()
                        + ".cleanRequest() (20)");
                uploadPolicy.displayErr(uploadPolicy
                        .getString("errDuringUpload")
                        + " (httpDataIn.close) ("
                        + e.getClass()
                        + ".doUpload()) : " + localException.getMessage());
            }
        } finally {
            httpDataIn = null;
        }

        try {
            // Throws java.io.IOException
            sock.close();
        } catch (NullPointerException e) {
            // sock is already null ...
        } catch (IOException e) {
            if (localException != null) {
                localException = new JUploadException(e, getClass().getName()
                        + ".cleanRequest() (30)");
                uploadPolicy.displayErr(uploadPolicy
                        .getString("errDuringUpload")
                        + " (sock.close)("
                        + e.getClass()
                        + ".doUpload()) : "
                        + e.getMessage());
            }
        } finally {
            sock = null;
        }

        if (localException != null) {
            throw localException;
        }
    }

    @Override
    void finishRequest() throws JUploadException {
        boolean readingHttpBody = false;
        String line;

        sbHttpResponseBody = new StringBuffer();
        try {
            while ((line = httpDataIn.readLine()) != null && !stop) {
                this.addServerOutPut(line);
                this.addServerOutPut("\n");

                // Store the http body
                if (readingHttpBody) {
                    sbHttpResponseBody.append(line).append("\n");
                }
                if (line.length() == 0) {
                    // Next lines will be the http body (or perhaps we already
                    // are in the body, but it's Ok anyway)
                    readingHttpBody = true;
                }
            }// while
        } catch (Exception e) {
            throw new JUploadException(e, this.getClass().getName()
                    + ".finishRequest()");
        }
    }

    /** @see DefaultFileUploadThread#getResponseBody() */
    @Override
    String getResponseBody() throws JUploadException {
        return sbHttpResponseBody.toString();
    }

    /** @see DefaultFileUploadThread#getOutputStream() */
    @Override
    OutputStream getOutputStream() throws JUploadException {
        return httpDataOut;
    }

    /**
     * Helper function for perforing a proxy CONNECT request.
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
        uploadPolicy.displayDebug(
                "Proxy response: " + line, 40);
        proxyIn.readLine(); // eat the header delimiter
        // we now are connected ...
        return proxysock;
    }

    /** @see DefaultFileUploadThread#startRequest(long, boolean, int, boolean) */
    @Override
    void startRequest(long contentLength, boolean bChunkEnabled, int chunkPart,
            boolean bLastChunk) throws JUploadException {
        StringBuffer header = new StringBuffer();
        String action = "init (FileUploadThreadHTTP)";

        try {
            String chunkHttpParam = "jupart=" + chunkPart + "&jufinal="
                    + (bLastChunk ? "1" : "0");
            uploadPolicy.displayDebug("chunkHttpParam: " + chunkHttpParam, 40);

            action = "get URL";
            URL url = new URL(uploadPolicy.getPostURL());

            action = "check proxy";
            Proxy proxy = null;
            proxy = ProxySelector.getDefault().select(url.toURI()).get(0);
            boolean useProxy = ((proxy != null) && (proxy.type() != Proxy.Type.DIRECT));
            boolean useSSL = url.getProtocol().equals("https");

            // Header: Request line
            action = "append headers";
            // Let's clear it. Useful only for chunked uploads.
            header.setLength(0);
            header.append("POST ");
            if (useProxy && (!useSSL)) {
                // with a proxy we need the absolute URL, but only if not
                // using SSL. (with SSL, we first use the proxy CONNECT method,
                // and then a plain request.)
                header.append(url.getProtocol());
                header.append("://");
                header.append(url.getHost());
            }
            header.append(url.getPath());

            if (null != url.getQuery() && !"".equals(url.getQuery())) {
                header.append("?").append(url.getQuery());
                // In case we divided the current upload in chunks, we have to
                // give some information about it
                // to the server:
                if (bChunkEnabled) {
                    header.append("&").append(chunkHttpParam);
                }
            } else if (bChunkEnabled) {
                header.append("?").append(chunkHttpParam);
            }

            header.append(" ").append(uploadPolicy.getServerProtocol()).append(
                    "\r\n");
            // Header: General
            header.append("Host: ");
            header.append(url.getHost());
            header.append("\r\n");
            header.append("Accept: */*\r\n");
            header.append("Content-type: multipart/form-data; boundary=");
            header.append(boundary.substring(2, boundary.length()) + "\r\n");
            header.append("Connection: close\r\n");
            header.append("Content-length: ").append(contentLength - 2).append(
                    "\r\n");

            // Get specific headers for this upload.
            uploadPolicy.onAppendHeader(header);

            // Blank line (end of header)
            header.append("\r\n");

            // ////////////////////////////////////////////////////////////////////////////////////////////////
            // Management of SSL, thanks to David Gnedt
            // Check if SSL connection is needed
            if (url.getProtocol().equals("https")) {
                SSLContext context = SSLContext.getInstance("SSL");
                // Allow all certificates
                context.init(null, new X509TrustManager[] { new TM() }, null);
                if (useProxy) {
                    if (proxy.type() == Proxy.Type.HTTP) {
                        // First establish a CONNECT, then do a normal SSL thru
                        // that connection.
                        action = "proxy connect";
                        uploadPolicy.displayDebug(
                                "Using SSL socket, via proxy", 20);
                        String host = url.getHost();
                        int port = (-1 == url.getPort()) ? 443 : url.getPort();
                        sock = (Socket) context.getSocketFactory()
                                .createSocket(
                                        HttpProxyConnect(proxy, host, port),
                                        host, port, true);
                    } else if (proxy.type() == Proxy.Type.SOCKS) {
                        throw new ConnectException("SOCKS proxy not supported");
                    } else
                        throw new ConnectException("Unkown proxy type "
                                + proxy.type());
                } else {
                    // If port not specified then use default https port 443.
                    uploadPolicy.displayDebug(
                            "Using SSL socket, direct connection", 20);
                    sock = (Socket) context.getSocketFactory().createSocket(
                            url.getHost(),
                            (-1 == url.getPort()) ? 443 : url.getPort());
                }
            } else {
                // If we are not in SSL, just use the old code.
                if (useProxy) {
                    if (proxy.type() == Proxy.Type.HTTP) {
                        InetSocketAddress sa = (InetSocketAddress) proxy
                                .address();
                        String host = (sa.isUnresolved()) ? sa.getHostName()
                                : sa.getAddress().getHostAddress();
                        int port = sa.getPort();
                        uploadPolicy.displayDebug(
                                "Using non SSL socket, proxy=" + host + ":"
                                        + port, 20);
                        sock = new Socket(host, port);
                    } else if (proxy.type() == Proxy.Type.SOCKS) {
                        throw new ConnectException("SOCKS proxy not supported");
                    } else
                        throw new ConnectException("Unkown proxy type "
                                + proxy.type());
                } else {
                    uploadPolicy.displayDebug(
                            "Using non SSL socket, direct connection", 20);
                    sock = new Socket(url.getHost(), (-1 == url.getPort()) ? 80
                            : url.getPort());
                }
            }
            // ////////////////////////////////////////////////////////////////////////////////////////////////

            httpDataOut = new DataOutputStream(new BufferedOutputStream(sock
                    .getOutputStream()));
            httpDataIn = new BufferedReader(new InputStreamReader(sock
                    .getInputStream()));
            // DataInputStream datain = new DataInputStream(new
            // BufferedInputStream(sock.getInputStream()));

            // Send http request to server
            action = "send bytes (1)";
            uploadPolicy.displayDebug(header.toString(), 100);
            httpDataOut.writeBytes(header.toString());
        } catch (Exception e) {
            throw new JUploadException(e, this.getClass().getName()
                    + ".startRequest (" + action + ")");
        }

        if (uploadPolicy.getDebugLevel() >= 80) {
            uploadPolicy.displayDebug(
                    "Sent to server : \n" + header.toString(), 80);
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
    private String getRandomString() {
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
     * Construction of the head for each file.
     * 
     * @param firstFileToUpload
     *            The index of the first file to upload, in the
     *            {@link #filesToUpload} area.
     * @param nbFilesToUpload
     *            Number of file to upload, in the next HTTP upload request.
     *            These files are taken from the {@link #filesToUpload} area
     * @param bound
     *            The String boundary between the post data in the HTTP request.
     * 
     * @throws JUploadException
     */
    private void setAllHead(int firstFileToUpload, int nbFilesToUpload,
            String bound) throws JUploadException {
        for (int i = 0; i < nbFilesToUpload; i++) {
            heads[i] = filesToUpload[firstFileToUpload + i].getFileHeader(i,
                    bound, -1);
        }
    }

    /**
     * Construction of the tail for each file.
     * 
     * @param firstFileToUpload
     *            The index of the first file to upload, in the
     *            {@link #filesToUpload} area.
     * @param nbFilesToUpload
     *            Number of file to upload, in the next HTTP upload request.
     *            These files are taken from the {@link #filesToUpload} area
     * @param bound
     *            Current boudnary, to apply for these tails.
     */
    private void setAllTail(int firstFileToUpload, int nbFilesToUpload,
            String bound) {
        for (int i = 0; i < nbFilesToUpload; i++) {
            tails[firstFileToUpload + i] = ("\r\n");
        }
        // Telling the Server we have Finished.
        tails[firstFileToUpload + nbFilesToUpload - 1] += bound + "--\r\n";
    }
}
