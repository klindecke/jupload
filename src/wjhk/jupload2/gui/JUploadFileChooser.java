//
// $Id: JUploadPanel.java 205 2007-05-28 20:24:01 +0000 (lun., 28 mai 2007)
// felfert $
// 
// jupload - A file upload applet.
// Copyright 2007 The JUpload Team
// 
// Created: ?
// Creator: Etienne Gauthier
// Last modified: $Date: 2007-05-28 20:24:01 +0000 (lun., 28 mai 2007) $
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

package wjhk.jupload2.gui;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFileChooser;

import wjhk.jupload2.policies.UploadPolicy;

// //////////////////////////////////////////////////////////////////////////////////////////////////
// ///////////////////////////// local class: JUploadFileView
// //////////////////////////////////////////////////////////////////////////////////////////////////

/** ImagePreview.java by FileChooserDemo2.java. */
class ImagePreview extends JComponent implements PropertyChangeListener {
    ImageIcon thumbnail = null;

    File file = null;

    public ImagePreview(JFileChooser fc) {
        setPreferredSize(new Dimension(100, 50));
        fc.addPropertyChangeListener(this);
    }

    public void loadImage() {
        if (file == null) {
            thumbnail = null;
            return;
        }
        // Don't use createImageIcon (which is a wrapper for getResource)
        // because the image we're trying to load is probably not one
        // of this program's own resources.
        ImageIcon tmpIcon = new ImageIcon(file.getPath());
        if (tmpIcon != null) {
            if (tmpIcon.getIconWidth() > 90) {
                thumbnail = new ImageIcon(tmpIcon.getImage().getScaledInstance(
                        90, -1, Image.SCALE_DEFAULT));
            } else { // no need to miniaturize
                thumbnail = tmpIcon;
            }
        }
    }

    public void propertyChange(PropertyChangeEvent e) {
        boolean update = false;
        String prop = e.getPropertyName();
        // If the directory changed, don't show an image.
        if (JFileChooser.DIRECTORY_CHANGED_PROPERTY.equals(prop)) {
            file = null;
            update = true;
            // If a file became selected, find out which one.
        } else if (JFileChooser.SELECTED_FILE_CHANGED_PROPERTY.equals(prop)) {
            file = (File) e.getNewValue();
            update = true;
        }
        // Update the preview accordingly.
        if (update) {
            thumbnail = null;
            if (isShowing()) {
                loadImage();
                repaint();
            }
        }
    }

    protected void paintComponent(Graphics g) {
        if (thumbnail == null) {
            loadImage();
        }
        if (thumbnail != null) {
            int x = getWidth() / 2 - thumbnail.getIconWidth() / 2;
            int y = getHeight() / 2 - thumbnail.getIconHeight() / 2;
            if (y < 0) {
                y = 0;
            }
            if (x < 5) {
                x = 5;
            }
            thumbnail.paintIcon(this, g, x, y);
        }
    }
}

// //////////////////////////////////////////////////////////////////////////////////////////////////
// ///////////////// JUploadFileChooser
// //////////////////////////////////////////////////////////////////////////////////////////////////

/**
 * This class allows easy personnalization of the java file chooser. It asks the
 * current upload policy for all current configuration parameters. It is created
 * by the {@link JUploadPanel} class.
 */
public class JUploadFileChooser extends JFileChooser {

    // /////////////////////////////////////////////////////////////////////////////////////////////////////
    // /////////////////////////////// Attributes
    // /////////////////////////////////////////////////////////////////////////////////////////////////////

    /** The current upload policy */
    private UploadPolicy uploadPolicy = null;

    private JUploadFileFilter fileFilter = null;

    /** This file view add picture management capabilities to the file chooser */
    private JUploadFileView fileView = null;

    // /////////////////////////////////////////////////////////////////////////////////////////////////////
    // /////////////////////////////// Methods
    // /////////////////////////////////////////////////////////////////////////////////////////////////////

    /** The 'standard' constructor for our file chooser */
    public JUploadFileChooser(UploadPolicy uploadPolicyParam) {
        this.uploadPolicy = uploadPolicyParam;

        this.fileFilter = new JUploadFileFilter(this.uploadPolicy);
        this.fileView = new JUploadFileView(this.uploadPolicy, this);
        setAccessory(new ImagePreview(this));

        // XXX:
        // This breaks usability. probably use a persistent value of a
        // cookie later.
        // this.fileChooser.setCurrentDirectory(new File(System
        // .getProperty("user.dir")));
        setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        setMultiSelectionEnabled(true);
        // The file view must be set, whether or not a file filter exists
        // for this upload policy.
        setFileView(this.fileView);
        if (this.uploadPolicy.fileFilterGetDescription() != null) {
            setFileFilter(this.fileFilter);
        }
    }

    /**
     * Shutdown any running task. Currently, only the JUploadFileView may have
     * running tasks, when calculating icon for picture files.
     */
    // TODO remove this method: it should be triggered by itself, when the file
    // chooser is closed.
    public void shutdownNow() {
        fileView.shutdownNow();
    }

}
