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
		  'link_comment' => 'Enviar arquivos para galeria, com a ajuda de um applet',
		  'perm_denied' => 'Você não tem permissão para realizar esta operação.<BR><BR>Se você não está logado por favor <a href="$1">efetue login</a>',
		  'select_album' => 'Escolha um album onde você deseja colocar as fotos',
		  'button_update_album' => 'Atualizar album',
		  'button_create_album' => 'Criar album',
		  'success' => 'Feito com sucesso! !',
		  'error_select_album' => 'Escolha um album',
		  'error_album_name' => 'Dê um nome ao album.',
		  'error_album_already_exists' => 'Já existe um álbum com esse nome.<BR><BR>Por favor clique  no botão <I>voltar</I> do seu navegador, para digitar outro nome para o album.',
		  'album_name' => 'Nome do album',
		  'album_presentation' => 'Selecione um album aqui. As fotos que você enviar para o servidor serão guardadas aqui. <BR>Se você não tem nenhum album a lista estará vazia. Use o botão \'Criar\' para criar um novo album.',
		  'album_description' => 'Descrição do album',
		  'add_pictures' => 'Adicionar fotos para o album selecionado',
		  'max_upload_size' => 'O tamanho maximo de envio de dados é de $1 KB',
		  'upload_presentation' => 'Se a página realmente se recusa a mostrar o applet, e/ou na pagina é mostrado que existe algum erro, recomanda-se que instale o java e após isso será bastante fácil utilizar, basta clicar em procurar, escolher o arquivo, e clicar em enviar.'
		. "<BR>Para usar a <U>a página de upload</U>, <a href='upload.php'>clique aqui</a>.",
		  'album' => 'Album',
		  //Since 2.1.0
		  'java_not_enabled' => 'Seu navegador não permite java. O Applet de upload precisa de java. Você pode facilmente fazer o download pelo site da sun <a href="http:\\java.sun.com\jre\">clicando aqui</a>',
		  //Since 3.0.0
		  'picture_data_explanation' => 'Click on this link, and enter data in the fields below, if you want these to be applied to all pictures in the next upload.',
		  'quota_used' => 'You are currently using $1 MB ($2%) of your $3 MB of storage.',
		  'quota_about_full' => 'Remove some pictures, or ask the admin to make your quota bigger.',
		  //Since 3.2.0
		  'need_approval' => 'The gallery admin must approve these uploaded pictures, before you can see them on the gallery.'
		)
	);
}