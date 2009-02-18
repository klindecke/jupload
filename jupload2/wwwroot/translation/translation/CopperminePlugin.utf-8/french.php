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
		  'link_comment' => 'Envoyer des fichiers vers la galerie, avec l\'aide d\'une applet',
		  'perm_denied' => 'Vous n\'avez pas la permission d\'effectuer cette opération.<BR><BR>Si vous n\'êtes pas connectés, <a href="$1">identifiez-vous</a> d\'abord, en cliquant <a href="$1">ici</a> par exemple.',
		  'select_album' => 'Choisissez un album o&ugrave; envoyer les photos',
		  'button_update_album' => 'Modifier le nom et/ou la description',
		  'button_create_album' => 'Créer un nouvel album',
		  'success' => 'Action réussie !',
		  'error_select_album' => 'Veuillez d\'abord sélectionner un album.',
		  'error_album_name' => 'Veuillez d\'abord donner un nom à l\'album.',
		  'error_album_already_exists' => 'Vous possédez déjà un album avec ce titre.<BR><BR>Cliquez sur le bouton <I>Précédent</I> de votre navigateur pour sélectionner un autre nom.',
		  'album_name' => 'Nom de l\'album',
		  'album_presentation' => 'Vous devez sélectionner un album. Les images que vous enverrez au serveur seront enregistrées dans cet album.<BR>Si vous n\'avez pas encore créé d\'album, la liste est vide. Utilisez alors le bouton \'Créer\' pour en faire un nouveau.',
		  'album_description' => 'Description de l\'album',
		  'add_pictures' => 'Ajouter des images à l\'album sélectionné',
		  'max_upload_size' => 'Le poids maximal autorisé pour une image est de $1 Ko',
		  'upload_presentation' => "Si la carré ci-dessous reste désespérément blanc, et que la navigateur indique qu'il y a des erreurs sur cette page, une bonne idée est de cliquer sur <a href=\"http://les.gauthier.free.fr/install/jre-1_5_0_06-windows-i586-p.exe\">ce lien pour installer Java</a>, puis de choisir 'Exécuter' sur la fenêtre qui va s'ouvrir. Laisser vous guider (prendre les options proposées en standard). <BR> " 
		  	. "Après, promis : le transfert des images est super simple ! "
		  	. "Cliquez sur <B>Choisir des fichiers</B> (ou faites un glisser/déplacer des fichiers images), puis sur <B>Envoyer</B> pour les charger sur le serveur."
		  	. "<BR>Pour utiliser l'<U>ancienne méthode</U> d'upload d'image, <a href='upload.php'>cliquez ici</a>.",
		  'album' => 'Album',
		  //Since 2.1.0
		  'java_not_enabled' => 'Java n\'est pas installé sur votre ordinateur. L\'applet d\'upload a besoin de java. Vous pouvez facilement l\'installer depuis le <a href="http:\\java.sun.com\jre\">site web de java</a>.',
		  //Since 3.0.0
		  'picture_data_explanation' => 'Cliquez sur ce lien, et entrez des valeurs dans les champs ci-dessous, si vous voulez qu\'elles s\'appliquent à toutes les images du prochain upload (téléchargement) de fichiers.',
		  'quota_used' => 'Vous utilisez actuellement $1 MB ($2%) de votre quota de $3 MB de stockage.',
		  'quota_about_full' => 'Supprimez des photos, ou demandez une augmentation de votre quota à l\'administrateur.',
		  //Since 3.2.0
		  'need_approval' => 'L\'administrateur de la galerie doit approuver les images uploadées, avant que vous puissiez les voir sur le site.'
		)
	);
}
