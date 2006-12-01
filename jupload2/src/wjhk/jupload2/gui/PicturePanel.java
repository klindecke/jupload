/* 
(C) 2002 Guillaume Chamberland-Larose

*/


package wjhk.jupload2.gui;

import java.awt.Canvas;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import wjhk.jupload2.exception.JUploadException;
import wjhk.jupload2.filedata.PictureFileData;
import wjhk.jupload2.policies.UploadPolicy;

/**
 * 
 * This panel is used to preview picture, when PictureUploadPolicy (or one of its 
 * inherited policy) is used. Manages the panel where pictures are displayed.
 * <BR>
 * Each time a user selects a file in the panel file, the PictureUploadPolicy calls
 * {@link #setPictureFile(PictureFileData)}. I did an attempt to store the Image
 * generated for the Panel size into the PictureFileData, to avoid to calculate the
 * offscreenPicture each time the user select the same file again. But it doesn't work:
 * the applet quickly runs out of memory, even after numerous calls of System.gc and finalize.
 *
 * <BR><BR>   
 * This file is taken from the PictureApplet ((C) 2002 Guillaume Chamberland-Larose), available here:
 * To contact Guillaume Chamberland-Larose for bugs, patches, suggestions:
 * Please use the forums on the sourceforge web page for this project, located at:
 * http://sourceforge.net/projects/picture-applet/
 * Updated : 2006 Etienne Gauthier
 * <BR>
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * <BR>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <BR>
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 */

public class PicturePanel extends Canvas implements MouseListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3439340009940699981L;
	private Container mainContainer;
	private PictureFileData pictureFileData;

	/**
	 * offscreenImage contains an image, that can be asked by {@link PictureFileData#getImage(Canvas, boolean)}. It 
	 * is used to preview this picture.
	 * 
	 */
	private Image offscreenImage = null;
	
	/**
	 * Indicates if the offscreen image should be calculated once and stored, to avoid to calculate it again. <BR>
	 * Indications: the offscreen image should be calculate only once for the picturePanel on the applet, and for each 
	 * display when the user ask to display the fulscreen picture (by a click on the picturePanel). 
	 */
	private boolean hasToStoreOffscreenPicture = false;

	/**
	 * That cursor which is displayed when the mouse is over this panel. The cursor can be changer to wait cursor,
	 * when long treatments are done, like loading a new picture.
	 */
	private Cursor picturePanelCursor = new Cursor(Cursor.HAND_CURSOR);
	
	/**
	 * The current upload policy.
	 */
	private UploadPolicy uploadPolicy;
	
	/**
	 * Standard constructor.
	 *
	 * @param mainContainer The main panel of the current window. It can be used to change the displayed cursor (to a WAIT_CURSOR for instance)
	 */
    public PicturePanel (Container mainContainer, boolean hasToStoreOffscreenPicture, UploadPolicy uploadPolicy) {
    	super();
    	
    	this.mainContainer = mainContainer;
    	this.hasToStoreOffscreenPicture = hasToStoreOffscreenPicture;
    	this.uploadPolicy = uploadPolicy;
    	
    	//We want to trap the mouse actions on this picture. 
	  	addMouseListener(this);

	  	//Indication to the user : this panel can be clicked on.
	  	setCursor(picturePanelCursor);
	}
    

    /**
     * This setter is called by {@link PictureFileData} to set the picture that is to be previewed.
     * 
     * @param pictureFileData The FileData for the image to be displayed. Null if no picture should be displayed.
     */
    public void setPictureFile(PictureFileData pictureFileData) {
    	//First : reset current picture configuration.
    	this.pictureFileData = null;
		offscreenImage = null; //Useful, if a repaint event occurs while we calculate the offscreenImage
		
		//Ask for an immediate repaint, to clear the panel (as offscreenImage is null). 
		repaint(0);

    	//Then, we store the new picture data, get the offscreen picture and ask for a repaint.
		this.pictureFileData = pictureFileData;
		calculateOffscreenImage();
		repaint();
    }

    
	public void paint(Graphics g) {
		//First : clear the panel area. 
		g.clearRect(0, 0, getWidth(), getHeight());
		//Do we have a picture to display
		//Now, we calculate the picture if we don't already have one. If not, we get it.
		if (offscreenImage == null) {
			calculateOffscreenImage();
		}
		
		/*
		 Seems useless. I keep it here, for some time...
		if (pictureFileData != null) {
			//Check current calculated image size :
			if (offscreenImage != null) {
				if (offscreenImage.getWidth(this) != getWidth()  &&  offscreenImage.getHeight(this)!=getHeight()) {
					uploadPolicy.displayDebug("Wrong width or height : recalculating offscreenImage (image : w=" 
							+ offscreenImage.getWidth(this)
							+ ", h="
							+ offscreenImage.getHeight(this)
							+ ", panel: w="
							+ getWidth()
							+ ", h="
							+ getHeight()
							, 20
							);
					offscreenImage = null;
				}
			}
		}
		*/
		
		//Then, display the picture, if any is defined.
    	if (offscreenImage != null) {
			//Let's center this picture
			int hMargin = (getWidth() - offscreenImage.getWidth(this))/2;
			int vMargin = (getHeight() - offscreenImage.getHeight(this))/2;
			g.drawImage(offscreenImage, hMargin, vMargin, this);
    	}
	}
		
	/**
	 * This function adds a quarter rotation to the current picture. 
	 * 
	 * @param quarter Number of quarters (90°) the picture should rotate. 1 means rotating of 90° 
	 * clockwise (?). Can be negative (counterclockwise),  more than 1...
	 */
	public void rotate (int quarter) {
		if (pictureFileData != null) {
			pictureFileData.addRotation(quarter);
			//The previously calculated picture is now wrong.
			offscreenImage = null;
			calculateOffscreenImage();
			
	    	repaint();
		} else {
			uploadPolicy.displayWarn("Strange: there is no pictureFileData in the PicturePanel! Command is ignored.");
		}
	}

	/**
     * This method get the offscreenImage from the current pictureFileData. This image is null, if pictureFileData
     * is null. In this case, the repaint will only clear the panel rectangle, on the screen.
     */
    private void calculateOffscreenImage () {
    	Cursor previousCursor = null;
    	if (mainContainer != null) {
    		previousCursor = mainContainer.getCursor();
    		mainContainer.setCursor(new Cursor(Cursor.WAIT_CURSOR));
    		this.setCursor(null);
    	}
    	if (pictureFileData == null) {
    		//Nothing to do. offscreenImage should be null.
    		if (offscreenImage != null) {
	    		offscreenImage = null;
	    		uploadPolicy.displayWarn("PicturePanel.calculateOffscreenImage(): offscreenImage set to null");
    		}
    	} else if (offscreenImage == null) {
    		uploadPolicy.displayDebug("PicturePanel.calculateOffscreenImage(): trying to calculate offscreenImage (PicturePanel.calculateOffscreenImage()", 40);
    		try {
    			offscreenImage = pictureFileData.getImage(this, hasToStoreOffscreenPicture);
	    	} catch (JUploadException e) {
	    		uploadPolicy.displayErr(e);
	    		//We won't try to display the picture for this file.
	    		this.pictureFileData = null;
	    		offscreenImage = null;
	    	}
    	}
    	
    	if (previousCursor != null && mainContainer != null) {
    		mainContainer.setCursor(previousCursor);
    		this.setCursor(picturePanelCursor);
    	}
    }
    
    /**
     * Is it really useful ??
     */
    protected void finalize() throws Throwable {
    	//super.finalize();
    	uploadPolicy.displayDebug("Within PicturePanel.finalize()", 90);
    	mainContainer = null;
    	pictureFileData = null;
    }
    
    //////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////      MouseListener interface   ////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////////////
	/** @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent) */
	public void mouseClicked(MouseEvent arg0) {
		if (pictureFileData != null) {
			//Ok, we have a picture. Let's display it.
			uploadPolicy.displayDebug("Opening PictureDialog", 60);
			new PictureDialog(null, pictureFileData, uploadPolicy);
		}
	}
	/** @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent) */
	public void mouseEntered(MouseEvent arg0) {
		// Nothing to do.		
	}
	/** @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent) */
	public void mouseExited(MouseEvent arg0) {
		// Nothing to do.		
	}
	/** @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent) */
	public void mousePressed(MouseEvent arg0) {
		// Nothing to do.		
	}
	/** @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent) */
	public void mouseReleased(MouseEvent arg0) {
		// Nothing to do.		
	}
}

