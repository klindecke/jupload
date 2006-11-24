/*
 * Created on 20 nov. 06
 */
package wjhk.jupload2.exception;

/**
 * This class should be used for all implementation of FileData or UploadPolicy that wants to transmit an IO exception,
 * and needs to ba conform with theit interface definition. 
 * 
 * @author Etienne Gauthier
 *
 */

public class JUploadIOException extends JUploadException {

	private static final long serialVersionUID = 1L;

	public JUploadIOException(String arg0) {
		super(arg0);
	}

	public JUploadIOException(Throwable arg0, String functionName) {
		super(arg0, functionName);
	}

	public JUploadIOException(String arg0, Throwable arg1, String functionName) {
		super(arg0, arg1, functionName);
	}

}
