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
 $Source: $
 $Revision: ?
 $Author: etienne_sf $
 $Date: $
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
		  'perm_denied' => '実行権限がありません。<BR><BR>最初に<a href="login.php' . ( isset($_SERVER['PHP_SELF']) ? '?referer=' . $_SERVER['PHP_SELF'] : '') . '">ログイン</a>してください。',
		  'select_album' => '画像を保存するアルバムを選択してください。',
		  'button_update_album' => 'アルバム更新',
		  'button_create_album' => 'アルバム作成',
		  'success' => '処理成功 !',
		  'error_select_album' => '最初にアルバムを選択してください。',
		  'error_album_name' => 'アルバムの名前を入力してください。',
		  'error_album_already_exists' => '同じ名前のアルバムがすでに存在しています。<BR><BR>ブラウザーの<I>戻る</I>ボタンをクリックして、違う名前を入力してください。',
		  'album_name' => 'アルバムの名前',
		  'album_presentation' => 'アルバムを選択してください。アップロードした画像は、選択したアルバムに保存されます。<BR>リストにアルバムがない場合は、最初に \'アルバム作成\' ボタンをクリックしてアルバムを作成してください。',
		  'album_description' => 'アルバムの説明',
		  'add_pictures' => '選択したアルバムに画像を追加',
		  'max_upload_size' => '画像の最大サイズは $1 KB です',
		  'upload_presentation' => '下にアプレットが表示されずにエラーが表示される場合は、Javaプラグインをインストールしてください。<BR>インストールした後には、 <B>ファイルを開く</B> ボタンをクリックしてファイルを選択するか、エクスプローラからドラッグ＆ドロップして、<B>アップロード</B> ボタンをクリックすることでサーバにファイルをアップロードできます。'
		  	. "<BR><U>old upload page</U>を使用するには<a href='upload.php'>ここ</a>をクリックしてください。",
		  'album' => 'アルバム',
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
