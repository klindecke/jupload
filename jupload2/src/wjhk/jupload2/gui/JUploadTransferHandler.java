package wjhk.jupload2.gui;

/**
 * The JUploadTransferHandler allows easy management of pasted files onto the
 * applet. It just checks that the pasted selection is compatible (that is: it's
 * a file list), and calls the addFile methods, to let the core applet work.
 */

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.TransferHandler;

import wjhk.jupload2.gui.filepanel.FilePanel;
import wjhk.jupload2.policies.UploadPolicy;

class JUploadTransferHandler extends TransferHandler implements ActionListener {

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
        FilePanel filePanel = this.uploadPanel.getFilePanel();
        if (canImport(c, t.getTransferDataFlavors())) {
            try {
                List<File> fileList = (List<File>) t
                        .getTransferData(this.fileListFlavor);
                Iterator<File> iterator = fileList.iterator();
                File[] fileArray = new File[1];
                while (iterator.hasNext()) {
                    fileArray[0] = iterator.next();
                    filePanel.addFiles(fileArray, null);
                }
                return true;
            } catch (UnsupportedFlavorException ufe) {
                System.out.println("importData: unsupported data flavor");
            } catch (IOException ioe) {
                System.out.println("importData: I/O exception");
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

    /**
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e) {
        String action = e.getActionCommand();
        String a = action;
        action = a;
        /*
         * this.uploadPolicy.getApplet().getUploadPanel().getFilePanel().actionPerformed(new
         * ActionEvent(focusOwner, ActionEvent.ACTION_PERFORMED, null));
         * 
         * ((JUploadPanel)this.uploadPolicy.getApplet().getUploadPanel()).actionPerformed(new
         * ActionEvent(this.uploadPolicy.getApplet().getUploadPanel(),
         * ActionEvent.ACTION_PERFORMED, (String) e.getActionCommand()));
         */
        // ((FilePanelTableImp)this.uploadPolicy.getApplet().getUploadPanel().getFilePanel());
    }
}
