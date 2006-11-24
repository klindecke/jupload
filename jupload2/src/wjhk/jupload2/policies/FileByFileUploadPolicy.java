/*
 * Created on 6 mai 2006
 */
package wjhk.jupload2.policies;


import wjhk.jupload2.JUploadApplet;


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
	 * @param applet The applet on which the UploadPolicy should apply.
	 */
	public FileByFileUploadPolicy(JUploadApplet theApplet) {
		super(theApplet);
		
		this.nbFilesPerRequest = 1;
	}

}
