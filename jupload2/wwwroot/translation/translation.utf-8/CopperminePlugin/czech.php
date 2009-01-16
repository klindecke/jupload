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
		  'perm_denied' => 'K provedení této operace nemáte dostatečná oprávnění.<BR><BR>Pokud nejste připojen, <a href="login.php' . ( isset($_SERVER['PHP_SELF']) ? '?referer=' . $_SERVER['PHP_SELF'] : '') . '">přihlašte se</a> prosím',
		  'select_album' => 'Vyberte prosím album, do kterého si přejete přenést obrázky',
		  'button_update_album' => 'Aktualizovat album',
		  'button_create_album' => 'Vytvořit album',
		  'success' => 'Úspěšně provedeno !',
		  'error_select_album' => 'Nejprve prosím vyberte album',
		  'error_album_name' => 'Zadejte prosím název alba.',
		  'error_album_already_exists' => 'Album s tímto názvem již vlastníte.<BR><BR>Klikněte prosím na tlačítko<I>Zpět</I> vašeho prohlížeče, abyste mohli vyplnit jiný název vašeho nového alba.',
		  'album_name' => 'Název alba',
		  'album_presentation' => 'Zde musíte vybrat album. Obrázky, které přenesete na server budou uloženy v tomto albu. <BR>Pokud nemáte žádné album, je seznam alb prázdný. Použijte tlačítko \'Vytvořit album\' k vytvoření vašeho prvního alba.',
		  'album_description' => 'Popis alba',
		  'add_pictures' => 'Vložit obrázky do vybraného alba',
		  'max_upload_size' => 'Maximální velikost obrázku je $1 Ko',
		  'upload_presentation' => 'Pokud prohlížeč odmítá zobrazit applet a hlásí na této stránce chyby, je vhodné nainstalovat rozšíření java runtime.<BR>Poté je již přenos souborů opravdu jednoduchý! Klikňete <B>Procházet</B> pro výběr souborů nebo použijte přetažení souborů (drag\'n\'drop) z Průzkumníka, poté klikněte <B>Přenést</B> k odeslání obrázků na server.'
		  	. "<BR>Pro použití <U>původní stránky pro přenos souborů</U>, <a href='upload.php'>klikněte zde</a>.",
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
		