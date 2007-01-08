/*
 * Created on 6 mai 2006
 */
package wjhk.jupload2.policies;

import java.lang.reflect.Constructor;

import wjhk.jupload2.JUploadApplet;

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
	 * @return The newly created UploadPolicy.
	 */
	public static UploadPolicy getUploadPolicy(JUploadApplet theApplet) throws Exception {
		UploadPolicy uploadPolicy = null;
		//Let's create the update policy.
	    String uploadPolicyStr = getParameter(theApplet, 
	    		UploadPolicy.PROP_UPLOAD_POLICY, 
				UploadPolicy.DEFAULT_UPLOAD_POLICY);

	    String action=null;
	    boolean usingDefaultUploadPolicy = false;
	    try {
	    	action = uploadPolicyStr;
	    	Class uploadPolicyClass = null;
	    	try {
	    		uploadPolicyClass = Class.forName(uploadPolicyStr);
	    	} catch (ClassNotFoundException e1) {
	    		//Hum, let's try to prefix the policy name by the default package
	    		try {
	    			uploadPolicyClass = Class.forName("wjhk.jupload2.policies." + uploadPolicyStr);
	    		} catch (ClassNotFoundException e2) {
	    			//Too bad, we don't know how to create this class.
	    			usingDefaultUploadPolicy = true;
	    			uploadPolicyClass = Class.forName("wjhk.jupload2.policies.DefaultUploadPolicy");
	    		}
	    	}
		    action = "constructorParameters";
		    Class[] constructorParameters = {Class.forName("wjhk.jupload2.JUploadApplet")};
		    Constructor constructor = uploadPolicyClass.getConstructor(constructorParameters);
		    Object[] params = {theApplet};
		    uploadPolicy = (UploadPolicy) constructor.newInstance(params);
	    } catch (Exception e) {
	    	System.out.println("-ERROR- " + e.getClass().getName() + " in " + action);
	    	throw e;
	    }
	    
	    //The current values are dispayed here, after the full initialization of all classes.
	    //It could also be displayed in the DefaultUploadPolicy (for instance), but then, the 
	    //display wouldn't show the modifications done by superclasses.	    
	    uploadPolicy.displayDebug("uploadPolicy parameter = " + uploadPolicyStr, 1);
	    if (usingDefaultUploadPolicy) {
	    	uploadPolicy.displayWarn("Unable to create the '" + uploadPolicyStr + "'. Using the DefaultUploadPolicy instead.");
	    } else {
	    	uploadPolicy.displayInfo("uploadPolicy = " + uploadPolicy.getClass().getName());
	    }
	    		
		return uploadPolicy ;
	}
	
	/**
	 * Get a String parameter value from applet properties or System properties.
	 * 
	 * @return the parameter value, or the default, if the system is not set. 
	 */
	static public String getParameter(JUploadApplet theApplet, String key, String def) {
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
	static public int getParameter(JUploadApplet theApplet, String key, int def) {
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
	static public boolean getParameter(JUploadApplet theApplet, String key, boolean def) {
		String paramStr;
		String paramDef = (def ? "false" : "true");
		
		//First, read the parameter as a String
		if (theApplet == null) {
			paramStr = System.getProperty(key) != null ? System.getProperty(key) : paramDef;
		} else {
			paramStr = theApplet.getParameter(key) != null ? theApplet.getParameter(key) : paramDef;
		}
	    
	    return parseBoolean(paramStr, def);
	}//getParameter(boolean)

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
		if (value.toUpperCase().equals("FALSE")) {
			return false;
		} else if (value.toUpperCase().equals("TRUE")) {
			return true;
		} else {
			return def;
		}
	}
}
