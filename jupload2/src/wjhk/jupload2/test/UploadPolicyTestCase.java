package wjhk.jupload2.test;

import wjhk.jupload2.context.JUploadContext;
import wjhk.jupload2.exception.JUploadException;
import wjhk.jupload2.policies.DefaultUploadPolicy;
import wjhk.jupload2.policies.UploadPolicy;

/**
 * This basic {@link UploadPolicy} is used to run test case, and manage what
 * happens in there.
 * 
 * @author etienne_sf
 */
public class UploadPolicyTestCase extends DefaultUploadPolicy {
    /**
     * @param juploadContext
     * @throws JUploadException
     */
    public UploadPolicyTestCase(JUploadContext juploadContext)
            throws JUploadException {
        super(juploadContext);
    }

    /**
     * Prevents any dialog box to be opened, when in debug mode. This allows
     * silent run of JUnit tests.
     * 
     * @see UploadPolicy#alertStr(String)
     */
    public void alertStr(String str) {
        // No action.
    }

}
