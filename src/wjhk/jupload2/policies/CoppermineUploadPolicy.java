/*
 * Created on 7 mai 2006
 */
package wjhk.jupload2.policies;

import java.applet.Applet;

import javax.swing.JTextArea;

import wjhk.jupload2.filedata.FileData;
import wjhk.jupload2.gui.FilePanel;

import netscape.javascript.JSException;
import netscape.javascript.JSObject;

//TODO cookies handling: desc to be mve to UploadPolicy presentation.
/**
 * Specific UploadPolicy for the coppermine picture gallery. It is based on the PictureUploadPolicy, and some
 * specific part to add the uploaded pictures to a coppermine existing album.
 * <BR>
 * Specific features for this policy are:
 * <UL>
 * <LI>Album handling : the setProperty("albumId", n) can be called from javascript, when the user 
 * selects another album (with n is the numeric id for the selected album). This needs that the MAYSCRIPT HTML 
 * parameter is set, in the APPLET tag (see the example below). The upload can not start if the user didn't first 
 * select an album.
 * <LI>File by file upload (one file by HTTP Request). Uploaded files are sent to the coppermine's 
 * xp_publish.php script. If given, the nbFilesPerRequest parameter is ignored. Files are always uploaded one by one.
 * <LI>If an orror occurs, the applet asks the user if he wants to send a mail to the webmaster. If he answered yes,
 * the full debug output is submitted to the URL pointed by urlToSendErrorTo. This URL should send a mail to the 
 * manager of the Coppermine galery. 
 * </UL>
 * 
 * <A NAME="example1"><H3>Call of the applet from a php script in coppermine</H3></A>
 * You'll find below an example of how to put the applet into a PHP page:
 * <BR>
 * <XMP>
	   <?php
	 	  $URL = $CONFIG['site_url'] . 'xp_publish.php';
	 	  $lang = $lang_translation_info['lang_country_code'];
	 	  $max_upl_width_height = $CONFIG['max_upl_width_height'];
	  ?>
      <APPLET  
          NAME="JUpload"
          CODE="wjhk.jupload2.JUploadApplet" 
          ARCHIVE="plugins/jupload/wjhk.jupload.jar" 
          <!-- Applet display size, on the navigator page -->
          WIDTH="500" 
          HEIGHT="700"
          <!-- The applet call some javascript function, so we must allow it : -->
          MAYSCRIPT
          >
          <!-- First, mandatory parameters -->
          <PARAM NAME="postURL"      VALUE="$URL">
          <PARAM NAME="uploadPolicy" VALUE="CoppermineUploadPolicy">
          <!-- Then, optional parameters -->
          <PARAM NAME="lang"         VALUE="$lang">
          <PARAM NAME="maxPicHeight" VALUE="$max_upl_width_height">
          <PARAM NAME="maxPicWidth"  VALUE="$max_upl_width_height">
          <PARAM NAME="debugLevel"   VALUE="0">
                
      Java 1.4 or higher plugin required.
      </APPLET>

 * </XMP>
 * 
 * 
 * <A NAME="example1"><H3>Example 2: albumId set by a javascript call.</H3></A>
 * <XMP>
 *  <script language="javascript" type="text/javascript">
 *  function onAlbumChange() {
 *    if (document.form_album.album_id.selectedIndex >= 0) {
 *      document.applets['JUpload'].setProperty('albumId', document.form_album.album_id.value);
 *      document.form_album.album_name.value = document.form_album.album_id.options[document.form_album.album_id.selectedIndex].text;
 *      document.form_album.album_description.value = description[document.form_album.album_id.value];
 *    } else {
 *      document.JUpload.setProperty('albumId', '');
 *      document.form_album.album_name.value = '';
 *      document.form_album.album_description.value = '';
 *    }
 *  }
 * </script>
 * </XMP>
 * 
 * @author Etienne Gauthier
 */
public class CoppermineUploadPolicy extends PictureUploadPolicy {
		
	private int albumId;

	/**
	 * @param postURL The URL where files should be posted to are read by the {@link UploadPolicyFactory}.
	 * @param albumId The identifier for the album, where pictures should be uploaded. This 
	 * identifier can be updated by calling <I>applet.setProperty("albumId", n)</I> (see an example on the 
	 * top of this page)
	 * @param theApplet Identifier for the current applet. It's necessary, to read information from the navigator.
	 * @param debugLevel See {@link UploadPolicy}
	 */
	protected CoppermineUploadPolicy(String postURL, int albumId, Applet theApplet, int debugLevel, JTextArea status) {
		//Let's call our mother !          :-)
		super(postURL, 1, theApplet, debugLevel, status);
		
		//The number of files per upload is always 1.
		int paramMaxFilesPerUpload = UploadPolicyFactory.getParameter(theApplet, PROP_NB_FILES_PER_REQUEST, -1);
		if (paramMaxFilesPerUpload != 1) {
			displayWarn(PROP_NB_FILES_PER_REQUEST + " is ignored in " + this.getClass().getName() + " (given value is: " + paramMaxFilesPerUpload);
		}

		//Now we explain her what we really want :
		this.albumId = albumId;
		stringUploadSuccess = "^SUCCESS$";
	}
	
	/**
	 * This method only handle the <I>albumId</I> parameter. The super.setProperty method 
	 * is then called.
	 * 
	 * @see wjhk.jupload2.policies.UploadPolicy#setProperty(java.lang.String, java.lang.String)
	 */
	public void setProperty(String prop, String value) {
		//In every case, transmission to the mother class.
		super.setProperty(prop, value);
		
		//The, we check our properties. 
		if (prop.equals(PROP_ALBUM_ID)) {
			albumId = UploadPolicyFactory.parseInt(value, 0);
		    displayInfo("Post URL = " + getPostURL());
		}
	}

	/**
	 * @see wjhk.jupload2.policies.UploadPolicy#getPostURL()
	 */
	public String getPostURL() {
		return postURL + "?cmd=add_picture&album=" + albumId;
	}
	
	/**
	 * @see UploadPolicy#getUploadFilename(FileData, int)
	 */
	public String getUploadFilename (FileData fileData, int index) {
		return "userpicture";
	}

	/**
	 * @see wjhk.jupload2.policies.UploadPolicy#isUploadReady()
	 *
	public boolean isUploadReady() {
		if (albumId <= 0) {
			alert("chooseAlbumFirst");
			return false;
		}
		// Default :  Let's ask the mother.
		return super.isUploadReady();
	}*/
	
	/**
	 * @see wjhk.jupload2.policies.UploadPolicy#afterUpload(FilePanel, Exception, String)
	 */
	public void afterUpload(FilePanel filePanel, Exception e, String serverOutput) {
        if(e == null){
        	try {
	        	//First : construction of the editpic URL :
	        	String editpicURL = postURL.substring(0,postURL.lastIndexOf('/')) 
						+ "/editpics.php?album=" + albumId;
			    
			    if (getDebugLevel() >= 100) {
				    alertStr ("No switch to property page, because debug level is " + getDebugLevel() + " (>=100)");
			    } else {
				    //Let's display an alert box, to explain what to do to the user: he will
				    //be redirected to the coppermine page that allow him to associate names
				    // and comments to the uploaded pictures.
				    alert ("coppermineUploadOk");
				    
				    //Let's change the current URL to edit names and comments, for the selected album. 
		        	//Ok, let's go and add names and comments to the newly updated pictures.
					JSObject applet = JSObject.getWindow(getApplet());
				    JSObject doc = (JSObject) applet.getMember("document");
				    JSObject loc = (JSObject) doc.getMember("location");
				    Object[] argsReplace = {editpicURL};
				    loc.call("replace", argsReplace);
			    }
        	} catch (JSException ee) {
        		//Oups, no navigator. We are probably in debug mode, within eclipse for instance.
        		displayErr(ee);
        	}
        }
	}

}
