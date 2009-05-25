package wjhk.jupload2.context;

import javax.swing.JFrame;

/**
 * 
 * This class is a used as a context, out of real JUpload application. It is
 * used to build contexts for JUnit tests in the JUpload package.
 * 
 * @author etienne_sf
 */
public class JUploadContextTest extends JUploadContextExecutable {

    private String TEST_PROPERTIES_FOLDER = "/tests/policies/";

    private String TEST_DEFAULT_PROPERTIES_FILE = TEST_PROPERTIES_FOLDER
            + "default_uploadPolicy.properties";

    /** A useless JFrame, to allow a correct creation of the context. */
    private JFrame jframe = new JFrame();

    /**
     * 
     * @param jframe
     * @param propertyFilename
     */
    public JUploadContextTest(JFrame jframe, String propertyFilename) {
        super(jframe, null);

        // Load default properties
        defaultProperties = loadPropertiesFromFile(
                TEST_DEFAULT_PROPERTIES_FILE, null);

        // Load uploadpolicy properties, from the given file
        daemonProperties = loadPropertiesFromFile(TEST_PROPERTIES_FOLDER
                + propertyFilename, defaultProperties);

        // Now, we're ready. Let's initialize the DefaultJUploadContext.
        init(this.jframe);
    }

}
