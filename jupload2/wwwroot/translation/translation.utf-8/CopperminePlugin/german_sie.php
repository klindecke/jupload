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
		  'success' => 'Vorgang erfolgreich!',
		  'error_select_album' => 'Bitte zuerst ein Album auswählen',
		  'error_album_name' => 'Bitte geben Sie dem Album einen Namen.',
		  'error_album_already_exists' => 'Sie haben bereits ein Album mit diesem Namen.<BR><BR>Bitte klicken Sie auf den <I>Zurück</I> Button ihres Browsers um einen anderen Titel zu wählen.',
		  'album_name' => 'Album Titel',
		  'album_presentation' => 'Bitte wählen Sie ein Album. Die Dateien, die Sie hochladen möchten, werden in diesem Album gespeichert. <BR>Wenn Sie noch kein Album erstellt haben, dann benutzen Sie bitte die \'Album erzeugen\' Funktion um ihr erstes Album zu erstellen.',
		  'album_description' => 'Album Beschreibung',
		  'add_pictures' => 'Dateien dem Album hinzufügen.',
		  'max_upload_size' => 'Die maximal zulässige Größe einer Datei beträgt $1 KB',
		  'upload_presentation' => 'Wenn die untere Box leer bleibt und/oder der Browser einen Fehler auf der Seite anzeigt, dann prüfen Sie bitte ob Sie die neueste Version des Java runtime plugins installiert haben.<BR> Um Dateien hochzuladen klicken Sie auf <B>Durchsuchen</B> und wählen Sie die Dateien aus oder fügen Sie diese per drag\'n\'drop aus ihrem Ordner ein. Anschließend klicken Sie bitte auf <B>Hochladen</B> um die Dateien an den Server zu übertragen.'
		  	. "<BR>Um die <U>alte</U> Datei hochladen-Funktion zu nutzen, klicken Sie bitte <a href='upload.php'>hier</a>.",
		  'album' => 'Album',
		  //Since 2.1.0
		  'java_not_enabled' => 'Ihr Browser erlaubt kein java. Das Jupload Applet braucht java. Sie können es unter <a href="http:\\java.sun.com\jre\">java web site</a> kostenlos herunterladen',
		  //Since 3.0.0
		  'picture_data_explanation' => 'Klicken Sie auf diesen Link und geben Sie die gewünschten Daten in die Felder ein wenn Sie sie zu allen Bildern des nächsten Uploads hinzufügen möchten.',
		  'quota_used' => 'Sie nutzen momentan $1 MB ($2%) von maximal $3 MB Speicher.',
		  'quota_about_full' => 'Entfernen Sie einige Bilder oder bitten Sie den Administrator Ihren Speicherplatz zu erhöhen.',
		  //Since 3.2.0
		  'need_approval' => 'Der Galerieadministrator muss die hochgeladenen Bilder genehmigen und freischalten, bevor sie gesehen werden können.'
		)
	);
}
