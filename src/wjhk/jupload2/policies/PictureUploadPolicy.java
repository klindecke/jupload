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
	 * @param postURL The URL where picture files are to be posted (applet parameter name: postURL).
	 * @param nbFilesPerRequest Number of files in one HTTP request to the server (applet parameter: nbFilesPerRequest)
	 * @param applet Reference to the current applet. Allows access to javasript functions.
	 * @param debugLevel Current debugLevel (applet parameter: debugLevel).
	 * @param status The status bar, where messages are to be displayed.
	 */
	public PictureUploadPolicy(JUploadApplet theApplet) {
		super(theApplet);
	    
	    //Creation of the PictureFileDataPolicy, from parameters given to
		//the applet, or from default values.
	    storeBufferedImage = UploadPolicyFactory.getParameter(theApplet, PROP_STORE_BUFFERED_IMAGE, DEFAULT_STORE_BUFFERED_IMAGE);
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
	  	picturePanel = new PicturePanel(mainPanel, false, this);
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
    //////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////      Getters and Setters   ////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * @return Returns the createBufferedImage.
	 */
	public boolean hasToStoreBufferedImage() {
		return storeBufferedImage;
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
	 * @return Returns the targetPictureFormat.
	 */
	public String getTargetPictureFormat() {
		return targetPictureFormat;
	}
}
