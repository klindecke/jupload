package wjhk.jupload;

import java.io.File;
import java.awt.*;
import javax.swing.*;

public class FilePanelImp extends Panel implements FilePanel{

  //------------- INFORMATION --------------------------------------------
  public static final double VERSION = 1.0;
  public static final String AUTHOR = "William Kwong Jinhua";
  public static final String AUTHOREMAIL = "wjhkwong@yahoo.com";
  public static final String DESCRIPTION = "FilePanel Implementation.";
  public static final String LASTMODIFIED = "01 July 2002";

  private DefaultListModel listModel;
  private JList jlist;

  public FilePanelImp(){
    this.setLayout(new BorderLayout());
    listModel = new DefaultListModel();
    jlist = new JList(listModel);
    JScrollPane listScroll = new JScrollPane(jlist);
    this.add(listScroll);
  }

  public void addFiles(File[] f){
    if(null != f){
      for(int i = 0; i < f.length; i++){
        addDirectoryFiles(listModel, f[i]);
      }
    }
  }


  protected void addDirectoryFiles(DefaultListModel dlm, File f){
    if(!f.isDirectory()){
      addFileOnly(dlm, f);
    }else{
      File[] dirFiles = f.listFiles();
      for(int i = 0 ; i < dirFiles.length; i++){
        if(dirFiles[i].isDirectory()){
          addDirectoryFiles(dlm, dirFiles[i]);
        }else{
          addFileOnly(dlm, dirFiles[i]);
        }
      }
    }
  }

  protected void addFileOnly(DefaultListModel dlm, File f){
    // Make sure we don't select the same file twice.
    if(!dlm.contains(f)) dlm.add(dlm.getSize(), f);
  }


  public File[] getFiles(){
    File[] arrayFiles = new File[listModel.size()];
    listModel.copyInto(arrayFiles);
    return arrayFiles;
  }

  public void removeSelected(){
    for(int i=0; i < listModel.size();){
      if(jlist.isSelectedIndex(i)){
        listModel.remove(i);
      }else{
        i++;
      }
    }
  }

  public void removeAll(){
    listModel.clear();
  }
}