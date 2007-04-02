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
	 * Returns an upload Policy for the given applet and URL. All other parameters for the uploadPolicy are
	 * take from avaiable applet parameters (or from system properties, if it is not run as an applet).
	 *
	 * @param theApplet if not null : use this Applet Parameters. If null, use System properties.  
	 * @return The newly created UploadPolicy.
	 */
	public static UploadPolicy getUploadPolicy(JUploadApplet theApplet) throws Exception {		
		UploadPolicy uploadPolicy = theApplet.getUploadPolicy();
		
		if (uploadPolicy == null) {
			//Let's create the update policy.
		    String uploadPolicyStr = getParameter(theApplet, 
		    		UploadPolicy.PROP_UPLOAD_POLICY, 
					UploadPolicy.DEFAULT_UPLOAD_POLICY,
					null);
	
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
			    action = "newInstance";
			    uploadPolicy = (UploadPolicy) constructor.newInstance(params);
		    } catch (Exception e) {
		    	System.out.println("-ERROR- " + e.getClass().getName() + " in " + action + "(error message: " + e.getMessage() + ")");
		    	throw e;
		    }
		    
		    //The current values are dispayed here, after the full initialization of all classes.
		    //It could also be displayed in the DefaultUploadPolicy (for instance), but then, the 
		    //display wouldn't show the modifications done by superclasses.	    
		    uploadPolicy.displayDebug("uploadPolicy parameter = " + uploadPolicyStr, 1);
		    if (usingDefaultUploadPolicy) {
		    	uploadPolicy.displayWarn("Unable to create the '" + uploadPolicyStr + "'. Using the DefaultUploadPolicy instead.");
		    } else {
		    	uploadPolicy.displayDebug("uploadPolicy = " + uploadPolicy.getClass().getName(), 20);
		    }

		    //Then, we display the applet parameter list.
		    uploadPolicy.displayParameterStatus();
		}
	    		
		return uploadPolicy ;
	}
	
	/**
	 * Get a String parameter value from applet properties or System properties.
	 * 
	 * @return the parameter value, or the default, if the system is not set. 
	 */
	static public String getParameter(JUploadApplet theApplet, String key, String def, UploadPolicy uploadPolicy) {
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
	static public int getParameter(JUploadApplet theApplet, String key, int def, UploadPolicy uploadPolicy) {
		String paramStr;
		String paramDef = Integer.toString(def);
		
		//First, read the parameter as a String
		if (theApplet == null) {
			paramStr = System.getProperty(key) != null ? System.getProperty(key) : paramDef;
		} else {
			paramStr = theApplet.getParameter(key) != null ? theApplet.getParameter(key) : paramDef;
		}
	    
	    return parseInt(paramStr, def, uploadPolicy);
	}//getParameter(int)

	/**
	 * Get a String parameter value from applet properties or System properties.
	 * 
	 * @return the parameter value, or the default, if the system is not set. 
	 */
	static public long getParameter(JUploadApplet theApplet, String key, long def, UploadPolicy uploadPolicy) {
		String paramStr;
		String paramDef = Long.toString(def);
		
		//First, read the parameter as a String
		if (theApplet == null) {
			paramStr = System.getProperty(key) != null ? System.getProperty(key) : paramDef;
		} else {
			paramStr = theApplet.getParameter(key) != null ? theApplet.getParameter(key) : paramDef;
		}
	    
	    return parseLong(paramStr, def, uploadPolicy);
	}//getParameter(int)

	/**
	 * Get a boolean parameter value from applet properties or System properties.
	 * 
	 * @return the parameter value, or the default, if the system is not set. 
	 */
	static public boolean getParameter(JUploadApplet theApplet, String key, boolean def, UploadPolicy uploadPolicy) {
		String paramStr;
		String paramDef = (def ? "true" : "false");
		
		//First, read the parameter as a String
		if (theApplet == null) {
			paramStr = System.getProperty(key) != null ? System.getProperty(key) : paramDef;
		} else {
			paramStr = theApplet.getParameter(key) != null ? theApplet.getParameter(key) : paramDef;
		}
	    
	    return parseBoolean(paramStr, def, uploadPolicy);
	}//getParameter(boolean)

	/**
	 * This function try to parse value as an integer. If value is not a correct integer,
	 * def is returned.
	 * 
	 * @param value
	 * @param def
	 * @return The integer value of value, or def if value is not valid.
	 */
	static public int parseInt (String value, int def, UploadPolicy uploadPolicy) {
		int ret = def;
		//Then, parse it as an integer.
	    try {
	    	ret = Integer.parseInt(value);
	    } catch (NumberFormatException e) {
	    	ret = def;
			if (uploadPolicy != null) {
				uploadPolicy.displayWarn("Invalid int value: " + value + ", using default value: " + def);
			}
	    }
	    
	    return ret;
	}
	/**
	 * This function try to parse value as a Long. If value is not a correct long,
	 * def is returned.
	 * 
	 * @param value
	 * @param def
	 * @return The integer value of value, or def if value is not valid.
	 */
	static public long parseLong (String value, long def, UploadPolicy uploadPolicy) {
		long ret = def;
		//Then, parse it as an integer.
	    try {
	    	ret = Long.parseLong(value);
	    } catch (NumberFormatException e) {
	    	ret = def;
			if (uploadPolicy != null) {
				uploadPolicy.displayWarn("Invalid long value: " + value + ", using default value: " + def);
			}
	    }
	    
	    return ret;
	}

	/**
	 * This function try to parse value as a boolean. If value is not a correct boolean,
	 * def is returned.
	 * 
	 * @param value The new value for this property. If invalid, the default value is used.
	 * @param def The default value: used if value is invalid.
	 * @param uploadPolicy If not null, it will be used to display a warning when the value is invalid.
	 * 
	 * @return The boolean value of value, or def if value is not a valid boolean.
	 */
	static public boolean parseBoolean (String value, boolean def, UploadPolicy uploadPolicy) {
		//Then, parse it as a boolean.
		if (value.toUpperCase().equals("FALSE")) {
			return false;
		} else if (value.toUpperCase().equals("TRUE")) {
			return true;
		} else {
			if (uploadPolicy != null) {
				uploadPolicy.displayWarn("Invalid boolean value: " + value + ", using default value: " + def);
			}
			return def;
		}
	}
}
