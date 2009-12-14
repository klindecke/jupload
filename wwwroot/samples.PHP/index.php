<?php
include 'jupload.php';

/**
 * This function will be called, once all files are uploaded, with the list of uploaded files as an argument.
 */
function handle_uploaded_files($files) {
	return "In handle_uploaded_files<BR>";
}

// Instantiate and initialize JUpload
new JUpload(
	//First: the applet parameters
    array(
        'maxFileSize' => '2G',
        //In the sourceforge project structure, the applet jar file is one folder below. Default
        //configuration is ok, if wjhk.jupload.jar is in the folder as the script containing this call. 
        'archive' => '../wjhk.jupload.jar',
        'debugLevel' => 99, // 100 disables redirect after upload, so we keep it below.
        //'lookAndFeel' => 'com.nilo.plaf.nimrod.NimRODLookAndFeel',
        //'archive' => 'wjhk.jupload.jar,NimRODLF-0.98b.jar',
    ),
    //Then: the jupload PHP class parameters
    array(
        //'demo_mode' => true,
        'allow_subdirs' => true,
        // The callbackAfterUploadManagement function will be called, once all files are uploaded, with the list
        //of uploaded files as an argument. See the above sample, and change it according to your needs.
        //'callbackAfterUploadManagement' => 'handle_uploaded_files',
        //I work on windows. The default configuration is /var/tmp/jupload_test
        'destdir' => 'c:/tmp'
        //'errormail' => 'me@my.domain.org',
    )
);
?>
<html>
  <head>
    <!--JUPLOAD_JSCRIPT-->
    <title>JUpload Example Page</title>
  </head>
  <body>
    <h1 align="center">JUpload Example Page</h1>
    <div align="center"><!--JUPLOAD_FILES--></div>
    <div align="center"><!--JUPLOAD_APPLET--></div>
  </body>
</html>