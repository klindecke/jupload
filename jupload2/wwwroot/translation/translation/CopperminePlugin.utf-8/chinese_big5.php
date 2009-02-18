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
		  'link_title' => '批次上傳',
		  'link_comment' => '上傳大量檔案至相簿 (瀏覽器需支援 Java Applet)',
		  'perm_denied' => '權限不足！<BR><BR>請先<a href="$1">登入相簿</a>',
		  'select_album' => '請先選擇相簿',
		  'button_update_album' => '更新相簿',
		  'button_create_album' => '建立相簿',
		  'success' => '操作完成！',
		  'error_select_album' => '相簿未選擇！請先選擇相簿',
		  'error_album_name' => '請為相簿命名',
		  'error_album_already_exists' => '相簿名稱已存在！<BR><BR>請按<I>回上頁</I>設定其它的名稱',
		  'album_name' => '相簿名稱',
		  'album_presentation' => '請選擇相簿！您上傳的圖檔將會存放在選擇的相簿中<BR>若您尚未建立任何相簿, 請按\'建立\'按鈕來建立您的第一本相簿',
		  'album_description' => '相簿描述',
		  'add_pictures' => '將圖檔加入選擇的相簿',
		  'max_upload_size' => '檔案大小限制為 $1 KB',
		  'upload_presentation' => '若 Applet 無法顯示, 且您的瀏覽器回報本頁的錯誤訊息, 請先安裝 Java 執行環境.<BR>之後, 您將可以更方便地上傳您的照片！按下<B>瀏覽</B>選擇檔案, 或是直接拖曳欲上傳的檔案至 Applet 視窗中, 並按下<B>上傳</B>完成檔案上傳的動作'
		. "<BR>若想使用<U>傳統的上傳介面</U>, <a href='upload.php'>請按這裡</a>.",
		  'album' => '相簿',
		  //Since 2.1.0
		  'java_not_enabled' => '您的瀏覽器不支援 Java Applet. 本上傳頁面使用 Java Applet 技術. 請至<a href="http:\\java.sun.com\jre\"> Jave 網站</a>下載最新版本的 Java 執行環境',
		  //Since 3.0.0
		  'picture_data_explanation' => '若您想將此設定值套用至所有的照片, 請點選連結並在資料欄位中輸入資料',
		  'quota_used' => '您目前已使用 $1/$3 MB ($2%) 個人空間',
		  'quota_about_full' => '空間已滿, 請移除一些照片, 或向管理員尋求協助',
		  //Last minute sentence :
		  //Since 3.2.0
		  'need_approval' => '檔案由管理者審核中, 待審核通過後您將可在相簿中觀看最新上傳的檔案.'
		)
	);
}
