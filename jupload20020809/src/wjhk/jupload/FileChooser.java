package wjhk.jupload;

import java.io.*;
import java.awt.*;

public interface FileChooser {
  public int showOpenDialog(Component parent);
  public File[] getSelectedFiles();
}