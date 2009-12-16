<?php
include 'jupload.php';

/**
 * WHAT IS THIS FILE ?
 * 
 * This file is a PHP, to show how to use the JUpload applet. All complexity is embedded in the PHP JUpload class, included in the 
 * jupload.php file.  
 * 
 * 
 * HOWTO USE IT ?
 *  The jupload.php file should be left unchanged, so that any change to manage new functinnality in the applet is simply get by using the
 * up to date jupload.php file.
 *  On the contrary: this file is aimed as a very simple demo of HOW SIMPLE it is, to embed the applet into your web site. You can update this
 * file the way you want, to give tries to the applet. Then integrate it into
 *  Also, the after_upload.php script is a way to manage uploaded files, in a different page from the applet one. Check the afterUploadURL
 * applet parameter, here below. 
 * 
 * You can change:
 * - The list of applet parameters. All available applet parameters are described here: http://jupload.sourceforge.net/howto-customization.html 
 * - The list of class parameters, to personnalize the way the PHP class manages the uploaded files. You can check the list of available class
 * parameters in the JUpload method of the PHP JUpload class, in the jupload.php script. Their name is quite clear.
 */


////////////////////////////////////////////////////////////////////////////////////////////////////////
///////////////   The user callback function, that can be called after upload   ////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////
/**
 * This function will be called, once all files are uploaded, with the list of uploaded files as an argument.
 * 
 * Condition to have this function called:
 * - Have the applet parameter afterUploadURL unset in this file. This makes the applet use its default behavior, that is: afterUploadURL is
 *  the current web page, with the ?afterupload=1 parameter added.
 * - Have the class parameter callbackAfterUploadManagement set to 'handle_uploaded_files', name of this callback. You can use any name you want,
 *  but the function must accept one unique parameter: the array that contains the file descriptions.
 * 
 * @param $juploadPhpSupportClass The instance of the JUpload PHP class.
 * @param $file The array wich contains info about all uploaded files. 
 */
function handle_uploaded_files($juploadPhpSupportClass, $files) {
	return 
		"<P>We are in the 'handle_uploaded_files' callback function, in the index.php script. To avoid double coding, we "
		. "just call the default behavior of the JUpload PHP class. Just replace this by your code...</P>"
		. $juploadPhpSupportClass->defaultAfterUploadManagement();
		;
	
}
////////////////////////////////////////////////////////////////////////////////////////////////////////



//First: the applet parameters
//
// Default value should work nice on most configuration. In this sample, we use some specific parameters, to show 
// how to use this array.
// See comment for the parameters used on this demo page.
//
// You can use all applet parameters in this array.
// see all details http://jupload.sourceforge.net/howto-customization.html 
//
$appletParameters = array(
		//Default value is ... maximum size for a file on the current FS. 2G is problably too much already.
        'maxFileSize' => '2G',
        //
        //In the sourceforge project structure, the applet jar file is one folder below. Default
        //configuration is ok, if wjhk.jupload.jar is in the same folder as the script containing this call. 
        'archive' => '../wjhk.jupload.jar',
        //To manage, other jar files, like the ftp jar files if postURL is an FTP URL:
        //'archive' => 'wjhk.jupload.jar,jakarta-commons-oro.jar,jakarta-commons-net.jar',
        //
        //Default afterUploadURL displays the list of uploaded files above the applet (in the <!--JUPLOAD_FILES--> markers, see below)
        //You can use any page you want, to manage the uploaded files. Here is a sample, that also only shows the list of files.
        'afterUploadURL' => 'after_upload.php', 
        //
        'debugLevel' => 99 // 100 disables redirect after upload, so we keep it below. This still gives a lot of information, in case of problem.
    );

//
//Then: the jupload PHP class parameters

$classParameters = array(
		//Files won't be stored on the server. Useful for first tests of the applet behavior ... and sourceforge demo site !
        'demo_mode' => true,
        //
        //Allow creation of subdirectories, when uploading several folders/files (drag and drop a folder on the applet to use it).
        'allow_subdirs' => true,
        //
        // The callbackAfterUploadManagement function will be called, once all files are uploaded, with the list
        //of uploaded files as an argument. See the above sample, and change it according to your needs.
        //'callbackAfterUploadManagement' => 'handle_uploaded_files',
        //
        //I work on windows. The default configuration is /var/tmp/jupload_test
        'destdir' => 'c:/tmp'  //Where to store the files on the web 
        //'errormail' => 'me@my.domain.org',
    );

////////////////////////////////////////////////////////////////////////////////////////////////////////
// Instantiate and initialize JUpload : integration of the applet in your web site.
$juploadPhpSupportClass = new JUpload($appletParameters, $classParameters);
////////////////////////////////////////////////////////////////////////////////////////////////////////



//Then, a simple HTML page, for the demo
//
// "<!--JUPLOAD_FILES-->" is the tag where the list of uploaded files will be written.
// "<!--JUPLOAD_APPLET-->" is the place where the applet will be integrated, in the web page.
?>
<html>
  <head>
    <!--JUPLOAD_JSCRIPT-->
    <title>JUpload PHP Sample Page</title>
  </head>
  <body>
    <h1 align="center">JUpload PHP Sample Page</h1>
    <div align="center"><!--JUPLOAD_FILES--></div>
    <div align="center"><!--JUPLOAD_APPLET--></div>
  </body>
</html>