//
// $Id$
// 
// jupload - A file upload applet.
// Copyright 2007 The JUpload Team
// 
// Created: 2006-05-06
// Creator: Etienne Gauthier
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

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;

import wjhk.jupload2.JUploadApplet;
import wjhk.jupload2.filedata.FileData;
import wjhk.jupload2.filedata.PictureFileData;
import wjhk.jupload2.gui.PicturePanel;

/**
 * This class add handling of pictures to upload. <BR>
 * <BR>
 * <H4>Functionalities:</H4>
 * <UL>
 * <LI> The top panel (upper part of the applet display) is modified, by using
 * UploadPolicy.{@link wjhk.jupload2.policies.UploadPolicy#createTopPanel(JButton, JButton, JButton, JPanel)}.
 * It contains a <B>preview</B> picture panel, and two additional buttons to
 * rotate the selected picture in one direction or the other.
 * <LI> Ability to set maximum width or height to a picture (with maxPicWidth
 * and maxPicHeight applet parameters, see the global explanation on the <a
 * href="UploadPolicy.html#parameters">parameters</a> section) of the
 * UploadPolicy API page.
 * <LI> Rotation of pictures, by quarter of turn.
 * <LI> <I>(To be implemented)</I> A target picture format can be used, to
 * force all uploaded pictures to be in one picture format, jpeg for instance.
 * All details are in the UploadPolicy <a
 * href="UploadPolicy.html#parameters">parameters</a> section.
 * </UL>
 * <BR>
 * <BR>
 * See an example of HTML that calls this applet, just below.
 * <H4>Parameters</H4>
 * The description for all parameters of all polices has been grouped in the
 * UploadPolicy <a href="UploadPolicy.html#parameters">parameters</a> section.
 * <BR>
 * The parameters implemented in this class are:
 * <UL>
 * <LI> maxPicWidth: Maximum width for the uploaded picture.
 * <LI> maxPicHeight: Maximum height for the uploaded picture.
 * <LI> <I>(To be implemented)</I> targetPictureFormat : Define the target
 * picture format. Eg: jpeg, png, gif...
 * </UL>
 * <A NAME="example">
 * <H4>HTML call example</H4>
 * </A> You'll find below an example of how to put the applet into a PHP page:
 * <BR>
 * <XMP> <APPLET NAME="JUpload" CODE="wjhk.jupload2.JUploadApplet"
 * ARCHIVE="plugins/jupload/wjhk.jupload.jar" <!-- Applet display size, on the
 * navigator page --> WIDTH="500" HEIGHT="700" <!-- The applet call some
 * javascript function, so we must allow it : --> MAYSCRIPT > <!-- First,
 * mandatory parameters --> <PARAM NAME="postURL"
 * VALUE="http://some.host.com/youruploadpage.php"> <PARAM NAME="uploadPolicy"
 * VALUE="PictureUploadPolicy"> <!-- Then, optional parameters --> <PARAM
 * NAME="lang" VALUE="fr"> <PARAM NAME="maxPicHeight" VALUE="768"> <PARAM
 * NAME="maxPicWidth" VALUE="1024"> <PARAM NAME="debugLevel" VALUE="0"> Java 1.4
 * or higher plugin required. </APPLET> </XMP>
 * 
 * @author Etienne Gauthier
 * @version $Revision$
 */

public class PictureUploadPolicy extends DefaultUploadPolicy implements
        ActionListener, ImageObserver {

    /**
     * Indicates that a BufferedImage is to be created when the user selects the
     * file. <BR>
     * If true : the Image is loaded once from the hard drive. This consumns
     * memory, but is interessant for big pictures, when they are resized (see
     * {@link #maxWidth} and {@link #maxHeight}). <BR>
     * If false : it is loaded for each display on the applet, then once for the
     * upload. <BR>
     * <BR>
     * Default : false, because the applet, while in the navigator, runs too
     * quickly out of memory.
     * 
     * @see wjhk.jupload2.policies.UploadPolicy#DEFAULT_STORE_BUFFERED_IMAGE
     */
    private boolean storeBufferedImage;

    /**
     * Iimage type that should be uploaded (JPG, GIF...). It should be a
     * standard type, as the JVM will create this file. If null, the same format
     * as the original file is used. <BR>
     * Currently <B>this flag is ignored when createBufferedImage is false</B> .
     * <BR>
     * Default: null.
     * 
     * @see wjhk.jupload2.policies.UploadPolicy#DEFAULT_TARGET_PICTURE_FORMAT
     */
    private String targetPictureFormat;

    /**
     * Indicates wether or not the preview pictures must be calculated by the
     * BufferedImage.getScaledInstance() method.
     */
    private boolean highQualityPreview;

    /**
     * Maximal width for the uploaded picture. If the actual width for the
     * picture is more than maxWidth, the picture is resized. The proportion
     * between widht and height are maintained. Negative if no maximum width (no
     * resizing). <BR>
     * Default: -1.
     * 
     * @see wjhk.jupload2.policies.UploadPolicy#DEFAULT_MAX_WIDTH
     */
    private int maxWidth = -1;

    /**
     * Maximal height for the uploaded picture. If the actual height for the
     * picture is more than maxHeight, the picture is resized. The proportion
     * between widht and height are maintained. Negative if no maximum height
     * (no resizing). <BR>
     * Default: -1.
     * 
     * @see wjhk.jupload2.policies.UploadPolicy#DEFAULT_MAX_HEIGHT
     */
    private int maxHeight = -1;

    /**
     * @see UploadPolicy
     */
    private int realMaxWidth = -1;

    /**
     * @see UploadPolicy
     */
    private int realMaxHeight = -1;

    /**
     * Button to allow the user to rotate the picture one quarter
     * counter-clockwise.
     */
    private JButton rotateLeftButton;

    /**
     * Button to allow the user to rotate the picture one quarter clockwise.
     */
    private JButton rotateRightButton;

    /**
     * The picture panel, where the selected picture is displayed.
     */
    private PicturePanel picturePanel;

    private static boolean alertShown = false;

    /**
     * The standard constructor, which transmit most informations to the
     * super.Constructor().
     * 
     * @param theApplet Reference to the current applet. Allows access to
     *            javasript functions.
     */
    public PictureUploadPolicy(JUploadApplet theApplet) {
        super(theApplet);

        // Creation of the PictureFileDataPolicy, from parameters given to the
        // applet, or from default values.
        setHighQualityPreview(UploadPolicyFactory.getParameter(theApplet,
                PROP_HIGH_QUALITY_PREVIEW, DEFAULT_HIGH_QUALITY_PREVIEW, this));
        setMaxHeight(UploadPolicyFactory.getParameter(theApplet,
                PROP_MAX_HEIGHT, DEFAULT_MAX_HEIGHT, this));
        setMaxWidth(UploadPolicyFactory.getParameter(theApplet, PROP_MAX_WIDTH,
                DEFAULT_MAX_WIDTH, this));
        setRealMaxHeight(UploadPolicyFactory.getParameter(theApplet,
                PROP_REAL_MAX_HEIGHT, DEFAULT_REAL_MAX_HEIGHT, this));
        setRealMaxWidth(UploadPolicyFactory.getParameter(theApplet,
                PROP_REAL_MAX_WIDTH, DEFAULT_REAL_MAX_WIDTH, this));
        setStoreBufferedImage(UploadPolicyFactory.getParameter(theApplet,
                PROP_STORE_BUFFERED_IMAGE, DEFAULT_STORE_BUFFERED_IMAGE, this));
        setTargetPictureFormat(UploadPolicyFactory
                .getParameter(theApplet, PROP_TARGET_PICTURE_FORMAT,
                        DEFAULT_TARGET_PICTURE_FORMAT, this));

        // The UploadPolicyFactory class will call displayParameterStatus(), so
        // that
        // we display all applet parameters, after initialization.
    }

    /**
     * This methods actually returns a {@link PictureFileData} instance. It
     * allows only pictures: if the file is not a picture, this method returns
     * null, thus preventing the file to be added to the list of files to be
     * uploaded.
     * 
     * @param file The file selected by the user (called once for each added
     *            file).
     * @return An instance of {@link PictureFileData} or null if file is not a
     *         picture.
     * @see wjhk.jupload2.policies.UploadPolicy#createFileData(File)
     */
    @Override
    public FileData createFileData(File file) {
        PictureFileData pfd = new PictureFileData(file, this);
        if (pfd.isPicture()) {
            return pfd;
        }
        if (!alertShown) {
            // Alert only once, when several files are not pictures... hum,
            alert("notAPicture", file.getName());
            alertShown = true;
        }
        return null;
    }

    /**
     * This method override the default topPanel, and adds:<BR>
     * <UL>
     * <LI>Two rotation buttons, to rotate the currently selected picture.
     * <LI>A Preview area, to view the selected picture
     * </UL>
     * 
     * @see wjhk.jupload2.policies.UploadPolicy#createTopPanel(javax.swing.JButton,
     *      javax.swing.JButton, javax.swing.JButton, javax.swing.JPanel)
     */
    @Override
    public JPanel createTopPanel(JButton browse, JButton remove,
            JButton removeAll, JPanel mainPanel) {
        // The top panel is verticaly divided in :
        // - On the left, the button bar (buttons one above another)
        // - On the right, the preview PicturePanel.

        // Creation of specific buttons
        this.rotateLeftButton = new JButton(getString("buttonRotateLeft"));
        this.rotateLeftButton.setIcon(new ImageIcon(getClass().getResource(
                "/images/rotateLeft.gif")));
        this.rotateLeftButton.addActionListener(this);
        this.rotateLeftButton.setEnabled(false);

        this.rotateRightButton = new JButton(getString("buttonRotateRight"));
        this.rotateRightButton.setIcon(new ImageIcon(getClass().getResource(
                "/images/rotateRight.gif")));
        this.rotateRightButton.addActionListener(this);
        this.rotateRightButton.setEnabled(false);

        // The button bar
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(5, 1));
        buttonPanel.add(browse);
        buttonPanel.add(this.rotateLeftButton);
        buttonPanel.add(this.rotateRightButton);
        buttonPanel.add(removeAll);
        buttonPanel.add(remove);

        // The preview PicturePanel
        JPanel pPanel = new JPanel();
        pPanel.setLayout(new GridLayout(1, 1));
        this.picturePanel = new PicturePanel(mainPanel, true, this);
        pPanel.add(this.picturePanel);

        // And last but not least ... creation of the top panel:
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new GridLayout(1, 2));
        topPanel.add(buttonPanel);
        topPanel.add(pPanel);

        return topPanel;
    }// createTopPanel

    /**
     * This method handles the clicks on the rotation buttons. All other actions
     * are managed by the {@link DefaultUploadPolicy}.
     * 
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e) {
        displayInfo("Action : " + e.getActionCommand());
        if (e.getActionCommand() == this.rotateLeftButton.getActionCommand()) {
            this.picturePanel.rotate(-1);
        } else if (e.getActionCommand() == this.rotateRightButton
                .getActionCommand()) {
            this.picturePanel.rotate(1);
        }
    }// actionPerformed

    /**
     * @see wjhk.jupload2.policies.UploadPolicy#onSelectFile(wjhk.jupload2.filedata.FileData)
     */
    @Override
    public void onSelectFile(FileData fileData) {
        if (fileData != null) {
            displayDebug("File selected: " + fileData.getFileName(), 30);
        }
        if (this.picturePanel != null) {
            this.picturePanel.setPictureFile((PictureFileData) fileData);
            this.rotateLeftButton.setEnabled(fileData != null);
            this.rotateRightButton.setEnabled(fileData != null);
        }
    }

    /** @see UploadPolicy#beforeUpload() */
    @Override
    public void beforeUpload() {
        // We clear the current picture selection. This insures a correct
        // managing of enabling/disabling of
        // buttons, even if the user stops the upload.
        getApplet().getFilePanel().clearSelection();
        if (this.picturePanel != null) {
            this.picturePanel.setPictureFile(null);
            this.rotateLeftButton.setEnabled(false);
            this.rotateRightButton.setEnabled(false);
        }

        // Then, we call the standard action, if any.
        super.beforeUpload();
    }

    // ////////////////////////////////////////////////////////////////////////////////////////////////////
    // /////////////////////// Getters and Setters
    // ////////////////////////////////////////////////
    // ////////////////////////////////////////////////////////////////////////////////////////////////////

    /** @return the applet parameter <I>highQualityPreview</I>. */
    public boolean getHighQualityPreview() {
        return this.highQualityPreview;
    }

    /** @param highQualityPreview the highQualityPreview to set */
    void setHighQualityPreview(boolean highQualityPreview) {
        this.highQualityPreview = highQualityPreview;
    }

    /**
     * @return Returns the maxHeight, that should be used by pictures non
     *         transformed (rotated...) by the applet.
     */
    public int getMaxHeight() {
        return this.maxHeight;
    }

    /** @param maxHeight the maxHeight to set */
    void setMaxHeight(int maxHeight) {
        this.maxHeight = maxHeight;
    }

    /**
     * @return Returns the maxWidth, that should be used by pictures non
     *         transformed (rotated...) by the applet.
     */
    public int getMaxWidth() {
        return this.maxWidth;
    }

    /** @param maxWidth the maxWidth to set */
    void setMaxWidth(int maxWidth) {
        this.maxWidth = maxWidth;
    }

    /**
     * @return Returns the maxHeight, that should be used by pictures that are
     *         transformed (rotated...) by the applet.
     */
    public int getRealMaxHeight() {
        return (this.realMaxHeight < 0) ? this.maxHeight : this.realMaxHeight;
    }

    /** @param realMaxHeight the realMaxHeight to set */
    void setRealMaxHeight(int realMaxHeight) {
        this.realMaxHeight = realMaxHeight;
    }

    /**
     * @return Returns the maxWidth, that should be used by pictures that are
     *         transformed (rotated...) by the applet.
     */
    public int getRealMaxWidth() {
        return (this.realMaxWidth < 0) ? this.maxWidth : this.realMaxWidth;
    }

    /** @param realMaxWidth the realMaxWidth to set */
    void setRealMaxWidth(int realMaxWidth) {
        this.realMaxWidth = realMaxWidth;
    }

    /** @return Returns the createBufferedImage. */
    public boolean hasToStoreBufferedImage() {
        return this.storeBufferedImage;
    }

    /** @param storeBufferedImage the storeBufferedImage to set */
    void setStoreBufferedImage(boolean storeBufferedImage) {
        this.storeBufferedImage = storeBufferedImage;
    }

    /** @return Returns the targetPictureFormat. */
    public String getTargetPictureFormat() {
        return this.targetPictureFormat;
    }

    /** @param targetPictureFormat the targetPictureFormat to set */
    void setTargetPictureFormat(String targetPictureFormat) {
        this.targetPictureFormat = targetPictureFormat;
    }

    /**
     * This method manages the applet parameters that are specific to this
     * class. The super.setProperty method is called for other properties.
     * 
     * @param prop The property which value should change
     * @param value The new value for this property. If invalid, the default
     *            value is used.
     * @see wjhk.jupload2.policies.UploadPolicy#setProperty(java.lang.String,
     *      java.lang.String)
     */
    @Override
    public void setProperty(String prop, String value) {
        // The, we check the local properties.
        if (prop.equals(PROP_STORE_BUFFERED_IMAGE)) {
            setStoreBufferedImage(UploadPolicyFactory.parseBoolean(value,
                    this.storeBufferedImage, this));
        } else if (prop.equals(PROP_HIGH_QUALITY_PREVIEW)) {
            setHighQualityPreview(UploadPolicyFactory.parseBoolean(value,
                    this.highQualityPreview, this));
        } else if (prop.equals(PROP_MAX_HEIGHT)) {
            setMaxHeight(UploadPolicyFactory.parseInt(value, this.maxHeight,
                    this));
        } else if (prop.equals(PROP_MAX_WIDTH)) {
            setMaxWidth(UploadPolicyFactory
                    .parseInt(value, this.maxWidth, this));
        } else if (prop.equals(PROP_REAL_MAX_HEIGHT)) {
            setRealMaxHeight(UploadPolicyFactory.parseInt(value,
                    this.realMaxHeight, this));
        } else if (prop.equals(PROP_REAL_MAX_WIDTH)) {
            setRealMaxWidth(UploadPolicyFactory.parseInt(value,
                    this.realMaxWidth, this));
        } else if (prop.equals(PROP_TARGET_PICTURE_FORMAT)) {
            setTargetPictureFormat(value);
        } else {
            // Otherwise, transmission to the mother class.
            super.setProperty(prop, value);
        }
    }

    /** @see DefaultUploadPolicy#displayParameterStatus() */
    @Override
    public void displayParameterStatus() {
        super.displayParameterStatus();

        if (this.maxWidth != DEFAULT_MAX_WIDTH
                || this.maxHeight != DEFAULT_MAX_HEIGHT) {
            displayDebug(PROP_MAX_WIDTH + " : " + this.maxWidth + ", "
                    + PROP_MAX_HEIGHT + " : " + this.maxHeight, 20);
        }
        if (this.realMaxWidth != DEFAULT_REAL_MAX_WIDTH
                || this.realMaxHeight != DEFAULT_REAL_MAX_HEIGHT) {
            displayDebug(PROP_REAL_MAX_WIDTH + " : " + this.realMaxWidth + ", "
                    + PROP_REAL_MAX_HEIGHT + " : " + this.realMaxHeight, 20);
        }
        displayDebug(PROP_HIGH_QUALITY_PREVIEW + " : "
                + this.highQualityPreview, 20);
        displayDebug(PROP_STORE_BUFFERED_IMAGE + " : "
                + this.storeBufferedImage, 20);
        displayDebug(PROP_TARGET_PICTURE_FORMAT + " : "
                + this.targetPictureFormat, 20);
    }

    /**
     * Returns null: the default icon is used.
     * 
     * @see UploadPolicy#fileViewGetIcon(File)
     */
    @Override
    public Icon fileViewGetIcon(File file) {
        ImageIcon imageIcon = null;
        displayDebug("In PictureUploadPolicy.fileViewGetIcon for "
                + file.getName(), 100);
        try {
            // First, we load the picture
            BufferedImage image = ImageIO.read(file);
            BufferedImage resized = resizePicture(image, 20, 20, false, this);
            imageIcon = new ImageIcon(resized);

            // Runtime.getRuntime().gc();
            displayDebug("freeMemory: " + Runtime.getRuntime().freeMemory(), 80);
        } catch (IOException e) {
            displayErr(e);
        }
        return imageIcon;
    }

    // ////////////////////////////////////////////////////////////////////////////////////////////////////
    // /////////////////////// static methods
    // ////////////////////////////////////////////////
    // ////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * This methods resizes the given picture to the given width and height. It
     * is largely inspired from the sample available on:
     * http://java.sun.com/products/java-media/2D/reference/faqs
     * 
     * @param originalImage The picture to resize
     * @param maxWidth The maximum width for the resized picture.
     * @param maxHeight The maximum height for the resized picture.
     * @param preserveAlpha
     */
    public static BufferedImage resizePicture(Image originalImage,
            int maxWidth, int maxHeight, boolean preserveAlpha,
            PictureUploadPolicy uploadPolicy) {
        // We calculate the real scale factor, that is must set both width less
        // than maxWidth and height less
        // than maxHeight.
        int originalWidth = originalImage.getWidth(uploadPolicy);
        int originalHeight = originalImage.getHeight(uploadPolicy);
        float widthScale = (float) maxWidth / originalWidth;
        float heightScale = (float) maxHeight / originalHeight;
        double scale = Math.min(widthScale, heightScale);
        // Picture will not be enlarged.
        scale = (scale > 1) ? 1 : scale;

        int scaledWidth = (int) (scale * originalWidth);
        int scaledHeight = (int) (scale * originalHeight);
        // Some rounding operation may generate wrong calculation.
        if (scaledWidth > maxWidth) {
            scaledWidth = maxWidth;
        }
        if (scaledHeight > maxHeight) {
            scaledHeight = maxHeight;
        }

        int imageType = preserveAlpha ? BufferedImage.TYPE_INT_RGB
                : BufferedImage.TYPE_INT_ARGB;
        BufferedImage scaledBI = new BufferedImage(scaledWidth, scaledHeight,
                imageType);
        Graphics2D g = scaledBI.createGraphics();
        if (preserveAlpha) {
            g.setComposite(AlphaComposite.Src);
        }
        g.drawImage(originalImage, 0, 0, scaledWidth, scaledHeight, null);
        g.dispose();
        return scaledBI;
    }

    /** Implementation of the ImageObserver interface */
    public boolean imageUpdate(@SuppressWarnings("unused")
    Image arg0, @SuppressWarnings("unused")
    int arg1, @SuppressWarnings("unused")
    int arg2, @SuppressWarnings("unused")
    int arg3, @SuppressWarnings("unused")
    int arg4, @SuppressWarnings("unused")
    int arg5) {
        return true;
    }
}