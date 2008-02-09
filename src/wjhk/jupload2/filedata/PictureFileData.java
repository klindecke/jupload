//
// $Id: PictureFileData.java 287 2007-06-17 09:07:04 +0000 (dim., 17 juin 2007)
// felfert $
// 
// jupload - A file upload applet.
// Copyright 2007 The JUpload Team
// 
// Created: 2006-05-09
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

package wjhk.jupload2.filedata;

import java.awt.Canvas;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.FileImageOutputStream;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import wjhk.jupload2.exception.JUploadException;
import wjhk.jupload2.exception.JUploadIOException;
import wjhk.jupload2.policies.PictureUploadPolicy;
import wjhk.jupload2.policies.UploadPolicy;

/**
 * This class contains all data about files to upload as a picture. It adds the
 * following elements to the {@link wjhk.jupload2.filedata.FileData} class :<BR>
 * <UL>
 * <LI> Ability to define a target format (to convert pictures to JPG before
 * upload, for instance)
 * <LI> Optional definition of a maximal width and/or height.
 * <LI> Ability to rotate a picture, with {@link #addRotation(int)}
 * <LI> Ability to store a picture into a BufferedImage. This is actualy a bad
 * idea within an applet (should run within a java application) : the applet
 * runs very quickly out of memory. With pictures from my Canon EOS20D (3,5M), I
 * can only display two pictures. The third one generates an out of memory
 * error, despite the System.finalize and System.gc I've put everywhere in the
 * code!
 * </UL>
 * 
 * @author Etienne Gauthier
 * @version $Revision$
 */
public class PictureFileData extends DefaultFileData implements ImageObserver {

    /**
     * Indicate whether the data for this fileData has already been intialized.
     */
    // private boolean initialized = false;
    /**
     * Indicates if this file is a picture or not. This is bases on the return
     * of ImageIO.getImageReadersByFormatName().
     */
    private boolean isPicture = false;

    /**
     * If set to true, the PictureFileData will keep the BufferedImage in
     * memory. That is: it won't load it again from the hard drive, and resize
     * and/or rotate it (if necessary) when the user select this picture. When
     * picture are big this is nice. <BR>
     * <BR>
     * <B>Caution:</B> this parameter is currently unused, as the navigator
     * applet runs quickly out of memory (after three or four picture for my
     * Canon EOS 20D, 8,5 Mega pixels).
     * 
     * @see UploadPolicy
     */
    boolean storeBufferedImage = UploadPolicy.DEFAULT_STORE_BUFFERED_IMAGE;// Will

    // be
    // erased
    // while
    // in
    // the
    // constructor.

    /**
     * bufferedImage contains a preloaded picture. This buffer is used according
     * to PictureFileDataPolicy.storeBufferedImage.
     * 
     * @see PictureUploadPolicy#storeBufferedImage
     */
    // private BufferedImage bufferedImage = null;
    // Currently commented, as it leads to memory leaks.
    /**
     * This picture is precalculated, and stored to avoid to calculate it each
     * time the user select this picture again, or each time the use switch from
     * an application to another.
     */
    private Image offscreenImage = null;

    /**
     * quarterRotation contains the current rotation that will be applied to the
     * picture. Its value should be one of 0, 1, 2, 3. It is controled by the
     * {@link #addRotation(int)} method.
     * <UL>
     * <LI>0 means no rotation.
     * <LI>1 means a rotation of 90ï¿½ clockwise (word = Ok ??).
     * <LI>2 means a rotation of 180ï¿½.
     * <LI>3 means a rotation of 900 counterclockwise (word = Ok ??).
     * </UL>
     */
    int quarterRotation = 0;

    /**
     * Width of the original picture. Negative if unknown, for instance if the
     * picture has not yet been opened.
     */
    int originalWidth = -1;

    /**
     * Height of the original picture. Negative if unknown, for instance if the
     * picture has not yet been opened.
     */
    int originalHeight = -1;

    /**
     * transformedPictureFile contains the reference to the temporary file that
     * stored the transformed picture, during upload. It is created by
     * {@link #getInputStream()} and freed by {@link #afterUpload()}.
     */
    private File transformedPictureFile = null;

    /**
     * Contains the reference to a copy of the original picture files.
     * Originally created because a SUN bug would prevent picture to be
     * correctly resized if the original picture filename contains accents (or
     * any non-ASCII characters).
     */
    private File workingCopyTempFile = null;

    /**
     * uploadLength contains the uploadLength, which is : <BR> - The size of the
     * original file, if no transformation is needed. <BR> - The size of the
     * transformed file, if a transformation were made. <BR>
     * <BR>
     * It is set to -1 whenever the user ask for a rotation (current only action
     * that need to recalculate the picture).
     */
    private long uploadLength = -1;

    /**
     * hasToTransformPicture indicates whether the picture should be
     * transformed. Null if unknown. This can happen (for instance) if no calcul
     * where done (during initialization), or after rotating the picture back to
     * the original orientation. <BR>
     * <B>Note:</B> this attribute is from the class Boolean (and not a simple
     * boolean), to allow null value, meaning <I>unknown</I>.
     */
    private Boolean hasToTransformPicture = null;

    /**
     * Defines the number of pixel for the current picture. Used to update the
     * progress bar.
     * 
     * @see #getBufferedImage(BufferedImage, int, int, boolean),
     *      #imageUpdate(Image, int, int, int, int, int)
     */
    int nbPixelsTotal = -1;

    /**
     * Indicates the number of pixels that have been read.
     * 
     * @see #nbPixelsTotal, #imageUpdate(Image, int, int, int, int, int)
     */
    int nbPixelsRead = 0;

    /**
     * The value that has the progress bar when starting to load the picture.
     * The {@link #imageUpdate(Image, int, int, int, int, int)} method will add
     * from 0 to 100, to indicate progress with a percentage value of picture
     * loading.
     */
    int progressBarBaseValue = 0;

    /**
     * For this class, the UploadPolicy is a PictureUploadPolicy, or one class
     * that inherits from it.
     * 
     * @Override
     */
    // PictureUploadPolicy uploadPolicy;
    // ////////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Standard constructor: needs a PictureFileDataPolicy.
     * 
     * @param file The files which data are to be handled by this instance.
     */
    public PictureFileData(File file, File root,
            PictureUploadPolicy uploadPolicy) {
        super(file, root, uploadPolicy);
        // EGR Should be useless
        // this.uploadPolicy = (PictureUploadPolicy) super.uploadPolicy;
        this.storeBufferedImage = uploadPolicy.hasToStoreBufferedImage();

        String fileExtension = getFileExtension();

        // Is it a picture?
        Iterator<ImageReader> iter = ImageIO
                .getImageReadersByFormatName(fileExtension);
        this.isPicture = iter.hasNext();
        uploadPolicy.displayDebug("isPicture=" + this.isPicture + " ("
                + file.getName() + "), extension=" + fileExtension, 75);

        // If it's a picture, we override the default mime type:
        if (this.isPicture) {
            setMimeTypeByExtension(fileExtension);
        }
    }

    /**
     * Implementation of the ImageObserver interface. Used to follow the
     * drawImage progression, and update the applet progress bar.
     */
    public boolean imageUpdate(Image img, int infoflags, int x, int y,
            int width, int height) {
        if ((infoflags & ImageObserver.WIDTH) == ImageObserver.WIDTH) {
            this.progressBarBaseValue = this.uploadPolicy.getApplet()
                    .getUploadPanel().getProgressBar().getValue();
            this.uploadPolicy.displayDebug(
                    "  imageUpdate (start of), progressBar geValue: "
                            + this.progressBarBaseValue, 100);
            int max = this.uploadPolicy.getApplet().getUploadPanel()
                    .getProgressBar().getMaximum();
            this.uploadPolicy.displayDebug(
                    "  imageUpdate (start of), progressBar maximum: " + max,
                    100);
        } else if ((infoflags & ImageObserver.SOMEBITS) == ImageObserver.SOMEBITS) {
            this.nbPixelsRead += width * height;
            int percentage = (int) ((long) this.nbPixelsRead * 100 / this.nbPixelsTotal);
            this.uploadPolicy.getApplet().getUploadPanel().getProgressBar()
                    .setValue(this.progressBarBaseValue + percentage);
            // TODO: drawImage in another thread, to allow repaint of the
            // progress bar ?
            // Current status: the progress bar is only updated ... when
            // draImage returns, that is: when everything is finished. NO
            // interest.
            this.uploadPolicy.getApplet().getUploadPanel().getProgressBar()
                    .repaint(100);
        } else if ((infoflags & ImageObserver.ALLBITS) == ImageObserver.ALLBITS) {
            this.uploadPolicy.displayDebug(
                    "  imageUpdate, total number of pixels: "
                            + this.nbPixelsRead + " read", 100);
        }

        // We want to go on, after these bits
        return true;
    }

    /**
     * Free any available memory. This method is called very often here, to be
     * sure that we don't use too much memory. But we still run out of memory in
     * some case.
     * 
     * @param caller Indicate the method or treatment from which this method is
     *            called.
     */
    public void freeMemory(String caller) {
        Runtime rt = Runtime.getRuntime();

        /*
         * uploadPolicy.displayDebug("freeMemory : " + caller, 80);
         * uploadPolicy.displayDebug("freeMemory (before " + caller + ") : " +
         * rt.freeMemory(), 80); uploadPolicy.displayDebug("maxMemory (before " +
         * caller + ") : " + rt.maxMemory(), 80);
         */

        // rt.runFinalization();
        rt.gc();

        this.uploadPolicy.displayDebug("freeMemory (after " + caller + ") : "
                + rt.freeMemory(), 80);
        /*
         * uploadPolicy.displayDebug("maxMemory (after " + caller + ") : " +
         * rt.maxMemory(), 80);
         */
    }

    /**
     * If this pictures needs transformation, a temporary file is created. This
     * can occurs if the original picture is bigger than the maxWidth or
     * maxHeight, of if it has to be rotated. This temporary file contains the
     * transformed picture. <BR>
     * The call to this method is optional, if the caller calls
     * {@link #getUploadLength()}. This method calls beforeUpload() if the
     * uploadLength is unknown.
     */
    @Override
    public void beforeUpload() throws JUploadException {
        if (this.uploadLength < 0) {
            try {
                // Let's read the original picture only once, for the two next
                // calls.
                BufferedImage bi = readImage();
                // Get the transformed picture file, if needed.
                if (hasToTransformPicture(bi)) {
                    getTransformedPictureFile(bi);
                }
            } catch (OutOfMemoryError e) {
                // Oups ! My EOS 20D has too big pictures to handle more than
                // two pictures in a navigator applet !!!!!
                // :-(
                //
                // We don't transform it. We clean the file, if it has been
                // created.
                deleteTransformedPictureFile();
                //
                tooBigPicture();
            }

            // If the transformed picture is correctly created, we'll upload it.
            // Else we upload the original file.
            if (this.transformedPictureFile != null) {
                this.uploadLength = this.transformedPictureFile.length();
            } else {
                this.uploadLength = getFile().length();
            }
        }

        // Let's check that everything is Ok
        super.beforeUpload();
    }

    /**
     * Returns the number of bytes, for this upload. If needed, that is, if
     * uploadlength is unknown, {@link #beforeUpload()} is called.
     * 
     * @return The length of upload. In this class, this is ... the size of the
     *         original file, or the transformed file!
     */
    @Override
    public long getUploadLength() throws JUploadException {
        if (this.uploadLength < 0) {
            // Hum, beforeUpload should have been called before. Let's correct
            // that.
            beforeUpload();
        }
        return this.uploadLength;
    }

    /**
     * This function create an input stream for this file. The caller is
     * responsible for closing this input stream. <BR>
     * This function assumes that the {@link #getUploadLength()} method has
     * already be called : it is responsible for creating the temporary file (if
     * needed). If not called, the original file will be sent.
     * 
     * @return An inputStream
     */
    @Override
    public InputStream getInputStream() throws JUploadException {
        // Do we have to transform the picture ?
        if (this.transformedPictureFile != null) {
            try {
                return new FileInputStream(this.transformedPictureFile);
            } catch (FileNotFoundException e) {
                throw new JUploadIOException(e);
            }
        }
        // Otherwise : we read the file, in the standard way.
        return super.getInputStream();
    }

    /**
     * Cleaning of the temporary file on the hard drive, if any. <BR>
     * <B>Note:</B> if the debugLevel is 100 (or more) this temporary file is
     * not removed. This allow control of this created file.
     */
    @Override
    public void afterUpload() {
        super.afterUpload();

        // Free the temporary file ... if any.
        if (this.transformedPictureFile != null) {
            // for debug : if the debugLevel is enough, we keep the temporary
            // file (for check).
            if (this.uploadPolicy.getDebugLevel() >= 100) {
                this.uploadPolicy.displayWarn("Temporary file not deleted");
            } else {
                deleteTransformedPictureFile();
            }
        }
    }

    /**
     * This method creates a new Image, from the current picture. The resulting
     * width and height will be less or equal than the given maximum width and
     * height. The scale is maintained. Thus the width or height may be inferior
     * than the given values.
     * 
     * @param canvas The canvas on which the picture will be displayed.
     * @param shadow True if the pictureFileData should store this picture.
     *            False if the pictureFileData instance should not store this
     *            picture. Store this picture avoid calculating the image each
     *            time the user selects it in the file panel.
     * @return The rescaled image.
     */
    public Image getImage(Canvas canvas, boolean shadow)
            throws JUploadException {
        Image localImage = null;

        if (canvas == null) {
            throw new JUploadException(
                    "canvas null in PictureFileData.getImage");
        }

        int canvasWidth = canvas.getWidth();
        int canvasHeight = canvas.getHeight();
        if (canvasWidth <= 0 || canvasHeight <= 0) {
            this.uploadPolicy
                    .displayDebug(
                            "canvas width and/or height null in PictureFileData.getImage()",
                            1);
        } else if (shadow && this.offscreenImage != null) {
            // We take and return the previous calculated image for this
            // PictureFileData.
            localImage = this.offscreenImage;
        } else if (this.isPicture) {
            try {
                // FIXME (minor) getBufferedImage() that can return a picture
                // smaller than the PictureDialog (if maxWidth or maxHeigth are
                // smaller than the PictureDialog)
                // bufferedImage
                localImage = getBufferedImage(null, canvasWidth, canvasHeight,
                        ((PictureUploadPolicy) this.uploadPolicy)
                                .getHighQualityPreview());
            } catch (OutOfMemoryError e) {
                // Too bad
                localImage = null;
                tooBigPicture();
            }
        } // If isPicture

        // We store it, if asked to.
        if (shadow) {
            this.offscreenImage = localImage;
        }

        freeMemory("end of " + this.getClass().getName() + ".getImage()");

        // The picture is now loaded. We clear the progressBar
        this.uploadPolicy.getApplet().getUploadPanel().getProgressBar()
                .setValue(this.progressBarBaseValue);

        return localImage;
    }// getImage

    /**
     * This function is used to rotate the picture.
     * 
     * @param quarter Number of quarters (90ï¿½) the picture should rotate. 1
     *            means rotating of 90ï¿½ clockwise (?). Can be negative.
     * @see #quarterRotation
     */
    public void addRotation(int quarter) {
        this.quarterRotation += quarter;

        // We'll have to recalculate the upload length, as the resulting file is
        // different.
        this.uploadLength = -1;
        // We don't know anymore if the picture has to be transformed. We let
        // the hasToTransform method decide.
        this.hasToTransformPicture = null;

        // We keep the 'quarter' in the segment [0;4[
        while (this.quarterRotation < 0) {
            this.quarterRotation += 4;
        }
        while (this.quarterRotation >= 4) {
            this.quarterRotation -= 4;
        }

        // We need to change the precalculated picture, if any
        if (this.offscreenImage != null) {
            this.offscreenImage.flush();
            this.offscreenImage = null;
        }
    }

    /**
     * @return the {@link #isPicture} flag.
     */
    public boolean isPicture() {
        return this.isPicture;
    }

    /** @see FileData#getMimeType() */
    @Override
    public String getMimeType() {
        return this.mimeType;
    }

    // ///////////////////////////////////////////////////////////////////////////////////////////
    // /////////////////////////// private METHODS
    // ///////////////////////////////////////////////////////////////////////////////////////////

    /**
     * This function resizes the picture, if necessary, according to the
     * maxWidth and maxHeight of fileDataPolicy. <BR>
     * This function should only be called if isPicture is true. But calling it
     * with isPicture to false just won't do anything. <BR>
     * Note (Update given by David Gnedt): the highquality will condition the
     * call of getScaledInstance, instead of a basic scale Transformation. The
     * generated picture is of better quality, but this is longer, especially on
     * 'small' CPU. Time samples, with one picture from my canon EOS20D, on a
     * PII 500M: <BR>
     * ~3s for the full screen preview with highquality to false, and a quarter
     * rotation. 12s to 20s with highquality to true. <BR>
     * ~5s for the first (small) preview of the picture, with both highquality
     * to false or true.
     * 
     * @param sourceBufferedImage The buffered image to transform. If null, a
     *            new BufferedImage is created from the current file.
     * @param maxWidth Maximum width allowed to the BufferedImage that will be
     *            returned.
     * @param maxHeight Maximum height allowed to the BufferedImage that will be
     *            returned.
     * @param highquality (added by David Gnedt): if set to true, the
     *            BufferedImage.getScaledInstance() is called. This generates
     *            better image, but consumes more CPU.
     * @return A BufferedImage which contains the picture according to current
     *         parameters (resizing, rotation...), or null if this is not a
     *         picture.
     */
    private BufferedImage getBufferedImage(BufferedImage sourceBufferedImage,
            int maxWidth, int maxHeight, boolean highquality)
            throws JUploadException {
        long msGetBufferedImage = System.currentTimeMillis();
        double theta = Math.toRadians(90 * this.quarterRotation);
        BufferedImage returnedBufferedImage = null;

        // TODO Finish optimization, here

        this.uploadPolicy.displayDebug("getBufferedImage: start", 10);

        // We do something, only if we actually are within a picture file.
        if (this.isPicture) {
            if (sourceBufferedImage == null) {
                sourceBufferedImage = readImage();
            }

            try {
                AffineTransform transform = new AffineTransform();

                // Let's store the original image width and height. It can be
                // used elsewhere (see hasToTransformPicture, below).
                this.originalWidth = sourceBufferedImage.getWidth();
                this.originalHeight = sourceBufferedImage.getHeight();

                // ////////////////////////////////////////////////////////////
                // Let's calculate by how much we should reduce the picture :
                // scale
                // ///////////////////////////////////////////////////////////

                // The width and height depend on the current rotation :
                // calculation of the width and height of picture after
                // rotation.
                int nonScaledRotatedWidth = this.originalWidth;
                int nonScaledRotatedHeight = this.originalHeight;
                if (this.quarterRotation % 2 != 0) {
                    // 90° or 270° rotation: width and height are switched.
                    nonScaledRotatedWidth = this.originalHeight;
                    nonScaledRotatedHeight = this.originalWidth;
                }
                // Now, we can compare these width and height to the maximum
                // width and height
                double scaleWidth = ((maxWidth < 0) ? 1 : ((double) maxWidth)
                        / nonScaledRotatedWidth);
                double scaleHeight = ((maxHeight < 0) ? 1
                        : ((double) maxHeight) / nonScaledRotatedHeight);
                double scale = Math.min(scaleWidth, scaleHeight);
                // FIXME The scaleWidth and scaleHeigth is wrong when the
                // maxHeight and maxWidth are different, and the picture must be
                // rotated by one quarter (in either direction)
                //
                if (scale < 1) {
                    // With number rounding, it can happen that width or size
                    // became one pixel too big. Let's correct it.
                    if ((maxWidth > 0 && maxWidth < (int) (scale
                            * Math.cos(theta) * nonScaledRotatedWidth))
                            || (maxHeight > 0 && maxHeight < (int) (scale
                                    * Math.cos(theta) * nonScaledRotatedHeight))) {
                        scaleWidth = ((maxWidth < 0) ? 1
                                : ((double) maxWidth - 1)
                                        / (nonScaledRotatedWidth));
                        scaleHeight = ((maxHeight < 0) ? 1
                                : ((double) maxHeight - 1)
                                        / (nonScaledRotatedHeight));
                        scale = Math.min(scaleWidth, scaleHeight);
                    }
                }

                // These variables contain the actual width and height after
                // rescaling, and before rotation.
                int scaledWidth = nonScaledRotatedWidth;
                int scaledHeight = nonScaledRotatedHeight;
                // Is there any rescaling to do ?
                // Patch for the first bug, tracked in the sourceforge bug
                // tracker ! ;-)
                if (scale < 1) {
                    scaledWidth *= scale;
                    scaledHeight *= scale;
                }
                this.uploadPolicy.displayDebug("Resizing factor (scale): "
                        + scale, 10);
                // Due to rounded numbers, the resulting targetWidth or
                // targetHeight
                // may be one pixel too big. Let's check that.
                if (scaledWidth > maxWidth) {
                    this.uploadPolicy.displayDebug("Correcting rounded width: "
                            + scaledWidth + " to " + maxWidth, 10);
                    scaledWidth = maxWidth;
                }
                if (scaledHeight > maxHeight) {
                    this.uploadPolicy.displayDebug(
                            "Correcting rounded height: " + scaledHeight
                                    + " to " + maxHeight, 10);
                    scaledHeight = maxHeight;
                }

                if (this.quarterRotation != 0) {
                    double translationX = 0, translationY = 0;
                    this.uploadPolicy.displayDebug("quarter: "
                            + this.quarterRotation, 30);

                    // quarterRotation is one of 0, 1, 2, 3 : see addRotation.
                    // If we're here : it's not 0, so it's one of 1, 2 or 3.
                    switch (this.quarterRotation) {
                        case 1:
                            translationX = 0;
                            translationY = -scaledWidth;
                            break;
                        case 2:
                            translationX = -scaledWidth;
                            translationY = -scaledHeight;
                            break;
                        case 3:
                            translationX = -scaledHeight;
                            translationY = 0;
                            break;
                        default:
                            this.uploadPolicy
                                    .displayWarn("Invalid quarterRotation : "
                                            + this.quarterRotation);
                            this.quarterRotation = 0;
                            theta = 0;
                    }
                    transform.rotate(theta);
                    transform.translate(translationX, translationY);
                }

                // If we have to rescale the picture, we first do it:
                if (scale < 1) {
                    if (highquality) {
                        this.uploadPolicy.displayDebug(
                                "Resizing picture(using high quality picture)",
                                40);

                        // We use SCALE_SMOOTH, as it is a (very) little quicker
                        // than SCALE_AREA_AVERAGING (8.5s against 8.7s on my
                        // test picture), and I can't see
                        // differences on the resulting uploaded picture.
                        // Other parameter give bad picture quality.
                        this.uploadPolicy.displayDebug("Before SCALE_SMOOTH",
                                100);
                        Image img = sourceBufferedImage.getScaledInstance(
                                (int) (this.originalWidth * scale),
                                (int) (this.originalHeight * scale),
                                Image.SCALE_SMOOTH);

                        // the localBufferedImage may be 'unknown'.
                        int localImageType = sourceBufferedImage.getType();
                        if (localImageType == BufferedImage.TYPE_CUSTOM) {
                            localImageType = BufferedImage.TYPE_INT_BGR;
                        }

                        this.uploadPolicy
                                .displayDebug("new BufferedImage", 100);
                        BufferedImage tempBufferedImage = new BufferedImage(
                                (int) (this.originalWidth * scale),
                                (int) (this.originalHeight * scale),
                                localImageType);

                        // drawImage can be long. Let's follow its progress,
                        // with the applet progress bar.
                        this.nbPixelsTotal = scaledWidth * scaledHeight;
                        this.nbPixelsRead = 0;

                        // Let's draw the picture: this code do the rescaling.
                        this.uploadPolicy.displayDebug("Before drawImage", 100);
                        tempBufferedImage.getGraphics().drawImage(img, 0, 0,
                                this);

                        this.uploadPolicy.displayDebug("Before flush1", 100);
                        tempBufferedImage.flush();

                        this.uploadPolicy.displayDebug("Before flush2", 100);
                        img.flush();
                        img = null;
                        // tempBufferedImage contains the rescaled picture. It's
                        // the source image for the next step (rotation).
                        sourceBufferedImage = tempBufferedImage;
                        tempBufferedImage = null;
                    } else {
                        // The scale method adds scaling before current
                        // transformation.
                        this.uploadPolicy
                                .displayDebug(
                                        "Resizing picture(using standard quality picture)",
                                        40);
                        transform.scale(scale, scale);
                    }
                }

                uploadPolicy.displayDebug("Picture is now rescaled", 80);

                if (transform.isIdentity()) {
                    returnedBufferedImage = sourceBufferedImage;
                } else {
                    AffineTransformOp affineTransformOp = null;
                    // Pictures are Ok.
                    affineTransformOp = new AffineTransformOp(transform,
                            AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
                    returnedBufferedImage = affineTransformOp
                            .createCompatibleDestImage(sourceBufferedImage,
                                    null);
                    // Checks, after the fact the pictures produces by the Canon
                    // EOS 30D are not properly resized: colors are 'strange'
                    // after resizing.
                    uploadPolicy.displayDebug(
                            "returnedBufferedImage.getColorModel(): "
                                    + sourceBufferedImage.getColorModel()
                                            .toString(), 80);
                    uploadPolicy.displayDebug(
                            "returnedBufferedImage.getColorModel(): "
                                    + sourceBufferedImage.getColorModel()
                                            .toString(), 80);
                    affineTransformOp.filter(sourceBufferedImage,
                            returnedBufferedImage);
                    affineTransformOp = null;

                    returnedBufferedImage.flush();
                }
            } catch (Exception e) {
                throw new JUploadException(e.getClass().getName()
                        + " (createBufferedImage) : " + e.getMessage());
            }

            if (returnedBufferedImage != null
                    && this.uploadPolicy.getDebugLevel() >= 60) {
                this.uploadPolicy.displayDebug("bufferedImage: "
                        + returnedBufferedImage, 60);
                this.uploadPolicy.displayDebug("bufferedImage MinX: "
                        + returnedBufferedImage.getMinX(), 60);
                this.uploadPolicy.displayDebug("bufferedImage MinY: "
                        + returnedBufferedImage.getMinY(), 60);
            }
        }

        this.uploadPolicy.displayDebug("getBufferedImage: was "
                + (System.currentTimeMillis() - msGetBufferedImage)
                + " ms long", 100);
        return returnedBufferedImage;
    }

    /**
     * This function indicate if the picture has to be modified. For instance :
     * a maximum width, height, a target format...
     * 
     * @see PictureUploadPolicy
     * @see #quarterRotation
     * @return true if the picture must be transformed. false if the file can be
     *         directly transmitted.
     */
    private boolean hasToTransformPicture(BufferedImage originalImage)
            throws JUploadException {

        // A first necessary step, is to get the original width and height.
        if (originalImage == null) {
            originalImage = readImage();
        }
        this.originalWidth = originalImage.getWidth();
        this.originalHeight = originalImage.getHeight();
        // Within the navigator, we have to free memory ASAP
        originalImage = null;
        freeMemory("hasToTransformPicture");

        // Did we already estimate if transformation is needed ?
        if (this.hasToTransformPicture == null) {
            // We only transform pictures.
            if (!this.isPicture) {
                this.hasToTransformPicture = Boolean.FALSE;
            }

            // First : the easiest test. Should we block metadata ?
            if (this.hasToTransformPicture == null
                    && !((PictureUploadPolicy) uploadPolicy)
                            .getPictureTransmitMetadata()) {
                this.hasToTransformPicture = Boolean.TRUE;
            }
            // Second : another easy test. A rotation is needed ?
            if (this.hasToTransformPicture == null && this.quarterRotation != 0) {
                this.uploadPolicy
                        .displayDebug(
                                getFileName()
                                        + " : hasToTransformPicture = true (quarterRotation != 0)",
                                20);
                this.hasToTransformPicture = Boolean.TRUE;
            }

            // Second : the picture format is the same ?
            if (this.hasToTransformPicture == null
                    && ((PictureUploadPolicy) this.uploadPolicy)
                            .getTargetPictureFormat() != null) {
                // A target format is positionned: is it the same as the current
                // file format ?
                String target = ((PictureUploadPolicy) this.uploadPolicy)
                        .getTargetPictureFormat().toLowerCase();
                String ext = getFileExtension().toLowerCase();

                if (target.equals("jpg"))
                    target = "jpeg";
                if (ext.equals("jpg"))
                    ext = "jpeg";

                if (!target.equals(ext)) {
                    this.uploadPolicy
                            .displayDebug(
                                    getFileName()
                                            + " : hasToTransformPicture = true (targetPictureFormat)",
                                    20);
                    // Correction given by David Gnedt: the following line was
                    // lacking!
                    this.hasToTransformPicture = Boolean.TRUE;
                }
            }

            // Third : should we resize the picture ?

            // Then, we calculated the rotated width and height, that we would
            // have if we rotate or not the picture
            // according to the current user choice.
            int rotatedWidth, rotatedHeight;
            int maxWidth, maxHeight;

            // The width and height of the transformed picture depends on the
            // rotation.
            if (this.quarterRotation % 2 == 0) {
                rotatedWidth = this.originalWidth;
                rotatedHeight = this.originalHeight;
            } else {
                rotatedWidth = this.originalHeight;
                rotatedHeight = this.originalWidth;
            }

            // If the image is rotated, we compare to realMaxWidth and
            // realMaxHeight, instead of maxWidth and maxHeight. This allows to
            // have a different picture size for rotated and not rotated
            // pictures. See the UploadPolicy javadoc for details
            // ... and a good reason ! ;-)
            if (this.quarterRotation == 0) {
                maxWidth = ((PictureUploadPolicy) this.uploadPolicy)
                        .getMaxWidth();
                maxHeight = ((PictureUploadPolicy) this.uploadPolicy)
                        .getMaxHeight();
            } else {
                // A transformation occured: we take the realMaxXxx. if
                // realMaxPicWidth is not set, the getter will return the value
                // of maxPicWidth.
                maxWidth = ((PictureUploadPolicy) this.uploadPolicy)
                        .getRealMaxWidth();
                maxHeight = ((PictureUploadPolicy) this.uploadPolicy)
                        .getRealMaxHeight();
            }

            // Ok, let's check if we would obtain a width superior to the given
            // maxPicWidth or realMaxPicWidth.
            if (this.hasToTransformPicture == null && maxWidth > 0) {
                if (rotatedWidth > maxWidth) {
                    this.uploadPolicy
                            .displayDebug(
                                    getFileName()
                                            + " : hasToTransformPicture = true (rotatedWidth > maxWidth)",
                                    20);
                    this.hasToTransformPicture = Boolean.TRUE;
                }
            }
            // Ok, let's check if we would obtain a width superior to the given
            // maxPichHeigth or realMaxPicHeigth.
            if (this.hasToTransformPicture == null && maxHeight > 0) {
                if (rotatedHeight > maxHeight) {
                    this.uploadPolicy
                            .displayDebug(
                                    getFileName()
                                            + " : hasToTransformPicture = true (rotatedHeight > maxHeight)",
                                    20);
                    this.hasToTransformPicture = Boolean.TRUE;
                }
            }

            // If we find no reason to transform the picture, then let's let the
            // picture unmodified.
            if (this.hasToTransformPicture == null) {
                this.uploadPolicy.displayDebug(getFileName()
                        + " : hasToTransformPicture = false", 20);
                this.hasToTransformPicture = Boolean.FALSE;
            }
        }

        return this.hasToTransformPicture.booleanValue();
    }// end of hasToTransformPicture

    /**
     * File.deleteOnExit() is pretty unreliable, especially in applets.
     * Therefore the applet provides a callback which is executed during applet
     * termination. This method performs the actual cleanup.
     */
    public void deleteWorkingCopyPictureFile() {
        if (null != this.workingCopyTempFile) {
            this.workingCopyTempFile.delete();
            this.workingCopyTempFile = null;
        }
    }

    /**
     * File.deleteOnExit() is pretty unreliable, especially in applets.
     * Therefore the applet provides a callback which is executed during applet
     * termination. This method performs the actual cleanup.
     */
    public void deleteTransformedPictureFile() {
        if (null != this.transformedPictureFile) {
            this.transformedPictureFile.delete();
            this.transformedPictureFile = null;
            this.uploadLength = -1;
        }
    }

    /**
     * Creation of a temporary file, that contains the transformed picture. For
     * instance, it can be resized or rotated. This method doesn't throw
     * exception when there is an IOException within its procedure. If an
     * exception occurs while building the temporary file, the exception is
     * caught, a warning is displayed,the temporary file is deleted (if it was
     * created), and the upload will go on with the original file. <BR>
     * Note: any JUploadException thrown by a method called within
     * getTransformedPictureFile() will be thrown within this method.
     */
    private File getTransformedPictureFile(BufferedImage originalImage)
            throws JUploadException {
        int targetMaxWidth;
        int targetMaxHeight;

        // Should transform the file, and do we already created the transformed
        // file ?
        if (hasToTransformPicture(originalImage)
                && this.transformedPictureFile == null) {
            // If the image is rotated, we compare to realMaxWidth and
            // realMaxHeight, instead of maxWidth and maxHeight. This allows
            // to have a different picture size for rotated and not rotated
            // pictures. See the UploadPolicy javadoc for details ... and a
            // good reason ! ;-)
            if (this.quarterRotation == 0) {
                targetMaxWidth = ((PictureUploadPolicy) this.uploadPolicy)
                        .getMaxWidth();
                targetMaxHeight = ((PictureUploadPolicy) this.uploadPolicy)
                        .getMaxHeight();
            } else {
                targetMaxWidth = ((PictureUploadPolicy) this.uploadPolicy)
                        .getRealMaxWidth();
                targetMaxHeight = ((PictureUploadPolicy) this.uploadPolicy)
                        .getRealMaxHeight();
            }

            // originalWidth and originalHeight must be set.
            if (originalWidth < 0 || originalHeight < 0) {
                throw new JUploadException(
                        "originalWidth and originalHeight must be set (originalWidth="
                                + originalWidth + ", originalHeight="
                                + originalHeight);
            }

            // We have to create a resized or rotated picture file, and all
            // needed information.
            // ...let's do it
            createTranformedPictureFile(targetMaxWidth, targetMaxHeight,
                    originalImage);
        }

        return this.transformedPictureFile;
    }// end of getTransformedPictureFile

    /**
     * Creates a transformed picture file of the given max width and max height.
     * If the {@link #transformedPictureFile} attribute is not set before
     * calling this method, it will be set. If set before, the existing
     * {@link #transformedPictureFile} is replaced by the newly transformed
     * picture file. It is cleared if an error occured. <BR>
     * 
     * @param targetMaxWidth
     * @param targetMaxHeight
     */
    void createTranformedPictureFile(int targetMaxWidth, int targetMaxHeight,
            BufferedImage originalImage) throws JUploadException {
        String action = null;
        File workingSourceFile = getWorkingSourceFile();

        try {
            createTransformedTempFile();

            originalImage = getBufferedImage(originalImage, targetMaxWidth,
                    targetMaxHeight, true);
            action = "BufferedImage created";

            // localPictureFormat is currently only used to define the correct
            // image writer. There is no transformation between to different
            // picture format (like JPG to GIF)
            String localPictureFormat = (((PictureUploadPolicy) this.uploadPolicy)
                    .getTargetPictureFormat() == null) ? getFileExtension()
                    : ((PictureUploadPolicy) this.uploadPolicy)
                            .getTargetPictureFormat();

            // Get the writer (to choose the compression quality)
            Iterator<ImageWriter> iter = ImageIO
                    .getImageWritersByFormatName(localPictureFormat);
            if (iter.hasNext()) {
                ImageWriter writer = iter.next();
                ImageWriteParam iwp = writer.getDefaultWriteParam();

                // For jpeg pictures, we force the compression level.
                if (localPictureFormat.equalsIgnoreCase("jpg")
                        || localPictureFormat.equalsIgnoreCase("jpeg")) {
                    iwp.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                    // Let's select a good compromise between picture size
                    // and quality.
                    iwp
                            .setCompressionQuality(((PictureUploadPolicy) this.uploadPolicy)
                                    .getPictureCompressionQuality());
                }

                //
                try {
                    this.uploadPolicy.displayDebug(
                            "ImageWriter1 (used), CompressionQuality="
                                    + iwp.getCompressionQuality(), 95);
                } catch (Exception e2) {
                    // If we come here, compression is not supported for
                    // this picture format, or parameters are not explicit
                    // mode, or ... (etc). May trigger several different
                    // errors. We just ignore them: this par of code is only
                    // to write some debug info.
                }

                // Now, we write the metadata from the original file to the
                // transformed one ... if any exists.
                uploadPolicy.displayInfo("Start of metadata managing, for "
                        + getFileName());
                IIOMetadata metadata = null;

                // Should we add the original metadata to the transformed
                // file ?
                // NOTE: THE CURRENT WAY IS NOT MARVELOUS!
                // Removing metadata from a picture that do not need any other
                // transformations still leads to a picture creating. With JPEG,
                // there will a new jpeg compression. That is:
                // - Good ping: jpeg will probably smaller, according to applet
                // configuration
                // - Bad thing: may be longer than needed.
                action = "Should we transmit metadata ?";
                if (((PictureUploadPolicy) uploadPolicy)
                        .getPictureTransmitMetadata()) {
                    String ext = getExtension(getFile());
                    uploadPolicy.displayDebug("Looking for a reader for " + ext
                            + " extension", 80);
                    Iterator<ImageReader> iterator = ImageIO
                            .getImageReadersBySuffix(ext);
                    ImageReader ir;
                    FileImageInputStream is = null;
                    while (iterator.hasNext()) {
                        ir = iterator.next();
                        try {
                            is = new FileImageInputStream(workingSourceFile);
                            ir.setInput(is);
                            metadata = ir.getImageMetadata(0);
                        } catch (IOException e2) {
                            uploadPolicy.displayErr(e2);
                            continue;
                        } finally {
                            ir.reset();
                            ir.dispose();
                            if (is != null) {
                                is.close();
                                is = null;
                            }
                        }
                        if (metadata != null) {
                            uploadPolicy
                                    .displayDebug(
                                            "Found one image reader that can read metadata!",
                                            20);
                            // Tested on JRE 1.6.0_03: no other reader can
                            // read JPEG and metadata.
                            break;
                        }
                    }// while

                    if (metadata == null) {
                        uploadPolicy
                                .displayInfo("No metadata found (or no reader for "
                                        + getFileName()
                                        + "). No metadata will be transmitted.");
                    }
                }

                // Let's create the picture file.
                action = "Creating FileImageOutputStream";
                FileImageOutputStream output = new FileImageOutputStream(
                        this.transformedPictureFile);
                writer.setOutput(output);
                action = "Writing IIOImage (before new IIOImage)";
                IIOImage image = new IIOImage(originalImage, null, metadata);
                action = "Writing IIOImage (before write)";
                writer.write(null, image, iwp);
                action = "Writing IIOImage (before dispose)";
                writer.dispose();
                action = "Writing IIOImage (new before close)";
                output.close();
                output = null;
                action = "IIOImage written";

                // For debug: test if any other driver exists.
                int i = 2;
                while (iter.hasNext()) {
                    this.uploadPolicy.displayDebug("ImageWriter" + i
                            + " (not used)", 60);
                }// while
            } else {
                // Too bad: no writer for the selected picture format
                throw new JUploadException("No writer for the '"
                        + localPictureFormat + "' picture format.");
            }

            // Within the navigator, we have to free memory ASAP
            action = "Finished";
            if (!this.storeBufferedImage) {
                originalImage = null;
                freeMemory("getTransformedPictureFile");
            }
            this.uploadPolicy.displayDebug("transformedPictureFile : "
                    + this.transformedPictureFile.getName(), 30);
        } catch (IOException e) {

            if (e != null) {
                // We mask any exception that occurs within this method. The
                // called method should raise JUploadException, so their
                // exceptions
                // won't be catched here.
                this.uploadPolicy.displayWarn(e.getClass().getName() + " ["
                        + e.getMessage() + "] " + " while writing the "
                        + this.transformedPictureFile.getName()
                        + " file. (picture will not be transformed) {action="
                        + action + "}");
                if (e instanceof FileNotFoundException) {
                    this.uploadPolicy
                            .displayInfo(e.getClass().getName()
                                    + " probably means that the directory containing the picture is readonly: the applet can't write its temporary file.");
                }
            }
            // We try to remove the temporary file, if it has been created.
            if (this.transformedPictureFile != null) {
                try {
                    this.transformedPictureFile.delete();
                } catch (Exception e2) {
                    this.uploadPolicy.displayWarn(e2.getClass()
                            + " while trying to remove temporary file ("
                            + e2.getMessage() + ")");
                }
            }
            this.transformedPictureFile = null;
        }
    }

    /**
     * Get the file that contains the orginal picture. This is used as a
     * workaround for the following JVM bug: once in the navigator, it can't
     * transform picture read from a file whose name contains non-ASCII
     * characters, like French accents.
     * 
     * @return The file that contains the original picture, as the source for
     *         picture transformation
     * @throws JUploadIOException
     */
    private File getWorkingSourceFile() throws JUploadIOException {

        if (this.workingCopyTempFile == null) {
            uploadPolicy.displayDebug(
                    "[getWorkingSourceFile] Creating a copy of "
                            + getFileName() + " as a source working target.",
                    20);
            copyOriginalToWorkingCopyTempFile();
        }
        return this.workingCopyTempFile;
    }

    /**
     * Copy the existing file into a temporary one. Can be done before
     * destructive file manipulation, like metadata clearing, if the
     * pictureTransmitMetadata applet parameter is false, and the picture is not
     * resized or rotated.
     */
    private void copyOriginalToWorkingCopyTempFile() throws JUploadIOException {
        FileInputStream is = null;
        FileOutputStream os = null;
        try {
            createWorkingCopyTempFile();

            is = new FileInputStream(getFile());
            os = new FileOutputStream(this.workingCopyTempFile);
            byte b[] = new byte[1024];
            int l;
            while ((l = is.read(b)) > 0) {
                os.write(b, 0, l);
            }
        } catch (IOException e) {
            throw new JUploadIOException(e);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    uploadPolicy
                            .displayWarn(e.getClass().getName()
                                    + " while trying to close FileInputStream, in PictureUploadPolicy.copyOriginalToWorkingCopyTempFile.");
                } finally {
                    is = null;
                }
            }
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    uploadPolicy
                            .displayWarn(e.getClass().getName()
                                    + " while trying to close FileOutputStream, in PictureUploadPolicy.copyOriginalToWorkingCopyTempFile.");
                } finally {
                    os = null;
                }
            }
        }
    }

    /**
     * This method is called when an OutOfMemoryError occurs. This can easily
     * happen within the navigator, with big pictures: I've put a lot of
     * freeMemory calls within the code, but they don't seem to work very well.
     * When running from eclipse, the memory is freed Ok !
     */
    private void tooBigPicture() {
        String msg = String.format(
                this.uploadPolicy.getString("tooBigPicture"), getFileName());
        JOptionPane.showMessageDialog(null, msg, "Warning",
                JOptionPane.WARNING_MESSAGE);
        this.uploadPolicy.displayWarn(msg);
    }

    /**
     * This methods set the {@link DefaultFileData#mimeType} to the image mime
     * type, that should be associate with the picture.
     */
    // FIXME PictureFileData.setMimeTypeByExtension(String): this method take
    // the mime type from the original picture file (to update according to the
    // targetPictureFormat parameter).
    private void setMimeTypeByExtension(String fileExtension) {
        String ext = fileExtension.toLowerCase();
        if (ext.equals("jpg")) {
            ext = "jpeg";
        }
        this.mimeType = "image/" + ext;
    }

    /**
     * If {@link #workingCopyTempFile} is null, create a new temporary file, and
     * assign it to {@link  #transformedPictureFile}. Otherwise, no action.
     * 
     * @throws IOException
     */
    private void createWorkingCopyTempFile() throws IOException {
        if (this.workingCopyTempFile == null) {
            // The temporary file must have the correct extension, so that
            // native Java method works on it.
            this.workingCopyTempFile = File.createTempFile("jupload_", ".tmp."
                    + getExtension(getFile()));
            this.uploadPolicy.getApplet().registerUnload(this,
                    "deleteWorkingCopyPictureFile");
            this.uploadPolicy.displayDebug("Using working copy temp file "
                    + this.workingCopyTempFile.getAbsolutePath() + " for "
                    + getFileName(), 50);
        }
    }

    /**
     * If {@link #transformedPictureFile} is null, create a new temporary file,
     * and assign it to {@link  #transformedPictureFile}. Otherwise, no action.
     * 
     * @throws IOException
     */
    private void createTransformedTempFile() throws IOException {
        if (this.transformedPictureFile == null) {
            this.transformedPictureFile = File.createTempFile("jupload_",
                    ".tmp");
            this.uploadPolicy.getApplet().registerUnload(this,
                    "deleteTransformedPictureFile");
            this.uploadPolicy.displayDebug("Using transformed temp file "
                    + this.transformedPictureFile.getAbsolutePath() + " for "
                    + getFileName(), 50);
        }
    }

    // ////////////////////////////////////////////////////////////////////////////////////////////////////
    // /////////////////////// static methods
    // ////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Read the Image from the source file. This call is the only call in this
     * class to read the picture file.
     * 
     * @return
     * @throws JUploadIOException
     */
    private BufferedImage readImage() throws JUploadIOException {
        BufferedImage bi;

        this.uploadPolicy.displayDebug("File read: "
                + getWorkingSourceFile().getAbsolutePath(), 60);
        try {
            bi = ImageIO.read(getWorkingSourceFile());
        } catch (IOException e) {
            throw new JUploadIOException(this.getClass().getName(), e);
        }
        // Before loading a new picture, let's free any unused memory.
        freeMemory("end of readImage");

        return bi;

    }

    /**
     * Returns an ImageIcon for the given file, resized according to the given
     * dimensions. If the original file contains a pictures smaller than these
     * width and height, the picture is returned as is (nor resized).
     * 
     * @param pictureFile The file, containing a picture, from which the user
     *            wants to extract a static picture.
     * @param maxWidth The maximum allowed width for the static picture to
     *            generate.
     * @param maxHeight The maximum allowed height for the static picture to
     *            generate.
     * @return The created static picture, or null if the file is null.
     */
    public static ImageIcon getImageIcon(File pictureFile, int maxWidth,
            int maxHeight) {
        ImageIcon thumbnail = null;

        if (pictureFile != null) {
            ImageIcon tmpIcon = new ImageIcon(pictureFile.getPath());
            if (tmpIcon != null) {
                double scaleWidth = ((double) maxWidth)
                        / tmpIcon.getIconWidth();
                double scaleHeight = ((double) maxHeight)
                        / tmpIcon.getIconHeight();
                double scale = Math.min(scaleWidth, scaleHeight);

                if (scale < 1) {
                    thumbnail = new ImageIcon(tmpIcon.getImage()
                            .getScaledInstance(
                                    (int) (scale * tmpIcon.getIconWidth()),
                                    (int) (scale * tmpIcon.getIconHeight()),
                                    Image.SCALE_FAST));
                } else { // no need to miniaturize
                    thumbnail = tmpIcon;
                }
            }
        }
        return thumbnail;
    }

    /**
     * Erases metadata in the transformed picture file, or in a copy of the
     * original file.<BR>
     * When the applet should not transmit the picture metadata, nor resize or
     * rotate the picture before upload, we need to copy the original file to a
     * temporary one, and erase the metadata in this copy. <BR>
     * <B>Note:</B> currently unused as there is no standard writter that can
     * replace metadata (see ImageWriter.canReplaceMetatadata method).
     * 
     * @throws JUploadException
     */
    @SuppressWarnings("unused")
    private void clearPictureFileMetadata() throws JUploadIOException {
        boolean metadataClearDone = false;

        try {
            Iterator<ImageWriter> iter = ImageIO
                    .getImageWritersByFormatName("JPG");
            FileImageOutputStream output = new FileImageOutputStream(
                    this.transformedPictureFile);

            while (iter.hasNext()) {
                ImageWriter writer = iter.next();
                writer.setOutput(output);
                if (writer.canReplaceImageMetadata(0)) {
                    writer.replaceImageMetadata(0, null);
                    // The work is done. Let's go out of this loop.
                    metadataClearDone = true;
                    break;
                }
            }// while
        } catch (IOException ioe) {
            throw new JUploadIOException(ioe);
        }

        if (!metadataClearDone) {
            uploadPolicy
                    .displayWarn("Image metada not cleared: will be transmitted with pictures");
        }
    }

}
