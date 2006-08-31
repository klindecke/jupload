package wjhk.jupload2.gui;

import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.io.File;
import java.util.List;


public class DnDListener implements DropTargetListener {

  //------------- INFORMATION --------------------------------------------
  public static final String TITLE = "JUpload DnDListener";
  public static final String DESCRIPTION =
      "Drap and Drop Listener.";
  public static final String AUTHOR = "William JinHua Kwong";

  public static final double VERSION = 0.1;
  public static final String LAST_MODIFIED = "22 January 2004";

  //------------- VARIABLES ----------------------------------------------
  private JUploadPanel jup;

  //------------- CONSTRUCTOR --------------------------------------------
  public DnDListener(JUploadPanel jup){
    this.jup = jup;
  }

  //------------- Public Functions ---------------------------------------
  public void dragEnter(DropTargetDragEvent e) {
    if(!e.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
      e.rejectDrag();
    }else{
      //e.acceptDrag(e.getDropAction());
    }
  }

  public void dragOver(DropTargetDragEvent e) {
  }

  public void dropActionChanged(DropTargetDragEvent e) {
  }

  public void drop(DropTargetDropEvent e) {
    if(!e.isDataFlavorSupported(DataFlavor.javaFileListFlavor)){
      e.rejectDrop();
    }else{
      e.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
      try{
        List fileList = (List) e.getTransferable().getTransferData(
            DataFlavor.javaFileListFlavor);
        jup.addFiles((File[])fileList.toArray());
        e.getDropTargetContext().dropComplete(true);
      }catch (IOException ioe) {
        ioe.printStackTrace();
        e.rejectDrop();
      }catch (UnsupportedFlavorException ufe) {
        ufe.printStackTrace();
        e.rejectDrop();
      }

    }
  }

  public void dragExit(DropTargetEvent e) {
  }
}
