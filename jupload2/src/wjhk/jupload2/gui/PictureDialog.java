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
import javax.swing.JPanel;

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
	PicturePanel picturePanel;
	
	public PictureDialog (Frame owner, PictureFileData pictureFileData, UploadPolicy uploadPolicy) {
		super(owner, pictureFileData.getFileName(), true);

		
		JPanel panel = new JPanel();
	  	panel.setLayout(new BorderLayout());	  	
		//Creation of the image area
	  	picturePanel = new DialogPicturePanel(this, uploadPolicy);
	  	//Creation of the buttonClose button.
	  	buttonClose = new JButton(uploadPolicy.getString("buttonClose"));
		buttonClose.setMaximumSize(new Dimension(100, 100));
	    buttonClose.addActionListener(this);
	    //Creation of the panel, that'll contains both objects.

	    panel.add(buttonClose, BorderLayout.SOUTH);
	    panel.add(picturePanel);;

	    getContentPane().add(panel);
	  	

	  	pack();
		setSize(getMaximumSize());

		picturePanel.setPictureFile(pictureFileData);
		//show();
		setVisible(true);
	}
	
    /**
     * This setter is called to set the picture that is to be previewed.
     * 
     * @param pictureFileData The pictureFileData instance that contains information about the picture.
     *
    public void setPictureFile(PictureFileData pictureFileData) {
		picturePanel.setPictureFile(pictureFileData);
    }
    */

	/** (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent event) {
	    if(event.getActionCommand() == buttonClose.getActionCommand()) {
	    	this.dispose();			
	    }
	}
}
