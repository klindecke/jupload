/*
 * Created on 6 mai 2006
 */
package wjhk.jupload2.policies;

import wjhk.jupload2.JUploadApplet;


/**
 * These is a now deprecated specialization of {@link wjhk.jupload2.policies.DefaultUploadPolicy}. The
 * DefaultUploadPolicy now reads itself the nbFilesPerRequest applet parameter. 
 * <BR> 
 * 
 * @author Etienne Gauthier
 * @see #CustomizedNbFilesPerRequestUploadPolicy(String, int, Applet, int, JTextArea)
 * @deprecated This class is of no use, as it actually behaves exactly as the
 * {@link wjhk.jupload2.policies.DefaultUploadPolicy}.
 */
public class CustomizedNbFilesPerRequestUploadPolicy extends DefaultUploadPolicy {

	/**
	 * @param applet The applet to whom the UploadPolicy must apply.
	 *  
	 * @see UploadPolicy
	 */
	public CustomizedNbFilesPerRequestUploadPolicy(JUploadApplet theApplet) {
		super(theApplet);
	}


}
