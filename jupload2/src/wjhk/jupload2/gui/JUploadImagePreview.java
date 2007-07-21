//
// $Id: JUploadPanel.java 295 2007-06-27 08:43:25 +0000 (mer., 27 juin 2007) etienne_sf $
// 
// jupload - A file upload applet.
// Copyright 2007 The JUpload Team
// 
// Last modified: $Date: 2007-06-27 08:43:25 +0000 (mer., 27 juin 2007) $
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


/**
 * This class contains the accessory that displays the image preview, when in picture mode.
 * 
 * @see PictureUploadPolicy
 */
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFileChooser;

/** ImagePreview.java by FileChooserDemo2.java. */
public class JUploadImagePreview extends JComponent implements PropertyChangeListener {
        /**
         * 
         */
        private static final long serialVersionUID = -6882108570945459638L;

        ImageIcon thumbnail = null;

        File file = null;

        public JUploadImagePreview(JFileChooser fc) {
            setPreferredSize(new Dimension(100, 50));
            fc.addPropertyChangeListener(this);
        }

        public void loadImage() {
            if (this.file == null) {
                this.thumbnail = null;
                return;
            }
            // Don't use createImageIcon (which is a wrapper for getResource)
            // because the image we're trying to load is probably not one
            // of this program's own resources.
            ImageIcon tmpIcon = new ImageIcon(this.file.getPath());
            if (tmpIcon != null) {
                if (tmpIcon.getIconWidth() > 90) {
                    this.thumbnail = new ImageIcon(tmpIcon.getImage().getScaledInstance(
                            90, -1, Image.SCALE_DEFAULT));
                } else { // no need to miniaturize
                    this.thumbnail = tmpIcon;
                }
            }
        }

        public void propertyChange(PropertyChangeEvent e) {
            boolean update = false;
            String prop = e.getPropertyName();
            // If the directory changed, don't show an image.
            if (JFileChooser.DIRECTORY_CHANGED_PROPERTY.equals(prop)) {
                this.file = null;
                update = true;
                // If a file became selected, find out which one.
            } else if (JFileChooser.SELECTED_FILE_CHANGED_PROPERTY.equals(prop)) {
                this.file = (File) e.getNewValue();
                update = true;
            }
            // Update the preview accordingly.
            if (update) {
                this.thumbnail = null;
                if (isShowing()) {
                    loadImage();
                    repaint();
                }
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            if (this.thumbnail == null) {
                loadImage();
            }
            if (this.thumbnail != null) {
                int x = getWidth() / 2 - this.thumbnail.getIconWidth() / 2;
                int y = getHeight() / 2 - this.thumbnail.getIconHeight() / 2;
                if (y < 0) {
                    y = 0;
                }
                if (x < 5) {
                    x = 5;
                }
                this.thumbnail.paintIcon(this, g, x, y);
            }
        }
    }
