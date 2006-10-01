/*
 * Created on 11 juil. 2006
 */
package wjhk.jupload2.gui;

import java.awt.event.MouseEvent;

import wjhk.jupload2.policies.UploadPolicyFactory;

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
	public DialogPicturePanel(PictureDialog pictureDialog) {
		super(pictureDialog.getContentPane(), false);
		
		this.pictureDialog = pictureDialog;
	}

	/**
	 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
	 */
	public void mouseClicked(MouseEvent arg0) {
		//Let's display the big picture.
		pictureDialog.dispose();
		UploadPolicyFactory.getCurrentUploadPolicy().displayDebug("DialogPicturePanel : mouseClicked()", 50);
	}
}
