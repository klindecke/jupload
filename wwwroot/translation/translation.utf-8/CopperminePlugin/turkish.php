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
		  'perm_denied' => 'Bu işlemi yapmaya yetkili değilsiniz.<BR><BR>Eğer bağlı değilseniz lütfen önce <a href="login.php' . ( isset($_SERVER['PHP_SELF']) ? '?referer=' . $_SERVER['PHP_SELF'] : '') . '">giriş yapın</a>',
		  'select_album' => 'Lütfen resimleri göndereceğiniz albümü seçiniz. ',
		  'button_update_album' => 'Albümü güncelle',
		  'button_create_album' => 'Albüm Yarat',
		  'success' => 'İşlem Başarılı !',
		  'error_select_album' => 'Lütfen önce albüm seçiniz',
		  'error_album_name' => 'Lütfen Albüm adını girin.',
		  'error_album_already_exists' => 'Bu isimde bir albüm zaten var<BR><BR>Albümünüze başka bir isim vermek için lütfen tarayıcınızdaki <I>Geri</I> düğmesine basınız.',
		  'album_name' => 'Albüm adı',
		  'album_presentation' => 'Burada bir albüm seçmelisiniz. Göndereceğiniz resimler sunucuda bu albüm içerisine kaydedilecektir. <BR>Eğer bir albümünüz yoksa albüm listesi boş gözükecektir. Önce \'Yarat\' düğmesine basıp ilk albümünüzü yaratabilirsiniz.',
		  'album_description' => 'Albüm tanımı',
		  'add_pictures' => 'Seçili albüme resim ekle',
		  'max_upload_size' => 'Maksimum resim boyutu: $1 Ko',
		  'upload_presentation' => 'Eğer aşağıdaki kare appleti göstermiyor ve tarayıcınız sayfada sürekli olarak hata mesajları veriyorsa, java runtime plugin\'i yüklemek gerekecektir..<BR>Sonrasında gönderim gerçekten basit olacaktır! <B>Gözat</B> a basıp dosyaları seçin ya da dosyaları bilgisaranızdan sürükleyip bırakın. Sonrasında <B> Gönder </B> e basıp resimleri sunucuya gönderin.'
		  	. "<BR><U>Eski gönderim sayfası</U>nı kullanmak için, <a href='upload.php'>tıklayınız</a>.",
		  'album' => 'Albüm',
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
