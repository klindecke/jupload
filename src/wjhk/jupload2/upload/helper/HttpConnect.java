//
// $Id: HttpConnect.java 286 2007-06-17 09:03:29 +0000 (dim., 17 juin 2007)
// felfert $
//
// jupload - A file upload applet.
//
// Copyright 2007 The JUpload Team
//
// Created: 07.05.2007
// Creator: felfert
// Last modified: $Date$
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.

package wjhk.jupload2.upload.helper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.Socket;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.SSLContext;

import wjhk.jupload2.exception.JUploadException;
import wjhk.jupload2.policies.UploadPolicy;

/**
 * This class implements the task of connecting to a HTTP(S) url using a proxy.
 * 
 * @author felfert
 */
public class HttpConnect {

    private final static String HTTPCONNECT_DEFAULT_PROTOCOL = "HTTP/1.1";

    /**
     * The current upload policy. Used for logging, and to get the post URL.
     * Also used to change this URL, when it has moved (301, 302 or 303 return
     * code)
     */
    private UploadPolicy uploadPolicy;

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
        String req = "CONNECT " + host + ":" + port + " "
                + HTTPCONNECT_DEFAULT_PROTOCOL + "\r\n\r\n";
        proxysock.getOutputStream().write(req.getBytes());
        BufferedReader proxyIn = new BufferedReader(new InputStreamReader(
                proxysock.getInputStream()));
        // We expect exactly one line: the proxy response
        String line = proxyIn.readLine();
        if (line == null || !line.matches("^HTTP/\\d\\.\\d\\s200\\s.*"))
            throw new ConnectException("Proxy response: " + line);
        this.uploadPolicy.displayDebug("Proxy response: " + line, 80);
        proxyIn.readLine(); // eat the header delimiter
        // we now are connected ...
        return proxysock;
    }

    /**
     * Connects to a given URL.
     * 
     * @param url The URL to connect to
     * @param proxy The proxy to be used, may be null if direct connection is
     *            needed
     * @return A socket, connected to the specified URL. May be null if an error
     *         occurs.
     * @throws NoSuchAlgorithmException
     * @throws KeyManagementException
     * @throws IOException
     * @throws UnknownHostException
     * @throws ConnectException
     * @throws CertificateException
     * @throws KeyStoreException
     * @throws UnrecoverableKeyException
     * @throws IllegalArgumentException
     */
    public Socket connect(URL url, Proxy proxy)
            throws NoSuchAlgorithmException, KeyManagementException,
            ConnectException, UnknownHostException, IOException,
            KeyStoreException, CertificateException, IllegalArgumentException,
            UnrecoverableKeyException {
        // Temporary socket for SOCKS support
        Socket tsock;
        Socket ret = null;
        String host = url.getHost();
        int port;
        boolean useProxy = ((proxy != null) && (proxy.type() != Proxy.Type.DIRECT));

        // Check if SSL connection is needed
        if (url.getProtocol().equals("https")) {
            port = (-1 == url.getPort()) ? 443 : url.getPort();
            SSLContext context = SSLContext.getInstance("SSL");
            // Allow all certificates
            InteractiveTrustManager tm = new InteractiveTrustManager(
                    this.uploadPolicy, url.getHost(), null);
            context.init(tm.getKeyManagers(), tm.getTrustManagers(),
                    SecureRandom.getInstance("SHA1PRNG"));
            if (useProxy) {
                if (proxy.type() == Proxy.Type.HTTP) {
                    // First establish a CONNECT, then do a normal SSL
                    // thru that connection.
                    this.uploadPolicy.displayDebug(
                            "Using SSL socket, via HTTP proxy", 20);
                    ret = context.getSocketFactory().createSocket(
                            HttpProxyConnect(proxy, host, port), host, port,
                            true);
                } else if (proxy.type() == Proxy.Type.SOCKS) {
                    this.uploadPolicy.displayDebug(
                            "Using SSL socket, via SOCKS proxy", 20);
                    tsock = new Socket(proxy);
                    tsock.connect(new InetSocketAddress(host, port));
                    ret = context.getSocketFactory().createSocket(tsock, host,
                            port, true);
                } else
                    throw new ConnectException("Unkown proxy type "
                            + proxy.type());
            } else {
                // If port not specified then use default https port
                // 443.
                this.uploadPolicy.displayDebug(
                        "Using SSL socket, direct connection", 20);
                ret = context.getSocketFactory().createSocket(host, port);
            }
        } else {
            // If we are not in SSL, just use the old code.
            port = (-1 == url.getPort()) ? 80 : url.getPort();
            if (useProxy) {
                if (proxy.type() == Proxy.Type.HTTP) {
                    InetSocketAddress sa = (InetSocketAddress) proxy.address();
                    host = (sa.isUnresolved()) ? sa.getHostName() : sa
                            .getAddress().getHostAddress();
                    port = sa.getPort();
                    this.uploadPolicy.displayDebug(
                            "Using non SSL socket, proxy=" + host + ":" + port,
                            20);
                    ret = new Socket(host, port);
                } else if (proxy.type() == Proxy.Type.SOCKS) {
                    this.uploadPolicy.displayDebug(
                            "Using non SSL socket, via SOCKS proxy", 20);
                    tsock = new Socket(proxy);
                    tsock.connect(new InetSocketAddress(host, port));
                    ret = tsock;
                } else
                    throw new ConnectException("Unkown proxy type "
                            + proxy.type());
            } else {
                this.uploadPolicy.displayDebug(
                        "Using non SSL socket, direct connection", 20);
                ret = new Socket(host, port);
            }
        }
        return ret;
    }

    /**
     * Connects to a given URL automatically using a proxy.
     * 
     * @param url The URL to connect to
     * @return A socket, connected to the specified URL. May be null if an error
     *         occurs.
     * @throws NoSuchAlgorithmException
     * @throws KeyManagementException
     * @throws IOException
     * @throws UnknownHostException
     * @throws ConnectException
     * @throws URISyntaxException
     * @throws UnrecoverableKeyException
     * @throws CertificateException
     * @throws KeyStoreException
     * @throws UnrecoverableKeyException
     * @throws IllegalArgumentException
     */
    public Socket connect(URL url) throws NoSuchAlgorithmException,
            KeyManagementException, ConnectException, UnknownHostException,
            IOException, URISyntaxException, KeyStoreException,
            CertificateException, IllegalArgumentException,
            UnrecoverableKeyException {
        Proxy proxy = ProxySelector.getDefault().select(url.toURI()).get(0);
        return connect(url, proxy);
    }

    /**
     * Retrieve the protocol to be used for the postURL of the current policy.
     * This method issues a HEAD request to the postURL and then examines the
     * protocol version returned in the response.
     * 
     * @return The string, describing the protocol (e.g. "HTTP/1.1")
     * @throws URISyntaxException
     * @throws IOException
     * @throws UnrecoverableKeyException
     * @throws IllegalArgumentException
     * @throws CertificateException
     * @throws KeyStoreException
     * @throws UnknownHostException
     * @throws NoSuchAlgorithmException
     * @throws KeyManagementException
     * @throws JUploadException
     */
    public String getProtocol() throws URISyntaxException,
            KeyManagementException, NoSuchAlgorithmException,
            UnknownHostException, KeyStoreException, CertificateException,
            IllegalArgumentException, UnrecoverableKeyException, IOException,
            JUploadException {

        String protocol = HTTPCONNECT_DEFAULT_PROTOCOL;
        URL url = new URL(this.uploadPolicy.getPostURL());
        this.uploadPolicy
                .displayDebug("Checking protocol with URL: " + url, 30);
        HTTPConnectionHelper connectionHelper = new HTTPConnectionHelper(url,
                "HEAD", false, true, this.uploadPolicy);
        connectionHelper.append("\r\n");
        this.uploadPolicy.displayDebug("Before sendRequest()", 30);
        connectionHelper.sendRequest();
        this.uploadPolicy.displayDebug("After sendRequest()", 30);
        connectionHelper.getOutputStream().flush();
        if (this.uploadPolicy.getDebugLevel() >= 80) {
            this.uploadPolicy
                    .displayDebug(
                            "-------------------------------------------------------------------------",
                            80);
            this.uploadPolicy
                    .displayDebug(
                            "-----------------   HEAD message sent (start)  --------------------------",
                            80);
            this.uploadPolicy
                    .displayDebug(
                            "-------------------------------------------------------------------------",
                            80);
            this.uploadPolicy.displayDebug(connectionHelper
                    .getByteArrayEncoder().getString(), 80);
            this.uploadPolicy
                    .displayDebug(
                            "-------------------------------------------------------------------------",
                            80);
            this.uploadPolicy
                    .displayDebug(
                            "-----------------   HEAD message sent (end) -----------------------------",
                            80);
            this.uploadPolicy
                    .displayDebug(
                            "-------------------------------------------------------------------------",
                            80);
            ;
        }

        int status = connectionHelper.readHttpResponse();
        this.uploadPolicy.displayDebug("HEAD status: " + status, 30);
        String headers = connectionHelper.getResponseHeaders();

        // Let's look for the protocol
        Matcher m = Pattern.compile("^(HTTP/\\d\\.\\d)\\s(.*)\\s.*$",
                Pattern.MULTILINE).matcher(headers);
        if (!m.find()) {
            // Using default value. Already initialized.
            this.uploadPolicy
                    .displayErr("Unexpected HEAD response (can't find the protocol): will use the default one.");
        } else {
            // We will return the found protocol.
            protocol = m.group(1);
            this.uploadPolicy.displayDebug("HEAD protocol: " + protocol, 30);
        }

        // Let's check if we're facing an IIS server. The applet is compatible
        // with IIS, only if allowHttpPersistent is false.
        Pattern pIIS = Pattern.compile("^Server: .*IIS*$", Pattern.MULTILINE);
        Matcher mIIS = pIIS.matcher(headers);
        if (mIIS.find()) {
            try {
                this.uploadPolicy.setProperty(
                        UploadPolicy.PROP_ALLOW_HTTP_PERSISTENT, "false");
                this.uploadPolicy
                        .displayWarn(UploadPolicy.PROP_ALLOW_HTTP_PERSISTENT
                                + "' forced to false, for IIS compatibility (in HttpConnect.getProtocol())");
            } catch (JUploadException e) {
                this.uploadPolicy.displayWarn("Can't set property '"
                        + UploadPolicy.PROP_ALLOW_HTTP_PERSISTENT
                        + "' to false, in HttpConnect.getProtocol()");
            }
        }

        // if we got a redirection code, we must find the new Location.
        if (status == 301 || status == 302 || status == 303) {
            Pattern pLocation = Pattern.compile("^Location: (.*)$",
                    Pattern.MULTILINE);
            Matcher mLocation = pLocation.matcher(headers);
            if (mLocation.find()) {
                // We found the location where we should go instead of the
                // original postURL
                this.uploadPolicy.displayDebug("Location read: "
                        + mLocation.group(1), 50);
                changePostURL(mLocation.group(1));
            }
        }
        
        //Let's free any used resource.
        connectionHelper.dispose();

        return protocol;
    } // getProtocol()

    /**
     * Reaction of the applet when a 301, 302 et 303 return code is returned.
     * The postURL is changed according to the Location header returned.
     * 
     * @param newLocation This new location may contain the
     *            http://host.name.domain part of the URL ... or not
     */
    private void changePostURL(String newLocation) throws JUploadException {
        String currentPostURL = this.uploadPolicy.getPostURL();
        String newPostURL;
        Pattern pHostName = Pattern.compile("http://([^/]*)/.*");
        Matcher mOldPostURL = Pattern.compile("(.*)\\?(.*)").matcher(
                currentPostURL);

        // If there is an interrogation point in the original postURL, we'll
        // keep the parameters, and just changed the URI part.
        if (mOldPostURL.matches()) {
            newPostURL = newLocation + '?' + mOldPostURL.group(2);
            // Otherwise, we change the whole URL.
        } else {
            newPostURL = newLocation;
        }

        // There are three main cases or newLocation:
        // 1- It's a full URL, with host name...
        // 2- It's a local full path on the same server (begins with /)
        // 3- It's a relative path (for instance, add of a prefix in the
        // filename) (doesn't begin with /)
        Matcher mHostOldPostURL = pHostName.matcher(currentPostURL);
        if (!mHostOldPostURL.matches()) {
            // Oups ! There is a little trouble here !
            throw new JUploadException(
                    "[HttpConnect.changePostURL()] No host found in the old postURL !");
        }

        // Let's analyze the given newLocation for these three cases.
        Matcher mHostNewLocation = pHostName.matcher(newLocation);
        if (mHostNewLocation.matches()) {
            // 1- It's a full URL, with host name. We already got this URL, in
            // the newPostURL initialization.
        } else if (newLocation.startsWith("/")) {
            // 2- It's a local full path on the same server (begins with /)
            newPostURL = "http://" + mHostOldPostURL.group(1) + newPostURL;
        } else {
            // 3- It's a relative path (for instance, add of a prefix in the
            // filename) (doesn't begin with /)
            Matcher mOldPostURLAllButFilename = Pattern
                    .compile("(.*)/([^/]*)$").matcher(currentPostURL);
            if (!mOldPostURLAllButFilename.matches()) {
                // Hum, that won't be easy.
                throw new JUploadException(
                        "[HttpConnect.changePostURL()] Can't find the filename in the URL !");
            }
            newPostURL = mOldPostURLAllButFilename.group(1) + "/" + newPostURL;
        }

        // Let's store this new postURL, and display some info about the change
        this.uploadPolicy.setPostURL(newPostURL);
        this.uploadPolicy.displayInfo("postURL switched from " + currentPostURL
                + " to " + newPostURL);
    }

    /**
     * Creates a new instance.
     * 
     * @param policy The UploadPolicy to be used for logging.
     */
    public HttpConnect(UploadPolicy policy) {
        this.uploadPolicy = policy;
    }
}
