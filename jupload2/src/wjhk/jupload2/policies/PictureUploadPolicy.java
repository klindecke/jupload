/*
 * Created on 7 mai 2006
 */
package wjhk.jupload2.policies;

import java.applet.Applet;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import wjhk.jupload2.filedata.FileData;
import wjhk.jupload2.filedata.PictureFileData;
import wjhk.jupload2.gui.PicturePanel;


/**
 * This class add handling of pictures to upload.
 * <BR><BR>
 * <B>Functionnalities:</B>
 * <UL>
 * <LI> Ability to set maximum width or height to a picture.
 * <LI> Rotation of pictures, by quarter of turn.
 * <LI> The top panel contains two additional buttons to rotate picture in one direction or the other. 
 * <LI> A target picture format can be used, to force all pictures to be in one picture format, jpeg for instance. The available formats
 * are the format given by the navigator JVM. All standard format should be available. More information on 
 * <A href='http://java.sun.com/j2se/1.4.2/docs/guide/imageio/spec/title.fm.html'>java.sun.com</A>.
 * </UL> 
 * <BR><BR>
 * The {@link wjhk.jupload2.policies.CoppermineUploadPolicy} description contains an example
 * of an applet HTML tag.
 * <BR>
 * Here are the available parameters that can be used to control the way files are uploaded:
 * <UL>
 * <LI> maxPicWidth: Maximum width for the uploaded picture. If negative, there is no maximum. Default: -1.
 * <LI> maxPicHeight: Maximum height for the uploaded picture. If negative, there is no maximum. Default: -1.
 * <LI> targetPictureFormat : Define the target picture format (see {@link #getTargetPictureFormat()}. Eg: jpeg, png, gif...
 * </UL>
 * 
 * @author Etienne Gauthier
 *
 */

public class PictureUploadPolicy extends DefaultUploadPolicy implements ActionListener {

	

	/**
	 * createBufferedImage indicates if a BufferedImage is to be created when
	 * the user selects the file.
	 * If True : the Image is loaded once. This consumns memory, but is interessant 
	 * for big pictures, when they are resized (see {#maxWidth} and {#maxHeight}).
	 * If False : it is loaded for each display on the applet, and once for the upload.
	 * 
	 * Default : true. 
	 * 
	 * @see wjhk.jupload2.policies.UploadPolicy#DEFAULT_CREATE_BUFFERED_IMAGE
	 */
	private boolean createBufferedImage;
	
	/**
	 * targetPictureFormat is the image type that should be uploaded (JPG, GIF...). 
	 * It should be a standard type, as the JVM will create this file.
	 * If null, the same format as the original file is used.
	 * <BR>
	 * Currently <B>this flag is ignored when createBufferedImage is false</B> .
	 * 
	 * Default: null. 
	 * 
	 * @see wjhk.jupload2.policies.UploadPolicy#DEFAULT_TARGET_PICTURE_FORMAT
	 * 
	 */
	private String targetPictureFormat;
	
	/**
	 * Maximal width for the uploaded picture. If the actual width for the picture
	 * is more than maxWidth, the picture is resized. The proportion between widht
	 * and height are maintained.
	 * Negative if no maximum width (no resizing).
	 * 
	 * Default: -1. 
	 * 
	 * @see wjhk.jupload2.policies.UploadPolicy#DEFAULT_MAX_WIDTH
	 */
	private int maxWidth = -1;

	
	/**
	 * Maximal height for the uploaded picture. If the actual height for the picture
	 * is more than maxHeight, the picture is resized. The proportion between widht
	 * and height are maintained.
	 * Negative if no maximum height (no resizing).
	 * 
	 * Default: -1. 
	 * 
	 * @see wjhk.jupload2.policies.UploadPolicy#DEFAULT_MAX_HEIGHT
	 */
	private int maxHeight = -1;
	
	
	/**
	 * Button to allow the user to rotate the picture one quarter counter-clockwise.
	 */
	private JButton rotateLeft;

	/**
	 * Button to allow the user to rotate the picture one quarter clockwise.
	 */
	private JButton rotateRight;

	/**
	 * The picture panel, where picture are displayed.
	 */
	private PicturePanel picturePanel;

	/**
	 * @param postURL
	 */
	protected PictureUploadPolicy(String postURL, int maxFilesPerUpload, Applet theApplet, int debugLevel, JTextArea status) {
		super(postURL, theApplet, debugLevel, status);
	    
	    //Creation of the PictureFileDataPolicy, from parameters given to
		//the applet, or from default values.
		this.maxFilesPerUpload = maxFilesPerUpload;
	    createBufferedImage = UploadPolicyFactory.getParameter(theApplet, PROP_CREATE_BUFFERED_IMAGE, DEFAULT_CREATE_BUFFERED_IMAGE);
		targetPictureFormat = UploadPolicyFactory.getParameter(theApplet, PROP_TARGET_PICTURE_FORMAT, DEFAULT_TARGET_PICTURE_FORMAT);
		maxWidth = UploadPolicyFactory.getParameter(theApplet, PROP_MAX_WIDTH, DEFAULT_MAX_WIDTH);
		maxHeight = UploadPolicyFactory.getParameter(theApplet, PROP_MAX_HEIGHT, DEFAULT_MAX_HEIGHT);
		
		if (maxWidth != DEFAULT_MAX_WIDTH) {
			displayInfo(PROP_MAX_WIDTH + " : " + maxWidth);
		}
		if (maxHeight != DEFAULT_MAX_HEIGHT) {
			displayInfo(PROP_MAX_HEIGHT + " : " + maxHeight);
		}

		
	}

	/**
	 * @see wjhk.jupload2.policies.UploadPolicy#createFileData(File)
	 */
	public FileData createFileData(File file) {
		return new PictureFileData(file);
	}

	/**
	 * 
	 * Default implementation of {@link wjhk.jupload2.policies.UploadPolicy#createTopPanel(javax.swing.JButton, javax.swing.JButton, javax.swing.JButton, javax.swing.JPanel)}. 
	 * It creates a JPanel, containing the three given JButton.  
	 * 
	 * @see wjhk.jupload2.policies.UploadPolicy#createTopPanel(javax.swing.JButton, javax.swing.JButton, javax.swing.JButton, javax.swing.JPanel)
	 */
	public JPanel createTopPanel(JButton browse, JButton remove, JButton removeAll, JPanel mainPanel) {
		//The top panel is verticaly divided in :
		//   - On the left, the button bar (buttons one above another)
		//	 - On the right, the preview PicturePanel.
		
		//Creation of specific buttons
	    rotateLeft = new JButton(getString("buttonRotateLeft"));
	    rotateLeft.setIcon(new ImageIcon(getClass().getResource("/images/rotateLeft.gif")));
	    rotateLeft.addActionListener(this);
	    rotateLeft.setEnabled(false);

	    rotateRight = new JButton(getString("buttonRotateRight"));
	    rotateRight.setIcon(new ImageIcon(getClass().getResource("/images/rotateRight.gif")));
	    rotateRight.addActionListener(this);
	    rotateRight.setEnabled(false);

	    //The button bar
	    JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new GridLayout(5, 1));
		buttonPanel.add(browse);
		buttonPanel.add(rotateLeft);
		buttonPanel.add(rotateRight);
		buttonPanel.add(removeAll);
		buttonPanel.add(remove);
		
		//The preview PicturePanel
		JPanel pPanel = new JPanel();
	  	pPanel.setLayout(new GridLayout(1,1));	  	
	  	picturePanel = new PicturePanel(mainPanel, false);
	  	pPanel.add(picturePanel);
	  	
	  	//And last but not least ... creation of the top panel:
		JPanel topPanel = new JPanel();
		topPanel.setLayout(new GridLayout(1,2));
	    topPanel.add(buttonPanel);
	    topPanel.add(pPanel);

		return topPanel;
	}//createTopPanel

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		displayInfo("Action : " + e.getActionCommand());
	    if(e.getActionCommand() == rotateLeft.getActionCommand()) {
	    	picturePanel.rotate(-1);
	    } else if(e.getActionCommand() == rotateRight.getActionCommand()){
	    	picturePanel.rotate(1);
	    }
	}//actionPerformed

	/**
	 * @see wjhk.jupload2.policies.UploadPolicy#onSelectFile(wjhk.jupload2.filedata.FileData)
	 */
	public void onSelectFile(FileData fileData) {
		if (fileData != null) {
			displayDebug("File selected: " + fileData.getFileName(), 30);
		}
		if (picturePanel != null) {
			picturePanel.setPictureFile((PictureFileData)fileData);
			rotateLeft.setEnabled(fileData != null);
		    rotateRight.setEnabled(fileData != null);
		}
	}
    //////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////      Getters and Setters   ////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * @return Returns the createBufferedImage.
	 */
	public boolean hasToCreateBufferedImage() {
		return createBufferedImage;
	}
	
	/**
	 * @return Returns the maxHeight.
	 */
	public int getMaxHeight() {
		return maxHeight;
	}
	/**
	 * @return Returns the maxWidth.
	 */
	public int getMaxWidth() {
		return maxWidth;
	}
	/**
	 * @return Returns the targetPictureForma.
	 */
	public String getTargetPictureFormat() {
		return targetPictureFormat;
	}
}
