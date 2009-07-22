//
// $Id: JUploadException.java 95 2007-05-02 03:27:05Z
// /C=DE/ST=Baden-Wuerttemberg/O=ISDN4Linux/OU=Fritz
// Elfert/CN=svn-felfert@isdn4linux.de/emailAddress=fritz@fritz-elfert.de $
//
// jupload - A file upload applet.
// Copyright 2007 The JUpload Team
//
// Created: 2008-04-09
// Creator: etienne_sf
// Last modified: $Date: 2008-11-28 11:45:19 +0100 (ven., 28 nov. 2008) $
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

package wjhk.jupload2.exception;

import wjhk.jupload2.policies.UploadPolicy;
import wjhk.jupload2.upload.DefaultFileUploadThread;

/**
 * This class is thrown in the {@link DefaultFileUploadThread}, when it detects
 * that the user clicked on the stop button. Using an exception allows to
 * interrupt the whole process. It's up to each method to close/free any
 * resource.
 * 
 * @author etienne_sf
 * 
 */
public class JUploadInterrupted extends Exception {

    /**
     * A default serial UID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The standard constructor for this class.
     * 
     * @param detectedInMethod The full name (with class) of the method that
     *            creates this exception. Used to log all needed information.
     * @param uploadPolicy The current upload policy, used to log a message.
     */
    public JUploadInterrupted(String detectedInMethod, UploadPolicy uploadPolicy) {
        super("Upload stopped by the user");
        uploadPolicy.displayInfo(getMessage() + " (interruption detected in: "
                + detectedInMethod + ")");
    }

}
