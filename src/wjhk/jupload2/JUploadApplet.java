//
// $Id$
//
// jupload - A file upload applet.
// Copyright 2007 The JUpload Team
//
// Created: ?
// Creator: William JinHua Kwong
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

package wjhk.jupload2;

import javax.swing.JApplet;

import wjhk.jupload2.context.JUploadContext;
import wjhk.jupload2.context.JUploadContextApplet;
import wjhk.jupload2.context.JavascriptHandler;
import wjhk.jupload2.policies.UploadPolicy;

/**
 * The applet. It contains quite only the call to creation of the
 * {@link JUploadContextApplet}, which contains the technical context. This
 * context is responsible for loading the relevant {@link UploadPolicy}. <BR>
 * <BR>
 * The behavior of the applet can easily be adapted, by : <DIR> <LI>Using an
 * existing {@link wjhk.jupload2.policies.UploadPolicy}, and specifying
 * parameters. <LI>Creating a new upload policy, based on the
 * {@link wjhk.jupload2.policies.DefaultUploadPolicy}, or created from scratch.
 * <BR>
 * For all details on this point, please read the <a
 * href="../../../howto-customization.html">howto-customization.html</a> page.
 *
 * @author William JinHua Kwong (largely updated by etienne_sf)
 * @version $Revision$
 */
public class JUploadApplet extends JApplet {

    /** A generated serialVersionUID, to avoid warning during compilation */
    private static final long serialVersionUID = -3207851532114846776L;

    /**
     * The current execution context.
     */
    transient JUploadContext juploadContext = null;

    /**
     * Called each time the applet is shown on the web page.
     */
    @Override
    public void start() {
        this.juploadContext = new JUploadContextApplet(this);
        // Let's refresh the display, and have the caret well placed.
        this.juploadContext.getUploadPolicy().displayInfo(
                "JUploadApplet is now started.");
    }

    /**
     * @see java.applet.Applet#stop()
     */
    @Override
    public void stop() {
        this.juploadContext.runUnload();
    }

    /**
     * This allow runtime modifications of properties, from javascript.
     * Currently, this can only be used after full initialization. This method
     * only calls the UploadPolicy.setProperty method. <BR>
     * Ex: document.jupload.setProperty(prop, value);
     *
     * @param prop The property name that must be set.
     * @param value The value of this property.
     * @see JUploadContext#setProperty(String, String)
     */
    public void setProperty(String prop, String value) {
        this.juploadContext.setProperty(prop, value);
    }

    /**
     * Javascript can call this method to start the upload.
     *
     * @return Returns the upload result. See the constants defined in the
     *         {@link JavascriptHandler} javadoc.
     */
    public String startUpload() {
        return this.juploadContext.startUpload();
    }
}
