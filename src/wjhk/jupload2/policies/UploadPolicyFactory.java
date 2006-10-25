/*
 * Created on 6 mai 2006
 */
package wjhk.jupload2.policies;

import java.applet.Applet;

import javax.swing.JTextArea;

/**
 * This class is used to control creation of the uploadPolicy instance, according to  applet parameters (or System properties).
 * <BR><BR>
 * The used parameters are:
 * <UL>
 * <LI>  postURL: The URL where files are to be uploaded. This parameter is mandatory 
 *  if called from a servlet.
 * <LI> uploadPolicy: the class name to be used as a policy. Currently available : not 
 * defined (then use DefaultUploadPolicy), {@link wjhk.jupload2.policies.DefaultUploadPolicy}, 
 * {@link wjhk.jupload2.policies.FileByFileUploadPolicy}, {@link wjhk.jupload2.policies.CustomizedNbFilesPerRequestUploadPolicy},
 * {@link wjhk.jupload2.policies.CoppermineUploadPolicy}
 * </UL> 
 * 
 * @author Etienne Gauthier
 *
 */
public class UploadPolicyFactory {
	

	/**
	 * This attribute contains the current UploadPolicy. It can be retrieved by the
	 * {@link #getCurrentUploadPolicy()} method.
	 *
	private static UploadPolicy currentUploadPolicy = null;
	*/
	
	
	/** 
	 * @return The current UploadPolicy. Null if no UploadPolicy was created.
	 * 
	 * @deprecated This method is static, which means that if the applet is opened two times (for instance 
	 * into two different pages), you don't can't control which uploadPolicy is returned.  
	 *
	public static UploadPolicy getCurrentUploadPolicy() {
		return currentUploadPolicy;
	}*/
	
	/**
	 * Returns an upload Policy for the given applet and URL. All other parameters for the uploadPolicy are
	 * take from avaiable applet parameters (or from system properties, if it is not run as an applet).
	 *
	 * @param theApplet if not null : use this Applet Parameters. If null, use System properties.  
	 * @param status A JTextArea, that'll contain output to the user. It will contain the ERROR, WARN and INFO
	 *    texts. The DEBUG ones will be displayed, according to the current debugLevel (see <a href="UploadPolicy.html#parameters">parameters</a>). 
	 * @return The newly created UploadPolicy.
	 */
	public static UploadPolicy getUploadPolicy(Applet theApplet, JTextArea status) {
		UploadPolicy uploadPolicy = null;
		//Let's create the update policy.
	    String uploadPolicyStr = getParameter(theApplet, 
	    		UploadPolicy.PROP_UPLOAD_POLICY, 
				UploadPolicy.DEFAULT_UPLOAD_POLICY);

	    if (uploadPolicyStr.equals("FileByFileUploadPolicy")) {
	    	uploadPolicy = new FileByFileUploadPolicy(theApplet, status);
	    } else if (uploadPolicyStr.equals("CustomizedNbFilesPerRequestUploadPolicy")) {
	    	uploadPolicy = new CustomizedNbFilesPerRequestUploadPolicy(theApplet, status);
	    } else if (uploadPolicyStr.equals("PictureUploadPolicy")) {
	    	uploadPolicy = new PictureUploadPolicy(theApplet, status);	    	
	    } else if (uploadPolicyStr.equals("CoppermineUploadPolicy")) {
	    	uploadPolicy = new CoppermineUploadPolicy(theApplet, status);	    	
	    } else {
	    	//Create default Policy
	    	uploadPolicy = new DefaultUploadPolicy(theApplet, status);
	    }
	    
	    //The current values are dispayed here, after the full initialization of all classes.
	    //It could also be displayed in the DefaultUploadPolicy (for instance), but then, the 
	    //display wouldn't show the modifications done by superclasses.	    
	    uploadPolicy.displayDebug("uploadPolicy parameter = " + uploadPolicyStr, 1);
	    uploadPolicy.displayInfo("uploadPolicy = " + uploadPolicy.getClass().getName());
	    uploadPolicy.displayInfo("postURL = " + uploadPolicy.getPostURL());
		///////////////////////////////////////////////////////////////////////////////
		// Let's display some information to the user.
	    uploadPolicy.displayDebug("debug : " + uploadPolicy.getDebugLevel(), 1); 
	    uploadPolicy.displayDebug("stringUploadSuccess : " + uploadPolicy.getStringUploadSuccess(), 20); 
	    uploadPolicy.displayDebug("urlToSendErrorTo: " + uploadPolicy.getUrlToSendErrorTo(), 20);
	    uploadPolicy.displayDebug("serverProtocol : " + uploadPolicy.getServerProtocol(), 20); 
	    		
		return uploadPolicy ;
	}
	
	/**
	 * Get a String parameter value from applet properties or System properties.
	 * 
	 * @return the parameter value, or the default, if the system is not set. 
	 */
	static public String getParameter(Applet theApplet, String key, String def) {
		if (theApplet == null) {
			return (System.getProperty(key) != null ? System.getProperty(key) : def);
		} else {
			return (theApplet.getParameter(key) != null ? theApplet.getParameter(key) : def);
		}
	}//getParameter(String)

	/**
	 * Get a String parameter value from applet properties or System properties.
	 * 
	 * @return the parameter value, or the default, if the system is not set. 
	 */
	static public int getParameter(Applet theApplet, String key, int def) {
		String paramStr;
		String paramDef = Integer.toString(def);
		
		//First, read the parameter as a String
		if (theApplet == null) {
			paramStr = System.getProperty(key) != null ? System.getProperty(key) : paramDef;
		} else {
			paramStr = theApplet.getParameter(key) != null ? theApplet.getParameter(key) : paramDef;
		}
	    
	    return parseInt(paramStr, def);
	}//getParameter(int)

	/**
	 * Get a boolean parameter value from applet properties or System properties.
	 * 
	 * @return the parameter value, or the default, if the system is not set. 
	 */
	static public boolean getParameter(Applet theApplet, String key, boolean def) {
		String paramStr;
		String paramDef = (def ? "false" : "true");
		
		//First, read the parameter as a String
		if (theApplet == null) {
			paramStr = System.getProperty(key) != null ? System.getProperty(key) : paramDef;
		} else {
			paramStr = theApplet.getParameter(key) != null ? theApplet.getParameter(key) : paramDef;
		}
	    
	    return parseBoolean(paramStr, def);
	}//getParameter(int)

	/**
	 * This function try to parse value as an integer. If value is not a correct integer,
	 * def is returned.
	 * 
	 * @param value
	 * @param def
	 * @return The integer value of value, or def if value is not valid.
	 */
	static public int parseInt (String value, int def) {
		int ret = def;
		//Then, parse it as an integer.
	    try {
	    	ret = Integer.parseInt(value);
	    } catch (NumberFormatException e) {
	    	//Nothing to do
	    }
	    
	    return ret;
	}

	/**
	 * This function try to parse value as a boolean. If value is not a correct boolean,
	 * def is returned.
	 * 
	 * @param value
	 * @param def
	 * @return The boolean value of value, or def if value is not a valid boolean.
	 */
	static public boolean parseBoolean (String value, boolean def) {
		//Then, parse it as a boolean.
		if (value.toUpperCase().equals("false")) {
			return false;
		} else if (value.toUpperCase().equals("false")) {
			return true;
		} else {
			return def;
		}
	}
}
