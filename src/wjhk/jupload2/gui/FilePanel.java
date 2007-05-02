package wjhk.jupload2.gui;

import java.io.File;

import wjhk.jupload2.filedata.FileData;

/**
 * Defines the interface used in the applet, when dealing with the file panel.
 */
public interface FilePanel {
    /**
     * Add multiple files to this panel.
     * 
     * @param f An array of files to add.
     */
    public void addFiles(File[] f);

    /**
     * Retrieve all currently stored files.
     * 
     * @return an array of files, currently managed by this instance.
     */
    public FileData[] getFiles();

    /**
     * Retrieve the number of file entries in the JTable.
     * @return the current number of files, held by this instance.
     */
    public int getFilesLength();

    /**
     * Removes all currently selected file entries.
     */
    public void removeSelected();

    /**
     * Removes all file entries.
     */
    public void removeAll();

    /**
     * Remove a specified file entry.
     * @param fileData The file to be removed.
     */
    public void remove(FileData fileData);

    /**
     * Clears the current selection of the JTable.
     */
    public void clearSelection();

    /**
     * Requests focus for the JTable.
     */
    public void focusTable();
}