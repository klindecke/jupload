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
// File jupload.php   in Danish
// ------------------------------------------------------------------------- //

if (defined('JUPLOAD_PHP')) {
	$lang_jupload_php = array_merge (
		$lang_jupload_php,
		array(
		  'perm_denied' => 'Du har ikke tilladelse til at udføre denne operation. BR><BR>Hvis du ikke er forbundet, så <a href="login.php' . ( isset($_SERVER['PHP_SELF']) ? '?referer=' . $_SERVER['PHP_SELF'] : '') . '">login</a> først',
		  'select_album' => 'Vælg venligst et album, hvori du vil uploade billeder',
		  'button_update_album' => 'Opdater album',
		  'button_create_album' => 'Opret album',
		  'success' => 'Handlingen lykkedes!',
		  'error_select_album' => 'Vælg venligst et album først',
		  'error_album_name' => 'Giv venligst albummet et navn.',
		  'error_album_already_exists' => 'Du ejer allerede et album med dette navn.<BR><BR>Klik venligst på <I>Tilbage</I>-knappen i din browser for at skrive en titel på dit nye album.',
		  'album_name' => 'Albumnavn',
		  'album_presentation' => 'Du skal vælge et album her. Billederne du sender til serveren, vil blive gemt i dette album. <BR>Hvis du ikke har et album, er albumlisten tom. Brug ‘Opret’-knappen til at oprette dit første album.',
		  'album_description' => 'Albumbeskrivelse',
		  'add_pictures' => 'Tilføj billeder til det valgte album',
		  'max_upload_size' => 'Den maximale størrelse for et billede er $1 Kb',
		  'upload_presentation' => 'Hvis firkanten nedenfor fuldstænding nægter at vise appletten, og navigatoren indikerer at der er fejl på denne side, er det en god ide at installere java runtime plugin-et.<BR>Når det er gjort, er upload virkelig enkelt ! Klik på <B>Søg...</B> for at vælge filer eller brug træk-og-slip fra stifinderen. Klik så på <b>Upload</b> for at sende billederne til serveren.'
		  	. "<BR>For at bruge den <U>gamle uploadside</U>, <a href='upload.php'>klik her</a>.",
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
