package wjhk.jupload2;

import java.applet.*;
import java.awt.*;

import wjhk.jupload2.gui.JUploadPanel;
import wjhk.jupload2.policies.UploadPolicyFactory;

/**
 * The applet. It contains quite only the call to creation of the {@link wjhk.jupload2.gui.JUploadPanel},
 * which contains the real code. 
 */
public class JUploadApplet extends Applet{

  //------------- INFORMATION --------------------------------------------
  public static final String TITLE = "JUpload JUploadApplet";
  public static final String DESCRIPTION =
      "Java Applet wrapper for JUploadPanel.";
  public static final String AUTHOR = "William JinHua Kwong (updated by Etienne Gauthier)";

  public static final String VERSION = "2.1.1";
  public static final String LAST_MODIFIED = "07 sept 2006";

  //----------------------------------------------------------------------

  //private boolean isStandalone = false;

  //----------------------------------------------------------------------

  //Initialize the applet
  public void init() {
  	
    this.setLayout(new BorderLayout());    

    //Creation of the Panel, containing all GUI objects for upload.
    JUploadPanel jp = new JUploadPanel(this);

    this.add(jp, BorderLayout.CENTER);

  }
  
	/**
	 * This allow runtime modifications of properties. Currently, this is only user after
	 * full initialization. This methods only calls the UploadPolicy.setProperty method. 
	 * 
	 * @param prop
	 * @param value
	 */
	public void setProperty (String prop, String value) {
		try {
			UploadPolicyFactory.getCurrentUploadPolicy().setProperty(prop, value);
		} catch (Exception e) {
			UploadPolicyFactory.getCurrentUploadPolicy().displayErr("setProperty (exception " + e.getClass().getName() + ") : " + e.getMessage());
		}
	}
 }
