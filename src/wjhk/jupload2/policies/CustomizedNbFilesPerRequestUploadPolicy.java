/*
 * Created on 6 mai 2006
 */
package wjhk.jupload2.policies;

import java.applet.Applet;

import javax.swing.JTextArea;


/**
 * These is a simple specialization of {@link wjhk.jupload2.policies.DefaultUploadPolicy}:
 * the constructor allows to set a maximum number of files per upload.
 * <BR> 
 * 
 * @author Etienne Gauthier
 * @see #CustomizedNbFilesPerRequestUploadPolicy(String, int, Applet, int, JTextArea)
 * @deprecated This class is actually of no use, as it will behave the same way as the
 * {@link wjhk.jupload2.policies.DefaultUploadPolicy}.
 */
public class CustomizedNbFilesPerRequestUploadPolicy extends DefaultUploadPolicy {

	/**
	 * @param postURL The target URL for uploaded pictures.
	 * @param maxFilesPerUpload Maximum number of files to upload in one HTTP request to the server.
	 * @param theApplet The applet to whom the UploadPolicy mus apply.
	 * @param debugLevel Current debug level (see {@link UploadPolicy#setDebugLevel(int)}).
	 * @param status Current instance of {@link wjhk.jupload2.gui.StatusArea}.
	 *  
	 * @see UploadPolicy
	 */
	protected CustomizedNbFilesPerRequestUploadPolicy(String postURL, int maxFilesPerUpload, Applet theApplet, int debugLevel, JTextArea status) {
		super(postURL, theApplet, debugLevel, status);
		
		//The given maxFilesPerUpload must superseed the default value.
		this.maxFilesPerUpload = maxFilesPerUpload;
	}


}
