//
// $Id$
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

package wjhk.jupload2.upload;

import java.awt.BorderLayout;
import java.io.BufferedReader;
import java.io.FileInputStream;
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
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import javax.swing.JEditorPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import wjhk.jupload2.policies.UploadPolicy;

/**
 * This class implements the task of connecting to a HTTP(S) url using a proxy.
 * 
 * @author felfert
 */
public class HttpConnect {

    private UploadPolicy uploadPolicy;

    /**
     * An implementation of {@link javax.net.ssl.X509TrustManager} which accepts
     * any certificate.
     */
    protected final class InteractiveTrustManager implements X509TrustManager {

        private TrustManagerFactory tmf = null;

        private KeyStore ks = null;

        // TODO: Make this configurable when everything works
        private boolean isDummy = true;

        public InteractiveTrustManager(String passwd)
                throws NoSuchAlgorithmException, KeyStoreException,
                CertificateException, IOException {
            if (!this.isDummy) {
                String tsname = System.getProperty("javax.net.ssl.trustStore");
                if (null == tsname)
                    tsname = System.getProperty("java.home")
                            + "/lib/security/cacerts";
                if (null == passwd) {
                    // The default password as distributed by Sun.
                    passwd = "changeit";
                }
                FileInputStream is = new FileInputStream(tsname);
                this.ks = KeyStore.getInstance(KeyStore.getDefaultType());
                this.ks.load(is, passwd.toCharArray());
                is.close();
                // this.kmf = KeyManagerFactory.getInstance(KeyManagerFactory
                // .getDefaultAlgorithm());
                // this.kmf.init(this.ks, passwd.toCharArray());
                this.tmf = TrustManagerFactory.getInstance(TrustManagerFactory
                        .getDefaultAlgorithm());
                this.tmf.init(this.ks);
            }

        }

        public KeyManager[] getKeyManagers() {
            return null; // this.isDummy ? null : this.kmf.getKeyManagers();
        }

        public X509TrustManager[] getTrustManagers() {
            return new X509TrustManager[] {
                this
            };
        }

        /**
         * @see javax.net.ssl.X509TrustManager#checkClientTrusted(java.security.cert.X509Certificate[],
         *      java.lang.String)
         */
        @SuppressWarnings("unused")
        public void checkClientTrusted(@SuppressWarnings("unused")
        X509Certificate[] arg0, @SuppressWarnings("unused")
        String arg1) throws CertificateException {
            System.out.println("checkClientTrusted");
            // Nothing to do.
        }

        private String formatDN(String dn, String cn) {

            StringBuffer ret = new StringBuffer();
            StringTokenizer t = new StringTokenizer(dn, ",");
            while (t.hasMoreTokens()) {
                String tok = t.nextToken();
                while (tok.endsWith("\\"))
                    tok += t.nextToken();
                String kv[] = tok.split("=", 2);
                if (kv.length == 2) {
                    if (kv[0].equals("C"))
                        ret.append("<tr><td>Country:</td><td>").append(kv[1])
                                .append("</td></tr>\n");
                    if (kv[0].equals("CN")) {
                        boolean ok = true;
                        if (null != cn)
                            ok = cn.equals(kv[1]);
                        ret.append("<tr><td>Common name:</td><td");
                        ret.append(ok ? ">" : " class=\"err\">").append(kv[1])
                                .append("</td></tr>\n");
                    }
                    if (kv[0].equals("L"))
                        ret.append("<tr><td>Locality:</td><td>").append(kv[1])
                                .append("</td></tr>\n");
                    if (kv[0].equals("ST"))
                        ret.append("<tr><td>State or province:</td><td>")
                                .append(kv[1]).append("</td></tr>\n");
                    if (kv[0].equals("O"))
                        ret.append("<tr><td>Organization:</td><td>").append(
                                kv[1]).append("</td></tr>\n");
                    if (kv[0].equals("OU"))
                        ret.append("<tr><td>Organizational unit:</td><td>")
                                .append(kv[1]).append("</td></tr>\n");
                }
            }
            return ret.toString();
        }

        private void CertDialog(X509Certificate c) throws CertificateException {
            int i;
            boolean expired = false;
            boolean notyet = false;
            try {
                c.checkValidity();
            } catch (CertificateExpiredException e1) {
                expired = true;
            } catch (CertificateNotYetValidException e2) {
                notyet = true;
            }

            StringBuffer msg = new StringBuffer();
            msg.append("<html><head>");
            msg.append("<style type=\"text/css\">\n");
            msg
                    .append("td, th, p, body { font-family: Arial, Helvetica, sans-serif; }\n");
            msg.append("th { text-align: left; }\n");
            msg.append("td { margin-left: 20; }\n");
            msg.append(".err { color: red; }\n");
            msg.append("</style>\n");
            msg.append("</head><body>");
            msg
                    .append("<h3>The certificate, presented by the server could not be verified.</h3>");
            msg.append("<h4>Certificate details:</h4>");
            msg.append("<table>");
            msg.append("<tr><th colspan=2>Subject:</th></tr>");
            msg.append(formatDN(c.getSubjectX500Principal().getName(), "foo"));
            msg.append("<tr><td>Not before:</td>");
            msg.append(notyet ? "<td class=\"err\">" : "<td>").append(
                    c.getNotBefore()).append("</td></tr>\n");
            msg.append("<tr><td>Not after:</td>");
            msg.append(expired ? "<td class=\"err\">" : "<td>").append(
                    c.getNotAfter()).append("</td></tr>\n");
            msg.append("<tr><td>Serial:</td><td>");
            msg.append(c.getSerialNumber());
            msg.append("</td></tr>\n");
            msg.append("<tr><td>SHA1 Fingerprint:</td><td>");
            MessageDigest d;
            StringBuffer fp = new StringBuffer();
            try {
                d = MessageDigest.getInstance("SHA1");
            } catch (NoSuchAlgorithmException e) {
                throw new CertificateException(
                        "Unable to calculate certificate SHA1 fingerprint: "
                                + e.getMessage());
            }
            byte[] sha1sum = d.digest(c.getEncoded());
            for (i = 0; i < sha1sum.length; i++) {
                if (i > 0)
                    fp.append(":");
                fp.append(Integer.toHexString((sha1sum[i] >> 4) & 0x0f));
                fp.append(Integer.toHexString(sha1sum[i] & 0x0f));
            }
            msg.append(fp).append("</td></tr>\n");
            fp.setLength(0);
            msg.append("<tr><td>MD5 Fingerprint:</td><td>");
            try {
                d = MessageDigest.getInstance("MD5");
            } catch (NoSuchAlgorithmException e) {
                throw new CertificateException(
                        "Unable to calculate certificate MD5 fingerprint: "
                                + e.getMessage());
            }
            byte[] md5sum = d.digest(c.getEncoded());
            for (i = 0; i < md5sum.length; i++) {
                if (i > 0)
                    fp.append(":");
                fp.append(Integer.toHexString((md5sum[i] >> 4) & 0x0f));
                fp.append(Integer.toHexString(md5sum[i] & 0x0f));
            }
            msg.append(fp).append("</td></tr>\n");
            msg.append("</table><table>");
            msg.append("<tr><th colspan=2>Issuer:</th></tr>");
            msg.append(formatDN(c.getIssuerX500Principal().getName(), null));
            msg.append("</table>");
            msg.append("<p><b>Do you want to accept this certificate?</b></p>");
            msg.append("</body></html>");

            JPanel p = new JPanel();
            p.setLayout(new BorderLayout());
            JEditorPane ep = new JEditorPane("text/html", msg.toString());
            ep.setEditable(false);
            ep.setBackground(p.getBackground());
            p.add(ep, BorderLayout.CENTER);

            int ans = JOptionPane.showOptionDialog(null, p,
                    "SSL Certificate Alert", JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.WARNING_MESSAGE, null, new String[] {
                            "Always", "Only for this session", "No"
                    }, "No");
            switch (ans) {
                case JOptionPane.CANCEL_OPTION:
                case JOptionPane.CLOSED_OPTION:
                    throw new CertificateException(
                            "Server certificate rejected.");
                case JOptionPane.NO_OPTION:
                case JOptionPane.YES_OPTION:
                    // Add certificate to truststore
                    try {
                        this.ks.setCertificateEntry(fp.toString(), c);
                    } catch (KeyStoreException e) {
                        throw new CertificateException(
                                "Unable to add certificate: " + e.getMessage());
                    }
                    if (ans == JOptionPane.YES_OPTION) {
                        // TODO: Save truststore for permanent acceptance.
                    }
            }
        }

        /**
         * @see javax.net.ssl.X509TrustManager#checkServerTrusted(java.security.cert.X509Certificate[],
         *      java.lang.String)
         */
        @SuppressWarnings("unused")
        public void checkServerTrusted(X509Certificate[] chain, String authType)
                throws CertificateException {
            System.out.println("checkServerTrusted");
            if (!this.isDummy) {
                if (null == chain || chain.length == 0)
                    throw new IllegalArgumentException(
                            "Certificate chain is null or empty");

                int i;
                TrustManager[] mgrs = this.tmf.getTrustManagers();
                for (i = 0; i < mgrs.length; i++) {
                    if (mgrs[i] instanceof X509TrustManager) {
                        X509TrustManager m = (X509TrustManager) (mgrs[i]);
                        try {
                            m.checkServerTrusted(chain, authType);
                            return;
                        } catch (Exception e) {
                            // try next
                        }
                    }
                }

                // If we get here, the certificate could not be verified.
                // Ask the user what to do.
                CertDialog(chain[0]);
            }
            // In dummy mode: Nothing to do.
        }

        /**
         * @see javax.net.ssl.X509TrustManager#getAcceptedIssuers()
         */
        public X509Certificate[] getAcceptedIssuers() {
            System.out.println("getAcceptedIssuers");
            return new X509Certificate[0];
        }
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
     */
    public Socket Connect(URL url, Proxy proxy)
            throws NoSuchAlgorithmException, KeyManagementException,
            ConnectException, UnknownHostException, IOException,
            KeyStoreException, CertificateException {
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
            InteractiveTrustManager tm = new InteractiveTrustManager(null);
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
     */
    public Socket Connect(URL url) throws NoSuchAlgorithmException,
            KeyManagementException, ConnectException, UnknownHostException,
            IOException, URISyntaxException, KeyStoreException,
            CertificateException, UnrecoverableKeyException {
        Proxy proxy = ProxySelector.getDefault().select(url.toURI()).get(0);
        return Connect(url, proxy);
    }

    /**
     * Retrieve the protocol to be used for the postURL of the current policy.
     * This method issues a HEAD request to the postURL and then examines the
     * protocol version returned in the response.
     * 
     * @return The string, describing the protocol (e.g. "HTTP/1.1")
     * @throws ConnectException if anything goes wrong.
     */
    public String getProtocol() throws ConnectException {
        try {
            URL url = new URL(this.uploadPolicy.getPostURL());
            Proxy proxy = ProxySelector.getDefault().select(url.toURI()).get(0);
            boolean useProxy = ((proxy != null) && (proxy.type() != Proxy.Type.DIRECT));
            boolean useSSL = url.getProtocol().equals("https");
            Socket s = Connect(url, proxy);
            BufferedReader in = new BufferedReader(new InputStreamReader(s
                    .getInputStream()));
            StringBuffer req = new StringBuffer();
            req.append("HEAD ");
            if (useProxy && (!useSSL)) {
                // with a proxy we need the absolute URL, but only if not
                // using SSL. (with SSL, we first use the proxy CONNECT method,
                // and then a plain request.)
                req.append(url.getProtocol()).append("://").append(
                        url.getHost());
            }
            req.append(url.getPath());
            /*
             * if (null != url.getQuery() && !"".equals(url.getQuery()))
             * req.append("?").append(url.getQuery());
             */
            req.append(" ").append("HTTP/1.1").append("\r\n");
            req.append("Host: ").append(url.getHost()).append("\r\n");
            req.append("Connection: close\r\n\r\n");
            s.getOutputStream().write(req.toString().getBytes());
            if (!(s instanceof SSLSocket))
                s.shutdownOutput();
            String line = in.readLine();
            s.close();
            Matcher m = Pattern.compile("^(HTTP/\\d\\.\\d)\\s.*").matcher(line);
            if (!m.matches())
                throw new ConnectException("HEAD response: " + line);
            this.uploadPolicy.displayDebug("HEAD response: " + line, 40);
            return m.group(1);
        } catch (Exception e) {
            if (e instanceof ConnectException)
                throw (ConnectException) e;
            throw new ConnectException(e.toString());
        }
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
