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

/**
 * Specific UploadPolicy for the coppermine picture gallery.
 * <BR>
 * Wecific features for this policy are:
 * <UL>
 * <LI> Cookies handling: the current cookies are read from the navigator, and sent in the
 * upload HTTP request. The upload is done within the current coppermine session.
 * <LI> Album handling : the setProperty("albumId", n) can be called from javascript, when the user 
 * selects another album (with n is the numeric id for the selected album). This needs that the MAYSCRIPT HTML 
 * parameter is set, in the APPLET tag (see the example below). The upload can not start if the user didn't first 
 * select an album.
 * <LI> File by file upload (one file by HTTP Request). Uploaded files are sent to the coppermine's xp_publish.php script.
 * <BR><BR>
 * <B>Example 1: Call of the applet from a php script in coppermine.</B> 
 * <XMP>
 * <?php
 	  $URL = $CONFIG['site_url'] . 'xp_publish.php';
 	  $lang = $lang_translation_info['lang_country_code'];
 	  $max_upl_width_height = $CONFIG['max_upl_width_height'];
 	  $uploadPolicy = "CoppermineUploadPolicy";
  ?>
      <SCRIPT LANGUAGE="JavaScript"><!--
          if (_ie == true) document.writeln('<OBJECT classid="clsid:8AD9C840-044E-11D1-B3E9-00805F499D93" NAME="JUpload" WIDTH = "640" HEIGHT = "300"  codebase="http://java.sun.com/update/1.4.2/jinstall-1_4-windows-i586.cab#Version=1,4,0,0"><NOEMBED><XMP>');
          else if (_ns == true && _ns6 == false) document.writeln('<EMBED \
      	    type="application/x-java-applet;version=1.4" \
                  CODE = "wjhk.jupload.JUploadApplet" \
                  ARCHIVE = "wjhk.jupload.jar" \
                  WIDTH = "640" \
                  HEIGHT = "300" \
                  postURL = "$URL" \
                  uploadPolicy = "$uploadPolicy" \
                  albumId = "$album_id" \
      	    scriptable=false \
      	    pluginspage="http://java.sun.com/products/plugin/index.html#download"><NOEMBED><XMP>');
      //--></SCRIPT>
        <APPLET  
          CODE="wjhk.jupload.JUploadApplet" 
          NAME="JUpload"
          ARCHIVE="wjhk.jupload.jar" 
          WIDTH="640" 
          HEIGHT="300"
          MAYSCRIPT>&lt;/XMP&gt;
          <PARAM NAME="CODE"       VALUE="wjhk.jupload.JUploadApplet" >
          <PARAM NAME="ARCHIVE"    VALUE="wjhk.jupload.jar" >
          <PARAM NAME="type"       VALUE="application/x-java-applet;version=1.4">
          <PARAM NAME="scriptable" VALUE="false">
                      
          <PARAM NAME="uploadPolicy" VALUE="$uploadPolicy">
          <PARAM NAME="albumId"      VALUE="$album_id">
          <PARAM NAME="postURL"      VALUE="$URL">
          <PARAM NAME="lang"         VALUE="$lang">
          <PARAM NAME="maxPicHeight" VALUE="$max_upl_width_height">
          <PARAM NAME="maxPicWidth"  VALUE="$max_upl_width_height">
          <PARAM NAME="debugLevel"   VALUE="0">
      
      Java 1.4 or higher plugin required.
      </APPLET>
      </NOEMBED>
      </EMBED>
      </OBJECT>
      <!--"END_CONVERTED_APPLET"-->
  </XMP>
 * <BR><BR>
 * <B>Example 2: albumId set by a javascript call.</B>
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
	 * @param postURL
	 */
	protected CoppermineUploadPolicy(String postURL, int albumId, Applet theApplet, int debugLevel, JTextArea status) {
		//Let's call our mother !          :-)
		super(postURL, 1, theApplet, debugLevel, status);

		this.albumId = albumId;
	}
	
	/**
	 * @see wjhk.jupload2.policies.UploadPolicy#setProperty(java.lang.String, java.lang.String)
	 */
	public void setProperty(String prop, String value) {
		if (prop.equals(PROP_ALBUM_ID)) {
			albumId = UploadPolicyFactory.parseInt(value, 0);
		    displayInfo("Post URL = " + getPostURL());
		}
		//In every case, transmission to the mother class.
		super.setProperty(prop, value);
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
	 */
	public boolean isUploadReady() {
		if (albumId <= 0) {
			//TODO Put a java MessageBox instead of a javascript one. 
			//This works Ok within an applet, but won't work within a java application.
			JSObject applet = JSObject.getWindow(getApplet());
		    JSObject win = (JSObject) applet.getMember("window");
		    Object[] args = {getString("chooseAlbumFirst")};
		    win.call("alert", args);
			return false;
		}
		// Default :  Let's ask the mother.
		return super.isUploadReady();
	}
	/**
	 * @see wjhk.jupload2.policies.UploadPolicy#afterUpload(FilePanel, Exception, String)
	 */
	public void afterUpload(FilePanel filePanel, Exception e, String serverOutput) {
        if(e != null){
        	afterUpload(filePanel, e, serverOutput);          
        } else {
        	try {
	        	//First : construction of the editpic URL :
	        	String editpicURL = postURL.substring(0,postURL.lastIndexOf('/')) 
						+ "/editpics.php?album=" + albumId;
	        	//Ok, let's go and add names and comments to the newly updated pictures.
				JSObject applet = JSObject.getWindow(getApplet());
			    JSObject win = (JSObject) applet.getMember("window");
			    JSObject doc = (JSObject) applet.getMember("document");
			    JSObject loc = (JSObject) doc.getMember("location");
			    
			    if (getDebugLevel() >= 100) {
				    Object[] argsAlert = {"No switch to property page, because debug level is " + getDebugLevel() + " (>=100)"};
				    win.call("alert", argsAlert);
			    } else {
				    //Let's display an alert box, to explain what to do to the user: he will
				    //be redirected to the coppermine page that allow him to associate names
				    // and comments to the uploaded pictures.
				    Object[] argsAlert = {getString("coppermineUploadOk")};
				    win.call("alert", argsAlert);
				    
				    //Let's change the current URL to edit names and comments, for the selected album. 
				    Object[] argsReplace = {editpicURL};
				    loc.call("replace", argsReplace);
			    }
        	} catch (JSException ee) {
        		//Oups, we must be in debug mode : no navigator ?
        		displayErr(ee);
        	}
        }
	}

}
