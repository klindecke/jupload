/*
 * Created on 6 mai 2006
 */
package wjhk.jupload2.policies;

import java.applet.Applet;

import javax.swing.JTextArea;


/**
 * Specialization of {@link wjhk.jupload2.policies.CustomizedNbFilesPerRequestUploadPolicy}, where each upload 
 * HTTP request contains only one file.
 * <BR><BR>
 * This policy :
 * <UL>
 * <LI> Upload files without tranformation
 * <LI> File by file (uploading 5 files needs 5 HTTP request toward the server)
 * <UL> 
 * <BR><BR>
 * The same behaviour can be obtained by specifying no UploadPolicy (or {@link FileByFileUploadPolicy}),
 * and give the nbFilesPerRequest (with a value set to 1) parameter.
 * 
 * 
 * @author Etienne Gauthier
 *
 */
public class FileByFileUploadPolicy extends DefaultUploadPolicy {

	/**
	 * @param postURL
	 */
	protected FileByFileUploadPolicy(String postURL, Applet theApplet, int debugLevel, JTextArea status) {
		super(postURL, theApplet, debugLevel, status);
		
		this.maxFilesPerUpload = 1;
	}

}
