package wjhk.jupload;

import java.io.*;
import java.awt.*;
import javax.swing.*;

public class FileChooserImp extends JFileChooser implements FileChooser{
  public FileChooserImp(){
    super.setCurrentDirectory(new File(System.getProperty("user.dir")));
    super.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
    super.setMultiSelectionEnabled(true);
  }
}