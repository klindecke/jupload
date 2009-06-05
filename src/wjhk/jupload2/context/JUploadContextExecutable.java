//
// $Id: JUploadApplet.java 750 2009-05-06 14:36:50Z etienne_sf $
//
// jupload - A file upload applet.
// Copyright 2007 The JUpload Team
//
// Created: ?
// Creator: William JinHua Kwong
// Last modified: $Date: 2009-05-06 16:36:50 +0200 (mer., 06 mai 2009) $
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

package wjhk.jupload2.context;

import java.awt.Cursor;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Properties;
import java.util.Vector;

import javax.swing.JApplet;
import javax.swing.JFrame;
import javax.swing.JPanel;

import wjhk.jupload2.JUploadDaemon;
import wjhk.jupload2.exception.JUploadException;

/**
 * Implementation of the {@link JUploadContext}, for an executable, that is: for
 * a stand alone application. One such context is created at run time. Its main
 * capabilities, is to load the properties either by a file in the jar file (see
 * DAEMON_PROPERTIES_FILE), or an URL given to the
 * {@link JUploadDaemon#main(String[])} method.
 * 
 * @see DefaultJUploadContext
 * @see JUploadDaemon
 * 
 * @author etienne_sf
 * @version $Revision: 750 $
 */
public class JUploadContextExecutable extends DefaultJUploadContext {

    final static String DEFAULT_PROPERTIES_FILE = "/conf/default_daemon.properties";

    final static String DAEMON_PROPERTIES_FILE = "/conf/daemon.properties";

    /**
     * The main window of the application.
     */
    private JFrame jframe = null;

    /**
     * The content pane of this window.
     */
    JPanel contentPane = null;

    /**
     * Content of the /conf/default_deamon.properties file. These value override
     * default value, that would be wrong values for the daemon standalone
     * application.
     */
    protected Properties defaultProperties = null;

    /**
     * Content of the /conf/_deamon.properties file. These value are the
     * properties given to parameterize the daemon, according to the specific
     * needs of the project.
     */
    protected Properties daemonProperties = null;

    /**
     * This constructor does nothing. It should be used by test case only.
     */
    protected JUploadContextExecutable() {
        // No action
    }

    /**
     * The constructor of the context, which needs the top level container to be
     * created.
     * 
     * @param jframe The owner TopLevelWindow
     * @param propertiesURL The URL where the configuration properties for the
     *            daemon can be read. If null, the daemon try to read the
     *            /conf/daemon.properties file, in the current jar.
     */
    public JUploadContextExecutable(JFrame jframe, String propertiesURL) {
        if (jframe == null) {
            throw new IllegalArgumentException("theApplet may not be null");
        }
        this.jframe = jframe;

        // Load default properties
        defaultProperties = loadPropertiesFromFileInJar(
                DEFAULT_PROPERTIES_FILE, null);

        // Load daemon properties: from the given URL or from the file.
        if (propertiesURL == null) {
            // No URL given. We load properties from the 'standard' file, in the
            // jar.
            daemonProperties = loadPropertiesFromFileInJar(
                    DAEMON_PROPERTIES_FILE, defaultProperties);
        } else {
            // Let's load the properties from this URL.
            daemonProperties = loadPropertiesFromURL(propertiesURL,
                    defaultProperties);
        }

        // Now, we're ready. Let's initialize the DefaultJUploadContext.
        init(this.jframe);
    }

    /**
     * Creates and loads a property file, and return the loaded result.
     * 
     * @param filename The name of the file, which contains the properties to
     *            load
     * @param defaultProperties The default properties value. Put null if no
     *            default Properties should be used.
     * @return The loaded properties. It's empty if an error occurs.
     */
    Properties loadPropertiesFromFileInJar(String filename,
            Properties defaultProperties) {
        Properties properties = new Properties(defaultProperties);
        try {
            // TODO use this.getClass() ?
            properties.load(Class.forName("wjhk.jupload2.JUploadApplet")
                    .getResourceAsStream(filename));
        } catch (IOException e1) {
            System.out.println("Error while loading " + filename + " ("
                    + e1.getClass().getName() + ")");
            e1.printStackTrace();
        } catch (ClassNotFoundException e1) {
            System.out.println("Error while loading " + filename + " ("
                    + e1.getClass().getName() + ")");
            e1.printStackTrace();
        }

        return properties;
    }

    /**
     * Creates and loads a property file, and return the loaded result.
     * 
     * @param propertiesURL The url that points to the properties configuration
     *            file for the daemon.
     * @param defaultProperties The default properties value. Put null if no
     *            default Properties should be used.
     * @return The loaded properties. It's empty if an error occurs.
     */
    private Properties loadPropertiesFromURL(String propertiesURL,
            Properties defaultProperties) {
        Properties properties = new Properties(defaultProperties);
        URL url;
        try {
            url = new URL(propertiesURL);
            URLConnection urlConnection = url.openConnection();
            properties.load(urlConnection.getInputStream());
        } catch (MalformedURLException e) {
            System.out.println("Error while loading url " + propertiesURL
                    + " (" + e.getClass().getName() + ")");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("Error while loading url " + propertiesURL
                    + " (" + e.getClass().getName() + ")");
            e.printStackTrace();
        }

        return properties;
    }

    /**
     * Get a String parameter value from applet properties or System properties.
     * 
     * @param key The name of the parameter to fetch.
     * @param def A default value which is used, when the specified parameter is
     *            not set.
     * @return The value of the applet parameter (resp. system property). If the
     *         parameter was not specified or no such system property exists,
     *         returns the given default value.
     */
    public String getParameter(String key, String def) {
        String paramStr = (daemonProperties.getProperty(key) != null ? daemonProperties
                .getProperty(key)
                : def);
        displayDebugParameterValue(key, paramStr);
        return paramStr;
    }

    /** {@inheritDoc} */

    public int getParameter(String key, int def) {
        String paramDef = Integer.toString(def);
        String paramStr = daemonProperties.getProperty(key) != null ? daemonProperties
                .getProperty(key)
                : paramDef;
        displayDebugParameterValue(key, paramStr);
        return parseInt(paramStr, def);
    }

    /** {@inheritDoc} */
    public float getParameter(String key, float def) {
        String paramDef = Float.toString(def);
        String paramStr = daemonProperties.getProperty(key) != null ? daemonProperties
                .getProperty(key)
                : paramDef;
        displayDebugParameterValue(key, paramStr);
        return parseFloat(paramStr, def);
    }

    /** {@inheritDoc} */
    public long getParameter(String key, long def) {
        String paramDef = Long.toString(def);
        String paramStr = daemonProperties.getProperty(key) != null ? daemonProperties
                .getProperty(key)
                : paramDef;
        displayDebugParameterValue(key, paramStr);
        return parseLong(paramStr, def);
    }

    /** {@inheritDoc} */
    public boolean getParameter(String key, boolean def) {
        String paramDef = (def ? "true" : "false");
        String paramStr = daemonProperties.getProperty(key) != null ? daemonProperties
                .getProperty(key)
                : paramDef;
        displayDebugParameterValue(key, paramStr);
        return parseBoolean(paramStr, def);
    }// getParameter(boolean)

    /** {@inheritDoc} */
    public void displayURL(String url, boolean success) {
        throw new UnsupportedOperationException(
                "JUploadContextExecution.displayURL(): Not implemented yet!");
    }

    /** {@inheritDoc} */
    public JApplet getApplet() {
        throw new UnsupportedOperationException(
                "Can't use getApplet(), when using the JUploadDaemon!");
    }

    /** {@inheritDoc} */
    public Cursor getCursor() {
        return this.jframe.getCursor();
    }

    /**
     * This class doesn't control the URL. It expects it to be already
     * normalized. No work here. {@inheritDoc}
     * */
    public String normalizeURL(String url) throws JUploadException {
        return url;
    }

    /** {@inheritDoc} */
    public void readCookieAndUserAgentFromNavigator(Vector<String> headers) {
        throw new UnsupportedOperationException(
                "Can't use readCookieAndUserAgentFromNavigator(), when using the JUploadDaemon!");
    }

    /** {@inheritDoc} */
    public Cursor setCursor(Cursor cursor) {
        Cursor previousCursor = this.jframe.getCursor();
        this.jframe.setCursor(cursor);
        return previousCursor;
    }

    /** {@inheritDoc} */
    public void showStatus(String status) {
        // TODO Auto-generated method stub

    }

}
