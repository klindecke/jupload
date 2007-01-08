/*
 * Created on 7 mai 2006
 */
package wjhk.jupload2.policies;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import wjhk.jupload2.JUploadApplet;
import wjhk.jupload2.exception.JUploadException;
import wjhk.jupload2.exception.JUploadExceptionUploadFailed;
import wjhk.jupload2.filedata.FileData;

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
 * <LI>If an error occurs, the applet asks the user if he wants to send a mail to the webmaster. If he answered yes,
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
	 * @param theApplet Identifier for the current applet. It's necessary, to read information from the navigator.
	 */
	public CoppermineUploadPolicy(JUploadApplet theApplet) {
		//Let's call our mother !          :-)
		super(theApplet);
		
		//Let's read the albumId from the applet parameter. It can be unset, but the user must then choose
		//an album before upload.
	    albumId = UploadPolicyFactory.getParameter(theApplet, PROP_ALBUM_ID, DEFAULT_ALBUM_ID);
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
		//The jupload.phg script gives the upload php script that will receive the uploaded files.
		//It can be xp_publish.php, or (much better) jupload.php.
		//In either case, the postURL given to the applet contains already one paramete: the cmd (for xp_publish) or
		//the action (for jupload). We just add one parameter.
		//Note: if the postURL (given to the applet) doesn't need any parameter, it's necessary to add a dummy one, 
		//      so that the line below generates a valid URL.
		return postURL + "&album=" + albumId;
	}
	
	/**
	 * @see UploadPolicy#getUploadName(FileData, int)
	 */
	public String getUploadName (FileData fileData, int index) {
		return "userpicture";
	}

	/**
	 * @see wjhk.jupload2.policies.UploadPolicy#isUploadReady()
	 */
	public boolean isUploadReady() {
		if (albumId <= 0) {
			alert("chooseAlbumFirst");
			return false;
		}
		// Default :  Let's ask the mother.
		return super.isUploadReady();
	}
	
	/**
	 * The default behaviour (see {@link DefaultUploadPolicy}) is to check that the stringUploadSuccess applet 
	 * parameter is present in the request. The return is :
	 * <DIR>
	 * <LI>True, if the stringUploadSuccess string is present in the serverOutputBody.
	 * <LI>True, If previous condition is not filled, but the HTTP header "HTTP(.*)200OK$" is present: the test is
	 *   currently non blocking, because I can not test all possible HTTP configurations.<BR>
	 *   Note: If "Transfer-Encoding: chunked" is present, the body may be cut by 'strange' characters, which prevent 
	 *   to find the success string. Then, a warning is displayed.
	 * <LI>False if the previous conditions are not fullfilled.
	 * </DIR>     
	 * 
	 * @param serverOutput The full HTTP answer, including the http headers. 
	 * @param serverOutputBody The body of the HTTP answer.
	 * @return True or False, indicating if the upload is a success or not.
	 * 
	 * @see UploadPolicy#checkUploadSuccess(String, String)
	 */
	public boolean checkUploadSuccess(String serverOutput, String serverOutputBody) throws JUploadException {
		final Pattern patternSuccess = Pattern.compile(stringUploadSuccess);
		final Pattern patternTransferEncodingChunked = Pattern.compile("^Transfer-Encoding: chunked", Pattern.CASE_INSENSITIVE);
		//La première ligne est de la forme "HTTP/1.1 NNN Texte", où NNN et le code HTTP de retour (200, 404, 500...)
		final Pattern patternHttpStatus = Pattern.compile("HTTP[^ ]* ([^ ]*) .*", Pattern.DOTALL);
		
		//The success string should be in the http body
		boolean uploadSuccess = patternSuccess.matcher(serverOutputBody).find();
		//The transfert encoding may be present in the serverOutput (that contains the http headers)
		boolean uploadTransferEncodingChunked = patternTransferEncodingChunked.matcher(serverOutput).find();
		
		/////////////////////////////////////////////////////////////////////////////////////
		//Changes from the DefaultUploadPolicy code : START (1/2)
		final Pattern patternError = Pattern.compile("ERROR: (.*)");
		Matcher matcherError = patternError.matcher(serverOutputBody);
		String errorMessage = null;
		if (matcherError.find()) {
			try {
				errorMessage = matcherError.group(1);
			} catch (IndexOutOfBoundsException e) {
				//Too bad, the RegExp didn't find any error message.
				errorMessage = "An error occurs during upload (but the applet couldn't find the error message)";
			}
		}		
		//Changes from the DefaultUploadPolicy code : END (1/2)
		/////////////////////////////////////////////////////////////////////////////////////
		
		//And have a match, to search for the http return code (200 for Ok)
		Matcher matcherUploadHttpStatus = patternHttpStatus.matcher(serverOutput);
		if (!matcherUploadHttpStatus.matches()) {
			throw new JUploadException("Can't find the HTTP status in serverOutput!");
		} else {
			int httpStatus = Integer.parseInt(matcherUploadHttpStatus.group(1));
			boolean upload_200_OK = (httpStatus == 200);
			
			displayDebug("HTTP return code: " + httpStatus, 40);
	
			//Let's find what we should answer:
			if (uploadSuccess) {
				//Everything is Ok, we leave here.
				return true;
			/////////////////////////////////////////////////////////////////////////////////////
			//Changes from the DefaultUploadPolicy code : START (2/2)
			} else if (errorMessage != null) {
				if (errorMessage.equals("")) {
					errorMessage = "An unknown error occurs during upload."; 
				}
				throw new JUploadExceptionUploadFailed (getClass().getName() + ".checkUploadSuccess(): " + errorMessage);
			//Changes from the DefaultUploadPolicy code : END (2/2)
			/////////////////////////////////////////////////////////////////////////////////////
			} else if (uploadTransferEncodingChunked && upload_200_OK) {
				//Hum, as the transfert encoding is chuncked, the success string may be splitted. We display 
				//an info message, and expect everything is Ok.
				//FIXME The chunked encoding should be correctly handled, instead of the current 'expectations' below. 
				displayInfo("The transertEncoding is chunked, and http upload is technically Ok, but the success string was not found. Suspicion is that upload was Ok...let's go on");
				return true;
			} else if (upload_200_OK) {
				//This method is currently non blocking.
				displayWarn("The http upload is technically Ok, but the success string was not found. Suspicion is that upload was Ok...let's go on");
				//We raise no exception (= success)
				return true;
			} else {
				//The upload is not successful: here, we know it!
				throw new JUploadExceptionUploadFailed (getClass().getName() + ".checkUploadSuccess(): The http return code is : " + httpStatus + " (should be 200)");
			}
		}
	}//isUploadSuccessful

	/**
	 * @see wjhk.jupload2.policies.UploadPolicy#afterUpload(Exception, String)
	 */
	public void afterUpload(Exception e, String serverOutput) {
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
