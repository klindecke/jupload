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
		  'perm_denied' => 'Sie haben keine Berechtigung diesen Vorgang durchzuführen.<BR><BR>Bitte <a href="$1">melden</a> Sie sich zuerst an.',
		  'select_album' => 'Bitte wählen Sie ein Album, in welches Sie Dateien hochladen möchten',
		  'button_update_album' => 'Album aktualisieren',
		  'button_create_album' => 'Album erstellen',
		  'success' => 'Aktion erfolgreich!',
		  'error_select_album' => 'Bitte zuerst ein Album auswählen',
		  'error_album_name' => 'Bitte geben Sie dem Album einen Namen.',
		  'error_album_already_exists' => 'Sie haben bereits ein Album mit diesem Namen.<BR><BR>Bitte klicken Sie auf den <I>Zurück</I> Button ihres Browsers um einen anderen Titel zu wählen.',
		  'album_name' => 'Album Titel',
		  'album_presentation' => 'Bitte wählen Sie hier ein Album aus. Die Datei, die Sie hochladen möchten, wird in diesem Album gespeichert. <BR>Wenn Sie noch kein Album erstellt haben, dann ist diese Liste leer. Benutzen Sie dann die \'Album erstellen\' Funktion um ihr erstes Album zu erzeugen.',
		  'album_description' => 'Album Beschreibung',
		  'add_pictures' => 'Dateien dem ausgewählten Album hinzufügen.',
		  'max_upload_size' => 'Die maximal zulässige Größe einer Datei beträgt $1 Ko',
		  'upload_presentation' => 'Wenn in der unteren Box nichts angezeigt wird und/oder der Browser einen Fehler auf der Seite anzeigt, dann prüfen Sie bitte ob Sie die neueste Version des Java runtime plugins installiert haben.<BR>Danach gestaltet sich der Upload-Vorgang wesentlich komfortabler! Klicken Sie auf <B>Durchsuchen</B> um die Dateien auszuwählen oder fügen Sie die Dateien per drag\'n\'drop aus ihrem Ordner ein. Anschließend klicken Sie bitte auf <B>Hochladen</B> um die Dateien an den Server zu übertragen.'
		  	. "<BR>Um die <U>alte Datei hochladen-Funktion</U> zu nutzen, klicken Sie bitte <a href='upload.php'>hier</a>.",
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
