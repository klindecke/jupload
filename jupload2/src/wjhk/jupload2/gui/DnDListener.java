package wjhk.jupload2.gui;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Our implementation of DND.
 * 
 * @author William JinHua Kwong
 */
public class DnDListener implements DropTargetListener {

    // ------------- INFORMATION --------------------------------------------
    public static final String TITLE = "JUpload DnDListener";

    public static final String DESCRIPTION = "Drap and Drop Listener.";

    public static final String AUTHOR = "William JinHua Kwong";

    public static final double VERSION = 0.1;

    public static final String LAST_MODIFIED = "22 January 2004";

    private JUploadPanel uploadPanel;

    /**
     * Creates a new instance.
     * 
     * @param uploadPanel The corresponding upload panel.
     */
    public DnDListener(JUploadPanel uploadPanel) {
        this.uploadPanel = uploadPanel;
    }

    /**
     * @see java.awt.dnd.DropTargetListener#dragEnter(java.awt.dnd.DropTargetDragEvent)
     */
    public void dragEnter(DropTargetDragEvent e) {
        if (!e.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
            e.rejectDrag();
        }
    }

    /**
     * @see java.awt.dnd.DropTargetListener#dragOver(java.awt.dnd.DropTargetDragEvent)
     */
    public void dragOver(@SuppressWarnings("unused")
    DropTargetDragEvent e) {
        // Nothing to do.
    }

    /**
     * @see java.awt.dnd.DropTargetListener#dropActionChanged(java.awt.dnd.DropTargetDragEvent)
     */
    public void dropActionChanged(@SuppressWarnings("unused")
    DropTargetDragEvent e) {
        // Nothing to do.
    }

    /**
     * @see java.awt.dnd.DropTargetListener#drop(java.awt.dnd.DropTargetDropEvent)
     */
    public void drop(DropTargetDropEvent e) {
        if (!e.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
            e.rejectDrop();
        } else {
            e.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
            try {
                List fileList = (List) e.getTransferable().getTransferData(
                        DataFlavor.javaFileListFlavor);
                this.uploadPanel.addFiles((File[]) fileList.toArray());
                e.getDropTargetContext().dropComplete(true);
            } catch (IOException ioe) {
                ioe.printStackTrace();
                e.rejectDrop();
            } catch (UnsupportedFlavorException ufe) {
                ufe.printStackTrace();
                e.rejectDrop();
            }

        }
    }

    /**
     * @see java.awt.dnd.DropTargetListener#dragExit(java.awt.dnd.DropTargetEvent)
     */
    public void dragExit(@SuppressWarnings("unused")
    DropTargetEvent e) {
        // Nothing to do.
    }
}
