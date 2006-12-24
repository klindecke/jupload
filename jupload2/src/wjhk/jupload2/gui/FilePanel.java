package wjhk.jupload2.gui;

import java.io.File;

import wjhk.jupload2.filedata.FileData;

/**
 * Defines the interface used in the applet, when dealing with the file panel.
 * 
 */
public interface FilePanel {
  public void addFiles(File[] f);
  public FileData[] getFiles();
  public int getFilesLength();
  public void removeSelected();
  public void removeAll();
  public void remove(FileData fileData);
  public void clearSelection();
}