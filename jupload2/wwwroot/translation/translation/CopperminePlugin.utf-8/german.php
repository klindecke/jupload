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
// File german.php
// ------------------------------------------------------------------------- //
if (defined('JUPLOAD_PHP')) {
	$lang_jupload_php = array_merge (
		$lang_jupload_php,
		array(
		  'perm_denied' => 'Du hast keine Berechtigung für diesen Vorgang.<BR><BR>Bitte <a href="$1">melde</a> Dich zuerst an.',
		  'select_album' => 'Bitte wähle ein Album aus, in das Du Dateien hochladen möchtest',
		  'button_update_album' => 'Album aktualisieren',
		  'button_create_album' => 'Album erzeugen',
		  'success' => 'Vorgang erfolgreich!',
		  'error_select_album' => 'Bitte zuerst ein Album auswählen',
		  'error_album_name' => 'Bitte gib dem Album einen Namen.',
		  'error_album_already_exists' => 'Du hast bereits ein Album mit diesem Namen.<BR><BR>Bitte klicke auf den <I>Zurück</I> Button Deines Browsers um einen anderen Titel zu wählen.',
		  'album_name' => 'Album Titel',
		  'album_presentation' => 'Bitte wähle ein Album aus. Die Dateien, die Du hochladen möchtest, werden in diesem Album gespeichert. <BR>Wenn Du noch kein Album erstellt hast, dann benutze bitte die \'Album erzeugen\' Funktion um Dein erstes Album zu erstellen.',
		  'album_description' => 'Album Beschreibung',
		  'add_pictures' => 'Dateien dem Album hinzufügen',
		  'max_upload_size' => 'Die maximal zulässige Größe einer Datei beträgt $1 KB',
		  'upload_presentation' => 'Wenn die unteren Box leer bleibt und/oder der Browser einen Fehler auf der Seite anzeigt, dann prüfe bitte, ob Du die neueste Version des Java runtime plugins installiert hast.<BR>Um Dateien hochzuladen klicke auf <B>Durchsuchen</B> und wähle die Dateien aus oder füge sie per drag\'n\'drop aus Deinem Ordner ein. Anschließend auf <B>Hochladen</B> klicken um die Dateien an den Server zu übertragen.'
		  	. "<BR>Um die <U>alte</U> Datei hochladen-Funktion zu nutzen, klicke bitte <a href='upload.php'>hier</a>.",
		  'album' => 'Album',
		  //Since 2.1.0
		  'java_not_enabled' => 'Dein Browser erlaubt kein java. Das Jupload Applet braucht java. Du kannst es unter <a href="http:\\java.sun.com\jre\">java web site</a> kostenlos herunterladen',
		  //Since 3.0.0
		  'picture_data_explanation' => 'Klicke auf diesen Link und gib die gewünschten Daten in die Felder ein wenn Du sie zu allen Bildern des nächsten Uploads hinzufügen möchtest.',
		  'quota_used' => 'Du nutzt momentan $1 MB ($2%) von maximal $3 MB Speicher.',
		  'quota_about_full' => 'Entferne einige Bilder oder bitte den Administrator Deinen Speicherplatz zu erhöhen.',
		  //Since 3.2.0
		  'need_approval' => 'Der Galerieadministrator muss die hochgeladenen Bilder genehmigen und freischalten, bevor sie gesehen werden können.'
		)
	);
}
