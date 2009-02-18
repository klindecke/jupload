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
		  'perm_denied' => 'U heft niet voldoende rechten voor deze actie.<BR><BR>Gelieve eerst in te loggen: <a href="$1">login</a>',
		  'select_album' => 'Selecteer een album waar u de nieuwe bestanden wilt plaatsen',
		  'button_update_album' => 'Album bijwerken',
		  'button_create_album' => 'Album aanmaken',
		  'success' => 'Gereed !',
		  'error_select_album' => 'Selecteer eerst een album',
		  'error_album_name' => 'Geef het album eerst een naam.',
		  'error_album_already_exists' => 'Deze naam is reeds in gebruik. <BR><BR>Druk op de <I>Terug</I> knop van uw browser, en voer een nieuwe naam in.',
		  'album_name' => 'Album naam',
		  'album_presentation' => 'U dient hier een album te selecteren. De geselecteerde afbeeldingen zullen aan dit album worden toegevoegd. <BR>Middels de knop \'Album aanmaken\' kunt u nieuwe albums aanmaken.',
		  'album_description' => 'Album omschrijving',
		  'add_pictures' => 'Afbeeldingen toevoegen aan het geselecteerde album',
		  'max_upload_size' => 'De maximale bestandsgrootte is $1 Ko',
		  'upload_presentation' => 'Als de applet niet weergegeven wordt, kan dit verholpen worden door de java runtime plugin te installeren.<BR>Vervolgens is uploaden zeer eenvoudig! Klik op <B>Bladeren</B> om bestanden te selecteren of gebruik de drag\'n\'drop functionaliteit van de verkenner. Klik vervolgens op <B>Upload</B> om afbeeldingen naar de server te verznden.'
		  	. "<BR>Om de originele <u>upload-functionaliteit</u> te gebruiken, <a href='upload.php'>klik hier</a>.",
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

