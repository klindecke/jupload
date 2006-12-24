/*
 * Created on 10 juil. 2006
 */
package wjhk.jupload2.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;

import wjhk.jupload2.filedata.PictureFileData;
import wjhk.jupload2.policies.UploadPolicy;

/**
 * 
 * A maximized modal dialog box, that display the selected picture.
 *   
 * @author Etienne Gauthier
 */
public class PictureDialog extends JDialog implements ActionListener {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 7802205907550854333L;
	JButton buttonClose;
	PictureFileData pictureFileData = null;
	PicturePanel picturePanel = null;
	UploadPolicy uploadPolicy = null;
	
	public PictureDialog (Frame owner, PictureFileData pictureFileData, UploadPolicy uploadPolicy) {
		super(owner, pictureFileData.getFileName(), true);
		
		this.uploadPolicy = uploadPolicy;
		this.pictureFileData = pictureFileData; 

		//Creation of the image area
	  	picturePanel = new DialogPicturePanel(this, uploadPolicy, pictureFileData);
	  	
	  	//Creation of the buttonClose button.
	  	buttonClose = new JButton(uploadPolicy.getString("buttonClose"));
		buttonClose.setMaximumSize(new Dimension(100, 100));
	    buttonClose.addActionListener(this);

	    getContentPane().add(buttonClose, BorderLayout.SOUTH);
	    getContentPane().add(picturePanel);
		
	    pack();
		setSize(getMaximumSize());

		//The dialog is modal: the next line will return when the DialogPicture is hidden (to be closed, in our case)
		setVisible(true);
		
		// MEMORY LEAK CORRECTION :
		
		//Let's free some memory.
		//This is necessary, as the finalize method is not called (if anyone has an explanation).
		//So, I have to manually free the memory consummed to display the image, here.  
		picturePanel.setPictureFile(null);
	}
	
	/**
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent event) {
	    if(event.getActionCommand() == buttonClose.getActionCommand()) {
			uploadPolicy.displayDebug("[PictureDialog] Before this.dispose()", 60);
	    	this.dispose();			
	    }
	}
	
	/**
	 * Free all locked data.
	 *
	protected void finalize () throws Throwable {
		super.finalize();
    	uploadPolicy.displayDebug("Within PictureDialog.finalize()", 90);
		picturePanel = null;
		buttonClose = null;
	}
	*/
}
