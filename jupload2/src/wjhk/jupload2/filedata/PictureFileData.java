//
// $Id: PictureFileData.java 137 2007-05-12 23:34:08 +0000 (sam., 12 mai 2007)
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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;
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
 * <LI> Optionnal definition of a maximal width and/or height.
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
public class PictureFileData extends DefaultFileData {

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
     * <LI>1 means a rotation of 90� clockwise (word = Ok ??).
     * <LI>2 means a rotation of 180�.
     * <LI>3 means a rotation of 90� counterclockwise (word = Ok ??).
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
    public PictureFileData(File file, PictureUploadPolicy uploadPolicy) {
        super(file, uploadPolicy);
        // EGR Should be useless
        // this.uploadPolicy = (PictureUploadPolicy) super.uploadPolicy;
        this.storeBufferedImage = uploadPolicy.hasToStoreBufferedImage();

        String fileExtension = getFileExtension();

        // Is it a picture?
        Iterator iter = ImageIO.getImageReadersByFormatName(fileExtension);
        this.isPicture = iter.hasNext();
        uploadPolicy.displayDebug("isPicture=" + this.isPicture + " ("
                + file.getName() + "), extension=" + fileExtension, 75);

        // If it's a picture, we override the default mime type:
        if (this.isPicture) {
            setMimeTypeByExtension(fileExtension);
        }
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
                if (hasToTransformPicture()) {
                    getTransformedPictureFile();
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
                this.uploadLength = -1;
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
                localImage = getBufferedImage(canvasWidth, canvasHeight,
                        ((PictureUploadPolicy) this.uploadPolicy)
                                .getHighQualityPreview());
                /**
                 * getBufferedImage returns a picture of the correct size. There
                 * is no need to do the checks below. int originalWidth =
                 * bufferedImage.getWidth (); int originalHeight =
                 * bufferedImage.getHeight(); debug: getBufferedImage() orw et
                 * orh float scaleWidth = (float) maxWidth / originalWidth;
                 * float scaleHeight = (float) maxHeight / originalHeight; float
                 * scale = Math.min(scaleWidth, scaleHeight); //Should we resize
                 * this picture ? if (scale < 1) { int width = (int) (scale *
                 * originalWidth); int height = (int) (scale * originalHeight);
                 * image = bufferedImage.getScaledInstance(width, height,
                 * Image.SCALE_DEFAULT); uploadPolicy.displayDebug("Picture
                 * resized: " + getFileName(), 75); //Diminuer le nombre de
                 * couleurs ? } else { image = bufferedImage;
                 * uploadPolicy.displayDebug("Picture not resized: " +
                 * getFileName(), 75); }
                 */
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

        return localImage;
    }// getImage

    /**
     * This function is used to rotate the picture.
     * 
     * @param quarter Number of quarters (90�) the picture should rotate. 1
     *            means rotating of 90� clockwise (?). Can be negative.
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
    // ///////////////////////////////////////////
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
    private BufferedImage getBufferedImage(int maxWidth, int maxHeight,
            boolean highquality) throws JUploadException {
        BufferedImage bufferedImage = null;

        if (!this.isPicture) {
            // This case is quite simple !
            bufferedImage = null;
            // This test is currently useless. But I hope I'll get the
            // bufferedImage back as a class attribute, instead of
            // a local parameter.
        } else if (bufferedImage == null) {
            BufferedImage localBufferedImage;

            // Before loading a new picture, let's free any unused memory.
            freeMemory("start of getBufferedImage");

            try {
                localBufferedImage = ImageIO.read(getFile());

                AffineTransform transform = new AffineTransform();

                // Let's store the original image width and height. It can be
                // used elsewhere.
                // (see hasToTransformPicture, below).
                this.originalWidth = localBufferedImage.getWidth();
                this.originalHeight = localBufferedImage.getHeight();

                // ////////////////////////////////////////////////////////////
                // Let's calculate by how much we should reduce the picture :
                // scale
                // ///////////////////////////////////////////////////////////

                // The width and height depend on the current rotation :
                // calculation of the width and height
                // of picture after rotation.
                int nonScaledRotatedWidth = this.originalWidth;
                int nonScaledRotatedHeight = this.originalHeight;
                if (this.quarterRotation % 2 != 0) {
                    // 90� or 270� rotation: width and height are switched.
                    nonScaledRotatedWidth = this.originalHeight;
                    nonScaledRotatedHeight = this.originalWidth;
                }
                // Now, we can compare these width and height to the maximum
                // width and height
                float scaleWidth = ((maxWidth < 0) ? 1 : ((float) maxWidth)
                        / nonScaledRotatedWidth);
                float scaleHeight = ((maxHeight < 0) ? 1 : ((float) maxHeight)
                        / nonScaledRotatedHeight);
                float scale = Math.min(scaleWidth, scaleHeight);
                // FIXME The scaleWidth and scaleHeigth is wrong when the
                // maxHeight and maxWidth are different, and the picture must be
                // rotated by one quarter (in either direction)
                //
                if (scale < 1) {
                    // With number rouding, it can happen that width or size
                    // became one pixel too big. Let's correct it.
                    if ((maxWidth > 0 && maxWidth < (int) (scale * nonScaledRotatedWidth))
                            || (maxHeight > 0 && maxHeight < (int) (scale * nonScaledRotatedHeight))) {
                        scaleWidth = ((maxWidth < 0) ? 1 : ((float) maxWidth)
                                / (nonScaledRotatedWidth - 1));
                        scaleHeight = ((maxHeight < 0) ? 1
                                : ((float) maxHeight)
                                        / (nonScaledRotatedHeight - 1));
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

                if (this.quarterRotation != 0) {
                    double theta = Math.toRadians(90 * this.quarterRotation);
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
                        Image img = localBufferedImage.getScaledInstance(
                                (int) (this.originalWidth * scale),
                                (int) (this.originalHeight * scale),
                                Image.SCALE_SMOOTH);
                        img.flush();

                        // the localBufferedImage may be 'unknwon'.
                        int localImageType = localBufferedImage.getType();
                        if (localImageType == BufferedImage.TYPE_CUSTOM) {
                            localImageType = BufferedImage.TYPE_INT_BGR;
                        }
                        localBufferedImage = new BufferedImage(
                                (int) (this.originalWidth * scale),
                                (int) (this.originalHeight * scale),
                                localImageType);
                        localBufferedImage.getGraphics().drawImage(img, 0, 0,
                                null);
                        localBufferedImage.flush();
                        img = null;
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

                if (transform.isIdentity()) {
                    // No transformation
                    bufferedImage = localBufferedImage;
                } else {
                    AffineTransformOp affineTransformOp = null;
                    /*
                     * //This switch is temporary : it allows easy comparison
                     * between different methods. // The pictures seems Ok, but
                     * if anyone has a better solution : I take it! // switch
                     * (0) { case 0:
                     */
                    // Pictures are Ok.
                    affineTransformOp = new AffineTransformOp(transform,
                            AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
                    bufferedImage = affineTransformOp
                            .createCompatibleDestImage(localBufferedImage, null);
                    /*
                     * break; case 1: //This options create black pictures !
                     * affineTransformOp = new AffineTransformOp(transform,
                     * AffineTransformOp.TYPE_BILINEAR); bufferedImage =
                     * affineTransformOp.createCompatibleDestImage(localBufferedImage,
                     * ColorModel.getRGBdefault()); break; case 2: //This
                     * options create also black pictures ! affineTransformOp =
                     * new AffineTransformOp(transform,
                     * AffineTransformOp.TYPE_NEAREST_NEIGHBOR); bufferedImage =
                     * affineTransformOp.createCompatibleDestImage(localBufferedImage,
                     * ColorModel.getRGBdefault()); break; case 3: //Pictures
                     * are Ok. affineTransformOp = new
                     * AffineTransformOp(transform,
                     * AffineTransformOp.TYPE_BILINEAR); bufferedImage =
                     * affineTransformOp.createCompatibleDestImage(localBufferedImage,
                     * null); break; case 100: //This options create black
                     * pictures ! affineTransformOp = new
                     * AffineTransformOp(transform,
                     * AffineTransformOp.TYPE_BILINEAR); //bufferedImage =
                     * affineTransformOp.createCompatibleDestImage(localBufferedImage,
                     * ColorModel.getRGBdefault()); bufferedImage = new
                     * BufferedImage(scaledWidth, scaledHeight,
                     * BufferedImage.TYPE_INT_RGB); break; }
                     */
                    affineTransformOp.filter(localBufferedImage, bufferedImage);
                    affineTransformOp = null;

                    bufferedImage.flush();
                }

                // Let's free some memory : useful when running as an applet
                localBufferedImage.flush();
                localBufferedImage = null;
                transform = null;
                freeMemory("end of getBufferedImage");
            } catch (Exception e) {
                throw new JUploadException(e.getClass().getName()
                        + " (createBufferedImage) : " + e.getMessage());
            }

            if (bufferedImage != null
                    && this.uploadPolicy.getDebugLevel() >= 60) {
                this.uploadPolicy.displayDebug("bufferedImage MinX ("
                        + bufferedImage + "): " + bufferedImage.getMinX(), 60);
                this.uploadPolicy.displayDebug("bufferedImage MinY ("
                        + bufferedImage + "): " + bufferedImage.getMinY(), 60);
                this.uploadPolicy.displayDebug("bufferedImage Width ("
                        + bufferedImage + "): " + bufferedImage.getWidth(), 60);
                this.uploadPolicy
                        .displayDebug("bufferedImage Height (" + bufferedImage
                                + "): " + bufferedImage.getHeight(), 60);
            }
        }
        return bufferedImage;
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
    private boolean hasToTransformPicture() throws JUploadException {

        // Did we already estimate if transformation is needed ?
        if (this.hasToTransformPicture == null) {
            // We only tranform pictures.
            if (!this.isPicture) {
                this.hasToTransformPicture = Boolean.FALSE;
            }

            // First : the easiest test. A rotation is needed ?
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
            // If we don't have the original size, we start by calculating it.
            if (this.hasToTransformPicture == null
                    && (this.originalWidth < 0 || this.originalHeight < 0)) {
                try {
                    BufferedImage originalImage = ImageIO.read(getFile());
                    this.originalWidth = originalImage.getWidth();
                    this.originalHeight = originalImage.getHeight();
                    // Within the navigator, we have to free memory ASAP
                    originalImage = null;
                    freeMemory("hasToTransformPicture");
                } catch (IOException e) {
                    throw new JUploadException(
                            "IOException in ImageIO.read (hasToTransformPicture) : "
                                    + e.getMessage());
                }
            }

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

            // If we find no reason to tranform the picture, then let's let the
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
    public void deleteTransformedPictureFile() {
        if (null != this.transformedPictureFile) {
            this.transformedPictureFile.delete();
            this.transformedPictureFile = null;
        }
    }

    /**
     * Creation of a temporary file, that contains the transformed picture. For
     * instance, it can be resized or rotated. This method doesn't throw
     * exception when there is an IOException within its procedure. If an
     * exception occurs while building the temporary file, the exception is
     * catched, a warning is displayed,the temporary file is deleted (if it was
     * created), and the upload will go on with the original file. <BR>
     * Note: any JUploadException thrown by a method called within
     * getTransformedPictureFile() will be thrown within this method.
     */
    private File getTransformedPictureFile() {
        BufferedImage bufferedImage = null;
        String tmpFileName = null;
        // Do we already created the transformed file ?
        if (this.transformedPictureFile == null) {
            try {
                this.transformedPictureFile = File.createTempFile("jupload_",
                        ".tmp");
                this.uploadPolicy.getApplet().registerUnload(this,
                        "deleteTransformedPictureFile");
                tmpFileName = this.transformedPictureFile.getAbsolutePath();
                this.uploadPolicy.displayDebug("Using temp file " + tmpFileName
                        + " for " + getFileName(), 50);

                String localPictureFormat = (((PictureUploadPolicy) this.uploadPolicy)
                        .getTargetPictureFormat() == null) ? getFileExtension()
                        : ((PictureUploadPolicy) this.uploadPolicy)
                                .getTargetPictureFormat();

                // Prepare (if not already done) the bufferedImage.

                // If the image is rotated, we compare to realMaxWidth and
                // realMaxHeight, instead of
                // maxWidth and maxHeight. This allows to have a different
                // picture size for rotated and
                // not rotated pictures. See the UploadPolicy javadoc for
                // details ... and a good reason ! ;-)
                if (this.quarterRotation == 0) {
                    bufferedImage = getBufferedImage(
                            ((PictureUploadPolicy) this.uploadPolicy)
                                    .getMaxWidth(),
                            ((PictureUploadPolicy) this.uploadPolicy)
                                    .getMaxHeight(), true);
                } else {
                    bufferedImage = getBufferedImage(
                            ((PictureUploadPolicy) this.uploadPolicy)
                                    .getRealMaxWidth(),
                            ((PictureUploadPolicy) this.uploadPolicy)
                                    .getRealMaxHeight(), true);
                }

                // Get the writer (to choose the compression quality)
                Iterator iter = ImageIO
                        .getImageWritersByFormatName(localPictureFormat);
                if (iter.hasNext()) {
                    ImageWriter writer = (ImageWriter) iter.next();
                    ImageWriteParam iwp = writer.getDefaultWriteParam();

                    // Remove the lower compression, and take the default one.
                    // This will create smaller resized pictures.
                    // Better for web use.
                    // iwp.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                    // float values[] = iwp.getCompressionQualityValues();
                    // Let's select the best available quality.
                    // iwp.setCompressionQuality(values[values.length - 1]);

                    //
                    try {
                        this.uploadPolicy.displayDebug(
                                "ImageWriter1 (used), CompressionQuality="
                                        + iwp.getCompressionQuality(), 95);
                    } catch (Exception e) {
                        // compression not supported. May trigger several
                        // different errors.
                    }

                    // Let's create the picture file.
                    FileImageOutputStream output = new FileImageOutputStream(
                            this.transformedPictureFile);
                    writer.setOutput(output);
                    IIOImage image = new IIOImage(bufferedImage, null, null);
                    writer.write(null, image, iwp);
                    writer.dispose();
                    output.close();
                    output = null;

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
                if (!this.storeBufferedImage) {
                    bufferedImage = null;
                    freeMemory("getTransformedPictureFile");
                }
                this.uploadPolicy.displayDebug("transformedPictureFile : "
                        + this.transformedPictureFile.getName(), 30);
            } catch (Exception e) {
                // We mask any exception that occurs within this method. The
                // called method should raise
                // JUploadException, so their exceptions won't be catched here.
                this.uploadPolicy.displayWarn(e.getClass().getName()
                        + " while writing the " + tmpFileName
                        + " file. (picture will not be transformed)");
                if (e instanceof FileNotFoundException) {
                    this.uploadPolicy
                            .displayInfo(e.getClass().getName()
                                    + " probably means that the directory containing the picture is readonly: the applet can't write its temporary file.");
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

        return this.transformedPictureFile;
    }// end of getTransformedPictureFile

    /**
     * This method is called when an OutOfMemoryError occurs. This can easily
     * happen within the navigator, with big pictures: I've put a lot of
     * freeMemory calls within the code, but they don't seem to work very well.
     * When running from eclipse, the memory is freed Ok !
     */
    private void tooBigPicture() {
        String msg = this.uploadPolicy
                .getString("tooBigPicture", getFileName());
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
}
