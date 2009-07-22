//
// $Id: JUploadApplet.java 88 2007-05-02 00:04:52Z
// /C=DE/ST=Baden-Wuerttemberg/O=ISDN4Linux/OU=Fritz
// Elfert/CN=svn-felfert@isdn4linux.de/emailAddress=fritz@fritz-elfert.de $
//
// jupload - A file upload applet.
// Copyright 2007 The JUpload Team
//
// Created: 2007-04-28
// Creator: felfert
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

package wjhk.jupload2.gui.filepanel;

import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import wjhk.jupload2.policies.UploadPolicy;

/**
 * Technical class, used to display file sizes. Used in
 * {@link wjhk.jupload2.gui.filepanel.FilePanelJTable}.
 * 
 * @author felfert
 * @version $Revision$
 */
public class SizeRenderer extends DefaultTableCellRenderer {

    /** A generated serialVersionUID, to avoid warning during compilation */
    private static final long serialVersionUID = -2029129064667754146L;

    /**
     * The current upload policy
     */
    private UploadPolicy uploadPolicy = null;

    /** Size of one gigabyte, for file size display */
    private static final double gB = 1024L * 1024L * 1024L;

    /** Size of one megabyte, for file size display */
    private static final double mB = 1024L * 1024L;

    /** Size of one kilobyte, for file size display */
    private static final double kB = 1024L;

    /**
     * Creates a new instance.
     * 
     * @param uploadPolicy The policy to be used for providing the translated
     *            unit strings.
     */
    public SizeRenderer(UploadPolicy uploadPolicy) {
        super();
        this.uploadPolicy = uploadPolicy;
    }

    /**
     * @see javax.swing.table.DefaultTableCellRenderer#getTableCellRendererComponent(javax.swing.JTable,
     *      java.lang.Object, boolean, boolean, int, int)
     */
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {
        Component cell = super.getTableCellRendererComponent(table, value,
                isSelected, hasFocus, row, column);

        if (value instanceof Long) {
            setValue(formatFileSize(((Long) value).longValue(),
                    this.uploadPolicy));
            super.setHorizontalAlignment(RIGHT);
        } else if (value != null) {
            // We have a value, but it's not a Long.
            this.uploadPolicy
                    .displayWarn("value is not an instance of Long, in SizeRenderer.getTableCellRendererComponent(");
        }
        return cell;
    }

    // ////////////////////////////////////////////////////////////////////////////
    // Various utilities for file size calculation
    // ////////////////////////////////////////////////////////////////////////////

    /**
     * Format a number of bytes into a well formatted string, like 122mB.
     * 
     * @param fileUploadSpeedParam
     * @param uploadPolicy
     * @return The formatted file upload speed, to be displayed to the user
     */
    public static String formatFileUploadSpeed(double fileUploadSpeedParam,
            UploadPolicy uploadPolicy) {
        String unit;
        double fileUploadSpeed = fileUploadSpeedParam;
        if (fileUploadSpeed >= gB) {
            fileUploadSpeed /= gB;
            unit = uploadPolicy.getString("speedunit_gb_per_second");
        } else if (fileUploadSpeed >= mB) {
            fileUploadSpeed /= mB;
            unit = uploadPolicy.getString("speedunit_mb_per_second");
        } else if (fileUploadSpeed >= kB) {
            fileUploadSpeed /= kB;
            unit = uploadPolicy.getString("speedunit_kb_per_second");
        } else {
            unit = uploadPolicy.getString("speedunit_b_per_second");
        }

        return String.format("%1$,3.2f %2$s", fileUploadSpeed, unit);

    }

    /**
     * Format a number of bytes of a file size (or a number of uploaded bytes,
     * or whatever), into a well formatted string, like 122mB.
     * 
     * @param fileSize
     * @param uploadPolicy
     * @return The formatted file size, to display to the user.
     */
    public static String formatFileSize(double fileSize,
            UploadPolicy uploadPolicy) {
        final String sizeunit_gigabytes = uploadPolicy
                .getString("unitGigabytes");
        final String sizeunit_megabytes = uploadPolicy
                .getString("unitMegabytes");
        final String sizeunit_kilobytes = uploadPolicy
                .getString("unitKilobytes");
        final String sizeunit_bytes = uploadPolicy.getString("unitBytes");
        String unit;

        double fileSizeToDisplay = fileSize;
        if (fileSizeToDisplay >= gB) {
            fileSizeToDisplay /= gB;
            unit = sizeunit_gigabytes;
        } else if (fileSizeToDisplay >= mB) {
            fileSizeToDisplay /= mB;
            unit = sizeunit_megabytes;
        } else if (fileSizeToDisplay >= kB) {
            fileSizeToDisplay /= kB;
            unit = sizeunit_kilobytes;
        } else {
            unit = sizeunit_bytes;
        }

        return String.format("%1$,3.2f %2$s", fileSizeToDisplay, unit);
    }

}
