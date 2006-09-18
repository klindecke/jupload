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
	 */
	private static UploadPolicy currentUploadPolicy = null;
	
	/** 
	 * @return The current UploadPolicy. Null if no UploadPolicy was created.
	 */
	public static UploadPolicy getCurrentUploadPolicy() {
		return currentUploadPolicy;
	}
	
	/**
	 * Use applet parameters.
	 *
	 * @param theApplet if not null : use this Applet Parameters. If null, use System properties.  
	 * @return The selected UploadPolicy.
	 */
	public static UploadPolicy getUploadPolicy(Applet theApplet, JTextArea status) {
	    String postURL = getParameter(theApplet, 
	    		UploadPolicy.PROP_POST_URL, 
	    		UploadPolicy.DEFAULT_POST_URL);
	    return getUploadPolicy(theApplet, status, postURL);
	}
	
	/**
	 * Returns an upload Policy for the given applet and URL. This method doesn't look
	 * at the postURL system property.
	 * 
	 * @param theApplet if not null : use this Applet Parameters (out of the postURL, which is given). If null, use System properties  (out of the postURL).  
	 * @param postURL The URL where files are to be posted.
	 * @return The selected UploadPolicy.
	 */
	public static UploadPolicy getUploadPolicy (Applet theApplet, JTextArea status, String postURL) {
		//Let's create the update policy.
	    String uploadPolicyStr;
	    uploadPolicyStr = getParameter(theApplet, 
	    		UploadPolicy.PROP_UPLOAD_POLICY, 
				UploadPolicy.DEFAULT_UPLOAD_POLICY);
	    int debugLevel = getParameter(theApplet, 
	    		UploadPolicy.PROP_DEBUG_LEVEL, 
	    		UploadPolicy.DEFAULT_DEBUG_LEVEL);

	    if (uploadPolicyStr.equals("FileByFileUploadPolicy")) {
	    	currentUploadPolicy = new FileByFileUploadPolicy(postURL, theApplet, debugLevel, status);
	    /*
	     * deprecated:  CustomizedNbFilesPerRequestUploadPolicy behaves exactly as DefaultUploadPolicy.
	    } else if (uploadPolicyStr.equals("CustomizedNbFilesPerRequestUploadPolicy")) {
		    int nbFilesPerRequest = getParameter(theApplet, 
		    		UploadPolicy.PROP_NB_FILES_PER_REQUEST, 
		    		UploadPolicy.DEFAULT_NB_FILES_PER_REQUEST);
		    currentUploadPolicy = new CustomizedNbFilesPerRequestUploadPolicy(postURL, nbFilesPerRequest, theApplet, debugLevel, status);
		*/	    	
	    } else if (uploadPolicyStr.equals("PictureUploadPolicy")) {
		    int nbFilesPerRequest = getParameter(theApplet, 
		    		UploadPolicy.PROP_NB_FILES_PER_REQUEST, 
		    		UploadPolicy.DEFAULT_NB_FILES_PER_REQUEST);
		    currentUploadPolicy = new PictureUploadPolicy(postURL, nbFilesPerRequest, theApplet, debugLevel, status);	    	
	    } else if (uploadPolicyStr.equals("CoppermineUploadPolicy")) {
		    int albumId = getParameter(theApplet, 
		    		UploadPolicy.PROP_ALBUM_ID, 
		    		UploadPolicy.DEFAULT_ALBUM_ID);
		    currentUploadPolicy = new CoppermineUploadPolicy(postURL, albumId, theApplet, debugLevel, status);	    	
	    } else {
	    	//Create default Policy
	    	currentUploadPolicy = new DefaultUploadPolicy(postURL, theApplet, debugLevel, status);
	    }
	    
	    currentUploadPolicy.displayDebug("uploadPolicy parameter = " + uploadPolicyStr, 1);
	    currentUploadPolicy.displayInfo("postURL = " + currentUploadPolicy.getPostURL());
	    currentUploadPolicy.displayInfo("uploadPolicy = " + currentUploadPolicy.getClass().getName());
		///////////////////////////////////////////////////////////////////////////////
		// Let's display some information to the user.
	    currentUploadPolicy.displayDebug("debug : " + debugLevel, 1); 
	    currentUploadPolicy.displayDebug("stringUploadSuccess : <" + currentUploadPolicy.getStringUploadSuccess() + ">", 20); 
	    currentUploadPolicy.displayDebug("serverProtocole : " + currentUploadPolicy.getServerProtocol(), 20); 
	    currentUploadPolicy.displayDebug("Java version  : " + System.getProperty("java.version"), 1); 
	    		
		return currentUploadPolicy ;
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
