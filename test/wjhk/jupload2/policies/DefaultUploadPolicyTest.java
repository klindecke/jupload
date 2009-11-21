package wjhk.jupload2.policies;

import java.io.InputStream;
import java.util.Properties;

import org.junit.Before;
import org.junit.Test;

import wjhk.jupload2.JUploadDaemon;
import wjhk.jupload2.context.JUploadContext;
import wjhk.jupload2.test.JUploadContextTest;

import junit.framework.TestCase;

/**
 * 
 * @author etienne_sf
 */
public class DefaultUploadPolicyTest extends TestCase {

    final static String DEFAULT_UPLOADPOLICY_PROPERTIES = "/tests/policies/default_uploadPolicy.properties";

    JUploadDaemon juploadDaemon;

    JUploadContext juploadContext;

    Properties defaultProperties;

    /**
     * @throws java.lang.Exception
     */
    @Override
    @Before
    public void setUp() throws Exception {
        defaultProperties = new Properties();
        InputStream is = getClass().getResourceAsStream(
                DEFAULT_UPLOADPOLICY_PROPERTIES);
        if (is == null) {
            throw new NullPointerException("Impossible d'ouvrir '"
                    + DEFAULT_UPLOADPOLICY_PROPERTIES + "'");
        }
        defaultProperties.load(is);
        is.close();
    }

    /**
     * Creation of JUploadApplet or JUploadExecutable
     * 
     * @param prop The property to set, before creating the uploadPolicy
     * @param value The value for this property.
     */
    private void initJUpload(String prop, String value) {
        defaultProperties.put(prop, value);
        initJUpload();
    }

    /**
     * Creation of JUploadApplet or JUploadExecutable
     */
    private void initJUpload() {
        juploadDaemon = new JUploadDaemon();
        juploadContext = new JUploadContextTest(juploadDaemon,
                defaultProperties);
    }

    /**
     * @throws Exception
     */
    @Test
    public void testSetPostURL() throws Exception {
        String postURL = "/test.jsp";
        initJUpload(UploadPolicy.PROP_POST_URL, postURL);
        String url = juploadContext.getUploadPolicy().getPostURL();
        assertTrue("postURL set to '" + postURL + "'", url.endsWith(postURL));

        postURL = "ftp://127.0.0.1/pub";
        initJUpload(UploadPolicy.PROP_POST_URL, postURL);
        url = juploadContext.getUploadPolicy().getPostURL();
        assertTrue("postURL set to '" + postURL + "'", url.endsWith(postURL));
    }

    /**
     * @throws Exception
     */
    @Test
    public void testSetUploadPolicy() throws Exception {
        // First: check the default upload policy
        initJUpload();
        assertTrue("uploadPolicy not set",
                juploadContext.getUploadPolicy() instanceof DefaultUploadPolicy);

        // Then, we manually set the DefaultUploadPolicy
        String uploadPolicyStr = "DefaultUploadPolicy";
        initJUpload(UploadPolicy.PROP_UPLOAD_POLICY, uploadPolicyStr);
        assertTrue("uploadPolicy set to '" + uploadPolicyStr + "'",
                juploadContext.getUploadPolicy().getClass().getName().endsWith(
                        "." + uploadPolicyStr));

        // Then, we manually set the PictureUploadPolicy
        uploadPolicyStr = "PictureUploadPolicy";
        initJUpload(UploadPolicy.PROP_UPLOAD_POLICY, uploadPolicyStr);
        assertTrue("uploadPolicy set to '" + uploadPolicyStr + "'",
                juploadContext.getUploadPolicy().getClass().getName().endsWith(
                        "." + uploadPolicyStr));
    }

    /**
     * @throws Exception
     */
    @Test
    public void testSetCurrentBrowsingDirectoryString() throws Exception {
        String dir = "test";
        defaultProperties.put(UploadPolicy.PROP_BROWSING_DIRECTORY, dir);
        initJUpload();

        assertEquals("current browsing directory set to '" + dir + "'", dir,
                juploadContext.getUploadPolicy().getCurrentBrowsingDirectory()
                        .getName());
    }
}
