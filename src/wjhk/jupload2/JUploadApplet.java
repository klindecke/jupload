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

import java.applet.Applet;

import javax.swing.JApplet;

import wjhk.jupload2.context.JUploadContext;
import wjhk.jupload2.context.JUploadContextApplet;

// FIXME Correct the following comment

/**
 * The applet. It contains quite only the call to creation of the
 * {@link wjhk.jupload2.gui.JUploadPanel}, which contains the real code. <BR>
 * <BR>
 * The behavior of the applet can easily be adapted, by : <DIR> <LI>Using an
 * existing {@link wjhk.jupload2.policies.UploadPolicy}, and specifying
 * parameters. <LI>Creating a new upload policy, based on the
 * {@link wjhk.jupload2.policies.DefaultUploadPolicy}, or created from scratch.
 * </DIR>
 * 
 * @author William JinHua Kwong (updated by etienne_sf)
 * @version $Revision$
 */
public class JUploadApplet extends JApplet {

    /** A generated serialVersionUID, to avoid warning during compilation */
    private static final long serialVersionUID = -3207851532114846776L;

    /**
     * The current execution context.
     */
    JUploadContext juploadContext = null;

    /**
     * Called each time the applet is shown on the web page.
     */
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


}
