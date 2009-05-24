//
// $Id: UploadPolicyFactory.java 629 2009-02-23 16:25:14Z etienne_sf $
//
// jupload - A file upload applet.
// Copyright 2007 The JUpload Team
//
// Created: 2009-05-11
// Creator: etienne_sf
// Last modified: $Date: 2009-02-23 17:25:14 +0100 (lun., 23 févr. 2009) $
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
import java.util.Vector;

import javax.swing.JApplet;

import wjhk.jupload2.exception.JUploadException;
import wjhk.jupload2.gui.JUploadPanel;
import wjhk.jupload2.gui.JUploadTextArea;
import wjhk.jupload2.policies.DefaultUploadPolicy;
import wjhk.jupload2.policies.UploadPolicy;
import wjhk.jupload2.upload.helper.ByteArrayEncoderHTTP;

/**
 * This interface it used in upload policies to get information on the current
 * Context. This context is responsible for: <DIR> <LI>Reading parameters from
 * the environment or the applet parameters. <LI>Storing the current
 * UploadPolicy (or null if not set yet) <LI> <LI>
 * </DIR>
 * 
 * 
 * @author etienne_sf
 * 
 */
public interface JUploadContext {

    /*
     * **************************************************************************
     * **************** VERSION MANAGEMENT
     * *************************************************************************
     */

    /**
     * 
     * @return The 'official' version (applet version and SVN revision)
     */
    public String getVersion();

    /**
     * @return Last modification date (date of last commit)
     */
    public String getLastModified();

    /**
     * @return Last modification date (date of last commit)
     */
    public String getBuildDate();

    /**
     * @return Last modification date (date of last commit)
     */
    public int getBuildNumber();

    /*
     * **************************************************************************
     * **************** GUI MANAGEMENT
     * *************************************************************************
     */

    /**
     * Displays a given URL, in the given target. The target is meant in html
     * &lt;A&gt; tag, and may be ignored if not relevant.
     * 
     * @param url The URL to display, in text format. It will be normalized
     *            'before use'.
     * @param success Indicates whether the upload was a success or not.
     */
    public void displayURL(String url, boolean success);

    /**
     * Retrieves the current applet. This call is still used by
     * {@link ByteArrayEncoderHTTP}, to append form variable. It will be
     * removed, in the future.
     * 
     * @return The current applet, or null if not running in an applet
     */
    public JApplet getApplet();

    /**
     * Retrieves the current log window of this applet. This log window may
     * visible or not depending on various applet parameter.
     * 
     * @return the current log window of this instance.
     * @see JUploadPanel#showOrHideLogWindow()
     */
    public JUploadTextArea getLogWindow();

    /**
     * Retrieves the current upload panel.
     * 
     * @return the current upload panel of this instance.
     */
    public JUploadPanel getUploadPanel();

    /**
     * This method the current UploadPolicy, associated with the current
     * execution context. This UploadPolicy is set once, when the application
     * start. It can not change afterwards.
     * 
     * @return The current UploadPolicy, or null of the uploadPolicy has not
     *         been set yet.
     */
    public UploadPolicy getUploadPolicy();

    /**
     * @return The current cursor.
     * @see UploadPolicy#setCursor(Cursor)
     */
    public Cursor getCursor();

    /**
     * @param cursor The cursor to set
     * @return The Cursor that was active, before setting the new one. It's up
     *         to the caller to remind it, to be able to restore it if
     *         necessary.
     * @see UploadPolicy#setCursor(Cursor)
     */
    public Cursor setCursor(Cursor cursor);

    /**
     * Sets the wait cursor on the current application (applet, executable...).
     * 
     * @return The cursor that was active before the call to this method
     * @see UploadPolicy#setCursor(Cursor)
     */
    public Cursor setWaitCursor();

    /**
     * Displays a message in the status window.
     * 
     * @param status
     */
    public void showStatus(String status);

    /*
     * **************************************************************************
     * **************** PARAMETERS MANAGEMENT
     * *************************************************************************
     */

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
    public String getParameter(String key, String def);

    /**
     * Get a String parameter value from applet properties or System properties.
     * 
     * @param key The parameter name
     * @param def The default value
     * 
     * @return the parameter value, or the default, if the system is not set.
     */
    public int getParameter(String key, int def);

    /**
     * Get a String parameter value from applet properties or System properties.
     * 
     * @param key The parameter name
     * @param def The default value
     * 
     * @return the parameter value, or the default, if the system is not set.
     */
    public float getParameter(String key, float def);

    /**
     * Get a String parameter value from applet properties or System properties.
     * 
     * @param key The parameter name
     * @param def The default value
     * 
     * @return the parameter value, or the default, if the system is not set.
     */
    public long getParameter(String key, long def);

    /**
     * Get a boolean parameter value from applet properties or System
     * properties.
     * 
     * @param key The parameter name
     * @param def The default value
     * 
     * @return the parameter value, or the default, if the system is not set.
     */
    public boolean getParameter(String key, boolean def);

    /**
     * This function try to parse value as an integer. If value is not a correct
     * integer, def is returned.
     * 
     * @param value The string value, that must be parsed
     * @param def The default value
     * @return The integer value of value, or def if value is not valid.
     */
    public int parseInt(String value, int def);

    /**
     * This function try to parse value as a float number. If value is not a
     * correct float, def is returned.
     * 
     * @param value The string value, that must be parsed
     * @param def The default value
     * @return The float value of value, or def if value is not valid.
     */
    public float parseFloat(String value, float def);

    /**
     * This function try to parse value as a Long. If value is not a correct
     * long, def is returned.
     * 
     * @param value The string value, that must be parsed
     * @param def The default value
     * @return The integer value of value, or def if value is not valid.
     */
    public long parseLong(String value, long def);

    /**
     * This function try to parse value as a boolean. If value is not a correct
     * boolean, def is returned.
     * 
     * @param value The new value for this property. If invalid, the default
     *            value is used.
     * @param def The default value: used if value is invalid.
     * @return The boolean value of value, or def if value is not a valid
     *         boolean.
     */
    public boolean parseBoolean(String value, boolean def);

    /*
     * **************************************************************************
     * **************** TECHNICAL MANAGEMENT
     * *************************************************************************
     */

    /**
     * This method allows to read the navigator cookies and userAgent. These
     * items will be added as headers, in the given Vector.
     * 
     * @param headers The headers, coming from {@link DefaultUploadPolicy}
     */
    public void readCookieAndUserAgentFromNavigator(Vector<String> headers);

    /**
     * Generates a valid URL, from a String. The generation may add the
     * documentBase of the applet.
     * 
     * @param url A url. Can be a path relative to the current one.
     * @return The normalized URL
     * @throws JUploadException
     */
    public String normalizeURL(String url) throws JUploadException;

    /**
     * Register a callback to be executed during applet termination.
     * 
     * @param o The Object instance to be registered
     * @param method The Method of that object to be registered. The method must
     *            be of type void and must not take any parameters and must be
     *            public.
     */
    public void registerUnload(Object o, String method);

    /**
     * Runs all callback that must be called when releasing the applet.
     */
    public void runUnload();

    /**
     * This allow runtime modifications of properties, from javascript.
     * Currently, this can only be used after full initialization. This method
     * only calls the UploadPolicy.setProperty method. <BR>
     * Ex: document.jupload.setProperty(prop, value);
     * 
     * @param prop The property name that must be set.
     * @param value The value of this property.
     */
    public void setProperty(String prop, String value);

    /**
     * Public method that can be called by Javascript to start upload
     * 
     * @return Returns the upload result. See the constants defined in the
     *         {@link JavascriptHandler} javadoc.
     */
    public String startUpload();
}
