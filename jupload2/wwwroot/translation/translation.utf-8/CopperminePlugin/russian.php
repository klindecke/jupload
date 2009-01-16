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
		  'perm_denied' => 'У Вас нет прав на выполнение этой операции.<br /><br />Если вы не авторизованы, пожалуйста, сначала <a href="login.php' . ( isset($_SERVER['PHP_SELF']) ? '?referer=' . $_SERVER['PHP_SELF'] : '') . '">авторизуйтесь</a>.',
		  'select_album' => 'Пожалуйста, выберите альбом, в который вы хотите добавить картинки.',
		  'button_update_album' => 'Обновить альбом',
		  'button_create_album' => 'Создать альбом',
		  'success' => 'Готово!',
		  'error_select_album' => 'Пожалуйста, сначала выберите альбом.',
		  'error_album_name' => 'Пожалуйста, назовите альбом.',
		  'error_album_already_exists' => 'У Вас уже есть альбом с таким названием.<br /><br />Пожалуйста, <a href="javascript:history.back()">вернитесь на предыдущую страницу (или нажмите <i>Назад</i> в навигаторе вашего браузера) и дайте другое название Вашему новому альбому.',
		  'album_name' => 'Название альбома',
		  'album_presentation' => 'Здесь Вы должны выбрать альбом. Картинки, которые вы будете загружать на сервер, будут сохранены в этом альбоме.<br />Если у Вас еще нет альбомов, список пустой. Нажмите \'Создать\' для создания нового альбома.',
		  'album_description' => 'Описание альбома',
		  'add_pictures' => 'Добавить картинки в выбранный альбом',
		  'max_upload_size' => 'Максимальный размер файла картинки $1 Кб',
		  'upload_presentation' => 'Если область экрана ниже однозначно отказывается отображать апплет и браузер сообщает об ошибках на странице, попробуйте установить плагин <a href="http://java.sum.com/" target="_blank">Java&#8482;</a> для вашего браузера.<br />После этого загрузка картинок будет намного проще! Нажмите <b>Обзор</b>, чтобы выбрать файлы, или используйте drag\'n\'drop из Explorer\'а, затем нажмите <b>Загрузить</b> чтобы отправить файлы на сервер.'
		        . '<br />Воспользоваться <u>старым способом загрузки</u> Вы можете <a href="upload.php">здесь</a>.',
		  'album' => 'Альбом',
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
