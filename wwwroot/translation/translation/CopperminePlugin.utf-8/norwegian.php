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
	      'link_title' => 'JUpload',
	      'link_comment' => 'Last opp filer til galleriet, ved hjelp av en applet',    
	      'perm_denied' => 'Du har ikke tillatelse til å utføre den opprasjonen.<BR><BR>Hvis du ikke er innlogget, vær vennlig og <a href="$1">logg inn</a> først',
	      'select_album' => 'Du må velge et album du ønsker å laste opp bilder til',
	      'button_update_album' => 'Oppdater album',
	      'button_create_album' => 'Lag album',
	      'success' => 'Handling utført !',
	      'error_select_album' => 'Du må velge et album først',
	      'error_album_name' => 'Du må gi albumet et navn',
	      'error_album_already_exists' => 'Det fines allerede et album med dette navnet.<BR><BR>Vær vennlig å klikke <I>Tilbake</I> knappen på nettleseren for å skrive inn en nytt navn på ditt album.',
	      'album_name' => 'Album navn',
	      'album_presentation' => 'Du må velge et album her. Bildene du laster opp vil bli lagret I dette albumet. <BR>Hvis du ikke har et album, er albumlisten tom. Bruk "Lage" knappen til å lage ditt første album.',
	      'album_description' => 'Album beskrivelse',
	      'add_pictures' => 'Legg til bilder i valgte album',
	      'max_upload_size' => 'Maksimum størrelse for et bilde er $1 kB',
	      'upload_presentation' => 'Hvis rammen nedenfor ikke kan vises, og nettleseren indikerer at det er feil på siden, må du installere Java plugin.<BR>Etter dette er opplasting veldig enkelt! Klikk på <B>Bla gjennom</B> for å velge filer eller bruk dra og slipp fra filutforskeren, velg så <B>Last opp</B> for å sende bildene til nettsiden.'
	    . "<BR>For å bruke <U>den vanlige filopplastingen</U>, <a href='upload.php'>klikk her</a>.",
		  'album' => 'Album',
		  //Since 2.1.0
		  'java_not_enabled' => 'Din nettleser tillater ikke Java. Opplastingsprogrammet krever Java. Du kan enkelt laste det ned fra <a href="http:\\java.sun.com\jre\">Java nettsidene</a>',
		  //Since 3.0.0
		  'picture_data_explanation' => 'Klikk på denne lenken og fyll inn informasjon i feltene nedenfor, dersom du ønkser at denne skal legges til alle bildene i neste opplasting.',
		  'quota_used' => 'Du bruker nå $1 MB ($2%) av din $3 MB store lagringsplass.',
		  'quota_about_full' => 'Fjern noen bilder, eller be administrator om å gjøre kontoen din større.',
		  //Since 3.2.0
		  'need_approval' => 'The gallery admin must approve these uploaded pictures, before you can see them on the gallery.'  	
		  )
	);
}
