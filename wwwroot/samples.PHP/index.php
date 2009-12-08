<?php
include 'jupload.php';

// Instantiate and initialize JUpload
new JUpload(
    array(
        'maxFileSize' => '2G',
        //'lookAndFeel' => 'com.nilo.plaf.nimrod.NimRODLookAndFeel',
        //'archive' => 'wjhk.jupload.jar,NimRODLF-0.98b.jar',
        //'debugLevel' => 99, // 100 disables redirect after upload, so we keep it below.
    ),
    array(
        //'errormail' => 'me@my.domain.org',
        'allow_subdirs' => true,
        //'demo_mode' => true,
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