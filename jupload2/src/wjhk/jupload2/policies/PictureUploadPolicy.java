/*
 * Created on 7 mai 2006
 */
package wjhk.jupload2.policies;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;

import wjhk.jupload2.JUploadApplet;
import wjhk.jupload2.filedata.FileData;
import wjhk.jupload2.filedata.PictureFileData;
import wjhk.jupload2.gui.PicturePanel;


/**
 * This class add handling of pictures to upload.
 * <BR><BR>
 * <H4>Functionalities:</H4>
 * <UL>
 * <LI> The top panel (upper part of the applet display) is modified, by using  
 * 		UploadPolicy.{@link wjhk.jupload2.policies.UploadPolicy#createTopPanel(JButton, JButton, JButton, JPanel)}. 
 *      It contains a <B>preview</B> picture panel, and two additional buttons to rotate the selected picture in one 
 *      direction or the other. 
 * <LI> Ability to set maximum width or height to a picture (with maxPicWidth and maxPicHeight applet parameters, see the global explanation on
 *      the <a href="UploadPolicy.html#parameters">parameters</a> section) of the UploadPolicy API page.
 * <LI> Rotation of pictures, by quarter of turn.
 * <LI> <I>(To be implemented)</I> A target picture format can be used, to force all uploaded pictures to be in 
 * 		one picture format, jpeg for instance. 
 * 		All details are in the UploadPolicy <a href="UploadPolicy.html#parameters">parameters</a> section.
 * </UL> 
 * <BR><BR>
 * See an example of HTML that calls this applet, just below.
 * <H4>Parameters</H4>
 * The description for all parameters of all polices has been grouped in the UploadPolicy 
 * <a href="UploadPolicy.html#parameters">parameters</a> section.
 * <BR>The parameters implemented in this class are:
 * <UL>
 * <LI> maxPicWidth: Maximum width for the uploaded picture.
 * <LI> maxPicHeight: Maximum height for the uploaded picture.
 * <LI> <I>(To be implemented)</I> targetPictureFormat : Define the target picture format. Eg: jpeg, png, gif...
 * </UL>
 * 
 * <A NAME="example"><H4>HTML call example</H4></A>
 * You'll find below an example of how to put the applet into a PHP page:
 * <BR>
 * <XMP>
      <APPLET  
          NAME="JUpload"
          CODE="wjhk.jupload2.JUploadApplet" 
          ARCHIVE="plugins/jupload/wjhk.jupload.jar" 
          <!-- Applet display size, on the navigator page -->
          WIDTH="500" 
          HEIGHT="700"
          <!-- The applet call some javascript function, so we must allow it : -->
          MAYSCRIPT
          >
          <!-- First, mandatory parameters -->
          <PARAM NAME="postURL"      VALUE="http://some.host.com/youruploadpage.php">
          <PARAM NAME="uploadPolicy" VALUE="PictureUploadPolicy">
          <!-- Then, optional parameters -->
          <PARAM NAME="lang"         VALUE="fr">
          <PARAM NAME="maxPicHeight" VALUE="768">
          <PARAM NAME="maxPicWidth"  VALUE="1024">
          <PARAM NAME="debugLevel"   VALUE="0">
                
      Java 1.4 or higher plugin required.
      </APPLET>

 * </XMP>
 * 
 * 
 * @author Etienne Gauthier
 *
 */

public class PictureUploadPolicy extends DefaultUploadPolicy implements ActionListener {
	
	/**
	 * Indicates that a BufferedImage is to be created when the user selects the file.
	 * <BR>
	 * If true : the Image is loaded once from the hard drive. This consumns memory, but is interessant 
	 * for big pictures, when they are resized (see {@link #maxWidth} and {@link #maxHeight}).
	 * <BR>
	 * If false : it is loaded for each display on the applet, then once for the upload.
	 * <BR><BR>
	 * Default : false, because the applet, while in the navigator, runs too quickly out of memory. 
	 * 
	 * @see wjhk.jupload2.policies.UploadPolicy#DEFAULT_STORE_BUFFERED_IMAGE
	 */
	private boolean storeBufferedImage;
	
	/**
	 * Iimage type that should be uploaded (JPG, GIF...). 
	 * It should be a standard type, as the JVM will create this file.
	 * If null, the same format as the original file is used.
	 * <BR>
	 * Currently <B>this flag is ignored when createBufferedImage is false</B> .
	 * <BR>
	 * Default: null. 
	 * 
	 * @see wjhk.jupload2.policies.UploadPolicy#DEFAULT_TARGET_PICTURE_FORMAT
	 * 
	 */
	private String targetPictureFormat;
	
	/**
	 * Indicates wether or not the preview pictures must be calculated by the BufferedImage.getScaledInstance() method.
	 */
	private boolean highQualityPreview;
	
	/**
	 * Maximal width for the uploaded picture. If the actual width for the picture
	 * is more than maxWidth, the picture is resized. The proportion between widht
	 * and height are maintained.
	 * Negative if no maximum width (no resizing).
	 * <BR>
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
	 * <BR>
	 * Default: -1. 
	 * 
	 * @see wjhk.jupload2.policies.UploadPolicy#DEFAULT_MAX_HEIGHT
	 */
	private int maxHeight = -1;
	

	/**
	 * @see UploadPolicy
	 */
	private int realMaxWidth = -1;

	/**
	 * @see UploadPolicy
	 */
	private int realMaxHeight = -1;

	/**
	 * Button to allow the user to rotate the picture one quarter counter-clockwise.
	 */
	private JButton rotateLeftButton;

	/**
	 * Button to allow the user to rotate the picture one quarter clockwise.
	 */
	private JButton rotateRightButton;

	/**
	 * The picture panel, where the selected picture is displayed.
	 */
	private PicturePanel picturePanel;

	/**
	 * The standard constructor, which transmit most informations to the super.Constructor(). 
	 * 
	 * @param theApplet Reference to the current applet. Allows access to javasript functions.
	 */
	public PictureUploadPolicy(JUploadApplet theApplet) {
		super(theApplet);
	    
	    //Creation of the PictureFileDataPolicy, from parameters given to the applet, or from default values.
	    setHighQualityPreview (UploadPolicyFactory.getParameter(theApplet, PROP_HIGH_QUALITY_PREVIEW, DEFAULT_HIGH_QUALITY_PREVIEW, this));
		setMaxHeight ( UploadPolicyFactory.getParameter(theApplet, PROP_MAX_HEIGHT, DEFAULT_MAX_HEIGHT, this));
		setMaxWidth (UploadPolicyFactory.getParameter(theApplet, PROP_MAX_WIDTH, DEFAULT_MAX_WIDTH, this));
		setRealMaxHeight (UploadPolicyFactory.getParameter(theApplet, PROP_REAL_MAX_HEIGHT, DEFAULT_REAL_MAX_HEIGHT, this));
		setRealMaxWidth (UploadPolicyFactory.getParameter(theApplet, PROP_REAL_MAX_WIDTH, DEFAULT_REAL_MAX_WIDTH, this));
	    setStoreBufferedImage (UploadPolicyFactory.getParameter(theApplet, PROP_STORE_BUFFERED_IMAGE, DEFAULT_STORE_BUFFERED_IMAGE, this));
		setTargetPictureFormat (UploadPolicyFactory.getParameter(theApplet, PROP_TARGET_PICTURE_FORMAT, DEFAULT_TARGET_PICTURE_FORMAT, this));
		
		//The superclass (DefaultUploadPolicy) will call displayParameterStatus(), so that 
		//we display all applet parameters, after initialization.
	}

	/**
	 * This methods actually returns a {@link PictureFileData} instance. It allows only pictures: if the file is not
	 * a picture, this method returns null, thus preventing the file to be added to the list of files to be uploaded.
	 * 
	 * @param file The file selected by the user (called once for each added file).  
	 * @return An instance of {@link PictureFileData} or null if file is not a picture.
	 * @see wjhk.jupload2.policies.UploadPolicy#createFileData(File)
	 */
	public FileData createFileData(File file) {
		PictureFileData pfd = new PictureFileData(file, this);
		if (pfd.isPicture()) {
			return pfd;
		} else {
			//TODO alert only once, when several files are not pictures... hum, hum: any idea, dear reader ?
			alert("notAPicture", file.getName());
			return null;
		}
	}

	/**
	 * This method override the default topPanel, and adds:<BR>
	 * <UL>
	 * <LI>Two rotation buttons, to rotate the currently selected picture.
	 * <LI>A Preview area, to view the selected picture
	 * </UL>
	 * 
	 * @see wjhk.jupload2.policies.UploadPolicy#createTopPanel(javax.swing.JButton, javax.swing.JButton, javax.swing.JButton, javax.swing.JPanel)
	 */
	public JPanel createTopPanel(JButton browse, JButton remove, JButton removeAll, JPanel mainPanel) {
		//The top panel is verticaly divided in :
		//   - On the left, the button bar (buttons one above another)
		//	 - On the right, the preview PicturePanel.
		
		//Creation of specific buttons
	    rotateLeftButton = new JButton(getString("buttonRotateLeft"));
	    rotateLeftButton.setIcon(new ImageIcon(getClass().getResource("/images/rotateLeft.gif")));
	    rotateLeftButton.addActionListener(this);
	    rotateLeftButton.setEnabled(false);

	    rotateRightButton = new JButton(getString("buttonRotateRight"));
	    rotateRightButton.setIcon(new ImageIcon(getClass().getResource("/images/rotateRight.gif")));
	    rotateRightButton.addActionListener(this);
	    rotateRightButton.setEnabled(false);

	    //The button bar
	    JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new GridLayout(5, 1));
		buttonPanel.add(browse);
		buttonPanel.add(rotateLeftButton);
		buttonPanel.add(rotateRightButton);
		buttonPanel.add(removeAll);
		buttonPanel.add(remove);
		
		//The preview PicturePanel
		JPanel pPanel = new JPanel();
	  	pPanel.setLayout(new GridLayout(1,1));
	  	picturePanel = new PicturePanel(mainPanel, true, this);
	  	pPanel.add(picturePanel);
	  	
	  	//And last but not least ... creation of the top panel:
		JPanel topPanel = new JPanel();
		topPanel.setLayout(new GridLayout(1,2));
	    topPanel.add(buttonPanel);
	    topPanel.add(pPanel);

		return topPanel;
	}//createTopPanel

	/**
	 * This method handles the clicks on the rotation buttons. All other actions are managed by the 
	 * {@link DefaultUploadPolicy}.
	 * 
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		displayInfo("Action : " + e.getActionCommand());
	    if(e.getActionCommand() == rotateLeftButton.getActionCommand()) {
	    	picturePanel.rotate(-1);
	    } else if(e.getActionCommand() == rotateRightButton.getActionCommand()){
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
			rotateLeftButton.setEnabled(fileData != null);
		    rotateRightButton.setEnabled(fileData != null);
		}
	}
	
	/** @see UploadPolicy#beforeUpload() */
	public void beforeUpload() {
		super.beforeUpload();
		
		//We clear the current picture selection. This insure a correct managing of enabling/disabling of
		//buttons, even if the user stops the upload.
		getApplet().getFilePanel().clearSelection();
		if (picturePanel != null) {
			picturePanel.setPictureFile(null);
			rotateLeftButton.setEnabled(false);
		    rotateRightButton.setEnabled(false);
		}
	}
    //////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////      Getters and Setters   ////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////////////
		
	/** @return the applet parameter <I>highQualityPreview</I>. */
	public boolean getHighQualityPreview() { return highQualityPreview; }
	/** @param highQualityPreview the highQualityPreview to set */
	void setHighQualityPreview(boolean highQualityPreview) { this.highQualityPreview = highQualityPreview; }

	/** @return Returns the maxHeight, that should be used by pictures non transformed (rotated...) by the applet. */
	public int getMaxHeight() { return maxHeight; }
	/** @param maxHeight the maxHeight to set */
	void setMaxHeight(int maxHeight) { this.maxHeight = maxHeight; }

	/** @return Returns the maxWidth, that should be used by pictures non transformed (rotated...) by the applet. */
	public int getMaxWidth() { return maxWidth; }
	/** @param maxWidth the maxWidth to set */
	void setMaxWidth(int maxWidth) { this.maxWidth = maxWidth; }

	/** @return Returns the maxHeight, that should be used by pictures that are transformed (rotated...) by the applet. */
	public int getRealMaxHeight() { return  (realMaxHeight < 0) ? maxHeight : realMaxHeight; }
	/** @param realMaxHeight the realMaxHeight to set */
	void setRealMaxHeight(int realMaxHeight) { this.realMaxHeight = realMaxHeight; }

	/** @return Returns the maxWidth, that should be used by pictures that are transformed (rotated...) by the applet. */
	public int getRealMaxWidth() { return  (realMaxWidth < 0) ? maxWidth : realMaxWidth; }
	/** @param realMaxWidth the realMaxWidth to set */
	void setRealMaxWidth(int realMaxWidth) { this.realMaxWidth = realMaxWidth; }

	/** @return Returns the createBufferedImage. */
	public boolean hasToStoreBufferedImage() { return storeBufferedImage; }
	/** @param storeBufferedImage the storeBufferedImage to set */
	void setStoreBufferedImage(boolean storeBufferedImage) { this.storeBufferedImage = storeBufferedImage; }
	
	/** @return Returns the targetPictureFormat. */
	public String getTargetPictureFormat() { return targetPictureFormat; }
	/** @param targetPictureFormat the targetPictureFormat to set */
	void setTargetPictureFormat(String targetPictureFormat) { this.targetPictureFormat = targetPictureFormat; }

	/**
	 * This method manages the applet parameters that are specific to this class. The super.setProperty method is 
	 * called for other properties.
	 * 
	 * @param prop The property which value should change
	 * @param value The new value for this property. If invalid, the default value is used.
	 * @see wjhk.jupload2.policies.UploadPolicy#setProperty(java.lang.String, java.lang.String)
	 */
	public void setProperty(String prop, String value) {		
		//The, we check the local properties. 
		if (prop.equals(PROP_STORE_BUFFERED_IMAGE)) {
			setStoreBufferedImage (UploadPolicyFactory.parseBoolean(value, storeBufferedImage, this));
		} else if (prop.equals(PROP_HIGH_QUALITY_PREVIEW)) {
			setHighQualityPreview (UploadPolicyFactory.parseBoolean(value, highQualityPreview, this));
		} else if (prop.equals(PROP_MAX_HEIGHT)) {
			setMaxHeight (UploadPolicyFactory.parseInt(value, maxHeight, this));
		} else if (prop.equals(PROP_MAX_WIDTH)) {
			setMaxWidth (UploadPolicyFactory.parseInt(value, maxWidth, this));
		} else if (prop.equals(PROP_REAL_MAX_HEIGHT)) {
			setRealMaxHeight (UploadPolicyFactory.parseInt(value, realMaxHeight, this));
		} else if (prop.equals(PROP_REAL_MAX_WIDTH)) {
			setRealMaxWidth (UploadPolicyFactory.parseInt(value, realMaxWidth, this));
		} else if (prop.equals(PROP_TARGET_PICTURE_FORMAT)) {
			setTargetPictureFormat(value);
		} else {
			//Otherwise, transmission to the mother class.
			super.setProperty(prop, value);
		}
	}

	/** @see DefaultUploadPolicy#displayParameterStatus() */
	public void displayParameterStatus() {
		super.displayParameterStatus();
		
		if (maxWidth != DEFAULT_MAX_WIDTH   ||   maxHeight != DEFAULT_MAX_HEIGHT) {
			displayDebug(PROP_MAX_WIDTH + " : " + maxWidth + ", " + PROP_MAX_HEIGHT + " : " + maxHeight, 20);
		}
		if (realMaxWidth != DEFAULT_REAL_MAX_WIDTH   ||   realMaxHeight != DEFAULT_REAL_MAX_HEIGHT) {
			displayDebug(PROP_REAL_MAX_WIDTH + " : " + realMaxWidth + ", " + PROP_REAL_MAX_HEIGHT + " : " + realMaxHeight, 20);
		}
		displayDebug(PROP_HIGH_QUALITY_PREVIEW + " : " + highQualityPreview, 20);		
		displayDebug(PROP_STORE_BUFFERED_IMAGE + " : " + storeBufferedImage, 20);		
		displayDebug(PROP_TARGET_PICTURE_FORMAT + " : " + targetPictureFormat, 20);		
	}


}
