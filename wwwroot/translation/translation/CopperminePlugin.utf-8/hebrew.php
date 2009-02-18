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
		  'link_comment' => 'העלאת קבצים לגלרייה בעזרת ישומון',
		  'perm_denied' => 'אין לך הרשאה לביצוע פעולה זו.<BR /><BR />אם אינך מחובר, נסה <a href="$1">להתחבר</a> תחילה.',
		  'select_album' => 'אנא בחר אלבום לאחסון התמונות.',
		  'button_update_album' => 'עדכן אלבום',
		  'button_create_album' => 'יצירת אלבום חדש',
		  'success' => 'פעולה הצליחה!',
		  'error_select_album' => 'אנא בחר אלבום תחילה',
		  'error_album_name' => 'אנא תן לאלבום שם.',
		  'error_album_already_exists' => 'כבר קיים אלבום עם שם זה.<BR /><BR />אנא לחץ על <I>אחורה</I> בדפדפן ובחר שם אחר לאלבום זה.',
		  'album_name' => 'שם האלבום',
		  'album_presentation' => 'אתה מוכרח לבחור אלבום. התמונות שתשלח לשרת יאוחסנו באלבום זה.<BR />אם לא קיימים אלבומים, הרשימה תהיה ריקה. לחץ על \'יצירת אלבום חדש\' תחילה.',
		  'album_description' => 'תיאור האלבום',
		  'add_pictures' => 'הוסף תמונות לאלבום הנבחר',
		  'max_upload_size' => 'הגודל המקסימלי לתמונה הינו $1 KB',
		  'upload_presentation' => 'אם הריבוע להלן אינו מציג את הישומון, והדפדפן מודיע על שגיאות בעמוד זו, כנראה שנדרשת התקנה של java runtime plugin.<BR />לאחר מכן, העלאת התמונות ממש פשוטה! לחץ על <B>עיון</B> ובחר את הקבצים הרצויים או גרור אותם אל הישומון מכל חלון אחר. לחיצה על <B></B> תשלח אותם אל השרת.'
		. "<BR />אם ברצונך להשתמש ב<U>ממשק ההעלאה הישן</U>, <a href='upload.php'>לחץ כאן</a>.",
		  'album' => 'אלבום',
		  //Since 2.1.0
		  'java_not_enabled' => 'הדפדפן שלך אינו מאפשר java. ישומון ההעלאה מחייב שימוש ב- java. ניתן לפתור זאת בקלות על ידי התקנה מ-  <a href="http:\\java.sun.com\jre\">java web site</a>',
		  //Since 3.0.0
		  'picture_data_explanation' => 'לחץ על קישור זה ומלא את השדות להלן אם ברצונך להחיל שינויים אלו על הפעם הבאה בה תעלה קבצים',
		  'quota_used' => 'אתה מנצל כעת $1 MB ($2%) מתוך $3 MB שהוקצו לך לאחסון.',
		  'quota_about_full' => 'מחק תמונות או בקש ממנהל האתר להקצות לך יותר מקום אחסון.',
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




