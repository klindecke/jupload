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
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;

import wjhk.jupload2.exception.JUploadException;
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
public class PictureFileData extends FileData  {

	/**
	 * Indicate whether the data for this fileData has already been intialized.
	 */
	//private boolean initialized = false;

	/**
	 * Indicates if this file is a picture or not.
	 */
	private boolean isPicture = false;

	/**
	 * If set to true, the PictureFileData will keep the BufferedImage in memory. That is: it won't
	 * load it again from the hard drive, and resize and/or rotate it (if necessary) when the user select this
	 * picture. When picture are big this is nice. 
	 * <BR><B>Caution:</B> the navigator applet runs quickly out of memory (after
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
	private BufferedImage bufferedImage = null;
	
	
	/**
	 * This picture is precalculated, and stored to avoid to calculate it each time the user select this picture again.
	 */
	private Image offscreenImage = null;
		
	/**
	 * quarterRotation contains the current rotation that will be applied to the picture.
	 */
	int quarterRotation = 0;
	
	/**
	 * Width of the original picture.
	 */

	int originalWidth = -1;
	/**
	 * Width of the original picture.
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
	 * <BR>
	 * It is se to -1 whenever the user ask for a rotation (current only action that need to recalculate the picture.
	 */
	private long uploadLength = -1;
	
	/**
	 * hasToTransformPicture indicates whether the tranform should be transformed. Null if unknown. This can happen
	 * (for instance) if no calcul where done (during initialization), or after rotating the picture back to the 
	 * original orientation.
	 */
	private Boolean hasToTransformPicture = null;
	
	/**
	 * For this class, the UploadPolicy is a PictureUploadPolicy.
	 */
	PictureUploadPolicy uploadPolicy;
	
	//////////////////////////////////////////////////////////////////////////////////////////////////////
	
	
	/**
	 * Standard constructor: needs a PictureFileDataPolicy.
	 * 
	 * @param file The files which data are to be handled by this instance.
	 */
	public PictureFileData(File file) {
		super(file);
		uploadPolicy = (PictureUploadPolicy)super.uploadPolicy;
		storeBufferedImage = uploadPolicy.hasToStoreBufferedImage();
	}
	
	/**
	 * free any available memory.
	 * 
	 * @param caller Indicate the method or treatment from which this method is called.
	 */
	void freeMemory(String caller) {
		Runtime rt = Runtime.getRuntime();

		/*
		uploadPolicy.displayDebug("freeMemory : " + caller, 80);
		uploadPolicy.displayDebug("freeMemory (before " + caller + ") : " + rt.freeMemory(), 80);
		uploadPolicy.displayDebug("maxMemory  (before " + caller + ") : " + rt.maxMemory(), 80);
		*/

		rt.runFinalization();
		rt.gc();
		
		/*
		uploadPolicy.displayDebug("freeMemory (after " + caller + ") : " + rt.freeMemory(), 80);
		uploadPolicy.displayDebug("maxMemory  (after " + caller + ") : " + rt.maxMemory(), 80);
		*/
	}
	/**
	 * Creation of a temporary file, that contains the transformed picture. For instance, it can be resized, rotated... 
	 */
	private File getTransformedPictureFile()  throws JUploadException {
		//Do we already created the transformed file ?
		if (transformedPictureFile == null) {
			transformedPictureFile = new File(getFile().getPath() + ".upload.tmp");
			String localPictureFormat = (((PictureUploadPolicy)uploadPolicy).getTargetPictureFormat() == null) ? getFileExtension() : ((PictureUploadPolicy)uploadPolicy).getTargetPictureFormat();
			try {
				//Prepare (if not already done) the bufferedImage.
				getBufferedImage();
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
				transformedPictureFile = null;
				throw new JUploadException (e, "PictureFileData.getTransformedPictureFile()");
			}
		}
		
		return transformedPictureFile;
	}
	
	/**
	 * 
	 * Calculate the number of bytes, for this upload. If needed, a temporary file is created, to 
	 * store the transformed picture.
	 * 
	 * @return The length of upload. In this class, this is ... the size of the file ! 
	 * @see PictureFileData 
	 */
	public long getUploadLength()  throws JUploadException {
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
	public InputStream getInputStream () throws IOException, JUploadException {
		//Do we have to transform the picture ?
		if (transformedPictureFile != null) {
			return new FileInputStream(transformedPictureFile);
		} else { 
			//Otherwise : we read the file.
			return new FileInputStream(getFile());
		}
	}
	
	/**
	 * @see FileData#afterUpload()
	 */
	public void afterUpload () {
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
	 * This function resize the picture, if necessary, according to the maxWidth and
	 * maxHeight of fileDataPolicy.
	 * This function should only be called if isPicture is true.
	 *  
	 * @return A BufferedImage which contains the picture according to current parameters (resizing, rotation...).
	 */
	private BufferedImage getBufferedImage() throws JUploadException {

		if (bufferedImage == null) {
			BufferedImage localBufferedImage;
			
			//Before loading a new picture, let's free any unused memory.
			freeMemory("start of getBufferedImage");
			
			try {
				localBufferedImage = ImageIO.read(getFile());
				//If we get here, we have a picture.
				isPicture = true;

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
				int maxWidth = uploadPolicy.getMaxWidth();
				int maxHeight = uploadPolicy.getMaxHeight();
				float scaleWidth  = ((maxWidth <0) ? 1 : ((float) maxWidth ) / nonScaledRotatedWidth );    
				float scaleHeight = ((maxHeight<0) ? 1 : ((float) maxHeight) / nonScaledRotatedHeight);
				float scale = Math.min(scaleWidth, scaleHeight);
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
			uploadPolicy.displayDebug("bufferedImage MinX: " + bufferedImage.getMinX(), 60);
			uploadPolicy.displayDebug("bufferedImage MinY: " + bufferedImage.getMinY(), 60);
			uploadPolicy.displayDebug("bufferedImage Width: " + bufferedImage.getWidth(), 60);
			uploadPolicy.displayDebug("bufferedImage Height: " + bufferedImage.getHeight(), 60);
		}
		return bufferedImage;
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
		Image image=null;
		boolean hasToCalculateImage = false;
		
		if (canvas == null) {
			throw new JUploadException("canvas null in PictureFileData.getImage");
		}

		int maxWidth=canvas.getWidth();
		int maxHeight=canvas.getHeight(); 
		if (maxWidth <= 0 || maxHeight <= 0) {
			hasToCalculateImage = false;
		} else if (! shadow) {
			hasToCalculateImage = true;
		} else if (offscreenImage == null) {
			hasToCalculateImage = true;
		} else {
			hasToCalculateImage = (offscreenImage.getWidth(canvas) != maxWidth  && offscreenImage.getHeight(canvas) != maxHeight);
		}

		if ( ! hasToCalculateImage) {
			image = offscreenImage;
		} else {
			try {
		        getBufferedImage();
				int originalWidth  = bufferedImage.getWidth ();
				int originalHeight = bufferedImage.getHeight();
				float scaleWidth = (float) maxWidth / originalWidth;
				float scaleHeight = (float) maxHeight / originalHeight;
				float scale = Math.min(scaleWidth, scaleHeight);
				//Should we resize this picture ?
				if (scale < 1) {
					int width  = (int) (scale * originalWidth);
					int height = (int) (scale * originalHeight);
					image = bufferedImage.getScaledInstance(width, height, Image.SCALE_DEFAULT);
					//Diminuer le  nombre de couleurs ?
				} else {
					image = bufferedImage;
				}
			} catch (OutOfMemoryError e) {
				//Too bad
				bufferedImage = null;
				image = null;
				tooBigPicture();
			}
			
			//We store it, if asked to.
			if (shadow) {
				offscreenImage = image;
			}
			
			//Within the navigator, we have to free memory ASAP
			if (!storeBufferedImage) {
				bufferedImage = null;
				freeMemory("end of getOffscreenImage");
			}
		}

		if (image != null) {
			uploadPolicy.displayDebug("image Width: " + image.getWidth(canvas), 60);
			uploadPolicy.displayDebug("image Height: " + image.getHeight(canvas), 60);
		}

		return image;
	}//getOffscreenImage
	
	/**
	 * This function is used to rotate the picture. 
	 * 
	 * @param quarter Number of quarters (90°) the picture should rotate. 1 means rotating of 90° clockwise (?). Can be negative.
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
	 * This function indicate if the picture has to be modified. For instance : a maximum width, height, a target format...
	 * 
	 * @see PictureUploadPolicy
	 * @see #quarterRotation
	 * 
	 * @return true if the picture must be transformed. false if the file can be directly transmitted. 
	 */
	boolean hasToTransformPicture () throws JUploadException {

		//Did we already estimate if transformation is needed ?
		if (hasToTransformPicture == null) {
			//We only tranform pictures.
			/*
			if (!isPicture) {
				hasToTransformPicture = Boolean.FALSE;
			}
			*/
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
	}
	
	/**
	 * This picture is called when an OutOfMemoryError occurs. This can easily happen within the navigator, with
	 * big pictures: I've put a lot of freeMemory calls within the code, but they doesn't seem to work very well.
	 * When running from eclipse, the memory is freed Ok !
	 */
	private void tooBigPicture() {
		//TODO Put a messageBox here.
		uploadPolicy.displayInfo(uploadPolicy.getString("tooBigPicture", getFileName()));
	}
	
}
