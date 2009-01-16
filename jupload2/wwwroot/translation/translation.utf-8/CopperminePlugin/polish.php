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


// Thanks to Wojtek Semik: wojteks at pvd dot pl

if (defined('JUPLOAD_PHP')) {
	$lang_jupload_php = array_merge (
		$lang_jupload_php,
		array(
		  'perm_denied' => 'Nie masz uprawnień do wykonania tej operacji.<BR><BR>Jeżeli nie jesteś połączony, proszę się najpierw <a href="login.php' . ( isset($_SERVER['PHP_SELF']) ? '?referer=' . $_SERVER['PHP_SELF'] : '') . '">zalogować</a>',
		  'select_album' => 'Proszę wybrać album, do którego zostaną przesłane pliki',
		  'button_update_album' => 'Aktualizuj album',
		  'button_create_album' => 'Stwórz album',
		  'success' => 'Operacja zakończona sukcesem',
		  'error_select_album' => 'Proszę najpierw wybrać album.',
		  'error_album_name' => 'Proszę nazwać album.',
		  'error_album_already_exists' => 'Istnieje już album o takiej nazwie w Twojej kolekcji.<BR><BR>Użyj przycisku <I>Powrót</I>w Twojej przeglądarce, aby wpisać inną nazwę albumu.',
		  'album_name' => 'Nazwa albumu',
		  'album_presentation' => 'Musisz teraz wybrać album, w którym będą przechowywane zdjęcia, które prześlesz na serwer. <BR>Jeżeli jeszcze nie utworzyłeś żadnego albumu, lista albumów będzie pusta. Uyżj przycisku \'Stwórz album\' aby utworzyć nowy album.',
		  'album_description' => 'Opis albumu',
		  'add_pictures' => 'Dodaj zdjęcia do wybranego albumu',
		  'max_upload_size' => 'Maksymalny rozmiar pliku ze zdjęciem to $1 Ko',
		'upload_presentation' => 'Jeżeli nie możesz wyświetlić apletu i przeglądarka informuje o błędach na stronie, najprawdopodobniej nie masz zainstalowanego środowiska uruchomieniowego Javy (Java JRE).<BR>Jak tylko je zainstalujesz, przesyłanie plików będzie naprawdę proste! Naciśniesz przycisk <B>Przeglądaj</B> aby wybrać pliki albo po prostu przeciągniesz je z menadżera plików, potem naciśniesz przycisk <B>Prześlij</B> i pliki zostaną wysłane na serwer.'
		  	. "<BR>Aby korzystać ze <U>starej strony przesyłania</U>, <a href='upload.php'>kliknij tutaj</a>.",
		  'album' => 'Album'  ,
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
