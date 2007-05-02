package wjhk.jupload2.exception;

import wjhk.jupload2.policies.UploadPolicy;

/**
 * This exception indicates, that the file that is to be uploaded is too big.
 * Note: the file to upload may be smaller than the file selected by the user.
 * For instance, a picture may be reduced before upload.
 */
public class JUploadExceptionTooBigFile extends JUploadException {

    /**
     * 
     */
    private static final long serialVersionUID = 4842380093113396023L;

    /**
     * Creates a new instance.
     * 
     * @param filename The filename for the file in error
     * @param uploadLength The length of this file
     * @param uploadPolicy The current upload policy.
     */
    public JUploadExceptionTooBigFile(String filename, long uploadLength,
            UploadPolicy uploadPolicy) {
        super(createErrorMessage(filename, uploadLength, uploadPolicy));
    }

    /**
     * This method creates the correct message for this exception.
     * 
     * @param filename
     * @param uploadLength
     * @param function
     */
    public static String createErrorMessage(String filename, long uploadLength,
            UploadPolicy uploadPolicy) {
        return uploadPolicy.getString("errFileTooBig", filename, Long
                .toString(uploadLength));
    }
}
