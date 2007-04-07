/*
 * Created on 6 avr. 07
 */
package wjhk.jupload2.gui;

import java.io.File;

import javax.swing.Icon;
import javax.swing.filechooser.FileView;

import wjhk.jupload2.policies.UploadPolicy;

public class JUploadFileView extends FileView {
	
	UploadPolicy uploadPolicy = null;
	
	public JUploadFileView (UploadPolicy uploadPolicy) {
		this.uploadPolicy = uploadPolicy;
	}
	
	public Icon getIcon(File file) {
		
		return null;		
	}

}
