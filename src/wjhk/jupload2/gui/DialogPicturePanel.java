/*
 * Created on 11 juil. 2006
 */
package wjhk.jupload2.gui;

import java.awt.event.MouseEvent;

import wjhk.jupload2.filedata.PictureFileData;
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
	public DialogPicturePanel(PictureDialog pictureDialog, UploadPolicy uploadPolicy, PictureFileData pictureFileData) {
		super(pictureDialog.getContentPane(), false, uploadPolicy);
		
		this.pictureDialog = pictureDialog;
		setPictureFile(pictureFileData);
	}

	/**
	 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
	 */
	@Override
    public void mouseClicked(@SuppressWarnings("unused")
    MouseEvent arg0) {
		//Let's close the current DialogBox, if it has not already be done.
		if (this.pictureDialog != null) {
			this.uploadPolicy.displayDebug("[DialogPicturePanel] Before pictureDialog.dispose()", 60);
			this.pictureDialog.dispose();
			this.pictureDialog = null;
		}
	}
}
