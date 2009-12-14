<?php
/**
 * This page is displayed by the applet, once all files are uploaded. You can add here any post-treatment you need.
 * 
 * This page content: just display the list of uploaded files. 
 */


Not finished at all .....



session_start();

echo("List of session content:<BR>");
foreach($_SESSION as $key => $value) {
	echo("$key: $value<BR>");
} 

?>
<P><A HREF="index.php">Go back to upload page</A></P>
