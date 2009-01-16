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
	$lang_jupload_php = array(
		  'link_title' => 'JUpload',
		  'link_comment' => 'ارسال فایل به گالری با استفاده از اپلت جاوا',
		  'perm_denied' => 'شما اجازه انجام این عملیات را ندارید.<BR><BR>لطفا ابتدا <a href="$1">وارد</a> شوید',
		  'select_album' => 'لطفا آلبومی را که میخواهید به آن فایل ارسال کنید ، انتخاب کنید',
		  'button_update_album' => 'به روز رسانی آلبوم',
		  'button_create_album' => 'ایجاد آلبوم',
		  'success' => 'عملیات با موفقیت انجام شد!',
		  'error_select_album' => 'لطفا ابتدا آلبوم را انتخاب کنید.',
		  'error_album_name' => 'لطفا نام آلبوم را مشخص کنید.',
		  'error_album_already_exists' => 'آلبومی با این نام وجود دارد.<BR><BR>لطفا بر روی کلید <I>Back</I> مرورگرتان کلیک کنید, تا نام دیگری برای آلبوم انتخاب نمائید.',
		  'album_name' => 'نام آلبوم',
		  'album_presentation' => 'شما باید آلبومی را انتخاب نمائید. فایلی را که ارسال می کنید در این آلبوم قرار خواهد گرفت. <BR>اگر آلبومی نساخته باشید لیست آلبوم ها خالی است . از کلید \'ساخت\' برای ساختن اولین البوم استفاده کنید.',
		  'album_description' => 'توضیحات آلبوم',
		  'add_pictures' => 'افزودن فایل به آلبوم انتخاب شده',
		  'max_upload_size' => 'حجم بزگترین فایل نباید بیشتر از $1 کیلوبایت باشد.',
		  'upload_presentation' => 'اگر اپلت جاوا در زیر نمایش داده نمی شود و مرورگر خطایی را نمایش می دهد بهترین کار این است که java runtime plugin را نصب کنید.<BR>ارسال بایل بسیار آسان است . کافی است بر روی کلید  <B>Browse</B> کلیک کنید و فایل را انتخاب کنید و یا میتوانید با موشواره فایلها را با داخل اپلت بکشیدو سپس بر روی کلید <B>Upload</B> کلیک کنید تا فایل ارسال شود.'
		. "<BR>برای ارسال فایل <U>به شیوه قدیمی</U> <a href='upload.php'>اینجا</a> کلیک کنید",
		  'album' => 'آلبوم',
		  //Since 2.1.0
		  'java_not_enabled' => 'جاوا در مرورگر شما غیر فعال شده است. اپلت جاوا برای ارسال فایل به جاوا نیاز دارد!. برای نصب جاوا میتوانید از <a href="http:\\java.sun.com\jre\">java web site</a> استفاده کنید (متاسفانه این سایت امکان دریافت فایل برای ایرانیان را فراهم نمی کند!)',
		  //Since 2.1.0
		  'java_not_enabled' => 'Your navigator doesn\'t allow java. The upload applet need java. You can easily download it from the <a href="http:\\java.sun.com\jre\">java web site</a>',
		  //Since 3.0.0
		  'picture_data_explanation' => 'Click on this link, and enter data in the fields below, if you want these to be applied to all pictures in the next upload.',
		  'quota_used' => 'You are currently using $1 MB ($2%) of your $3 MB of storage.',
		  'quota_about_full' => 'Remove some pictures, or ask the admin to make your quota bigger.',
		  //Since 3.2.0
		  'need_approval' => 'The gallery admin must approve these uploaded pictures, before you can see them on the gallery.'
	);
}
