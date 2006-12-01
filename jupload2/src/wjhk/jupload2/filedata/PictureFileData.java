/*
 * Created on 9 mai 2006
 */
package wjhk.jupload2.filedata;

import java.awt.Canvas;
import java.awt.Image;
import java.awt.color.ColorSpace;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
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

import wjhk.jupload2.exception.JUploadException;
import wjhk.jupload2.exception.JUploadIOException;
import wjhk.jupload2.policies.PictureUploadPolicy;
import wjhk.jupload2.policies.UploadPolicy;


/**
 * 
 * This class contains all data for files to upload a picture. It adds the following 
 * elements to the {@link wjhk.jupload2.filedata.FileData} class :<BR>
 * <UL>
 * <LI> Ability to define a target format (to convert pictures to JPG before upload, for instance)
 * <LI> Optionnal definition of a maximal width and/or height.
 * <LI> Ability to rotate a picture, with {@link #addRotation(int)}
 * <LI> Ability to store a picture into a BufferedImage. This is actualy a bad idea within
 * an applet (should run within a java application) : the applet runs very quickly out of 
 * memory. With pictures from my Canon EOS20D (3,5M), I can only display two pictures. The 
 * third one generates an out of memory error, despiste the System.finalize and System.gc 
 * I've put everywhere in the code!   
 * </UL>
 * 
 * @author Etienne Gauthier
 *
 */
public class PictureFileData extends DefaultFileData  {

	/**
	 * Indicate whether the data for this fileData has already been intialized.
	 */
	//private boolean initialized = false;

	/**
	 * Indicates if this file is a picture or not. This is bases on the return of 
	 * ImageIO.getImageReadersByFormatName().
	 * 
	 */
	private boolean isPicture = false;

	/**
	 * If set to true, the PictureFileData will keep the BufferedImage in memory. That is: it won't
	 * load it again from the hard drive, and resize and/or rotate it (if necessary) when the user select this
	 * picture. When picture are big this is nice. 
	 * <BR><BR>
	 * <B>Caution:</B> this parameter is currently unused, as the navigator applet runs quickly out of memory (after
	 * three or four picture for my Canon EOS 20D, 8,5 Mega pixels). 
	 * 
	 * @see UploadPolicy
	 */
	boolean storeBufferedImage = UploadPolicy.DEFAULT_STORE_BUFFERED_IMAGE;//Will be erased while in the constructor.
	
	/**
	 * bufferedImage contains a preloaded picture. This buffer is used according to 
	 * PictureFileDataPolicy.storeBufferedImage.
	 * 
	 * @see PictureUploadPolicy#storeBufferedImage
	 * 
	 */
	//private BufferedImage bufferedImage = null;
	//Currently commented, as it leads to memory leaks.
	
	
	/**
	 * This picture is precalculated, and stored to avoid to calculate it each time the user 
	 * select this picture again, or each time the use switch from an application to another.
	 */
	private Image offscreenImage = null;
		
	/**
	 * quarterRotation contains the current rotation that will be applied to the picture. Its value should be one
	 * of 0, 1, 2, 3. It is controled by the {@link #addRotation(int)} method.
	 * <UL>
	 * <LI>0 means no rotation. 
	 * <LI>1 means a rotation of 90° clockwise (word = Ok ??). 
	 * <LI>2 means a rotation of 180°. 
	 * <LI>3 means a rotation of 90° counterclockwise  (word = Ok ??).
	 * </UL>  
	 */
	int quarterRotation = 0;
	
	/**
	 * Width of the original picture. Negative if unknown, for instance if the picture has not yet been opened. 
	 */
	int originalWidth = -1;
	
	/**
	 * Height of the original picture. Negative if unknown, for instance if the picture has not yet been opened.
	 */
	int originalHeight = -1;
	
	
	/**
	 * transformedPictureFile contains the reference to the temporary file that stored the transformed picture,
	 * during upload. It is created by {@link #getInputStream()} and freed by {@link #afterUpload()}.
	 */
	private File transformedPictureFile = null;
	
	/**
	 * uploadLength contains the uploadLength, which is :
	 * <BR> - The size of the original file, if no transformation is needed.
	 * <BR> - The size of the transformed file, if a transformation were made.
	 * <BR><BR>
	 * It is set to -1 whenever the user ask for a rotation (current only action that need to recalculate the picture).
	 */
	private long uploadLength = -1;
	
	/**
	 * hasToTransformPicture indicates whether the tranform should be transformed. Null if unknown. This can happen
	 * (for instance) if no calcul where done (during initialization), or after rotating the picture back to the 
	 * original orientation.
	 * <BR>
	 * <B>Note:</B> this attribute is from the class Boolean (and not a simple boolean), to allow null value, meaning
	 * <I>unknown</I>.
	 */
	private Boolean hasToTransformPicture = null;
	
	/**
	 * For this class, the UploadPolicy is a PictureUploadPolicy, or one class that inherits from it.
	 */
	PictureUploadPolicy uploadPolicy;
	
	//////////////////////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * Standard constructor: needs a PictureFileDataPolicy.
	 * 
	 * @param file The files which data are to be handled by this instance.
	 */
	public PictureFileData(File file, PictureUploadPolicy uploadPolicy) {
		super(file, uploadPolicy);
		this.uploadPolicy = (PictureUploadPolicy)super.uploadPolicy;
		storeBufferedImage = uploadPolicy.hasToStoreBufferedImage();
		
		String fileExtension = getFileExtension();
		
		//Is it a picture?
		Iterator iter = ImageIO.getImageReadersByFormatName(fileExtension);
		isPicture = iter.hasNext();
		uploadPolicy.displayDebug("isPicture=" + isPicture + " (" + file.getName()+ "), extension=" + fileExtension, 75);
		
		//If it's a picture, we override the default mime type:
		if (isPicture) {
			setMimeTypeByExtension(fileExtension);
		}
	}
	
	/**
	 * Free any available memory. This method is called very often here, to be sure that we don't use too much
	 * memory. But we still run out of memory in some case.
	 * 
	 * @param caller Indicate the method or treatment from which this method is called.
	 */
	public void freeMemory(String caller) {
		Runtime rt = Runtime.getRuntime();

		/*
		uploadPolicy.displayDebug("freeMemory : " + caller, 80);
		uploadPolicy.displayDebug("freeMemory (before " + caller + ") : " + rt.freeMemory(), 80);
		uploadPolicy.displayDebug("maxMemory  (before " + caller + ") : " + rt.maxMemory(), 80);
		*/

		rt.runFinalization();
		rt.gc();
		
		uploadPolicy.displayDebug("freeMemory (after " + caller + ") : " + rt.freeMemory(), 80);
		/*
		uploadPolicy.displayDebug("maxMemory  (after " + caller + ") : " + rt.maxMemory(), 80);
		*/
	}
	
	/**
	 * If this pictures needs transformation, a temporary file is created. This can occurs if the original picture
	 * is bigger than the maxWidth or maxHeight, of if it has to be rotated. This temporary file contains the 
	 * transformed picture. 
	 * <BR>
	 * The call to this method is optional, if the caller calls {@link #getUploadLength()}. This method calls
	 * beforeUpload() if the uploadLength is unknown.
	 */
	public void beforeUpload() throws JUploadException {
		if (uploadLength < 0) {
			try {
				if (hasToTransformPicture()) {
					getTransformedPictureFile();
				}
			} catch (OutOfMemoryError e) {
				//Oups ! My EOS 20D has too big pictures to handle more than two pictures in a navigator applet !!!!!
				//    :-(
				//
				//We don't transform it. We clean the file, if it has been created.
				if (transformedPictureFile != null) {
					transformedPictureFile.delete();
				}
				transformedPictureFile = null;
				//
				tooBigPicture();
			}
	
			//If the transformed picture is correctly created, we'll upload it. Else we upload the original file.
			if (transformedPictureFile != null) {
				uploadLength = transformedPictureFile.length();
			} else {
				uploadLength = getFile().length();
			}
		}
	}

	/**
	 * 
	 * Returns the number of bytes, for this upload. If needed, that is, if uploadlength is unknown, 
	 * {@link #beforeUpload()} is called.
	 * 
	 * @return The length of upload. In this class, this is ... the size of the original file, or the transformed file! 
	 */
	public long getUploadLength() throws JUploadException {
		if (uploadLength < 0) {
			//Hum, beforeUpload should have been called before. Let's correct that. 
			beforeUpload();
		}
		return uploadLength;
	}

	/**
	 * This function create an input stream for this file. The caller is responsible
	 * for closing this input stream.
	 * <BR>
	 * This function assumes that the {@link #getUploadLength()} method has already be called : it is responsible
	 * for creating the temporary file (if needed). If not called, the original file will be sent. 
	 * 
	 * @return An inputStream 
	 */
	public InputStream getInputStream () throws JUploadException {
		//Do we have to transform the picture ?
		if (transformedPictureFile != null) {
			try {
				return new FileInputStream(transformedPictureFile);
			} catch (FileNotFoundException e) {
				throw new JUploadIOException(e, "PictureFileData.getInputStream()");
			}
		} else { 
			//Otherwise : we read the file, in the standard way.
			return super.getInputStream();
		}
	}
	
	/**
	 * Cleaning of the temporary file on the hard drive, if any. 
	 * <BR>
	 * <B>Note:</B> if the debugLevel is 100 (or more) this temporary file is not removed. This allow control
	 * of this created file. 
	 */
	public void afterUpload () {
		super.afterUpload();
		
		//Free the temporary file ... if any.
		if (transformedPictureFile != null) {
			//for debug : if the debugLevel is enough, we keep the temporary file (for check). 
			if (uploadPolicy.getDebugLevel() >= 100) {
				uploadPolicy.displayWarn("Temporary file not deleted");
			} else {
				transformedPictureFile.delete();
				transformedPictureFile = null;
				uploadLength = -1;
			}
		}
	}
		
	/**
	 * This method creates a new Image, from the current picture. The resulting width and height will be less or
	 * equal than the given maximum width and height. The scale is maintained. Thus the width or height may be 
	 * inferior than the given values. 
	 *  
	 * @param canvas The canvas on which the picture will be displayed.
	 * @param shadow True if the pictureFileData should store this picture. False if the pictureFileData instance should
	 * not store this picture. Store this picture avoid calculating the image each time the user selects it in the file panel. 
	 * 
	 * @return The rescaled image.
	 */
	public Image getImage (Canvas canvas, boolean shadow) throws JUploadException {
		freeMemory("start of " + this.getClass().getName() + ".getImage()");

		BufferedImage bufferedImage = null;
		Image image=null;
		boolean hasToCalculateImage = false;
		
		if (canvas == null) {
			throw new JUploadException("canvas null in PictureFileData.getImage");
		}

		int canvasWidth  = canvas.getWidth();
		int canvasHeight = canvas.getHeight(); 
		if (canvasWidth <= 0 || canvasHeight <= 0) {
			//Target width and/or height currenlty unknown (see Image.GetWidth())
			hasToCalculateImage = false;
		} else if (! shadow) {
			hasToCalculateImage = true;
		} else if (offscreenImage == null) {
			hasToCalculateImage = true;
		/*
		 * This test seems non accurate (offscreenImage.getWidth(canvas) returns the image side, truncated by the 
		 * canvas side), and useles
		 * I keep it in the code. I'll remove it when I'm sure everything is Ok. 
		} else {
			hasToCalculateImage = (offscreenImage.getWidth(canvas) != maxWidth  || offscreenImage.getHeight(canvas) != maxHeight);
		*/
		}

		if ( ! hasToCalculateImage) {
			image = offscreenImage;
		} else {
	        if (isPicture) {
				try {
					//FIXME (minor) getBufferedImage() that can be smaller than the PictureDialog (if maxWidth or maxHeigth are smaller than the PictureDialog)
					//bufferedImage 
					image = getBufferedImage(canvasWidth, canvasHeight);
					/**
					 * getBufferedImage returns a picture of the correct size.
					 * There is no need to do the checks below.
					int originalWidth  = bufferedImage.getWidth ();
					int originalHeight = bufferedImage.getHeight();
					
					debug: getBufferedImage() orw et orh
					
					float scaleWidth = (float) maxWidth / originalWidth;
					float scaleHeight = (float) maxHeight / originalHeight;
					float scale = Math.min(scaleWidth, scaleHeight);
					//Should we resize this picture ?
					if (scale < 1) {
						int width  = (int) (scale * originalWidth);
						int height = (int) (scale * originalHeight);
						image = bufferedImage.getScaledInstance(width, height, Image.SCALE_DEFAULT);
						uploadPolicy.displayDebug("Picture resized: " + getFileName(), 75);
						//Diminuer le  nombre de couleurs ?
					} else {
						image = bufferedImage;
						uploadPolicy.displayDebug("Picture not resized: " + getFileName(), 75);
					}
					*/
				} catch (OutOfMemoryError e) {
					//Too bad
					bufferedImage = null;
					image = null;
					tooBigPicture();
				}
	        } //If isPicture
			
			//We store it, if asked to.
			if (shadow) {
				offscreenImage = image;
			}
			
			//Within the navigator, we have to free memory ASAP
			if (!storeBufferedImage  &&  bufferedImage != null) {
					bufferedImage.flush();
					bufferedImage = null;
					freeMemory("end of getOffscreenImage");
			}
		}

		if (image != null) {
			uploadPolicy.displayDebug("image Width: " + image.getWidth(canvas), 60);
			uploadPolicy.displayDebug("image Height: " + image.getHeight(canvas), 60);
		}

		freeMemory("end of " + this.getClass().getName() + ".getImage()");
		return image;
	}//getOffscreenImage
	
	/**
	 * This function is used to rotate the picture. 
	 * 
	 * @param quarter Number of quarters (90°) the picture should rotate. 1 means rotating of 90° clockwise (?). Can be negative.
	 * @see #quarterRotation
	 */
	public void addRotation(int quarter) {
		quarterRotation += quarter;
		uploadLength = -1;
		
		//We keep the 'quarter' in the segment [0;4[
		while (quarterRotation < 0) {
			quarterRotation += 4;
		}
		while (quarterRotation >= 4) {
			quarterRotation -= 4;
		}
		
		//Should we tranform the picture ?
		if (quarterRotation == 0) {
			//We're back to a non rotated picture. We don't know here if the picture is to be transformed.
			hasToTransformPicture = null;
		} else {
			hasToTransformPicture = Boolean.TRUE;
		}
		
		
		//The current calculated pictures are now wrong
		afterUpload();
	}
	
	/**
	 * @return the {@link #isPicture} flag.
	 */
	public boolean isPicture() {
		return isPicture;
	}
	
	/** @see FileData#getMimeType() */
	public String getMimeType () {
		return mimeType;
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////  private METHODS    ///////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * This function resizes the picture, if necessary, according to the maxWidth and
	 * maxHeight of fileDataPolicy.
	 * This function should only be called if isPicture is true. But calling it with isPicture to false	just
	 * won't do anything. 
	 *  
	 * @return A BufferedImage which contains the picture according to current parameters (resizing, rotation...), or
	 * null if this is not a picture.
	 */
	private BufferedImage getBufferedImage(int maxWidth, int maxHeight) throws JUploadException {
		BufferedImage bufferedImage = null;

		
		if (! isPicture) {
			//This case is quite simple !
			bufferedImage = null;
		//This test is currently useless. But I hope I'll get the bufferedImage back as a class attribute, instead of
		//a local parameter.
		} else if (bufferedImage == null) {
			BufferedImage localBufferedImage;
			
			//Before loading a new picture, let's free any unused memory.
			freeMemory("start of getBufferedImage");
			
			try {
				localBufferedImage = ImageIO.read(getFile());
				
				AffineTransform transform = new AffineTransform ();

				//Let's store the original image width and height. It can be used elsewhere.
				//(see hasToTransformPicture, below).
				originalWidth  = localBufferedImage.getWidth();
				originalHeight = localBufferedImage.getHeight();	    		
				
				//////////////////////////////////////////////////////////////
				// Let's calculate by how much we should reduce the picture : scale
				/////////////////////////////////////////////////////////////				
								
				//The width and height depend on the current rotation : calculation of the width and height
				//of picture after rotation.
				int nonScaledRotatedWidth  = originalWidth ;
				int nonScaledRotatedHeight = originalHeight;
				if (quarterRotation%2 != 0) {
					//90° or 270° rotation: width and height are switched.
					nonScaledRotatedWidth  = originalHeight;
					nonScaledRotatedHeight = originalWidth ;
				}
				//Now, we can compare these width and height to the maximum width and height
				float scaleWidth  = ((maxWidth <0) ? 1 : ((float) maxWidth ) / nonScaledRotatedWidth );    
				float scaleHeight = ((maxHeight<0) ? 1 : ((float) maxHeight) / nonScaledRotatedHeight);
				float scale = Math.min(scaleWidth, scaleHeight);
				//FIXME The scaleWidth and scaleHeigth is wrong when the maxHeight and maxWidth are different, and the picture must be rotated by one quarter (in either direction)
				//
				if (scale < 1) {
					//With number rouding, it can happen that width or size became one pixel too big. Let's correct it.
					if (   (maxWidth >0  &&  maxWidth <(int)(scale*nonScaledRotatedWidth ))  
						|| (maxHeight>0  &&  maxHeight<(int)(scale*nonScaledRotatedHeight))  ) {
						scaleWidth  = ((maxWidth <0) ? 1 : ((float) maxWidth ) / (nonScaledRotatedWidth -1) );    
						scaleHeight = ((maxHeight<0) ? 1 : ((float) maxHeight) / (nonScaledRotatedHeight-1) );
						scale = Math.min(scaleWidth, scaleHeight);
					}
				}
				
				//These variables contain the actual width and height after recaling, and before rotation.
				int scaledWidth  = (int) (nonScaledRotatedWidth * scale);
				int scaledHeight = (int) (nonScaledRotatedHeight* scale);
				
				if (quarterRotation != 0) {
					double theta = Math.toRadians(90 * quarterRotation);
					double translationX=0, translationY=0;
					uploadPolicy.displayDebug("quarter: " + quarterRotation, 30);
					
					//quarterRotation is one of 0, 1, 2, 3 : see addRotation.
					//If we're here : it's not 0, so it's one of 1, 2 or 3.
					switch (quarterRotation) {
					case 1:
						translationX  = 0;
						translationY  = -scaledWidth;
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
						uploadPolicy.displayWarn("Invalid quarterRotation : " + quarterRotation);
						quarterRotation = 0;
						theta = 0;
					}
					transform.rotate(theta);
					transform.translate(translationX, translationY);
				}
								
				//If we have to rescale the picture, we first do it:
				if (scale < 1) {
					//The scale method adds scaling before current transformation.
		    		transform.scale(scale, scale);
				}	
				
				if (transform.isIdentity()) {
					//No transformation
					bufferedImage = localBufferedImage;
				} else {
					AffineTransformOp affineTransformOp = null;
					/*
					//This switch is temporary : it allows easy comparison between different methods.
					// The pictures seems Ok, but if anyone has a better solution : I take it!
					// 
					switch (0) {
					case 0:
					*/
						//Pictures are Ok.
						affineTransformOp = new AffineTransformOp(transform, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
						bufferedImage = affineTransformOp.createCompatibleDestImage(localBufferedImage, null);
					/*
						break;
					case 1:
						//This options create black pictures !
						affineTransformOp = new AffineTransformOp(transform, AffineTransformOp.TYPE_BILINEAR);
						bufferedImage = affineTransformOp.createCompatibleDestImage(localBufferedImage, ColorModel.getRGBdefault());
						break;
					case 2:
						//This options create also black pictures !
						affineTransformOp = new AffineTransformOp(transform, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
						bufferedImage = affineTransformOp.createCompatibleDestImage(localBufferedImage, ColorModel.getRGBdefault());
						break;
					case 3:
						//Pictures are Ok.
						affineTransformOp = new AffineTransformOp(transform, AffineTransformOp.TYPE_BILINEAR);
						bufferedImage = affineTransformOp.createCompatibleDestImage(localBufferedImage, null);
						break;
					case 100:
						//This options create black pictures !
						affineTransformOp = new AffineTransformOp(transform, AffineTransformOp.TYPE_BILINEAR);
						//bufferedImage = affineTransformOp.createCompatibleDestImage(localBufferedImage, ColorModel.getRGBdefault());
						bufferedImage = new BufferedImage(scaledWidth, scaledHeight, BufferedImage.TYPE_INT_RGB);
						break;
					}
					*/
					affineTransformOp.filter(localBufferedImage, bufferedImage);
					affineTransformOp = null;
				}

				//Let's free some memory : useful when running as an applet
				localBufferedImage = null;
				transform = null;
				freeMemory("end of getBufferedImage");
			} catch (IOException e) {
				throw new JUploadException("IOException (createBufferedImage) : " + e.getMessage());
			}
			
			if (bufferedImage != null) {
				uploadPolicy.displayDebug("bufferedImage MinX: " + bufferedImage.getMinX(), 60);
				uploadPolicy.displayDebug("bufferedImage MinY: " + bufferedImage.getMinY(), 60);
				uploadPolicy.displayDebug("bufferedImage Width: " + bufferedImage.getWidth(), 60);
				uploadPolicy.displayDebug("bufferedImage Height: " + bufferedImage.getHeight(), 60);
			}
		}
		return bufferedImage;
	}
	
	/**
	 * This function indicate if the picture has to be modified. For instance : a maximum width, height, a target format...
	 * 
	 * @see PictureUploadPolicy
	 * @see #quarterRotation
	 * 
	 * @return true if the picture must be transformed. false if the file can be directly transmitted. 
	 */
	private boolean hasToTransformPicture() throws JUploadException {

		//Did we already estimate if transformation is needed ?
		if (hasToTransformPicture == null) {
			//We only tranform pictures.
			if (!isPicture) {
				hasToTransformPicture = Boolean.FALSE;
			}
			
			//First : the easiest test. A rotation is needed ?
			if (hasToTransformPicture == null && quarterRotation != 0) {
				uploadPolicy.displayDebug(getFileName() + " : hasToTransformPicture = true", 20);
				hasToTransformPicture = Boolean.TRUE;
			}
			
			//Second : the picture format is the same ?
			if (hasToTransformPicture == null && uploadPolicy.getTargetPictureFormat() != null) {
				//A target format is positionned: is it the same as the current file format ?
				String target = uploadPolicy.getTargetPictureFormat().toLowerCase();
				String ext = getFileExtension().toLowerCase();
				
				if (target.equals("jpg")) target = "jpeg";
				if (ext   .equals("jpg")) ext    = "jpeg";
				
				if (! target.equals(ext)) {
					uploadPolicy.displayDebug(getFileName() + " : hasToTransformPicture = true", 20);
					//Third : should we resize the picture ?
				}
			}
			
			//Third : should we resize the picture ?
			//If we don't have the original size, we start by calculating it.
			if (hasToTransformPicture == null && (originalWidth<0 || originalHeight<0) ) {
				try {
					BufferedImage originalImage = ImageIO.read(getFile());
					originalWidth  = originalImage.getWidth();
					originalHeight = originalImage.getHeight();
					//Within the navigator, we have to free memory ASAP
					originalImage = null;
					freeMemory("hasToTransformPicture");
				} catch (IOException e) {
					throw new JUploadException("IOException in ImageIO.read (hasToTransformPicture) : " + e.getMessage());
				}
			}
			
			int rotatedWidth, rotatedHeight;
			//The width and height of the transformed picture depends on the rotation.
			if (quarterRotation%2 == 0) {
				rotatedWidth = originalWidth;
				rotatedHeight = originalHeight;
			} else {
				rotatedWidth = originalHeight;
				rotatedHeight = originalWidth;
			}
			
			if (hasToTransformPicture == null && uploadPolicy.getMaxWidth() > 0) { 
				if (rotatedWidth > uploadPolicy.getMaxWidth()) {
					uploadPolicy.displayDebug(getFileName() + " : hasToTransformPicture = true", 20);
					hasToTransformPicture = Boolean.TRUE;
				}
			}
			if (hasToTransformPicture == null && uploadPolicy.getMaxHeight() > 0) { 
				if (rotatedHeight > uploadPolicy.getMaxHeight()) {
					uploadPolicy.displayDebug(getFileName() + " : hasToTransformPicture = true", 20);
					hasToTransformPicture = Boolean.TRUE;
				}
			}				

			//If we find no reason to tranform the picture, then let's let the picture unmodified.
			if (hasToTransformPicture == null) {
				uploadPolicy.displayDebug(getFileName() + " : hasToTransformPicture = false", 20);
				hasToTransformPicture = Boolean.FALSE;
			}			
		}
		
		return hasToTransformPicture.booleanValue();
	}//end of hasToTransformPicture

	/**
	 * Creation of a temporary file, that contains the transformed picture. For instance, it can be 
	 * resized or rotated. 
	 * This method doesn't throw exception when there is an IOException within its procedure. If an exception 
	 * occurs while building the temporary file, the exception is catched, a warning is displayed,the temporary 
	 * file is deleted (if it was created), and the upload will go on with the original file.
	 * <BR>
	 * Note: any JUploadException thrown by a method called within getTransformedPictureFile() will be thrown
	 * within this method. 
	 */
	private File getTransformedPictureFile() throws JUploadException {
		BufferedImage bufferedImage = null;
		//Do we already created the transformed file ?
		if (transformedPictureFile == null) {
			String tmpFileName = getFile().getPath() + ".upload.tmp";
			try {
				transformedPictureFile = new File(tmpFileName);
				String localPictureFormat = (uploadPolicy.getTargetPictureFormat() == null) ? getFileExtension() : uploadPolicy.getTargetPictureFormat();

				//Prepare (if not already done) the bufferedImage.
				bufferedImage = getBufferedImage(uploadPolicy.getMaxWidth(), uploadPolicy.getMaxHeight());
				//Get the writer (to choose the compression quality)
				Iterator iter = ImageIO.getImageWritersByFormatName(localPictureFormat);
				if (iter.hasNext()) {					
		            ImageWriter writer = (ImageWriter) iter.next();
		            ImageWriteParam iwp = writer.getDefaultWriteParam();

		            iwp.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
		            float values[] = iwp.getCompressionQualityValues();
		            //Let's select the best available quality.
		            iwp.setCompressionQuality(values[values.length-1]);
		            
		            //
		            uploadPolicy.displayDebug("ImageWriter1 (used), CompressionQuality=" + iwp.getCompressionQuality(), 95);
		            
					//Let's create the picture file.
	                FileImageOutputStream output = new FileImageOutputStream(transformedPictureFile);
	                writer.setOutput(output);
	                IIOImage image = new IIOImage(bufferedImage, null, null);
	                writer.write(null, image, iwp);
	                writer.dispose();
	                output.close();
	                output = null;
	                
	                //For debug : let's display some parameters for the current image.
	                ColorModel cm = bufferedImage.getColorModel();
	                ColorSpace cs = cm.getColorSpace();
                	uploadPolicy.displayDebug("  colorSpace: isCS_RGB=" + (cs.isCS_sRGB() ? "true" : "false"), 90);
	                int nbComponents = cs.getNumComponents();
	                for (int i=0; i<nbComponents; i+=1) {
	                	uploadPolicy.displayDebug("  colorSpace: component " + cs.getName(i) + "=" 
	                			+ cs.getMinValue(i) + "-" + cs.getMaxValue(i), 90);
	                }
	                
	                
	                //For debug: test if any other driver exists.
	                int i=2;
	                while (iter.hasNext()) {
	                	uploadPolicy.displayDebug("ImageWriter" + i + " (not used)", 60);
	                }//while
				} else {
					//Too bad: no writer for the selected picture format
					throw new JUploadException ("No writer for the '" + localPictureFormat + "'picture format.");
				}
				

                //Within the navigator, we have to free memory ASAP
				if (!storeBufferedImage) {
					bufferedImage = null;
					freeMemory("getTransformedPictureFile");
				}
				uploadPolicy.displayDebug("transformedPictureFile : " + transformedPictureFile.getName(), 30);
			} catch (IOException e) {
				//We mask any exception that occurs within this method. The called method should raise
				//JUploadException, so their exceptions won't be catched here.
				uploadPolicy.displayWarn(e.getClass().getName() + " while writing the " + tmpFileName + " file. (picture will not be transformed)");
				if (e instanceof FileNotFoundException) {
					uploadPolicy.displayInfo(e.getClass().getName() + " probably means that the directory containing the picture is readonly: the applet can't write its temporary file.");
				}
				//We try to remove the temporary file, if it has been created.
				if (transformedPictureFile != null) {
					transformedPictureFile.delete();
				}
				transformedPictureFile = null;
			}
		}
		
		return transformedPictureFile;
	}//end of getTransformedPictureFile

	/**
	 * This picture is called when an OutOfMemoryError occurs. This can easily happen within the navigator, with
	 * big pictures: I've put a lot of freeMemory calls within the code, but they doesn't seem to work very well.
	 * When running from eclipse, the memory is freed Ok !
	 */
	private void tooBigPicture() {
		//TODO Put a messageBox here.
		uploadPolicy.displayWarn(uploadPolicy.getString("tooBigPicture", getFileName()));
	}

	/**
	 * This methods set the {@link DefaultFileData#mimeType} to the image mime type, that should be associate
	 * with the picture.
	 */	
	// FIXME PictureFileData.setMimeTypeByExtension(String): this method take the mime type from the original picture file (to update according to the targetPictureFormat parameter).
	private void setMimeTypeByExtension (String fileExtension) {
		String ext = fileExtension.toLowerCase();
		if (ext.equals("jpg")) {
			ext = "jpeg";
		}
		mimeType = "image/" + ext;
	}
}
