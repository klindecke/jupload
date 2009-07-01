package wjhk.jupload2;

import javax.swing.JFrame;

import wjhk.jupload2.context.JUploadContext;
import wjhk.jupload2.context.JUploadContextExecutable;
import wjhk.jupload2.policies.UploadPolicy;

/**
 * 
 * This class allows to use JUpload as a stand alone application. It can then be
 * used to manage upload, as do the applet. Or it can be used as a daemon. In
 * this case the applet 'paste' files to the daemon, and the daemon is
 * responsible for the upload. This is a good point when uploading big files:
 * the user can go on browsing, or close his/her browser. The daemon will keep
 * on uploading the file(s). <BR>
 * The configuration can be stored in the jar file, or in a property file
 * available on the net, through a URL. See the {link {@link #main(String[])}
 * method for details.<BR>
 * The daemon parameters are the same for the applet and the daemon. They are
 * described on the {@link UploadPolicy} page.
 * 
 * @author etienne_sf
 * 
 */
public class JUploadDaemon extends JFrame {

    /** A generated serialVersionUID */
    private static final long serialVersionUID = 1L;

    /**
     * The URL, that 'perhaps' was given to the main method. Used only to
     * transfer the value between the invokeLater and the actual execution of
     * this method. It's not very clean, but I guess there will never be two
     * execution of the daemon that will start in the same quarter of second
     * with a different URL.
     */
    private static String propertiesURL = null;

    /**
     * The current execution context.
     */
    transient JUploadContext juploadContext = null;

    /**
     * Default constructor.
     */
    public JUploadDaemon() {
        // TODO add a way to personalize the window title.
        super("JUpload daemon");

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    /**
     * Create the GUI and show it. For thread safety, this method should be
     * invoked from the event-dispatching thread.
     */
    private static void createAndShowGUI() {
        JUploadDaemon juploadDaemon = new JUploadDaemon();
        juploadDaemon.juploadContext = new JUploadContextExecutable(
                juploadDaemon, propertiesURL);

        // Display the window.
        juploadDaemon.pack();
        juploadDaemon.setVisible(true);
    }

    /**
     * The start of the application, when launched as a Stand Alone one. If an
     * argument is given, it must be a valid URL to the JUpload configuration
     * file. The daemon will load it as a property file. The allowed parameters
     * and values are the same as the applet parameters. These are indicated in
     * the {@link wjhk.jupload2.policies.UploadPolicy} page.<BR>
     * If this URL is not given, the /conf folder in the jar file must contain
     * the daemon property file.
     * 
     * @param args args[1] is optional, and may contain the URL pointing to the
     *            configuration page.
     */
    public static void main(String[] args) {
        propertiesURL = null;
        if (args.length > 0) {
            propertiesURL = args[0];
        }
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }
}
