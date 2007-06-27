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
