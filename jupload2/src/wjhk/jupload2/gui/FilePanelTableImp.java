package wjhk.jupload2.gui;


import java.io.File;

import java.awt.BorderLayout;
import java.awt.Panel;
import java.awt.dnd.DropTarget;
import javax.swing.JScrollPane;
import javax.swing.table.TableColumnModel;

import wjhk.jupload2.filedata.FileData;
import wjhk.jupload2.policies.UploadPolicy;

/**
 * Implementation of the FilePanel : it creates the {@link wjhk.jupload2.gui.FilePanelJTable}, 
 * and handles the necessary functionnalities.
 */
public class FilePanelTableImp extends Panel implements FilePanel{

  /**
	 * 
	 */
	private static final long serialVersionUID = -8273990467324350526L;
//------------- INFORMATION --------------------------------------------
  public static final String TITLE = "JUpload FilePanelTableImp";
  public static final String DESCRIPTION = "FilePanel Table Implementation.";
  public static final String AUTHOR = "William JinHua Kwong";

  public static final double VERSION = 1.0;
  public static final String LAST_MODIFIED = "07 February 2004";

  //------------- VARIABLES ----------------------------------------------
  private FilePanelJTable jtable;
  private FilePanelDataModel2 model;

  public FilePanelTableImp(JUploadPanel jup, UploadPolicy uploadPolicy){
    setLayout(new BorderLayout());

    jtable = new FilePanelJTable(uploadPolicy);

    model = new FilePanelDataModel2(uploadPolicy);
    jtable.setModel(model);

    TableColumnModel colModel = jtable.getColumnModel();
    for (int i = 0; i < model.getColumnCount(); i++) {
      colModel.getColumn(i).setPreferredWidth(model.getColumnSize(i));
    }

    JScrollPane scrollPane = new JScrollPane(jtable);
    add( scrollPane, BorderLayout.CENTER );

    new DropTarget(scrollPane, new DnDListener(jup));
  }

  public void addFiles(File[] f){
    if(null != f){
      for(int i = 0; i < f.length; i++){
        addDirectoryFiles(f[i]);
      }
    }
  }

  private void addDirectoryFiles(File f){
    if(!f.isDirectory()){
      addFileOnly(f);
    }else{
      File[] dirFiles = f.listFiles();
      for(int i = 0 ; i < dirFiles.length; i++){
        if(dirFiles[i].isDirectory()){
          addDirectoryFiles(dirFiles[i]);
        }else{
          addFileOnly(dirFiles[i]);
        }
      }
    }
  }

  private void addFileOnly(File f){
    // Make sure we don't select the same file twice.
    if (!model.contains(f)) {
      model.addFile(f);
    }
  }

  public FileData[] getFiles(){
    FileData[] files = new FileData[getFilesLength()];
    for(int i = 0; i < files.length; i++){
      files[i] = model.getFileDataAt(i);
    }
    return files;
  }

  public int getFilesLength(){
    return jtable.getRowCount();
  }

  public void removeSelected(){
    int[] rows = jtable.getSelectedRows();
    for(int i = rows.length - 1; 0 <= i; i--){
      model.removeRow(rows[i]);
    }
  }

  public void removeAll(){
    for (int i = getFilesLength() - 1; 0 <= i; i--) {
      model.removeRow(i);
    }
  }

}
