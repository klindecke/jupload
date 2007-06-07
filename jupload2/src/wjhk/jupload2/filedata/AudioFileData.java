//
// $Id$
// 
// jupload - A file upload applet.
// Copyright 2007 The JUpload Team
// 
// Created: 2007-01-19
// Creator: Etienne Gauthier
// Last modified: $Date$
//
// This program is free software; you can redistribute it and/or modify it under
// the terms of the GNU General Public License as published by the Free Software
// Foundation; either version 2 of the License, or (at your option) any later
// version. This program is distributed in the hope that it will be useful, but
// WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
// FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
// details. You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software Foundation, Inc.,
// 675 Mass Ave, Cambridge, MA 02139, USA.

package wjhk.jupload2.filedata;

import java.io.File;

import wjhk.jupload2.policies.UploadPolicy;

/**
 * This class contains all data for files to upload an audio file. It adds the
 * following elements to the {@link wjhk.jupload2.filedata.FileData} class :<BR>
 * <UL>
 * <LI> Pre-earing of audio files
 * <LI> To be completed
 * </UL>
 * <BR>
 * <BR>
 * The audio functionalities are taken from the entagged sourceforge project.
 * The entagged-audioformats.jar is used to generate the sound, within the
 * applet. To keep the applet jar file small, the entagged jar file is not
 * embedded into the jupload jar. It is loaded independantly. <BR>
 * <BR>
 * To be completed
 * 
 * @author Etienne Gauthier
 * @version $Revision$
 */

public class AudioFileData extends DefaultFileData {

    /**
     * Creates a new instance.
     * 
     * @param file The file to use as data source.
     * @param uploadPolicy The upload policy to apply.
     */
    public AudioFileData(File file, File root, UploadPolicy uploadPolicy) {
        super(file, root, uploadPolicy);
    }

}
