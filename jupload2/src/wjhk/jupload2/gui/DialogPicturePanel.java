/*
 * Created on 11 juil. 2006
 */
package wjhk.jupload2.gui;

import java.awt.event.MouseEvent;

import wjhk.jupload2.policies.UploadPolicy;


/**
 * 
 * The picture for the PictureDialog. The difference with the PicturePanel, is that
 * a click on it closes the Dialog. 
 * 
 * @author Etienne Gauthier
 */
public class DialogPicturePanel extends PicturePanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1333603128496671158L;
	/**
	 * The JDialog containing this panel.
	 */
	PictureDialog pictureDialog;
	
	/**
	 * 
	 */
	public DialogPicturePanel(PictureDialog pictureDialog, UploadPolicy uploadPolicy) {
		super(pictureDialog.getContentPane(), false, uploadPolicy);
		
		this.pictureDialog = pictureDialog;
	}

	/**
	 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
	 */
	public void mouseClicked(MouseEvent arg0) {
		//Let's close the current DialogBox, if it has not already be done.
		if (pictureDialog != null) {
			pictureDialog.dispose();
			pictureDialog = null;
		}
	}
}
