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
		  'link_comment' => 'Pubblicazione di files nella galleria con l\'aiuto di un\'applet',
		  'perm_denied' => 'Non hai il permesso per eseguire questa operazione.<BR><BR>Se non sei connesso per favore esegui prima il <a href="$1">login</a> ',
		  'select_album' => 'Scegli l\'album dove postare le tue immagini',
		  'button_update_album' => 'Aggiorna album',
		  'button_create_album' => 'Crea album',
		  'success' => 'Azione eseguita !',
		  'error_select_album' => 'Per favore scegli prima un album',
		  'error_album_name' => 'Inserisci il nome dell\'album',
		  'error_album_already_exists' => 'Esiste gi&#225; un album con questo nome.<BR><BR>Clicca sul tasto <I>Back</I> del tuo browser, e scegli un\'altro nome.',
		  'album_name' => 'Nome dell\'album',
		  'album_presentation' => 'Puoi selezionare un\'album. Le immagini che invierai al server saranno salvate in questo album. <BR>Non hai nessun album, la lista album &#232 vuota. Usa il bottone \'Create\' per creare il tuo primo album.',
		  'album_description' => 'Descrizione album',
		  'add_pictures' => 'Aggiungi immagini nell\'album selezionato',
		  'max_upload_size' => 'La massima dimensione delle immagini &#232 $1 Ko',
		  'upload_presentation' => 'Se il riquadro in basso non visualizza l\'applet e il vostro browser segnala un errore nella pagina dovrete installare le runtime Java.<BR>Dopo, la pubblicazione sar&#225; molto semplice! Clicca su <B>Browse</B> per selezionare i files o usa Copia e Incolla da explorer, cilicca su <B>Upload</B> per inviare le immagini al server.'
		. "<BR>Per usare <U>la vecchia pagina di upload </U>, <a href='upload.php'>clicca qui</a>.",
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
