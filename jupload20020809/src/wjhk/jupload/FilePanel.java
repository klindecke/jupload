package wjhk.jupload;

import java.io.File;

public interface FilePanel {
  public void addFiles(File[] f);
  public File[] getFiles();
  public void removeSelected();
  public void removeAll();
}