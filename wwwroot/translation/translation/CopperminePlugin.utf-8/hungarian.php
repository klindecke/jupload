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
 
 //     Hungarian translation:
 //     Gábor Rézsó
 //     gabor.rezso at gmail dot com
 
// ------------------------------------------------------------------------- //
// File hungarian.php
// ------------------------------------------------------------------------- //

if (defined('JUPLOAD_PHP')) {
	$lang_jupload_php = array_merge (
		$lang_jupload_php,
		array(
		  'link_title' => 'JUpload-Kép Feltöltés+',
		  'link_comment' => 'Képek feltöltése a galériába',
		  'perm_denied' => 'Nincs megfelelő jogosultságod.<BR><BR>Ha nem vagy belépve, <a href="$1">itt</a> megteheted',
		  'select_album' => 'Válassz albumot, ahova a képeket töltenéd',
		  'button_update_album' => 'Album frissítése',
		  'button_create_album' => 'Album létrehozása',
		  'success' => 'Sikeres művelet!',
		  'error_select_album' => 'Kérlek, először válassz albumot',
		  'error_album_name' => 'Kérlek, először adj nevet az albumnak.',
		  'error_album_already_exists' => 'Már van ilyen nevű album.<BR><BR>KLikk a <I>vissza</I> nyilra, új album név megadásához.',
		  'album_name' => 'Album név',
		  'album_presentation' => 'Válassz albumot. A feltöltött képek . <BR>Ha nincs még album létrehozva, a lista üres. Az Új album gomra kattintva hozhatod létre első albumodat.',
		  'album_description' => 'Album leírás',
		  'add_pictures' => 'Képek feltöltése a kijelölt albumba',
		  'max_upload_size' => 'A képek maximális mérete 1 KB',
		  'upload_presentation' => 'Ha az alábbi négyzetben nem töltődik be a program, ill a böngésző hibaüzenetet jelenít meg, ajánlatos újratelepíteni a java runtime plugin -t.<BR>A feltöltés, sokkal gyorsabb és könnyebb! Klikk <B>Tallóz</B> képek kiválasztásához vagy húzd bele a képeket az egérrel az intézőből, majd klikk <B>Feltölt</B> gombra a feltöltés elindításához.'
		. "<BR>a <U>régi feltöltési mód</U>, használatához <a href='upload.php'>Klikk ide</a>.",
		  'album' => 'Album',
		  //Since 2.1.0
		  'java_not_enabled' => 'Your navigator doesn\'t allow java. The upload applet need java. You can easily download it from the <a href="http:\\java.sun.com\jre\">java web site</a>',
		  //Since 3.0.0
		  'picture_data_explanation' => 'Klikk ide és add meg az alapvető kép információkat ha minden most feltöltött képre szeretnéd alkalmazni azokat.',
		  'quota_used' => 'Jelenleg $1 MB ($2%) tárhelyet használsz, a rendelkezésre álló $3 MB -ból.',
		  'quota_about_full' => 'Törölj pár képet vagy bővítsd a tárhelyet.',
		  //Since 3.2.0
		  'need_approval' => 'A galéria adminisztrátorának engedélyeznie kell a feltöltést, mielőtt megjelenne a galériában.'
		)
	);
}
