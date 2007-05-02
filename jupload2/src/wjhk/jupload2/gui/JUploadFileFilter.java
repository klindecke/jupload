package wjhk.jupload2.gui;

import java.io.File;

import javax.swing.filechooser.FileFilter;

import wjhk.jupload2.policies.UploadPolicy;

/**
 * Default File Filter used by the {@link wjhk.jupload2.policies.DefaultUploadPolicy} to filter the
 * allowed file in the JFileChooser. This class is an empty one: it just calls
 * the {
 */
public class JUploadFileFilter extends FileFilter {

    UploadPolicy uploadPolicy = null;

    JUploadFileFilter(UploadPolicy uploadPolicy) {
        this.uploadPolicy = uploadPolicy;
    }

    /**
     * @see javax.swing.filechooser.FileFilter#accept(java.io.File)
     */
    @Override
    public boolean accept(File file) {
        return this.uploadPolicy.fileFilterAccept(file);
    }

    /**
     * @see javax.swing.filechooser.FileFilter#getDescription()
     */
    @Override
    public String getDescription() {
        return this.uploadPolicy.fileFilterGetDescription();
    }

}
