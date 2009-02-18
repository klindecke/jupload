<?php
/*
 *******************************************
 plugin JUpload for Coppermine Photo Gallery
 *******************************************

 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2 of the License, or
 (at your option) any later version.
 ********************************************
 $Revision: 185 $
 $Author: etienne_sf $
 $Date: 2008-03-12 20:26:16 +0100 (mer., 12 mars 2008) $
 ********************************************
 *
 * Allows easy upload to the gallery, through a java applet. 
 * 
 * Up to date version of this script can be retrieved with the full JUpload package, here:
 * 
 * http://etienne.sf.free.fr/wiki
 * 
 * Directly here:
 * http://etienne.sf.free.fr/wiki/doku.php?id=jupload_coppermine_download_gb
 * 
 * Support is available on this forum:
 * http://coppermine-gallery.net/forum/index.php?topic=43432
 * 
 * The applet is published on sourceforge:
 * http://jupload.sourceforge.net
 * 
 */

// ------------------------------------------------------------------------- //
// File jupload.php
// ------------------------------------------------------------------------- //

if (defined('JUPLOAD_PHP')) {
	$lang_jupload_php = array_merge (
		$lang_jupload_php,
		array(
		  'link_title' => 'JUpload',
		  'link_comment' => 'Upload files to the gallery, with the help of an applet',
		  'perm_denied' => 'You don\'t have permission to perform this operation.<BR><BR>If you\'re not connected, please <a href="$1">login</a> first',
		  'select_album' => 'Please, choose an album, where you want to upload pictures',
		  'button_update_album' => 'Update album',
		  'button_create_album' => 'Create album',
		  'success' => 'Action success !',
		  'error_select_album' => 'Please, choose an album first',
		  'error_album_name' => 'Please give a name to the album.',
		  'error_album_already_exists' => 'You already own an album with this name.<BR><BR>Please click on the <I>Back</I> button of your navigator, to type another title for your new album.',
		  'album_name' => 'Album name',
		  'album_presentation' => 'You must select an album here. The pictures you\'ll send to the server will be stored in this album. <BR>If you don\'t have any album, the album list is empty. Use the \'Create\' button to create your first album.',
		  'album_description' => 'Album description',
		  'add_pictures' => 'Add pictures to the selected album',
		  'max_upload_size' => 'The maximum size for a picture is $1 KB',
		  'upload_presentation' => 'If the square below definitely refuses to display the applet, and the navigator indicates that there are errors on this page, a good idea would be to install the java runtime plugin.<BR>After, upload is really simple! Click on <B>Browse</B> to select files or use drag\'n\'drop from the explorer, then click on <B>Upload</B> to send the pictures to the server.'
		. "<BR>To use the <U>old upload page</U>, <a href='upload.php'>click here</a>.",
		  'album' => 'Album',
		  //Since 2.1.0
		  'java_not_enabled' => 'Your navigator doesn\'t allow java. The upload applet need java. You can easily download it from the <a href="http:\\java.sun.com\jre\">java web site</a>',
		  //Since 3.0.0
		  'picture_data_explanation' => 'Click on this link, and enter data in the fields below, if you want these to be applied to all pictures in the next upload.',
		  'quota_used' => 'You are currently using $1 MB ($2%) of your $3 MB of storage.',
		  'quota_about_full' => 'Remove some pictures, or ask the admin to make your quota bigger.',
		  //Since 3.2.0
		  'need_approval' => 'The gallery admin must approve these uploaded pictures, before you can see them on the gallery.'
		)
	);
}
