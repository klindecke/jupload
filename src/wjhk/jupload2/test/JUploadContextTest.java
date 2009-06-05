package wjhk.jupload2.test;

import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import javax.swing.JFrame;

import wjhk.jupload2.context.JUploadContextExecutable;

/**
 * 
 * This class is a used as a context, out of real JUpload application. It is
 * used to build contexts for JUnit tests in the JUpload package.
 * 
 * @author etienne_sf
 */
public class JUploadContextTest extends JUploadContextExecutable {

    /**
     * Root folder for policies configuration files, that are used in JUnit
     * tests. The current user dir is the root of the eclipse project.
     */
    public final static String TEST_ROOT_FOLDER = "src/tests/";

    /**
     * Root folder for policies configuration files, that are used in JUnit
     * tests.
     */
    public final static String TEST_PROPERTIES_FOLDER = TEST_ROOT_FOLDER
            + "policies/";

    /**
     * Root folder for policies configuration files, that are used in JUnit
     * tests.
     */
    public final static String TEST_FILES_FOLDER = TEST_ROOT_FOLDER + "files/";

    /**
     * The policy default configuration file. It contains default values that
     * are shared by all policy configuration files.
     */
    public final static String TEST_DEFAULT_PROPERTIES_FILE = TEST_PROPERTIES_FOLDER
            + "default_uploadPolicy.properties";

    /** A useless JFrame, to allow a correct creation of the context. */
    private JFrame jframe = new JFrame();

    /**
     * 
     * @param jframe
     * @param propertyFilename
     */
    public JUploadContextTest(JFrame jframe, String propertyFilename) {

        // Load default properties
        defaultProperties = loadPropertiesFromTestFile(
                TEST_DEFAULT_PROPERTIES_FILE, null);

        // Load uploadpolicy properties, from the given file
        daemonProperties = loadPropertiesFromTestFile(TEST_PROPERTIES_FOLDER
                + propertyFilename, defaultProperties);

        // Now, we're ready. Let's initialize the DefaultJUploadContext.
        this.uploadPolicy = null;
        init(this.jframe);
    }

    /**
     * Creates and loads a property file, and return the loaded result.
     * 
     * @param filename The name of the file, which contains the properties to
     *            load
     * @param defaultProperties The default properties value. Put null if no
     *            default Properties should be used.
     * @return The loaded properties. It's empty if an error occurs.
     */
    Properties loadPropertiesFromTestFile(String filename,
            Properties defaultProperties) {
        Properties properties = new Properties(defaultProperties);
        try {
            // TODO use this.getClass() ?
            properties.load(new FileReader(filename));
        } catch (IOException e1) {
            System.out.println("Error while loading " + filename + " ("
                    + e1.getClass().getName() + ")");
            e1.printStackTrace();
        }

        return properties;
    }

}
