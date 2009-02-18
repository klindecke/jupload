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
// File croatian.php
// ------------------------------------------------------------------------- //
if (defined('JUPLOAD_PHP')) {
	$lang_jupload_php = array_merge (
		$lang_jupload_php,
		array(
		  'perm_denied' => 'Nedostaju prava za izvršenje.<BR><BR>Prijaviti se možete <a href="login.php' . ( isset($_SERVER['PHP_SELF']) ? '?referer=' . $_SERVER['PHP_SELF'] : '') . '">ovdje</a>.',
		  'select_album' => 'Molimo izaberite album u koji želite spremiti slike.',
		  'button_update_album' => 'Osvježi album',
		  'button_create_album' => 'Otvori novi album',
		  'success' => 'Uspješno izvršeno!',
		  'error_select_album' => 'Molim prvo izaberite album',
		  'error_album_name' => 'Molim upišite ime albuma.',
		  'error_album_already_exists' => 'Album sa tim imenom već postoji.<BR><BR>Molim kliknite izbornik <I>Nazad</I> u pregledniku te upišite drugo ime albuma.',
		  'album_name' => 'Naslov albuma',
		  'album_presentation' => 'Molim izaberite album. Datoteke koje su izabrane za slanje na server biti će pohranjene u ovaj album. <BR>Ako album još ne postoji, izaberite opciju \'Otvori novi album\'.',
		  'album_description' => 'Opis albuma',
		  'add_pictures' => 'Pridruži nove slike albumu',
		  'max_upload_size' => 'Najveća dopištena veličina slike iznosi $1 KB',
		  'upload_presentation' => 'Ukoliko se Applet ne prikaže i/ili preglednik vrati grešku, molim provjerite da li koristite zadnju verziju java runtime plugin-a.<BR>Nakon aktualiziranja applet će raditi znatno bolje. Za slanje slika na server kliknite na <B>Pretraži...</B> i izberite datoteke ili \'povucite\' datoteke mišem iz direktorija u applet. Nakon toga kliknite na <B>Pošalji</B> da biste pokrenuli slanje.'
		  	. "<BR>Ako želite koristiti <U>staru</U> funkciju slanja, kliknite molim <a href='upload.php'>ovdje</a>.",
		  'album' => 'album',
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