//
// $Id$
//
// jupload - A file upload applet.
// Copyright 2007 The JUpload Team
//
// Created: 2006-05-06
// Creator: etienne_sf
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

package wjhk.jupload2.policies;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import wjhk.jupload2.context.JUploadContext;

/**
 * This class is used to control creation of the uploadPolicy instance,
 * according to applet parameters (or System properties). <BR>
 * <BR>
 * The used parameters are:
 * <UL>
 * <LI>postURL: The URL where files are to be uploaded. This parameter is
 * mandatory if called from a servlet.
 * <LI>uploadPolicy: the class name to be used as a policy. Currently available
 * : not defined (then use DefaultUploadPolicy),
 * {@link wjhk.jupload2.policies.DefaultUploadPolicy},
 * {@link wjhk.jupload2.policies.CoppermineUploadPolicy}
 * </UL>
 *
 * @author etienne_sf
 * @version $Revision$
 */
public class UploadPolicyFactory {

    /**
     * Returns an upload Policy for the given applet and URL. All other
     * parameters for the uploadPolicy are take from avaiable applet parameters
     * (or from system properties, if it is not run as an applet).
     *
     * @param theAppletContext if not null : use this Applet Parameters. If
     *            null, use System properties.
     * @return The newly created UploadPolicy.
     * @throws Exception
     */
    public static UploadPolicy getUploadPolicy(JUploadContext theAppletContext)
            throws Exception {
        UploadPolicy uploadPolicy = theAppletContext.getUploadPolicy();

        if (uploadPolicy == null) {
            // Let's create the update policy.
            String uploadPolicyStr = theAppletContext.getParameter(
                    UploadPolicy.PROP_UPLOAD_POLICY,
                    UploadPolicy.DEFAULT_UPLOAD_POLICY);
            int debugLevel = theAppletContext.getParameter(
                    UploadPolicy.PROP_DEBUG_LEVEL,
                    UploadPolicy.DEFAULT_DEBUG_LEVEL);

            String action = null;
            boolean usingDefaultUploadPolicy = false;
            try {
                logDebug("Trying to load the given uploadPolicy: "
                        + uploadPolicyStr, debugLevel);

                action = uploadPolicyStr;
                Class<?> uploadPolicyClass = null;
                // Our default is "DefaultUploadPolicy", (without prefix)
                // so we try the prefixed variant first. But only, if the
                // user had specified an unqualified class name.
                if (!uploadPolicyStr.contains(".")) {
                    try {
                        uploadPolicyClass = Class
                                .forName("wjhk.jupload2.policies."
                                        + uploadPolicyStr);
                        logDebug("wjhk.jupload2.policies." + uploadPolicyStr
                                + " class found.", debugLevel);
                    } catch (ClassNotFoundException e1) {
                        logDebug(e1.getClass().getName()
                                + " when looking for [wjhk.jupload2.policies.]"
                                + uploadPolicyStr, debugLevel);
                        uploadPolicyClass = null;
                    }
                }
                if (null == uploadPolicyClass) {
                    // Let's try without the prefix
                    try {
                        uploadPolicyClass = Class.forName(uploadPolicyStr);
                        logDebug(uploadPolicyStr + " class found.", debugLevel);
                    } catch (ClassNotFoundException e2) {
                        logDebug(e2.getClass().getName()
                                + " when looking for the given uploadPolicy ("
                                + uploadPolicyStr + ")", debugLevel);
                        // Too bad, we don't know how to create this class.
                        // Fall back to built-in default.
                        usingDefaultUploadPolicy = true;
                        uploadPolicyClass = Class
                                .forName("wjhk.jupload2.policies.DefaultUploadPolicy");
                        logDebug(
                                "Using default upload policy: wjhk.jupload2.policies.DefaultUploadPolicy",
                                debugLevel);
                    }
                }
                action = "constructorParameters";
                Class<?>[] constructorParameters = {
                    Class.forName("wjhk.jupload2.context.JUploadContext")
                };
                Constructor<?> constructor = uploadPolicyClass
                        .getConstructor(constructorParameters);
                Object[] params = {
                    theAppletContext
                };
                action = "newInstance";
                uploadPolicy = (UploadPolicy) constructor.newInstance(params);
            } catch (Exception e) {
                if (e instanceof InvocationTargetException) {
                    // If the policy's constructor has thrown an exception,
                    // Get that "real" exception and print its details and
                    // stack trace
                    Throwable t = ((InvocationTargetException) e)
                            .getTargetException();
                    System.out.println("-ERROR- " + e.getClass().getName()
                            + " (message: " + t.getMessage() + ")");
                    t.printStackTrace();
                }
                System.out.println("-ERROR- " + e.getClass().getName() + " in "
                        + action + "(error message: " + e.getMessage() + ")");
                throw e;
            }

            // The current values are displayed here, after the full
            // initialization of all classes.
            // It could also be displayed in the DefaultUploadPolicy (for
            // instance), but then, the
            // display wouldn't show the modifications done by superclasses.
            uploadPolicy.displayDebug("uploadPolicy parameter = "
                    + uploadPolicyStr, 1);
            if (usingDefaultUploadPolicy) {
                uploadPolicy.displayWarn("Unable to create the '"
                        + uploadPolicyStr
                        + "'. Using the DefaultUploadPolicy instead.");
            } else {
                uploadPolicy.displayDebug("uploadPolicy = "
                        + uploadPolicy.getClass().getName(), 20);
            }

            // Then, we display the applet parameter list.
            uploadPolicy.displayParameterStatus();
        }

        return uploadPolicy;
    }

    /**
     * Help to log debug information to the user. Use of log4j, one day in the
     * future, would help.
     *
     * @param currentDebugLevel
     */
    private static void logDebug(String msg, int currentDebugLevel) {
        if (currentDebugLevel > 0) {
            System.out.println("[DEBUG] " + msg);
        }
    }
}
