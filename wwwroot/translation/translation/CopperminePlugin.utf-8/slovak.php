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
		  'link_comment' => 'Odosielajte súbory do galérie s pomocou Java appletu',
		  'perm_denied' => 'Nemáte oprávnenie pre vykonanie tejto akcie.<BR><BR>Ak nie ste prihlásený, prosím <a href="$1">prihláste sa</a> najskôr',
		  'select_album' => 'Prosím vyberte album, do ktorého chcete odoslať obrázky',
		  'button_update_album' => 'Upraviť album',
		  'button_create_album' => 'Vytvoriť album',
		  'success' => 'Akcia bola vykonaná úspešne !',
		  'error_select_album' => 'Prosím, vyberte najskôr album',
		  'error_album_name' => 'Prosím, zadajte názov albumu.',
		  'error_album_already_exists' => 'Album s takýmto názvom už máte.<BR><BR>Prosím kliknite na tlačidlo <I>Späť</I> na svojom prehliadači, pre napísanie iného názvu vášho nového albumu.',
		  'album_name' => 'Názov albumu',
		  'album_presentation' => 'Tu je potrebné vybrať album. Obrázky, ktoré pošlete server, budú uložené práve do tohto albumu. <BR>Ak nemáte vytvorený album, zoznam albumov je prázdny. Použite tlačidlo \'Vytvoriť album\' na vytvorenie prvého albumu.',
		  'album_description' => 'Popis albumu',
		  'add_pictures' => 'Pridať obrázky do vybranému albumu',
		  'max_upload_size' => 'Maximálny veľkost obrázku môže byť $1 KB',
		  'upload_presentation' => 'Ak sa oblasti dole odmietne zobraziť applet a prehliadač oznamuje, že na stránke sa objavili chyby, pravdepodobne potrebujete nainštalovať java runtime plugin.<BR>Potom je už odoslanie jednoduché! Kliknite na <B>Hľadať</B> pre zvolenie súborov, alebo ich cez drag\'n\'drop preneste kurzorom, následne kliknite na <B>Odoslať</B> pre odoslanie súborov na server.'
		. "<BR>Pre použitie <U>starej stránky pre upload</U>, <a href='upload.php'>kliknite sem</a>.",
		  'album' => 'Album',
		  //Since 2.1.0
		  'java_not_enabled' => 'Váš prehliadač nepodporuje Javu. Applet pre odosielanie vyžaduje Javu. Môžete si ju zdarma stiahnuť zo <a href="http:\\java.sun.com\jre\">stránok Javy</a>',
		  //Since 3.0.0
		  'picture_data_explanation' => 'Kliknite na tento odkaz a vyplňte informácie do políčok dole, ak chcete aby boli použité na všetky obrázky v ďalšom odoslaní.',
		  'quota_used' => 'Aktuálne pužívate $1 MB ($2%) z $3 MB vášho priestoru.',
		  'quota_about_full' => 'Odstráňte nejaké obrázky, alebo požiadajte administrátora aby vám poskytol viac priestoru.',
		  //Since 3.2.0
		  'need_approval' => 'Odoslané obrázky sa zobrazia v galérii až po potvrdení administrátorom galérie.'
		)
	);
}

