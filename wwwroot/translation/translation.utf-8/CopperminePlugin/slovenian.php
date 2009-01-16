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


// Thanks to Wojtek Semik: wojteks at pvd dot pl

if (defined('JUPLOAD_PHP')) {
	$lang_jupload_php = array_merge (
		$lang_jupload_php,
		array(
		  'perm_denied' => 'Nimaš pooblastil za izvedbo tega opravila.<BR><BR>Če ni povezave, se najprej <a href="login.php' . ( isset($_SERVER['PHP_SELF']) ? '?referer=' . $_SERVER['PHP_SELF'] : '') . '">prijavi</a>',
		  'select_album' => 'Prosim, izberi album, v katerega želiš naložiti slike',
		  'button_update_album' => 'Osveži album',
		  'button_create_album' => 'Ustvari album',
		  'success' => 'Postopek je uspel!',
		  'error_select_album' => 'Prosim, najprej izberi album',
		  'error_album_name' => 'Prosim, poimenuj album.',
		  'error_album_already_exists' => 'V galeriji že imaš album s tem imenom.<BR><BR>Prosim, klikni gumb <I>Nazaj</I> v svojem brskalniku in vpiši drugo ime za svoj novi album.',
		  'album_name' => 'Ime albuma',
		  'album_presentation' => 'Izbrati moraš album, v katerega želiš naložiti slike. <BR>Če albumov ni, je seznam prazen. Uporabi gumb \'Ustvari album\' in ustvari svoj prvi album.',
		  'album_description' => 'Opis albuma',
		  'add_pictures' => 'Dodaj slike v izbrani album',
		  'max_upload_size' => 'Največja dovoljena velikost slike je $1 KB',
		  'upload_presentation' => 'Če se v spodnjem polju ne pokaže programček, brskalnik pa opozarja na napako na strani, moraš namestiti <a href=\'http://www.java.com\'>javo</a>.<BR>Uporaba je preprosta! Klikni <B>Išči</B> in izberi datoteke ali pa uporabi \'povleci in spusti\' v raziskovalcu ter nato klikni <B>Naloži</B> in pošlji slike na strežnik.'
		        . "<BR><U>Staro stran za nalaganje</U>, <a href='upload.php'>najdeš tukaj</a>.",
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
