/*
 * Created on 15 sept. 2006
 */
package wjhk.jupload2.exception;

/**
 * This exception occurs when an upload failed. It can be generated if the
 * server response to the upload doesn't match the
 * {@link wjhk.jupload2.policies.UploadPolicy#PROP_STRING_UPLOAD_SUCCESS}
 * regular expression.
 * 
 * @author Etienne Gauthier
 */
public class JUploadExceptionUploadFailed extends JUploadException {

    /**
     * 
     */
    private static final long serialVersionUID = -9031106357048838553L;

    public JUploadExceptionUploadFailed(String arg0) {
        super(arg0);
    }

    public JUploadExceptionUploadFailed(Throwable arg0) {
        super(arg0);
    }

    public JUploadExceptionUploadFailed(String arg0, Throwable arg1) {
        super(arg0, arg1);
    }
}
