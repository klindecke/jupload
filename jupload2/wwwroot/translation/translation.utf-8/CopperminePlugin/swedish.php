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
 Maintainer: Erik Lindahl erik@fisensmosse.se
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
		  'link_comment' => 'Ladda upp bilder till galleriet med hjälp av appleten',
		  'perm_denied' => 'Du har inte behörighet att utföra åtgärden<BR><BR>Om du inte är ansluten, <a href="$1">logga in</a> först',
		  'select_album' => 'Välj ett album där du vill spara bilderna',
		  'button_update_album' => 'Uppdatera album',
		  'button_create_album' => 'Skapa album',
		  'success' => 'Utfört!',
		  'error_select_album' => 'Välj ett album först',
		  'error_album_name' => 'Namnge album',
		  'error_album_already_exists' => 'Du har redan ett album med de namnet.<BR><BR>Klicka på <I>båkåt</I> knappen i webbläsaren, för att välja ett annat namn.',
		  'album_name' => 'Album namn',
		  'album_presentation' => 'Du måste välja ett album här. Bilderna du laddar upp kommer sparas där.<BR>Om du inte har ett album är listan tom. Använd \'Skapa\' kanppen för att göra ett.',
		  'album_description' => 'Album förklaring',
		  'add_pictures' => 'Lägg till bilder i valt album',
		  'max_upload_size' => 'Största godkända storleken är $1 KB',
		  'upload_presentation' => 'Om rutan nedanför inte visar appleten och webbläsaren visar att det finns fel måste du installera en Java plugin<BR><BR> För att använda den <U>gamla sidan</U>, <a href="upload.php">tryck här</a>.',
		  'album' => 'Album',
		  //Since 2.1.0
		  'java_not_enabled' => 'Din webbläsare saknar Java, går att ladda ner från <a href="http:\\java.sun.com\jre\">http:\\java.sun.com\jre</a>',
		  //Since 3.0.0
		  'picture_data_explanation' => 'Klicka på länken och fyll i fälten nedanför. Om du vill att de ska gälla för alla filer nästa uppladdning.',
		  'quota_used' => 'Du använder förnärvarande $1 MB ($2%) av $3 MB.',
		  'quota_about_full' => 'Du måste frigöra minne, din kvot är fylld.',
		  //Since 3.2.0
		  'need_approval' => 'The gallery admin must approve these uploaded pictures, before you can see them on the gallery.'
		)
	);
}
