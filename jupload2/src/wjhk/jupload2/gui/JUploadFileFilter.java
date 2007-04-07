/**
 * Default File Filter used by the {@link DefaultUploadPolicy} to filter the allowed file in the JFileChooser.
 * This class is an empty one: it just calls the {
 */
package wjhk.jupload2.gui;

import java.io.File;

import javax.swing.filechooser.FileFilter;

import wjhk.jupload2.policies.UploadPolicy;

public class JUploadFileFilter extends FileFilter {
	
	UploadPolicy uploadPolicy = null;
	
	JUploadFileFilter(UploadPolicy uploadPolicy) {
		this.uploadPolicy = uploadPolicy;
	}

	public boolean accept(File file) {
		return uploadPolicy.fileFilterAccept(file);
	}

	public String getDescription() {
		return uploadPolicy.fileFilterGetDescription();
	}

}
