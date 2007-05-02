/*
 * Created on 19 janv. 07
 */
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
 */

public class AudioFileData extends DefaultFileData {

    /**
     * Creates a new instance.
     * 
     * @param file The file to use as data source.
     * @param uploadPolicy The upload policy to apply.
     */
    public AudioFileData(File file, UploadPolicy uploadPolicy) {
        super(file, uploadPolicy);
    }

}
