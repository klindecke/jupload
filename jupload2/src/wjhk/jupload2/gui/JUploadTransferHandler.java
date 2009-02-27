package wjhk.jupload2.gui;

/**
 * The JUploadTransferHandler allows easy management of pasted files onto the
 * applet. It just checks that the pasted selection is compatible (that is: it's
 * a file list), and calls the addFile methods, to let the core applet work.
 */

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.TransferHandler;

import wjhk.jupload2.filedata.DefaultFileData;
import wjhk.jupload2.policies.UploadPolicy;

class JUploadTransferHandler extends TransferHandler {

    /** A generated serialVersionUID, to avoid warning during compilation */
    private static final long serialVersionUID = -1241261479500810699L;

    DataFlavor fileListFlavor = DataFlavor.javaFileListFlavor;

    /**
     * The JUpload panel for this applet.
     */
    JUploadPanel uploadPanel = null;

    /**
     * The current upload policy.
     */
    UploadPolicy uploadPolicy = null;

    /**
     * The standard constructor.
     * 
     * @param uploadPolicy The current uploadPolicy
     * @param uploadPanel The JUploadPanel. Must given here, as this constructor
     *            is called in the JUploadPanel construction. So the
     *            uploadPolicy.getUploadPanel() returns null.
     */
    public JUploadTransferHandler(UploadPolicy uploadPolicy,
            JUploadPanel uploadPanel) {
        this.uploadPolicy = uploadPolicy;
        this.uploadPanel = uploadPanel;
    }

    /**
     * @see javax.swing.TransferHandler#importData(javax.swing.JComponent,
     *      java.awt.datatransfer.Transferable)
     */
    @Override
    @SuppressWarnings("unchecked")
    public boolean importData(JComponent c, Transferable t) {
        if (canImport(c, t.getTransferDataFlavors())) {
            try {
                List<File> fileList = (List<File>) t
                        .getTransferData(this.fileListFlavor);
                File[] fileArray = (File[]) fileList.toArray();
                this.uploadPanel.getFilePanel().addFiles(fileArray,
                        DefaultFileData.getRoot(fileArray));
                return true;
            } catch (UnsupportedFlavorException ufe) {
                this.uploadPolicy.displayErr(this.getClass().getName()
                        + ".importData()", ufe);
            } catch (IOException ioe) {
                this.uploadPolicy.displayErr(this.getClass().getName()
                        + ".importData()", ioe);
            }
        }
        return false;
    }

    /**
     * @see javax.swing.TransferHandler#getSourceActions(javax.swing.JComponent)
     */
    @Override
    public int getSourceActions(JComponent c) {
        return MOVE;
    }

    /**
     * @see javax.swing.TransferHandler#canImport(javax.swing.JComponent,
     *      java.awt.datatransfer.DataFlavor[])
     */
    @Override
    public boolean canImport(JComponent c, DataFlavor[] flavors) {
        for (int i = 0; i < flavors.length; i++) {
            if (this.fileListFlavor.equals(flavors[i])) {
                return true;
            }
        }
        return false;
    }
}
