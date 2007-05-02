/*
 * Created on 20 nov. 06
 */
package wjhk.jupload2.exception;

/**
 * This class should be used for all implementations of FileData or UploadPolicy
 * that want to throw an IO exception, and need to be conform with the
 * interface definition.
 * 
 * @author Etienne Gauthier
 */

public class JUploadIOException extends JUploadException {

    /**
     * 
     */
    private static final long serialVersionUID = 4202340617039827612L;

    public JUploadIOException(String arg0) {
        super(arg0);
    }

    public JUploadIOException(Throwable arg0) {
        super(arg0);
    }

    public JUploadIOException(String arg0, Throwable arg1) {
        super(arg0, arg1);
    }

}
