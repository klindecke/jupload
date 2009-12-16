<?php
/**
 * This page is displayed by the applet, once all files are uploaded. You can add here any post-treatment you need.
 * 
 * This page content: just display the list of uploaded files. 
 */


session_start();

echo("<H3>List of session content</H3>");
foreach($_SESSION as $key => $value) {
	echo("$key: $value<BR>");
} 

echo("<H3>List of uploaded files</H3>");
$files = $_SESSION['juvar.files'];
echo ('Nb uploaded files is: ' . sizeof($files));
echo('<table border="1"><TR><TH>Filename</TH><TH>file size</TH><TH>Relative path</TH><TH>Full name</TH><TH>md5sum</TH><TH>Specific parameters</TH></TR>');
foreach ($files as $f) {
    echo('<tr><td>');
    echo($f['name']);
    echo('</td><td>');
    echo($f['size']);
    echo('</td><td>');
    echo($f['relativePath']);
    echo('</td><td>');
    echo($f['fullName']);
    echo('</td><td>');
    echo($f['md5sum']);
    $addBR = false;
	foreach ($f as $key=>$value) {
		//If it's a specific key, let's display it:
		if ($key != 'name' && $key != 'size' && $key != 'relativePath' && $key != 'fullName' && $key != 'md5sum') {
    		if ($addBR) {
    			echo('<br>');
    		} else {
    			// First line. We must add a new 'official' list separator.
			    echo('</td><td>');
    			$addBR = true;
    		}
			echo("$key => $value");
		}
	}
    echo('</td></tr>');
    echo("\n");
}
echo("</table>\n");

?>
<P><A HREF="index.php">Go back to the upload page</A></P>
