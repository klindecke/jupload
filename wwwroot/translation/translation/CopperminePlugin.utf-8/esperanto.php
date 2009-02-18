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
		  'perm_denied' => 'Vi ne rajtas fari tion.<br><br>If vi ne estas konektita, bonvolu <a href="$1">ensaluti</a> unue',
		  'select_album' => "Bonvolu elekti fotoalbumon, kie vi volas al\u015Duti fotojn",
		  'button_update_album' => "\u011Cidatigu albumon",
		  'button_create_album' => 'Kreu albumon',
		  'success' => 'Sukceso!',
		  'error_select_album' => 'Bonvolu elekti fotoalbumon unue',
		  'error_album_name' => 'Bonvolu nomi la fotoalbumon.',
		  'error_album_already_exists' => 'Vi jam havas albumon kun tiu nomo.<br><br>Bonvolu klaki sur la <i>Reen</i>-butono de via retumilo, por tajpi alian titolon por via nova fotoalbumo.',
		  'album_name' => 'Albuma nomo',
		  'album_presentation' => "Vi devas elekti albumon \u0109i tie.  La bildoj kiujn vi sendis al la servilo konservi/u011Dos en \u0109i tiu albumo.<br>Se vi ne havas albumon, la albuma listo estas malplena. Uzu la 'Kreu'-butonon por krei vian unuan albumon.",
		  'album_description' => 'Albuma priskribo',
		  'add_pictures' => 'Aldonu bildojn al la elektita albumo',
		  'max_upload_size' => 'La maksimuma grandeco por bildo estas $1 Kb',
		  'upload_presentation' => "Se la kvadrato sube rifuzas montri la apleton, kaj la retumilo indikas ke estas eraroj sur la pa/u011Do, bona ideo estus instali la \"Java Runtime Plugin\".<br>Poste, al\u015Dutado simplos! Klaku sur <b>Foliumi</b> por elekti dosierojn a\u016D uzu \"klaktenu kaj metu\" de la retumilo, tiam klaku sur <b>Al\u015Duti</b> por sendi la bildojn al la servilo."
		  . "<br>Por uzi la <u>malnova al\u015Duta pa/u011Do</u>, <a href='upload.php'>klaku \u0109i tie</a>.",
		  'album' => 'Albumo',
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
