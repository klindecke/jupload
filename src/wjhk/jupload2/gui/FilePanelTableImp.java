package wjhk.jupload2.gui;

import java.awt.BorderLayout;
import java.awt.Panel;
import java.awt.dnd.DropTarget;
import java.io.File;

import javax.swing.JScrollPane;
import javax.swing.table.TableColumnModel;

import wjhk.jupload2.filedata.FileData;
import wjhk.jupload2.policies.UploadPolicy;

/**
 * Implementation of the FilePanel : it creates the
 * {@link wjhk.jupload2.gui.FilePanelJTable}, and handles the necessary
 * functionnalities.
 * 
 * @author William JinHua Kwong
 * @version $Revision$
 */
public class FilePanelTableImp extends Panel implements FilePanel {

    /**
     * 
     */
    private static final long serialVersionUID = -8273990467324350526L;

    // ------------- INFORMATION --------------------------------------------
    public static final String TITLE = "JUpload FilePanelTableImp";

    public static final String DESCRIPTION = "FilePanel Table Implementation.";

    public static final String AUTHOR = "William JinHua Kwong";

    public static final double VERSION = 1.0;

    public static final String LAST_MODIFIED = "$Date$";

    // ------------- VARIABLES ----------------------------------------------
    private FilePanelJTable jtable;

    private FilePanelDataModel2 model;

    /**
     * Creates a new instance.
     * @param jup The upload panel (parent).
     * @param uploadPolicy The upload policy to apply.
     */
    public FilePanelTableImp(JUploadPanel jup, UploadPolicy uploadPolicy) {
        setLayout(new BorderLayout());
        addMouseListener(jup);

        this.jtable = new FilePanelJTable(jup, uploadPolicy);

        this.model = new FilePanelDataModel2(uploadPolicy);
        this.jtable.setModel(this.model);

        TableColumnModel colModel = this.jtable.getColumnModel();
        for (int i = 0; i < this.model.getColumnCount(); i++) {
            colModel.getColumn(i).setPreferredWidth(this.model.getColumnSize(i));
        }

        JScrollPane scrollPane = new JScrollPane(this.jtable);
        add(scrollPane, BorderLayout.CENTER);
        scrollPane.addMouseListener(jup);

        new DropTarget(scrollPane, new DnDListener(jup));
    }

    /**
     * @see wjhk.jupload2.gui.FilePanel#addFiles(java.io.File[])
     */
    public final void addFiles(File[] f) {
        if (null != f) {
            for (int i = 0; i < f.length; i++) {
                addDirectoryFiles(f[i]);
            }
        }
    }

    private final void addDirectoryFiles(File f) {
        if (!f.isDirectory()) {
            addFileOnly(f);
        } else {
            File[] dirFiles = f.listFiles();
            for (int i = 0; i < dirFiles.length; i++) {
                if (dirFiles[i].isDirectory()) {
                    addDirectoryFiles(dirFiles[i]);
                } else {
                    addFileOnly(dirFiles[i]);
                }
            }
        }
    }

    private final void addFileOnly(File f) {
        // Make sure we don't select the same file twice.
        if (!this.model.contains(f)) {
            this.model.addFile(f);
        }
    }

    /**
     * @see wjhk.jupload2.gui.FilePanel#getFiles()
     */
    public final FileData[] getFiles() {
        FileData[] files = new FileData[getFilesLength()];
        for (int i = 0; i < files.length; i++) {
            files[i] = this.model.getFileDataAt(i);
        }
        return files;
    }

    /**
     * @see wjhk.jupload2.gui.FilePanel#getFilesLength()
     */
    public final int getFilesLength() {
        return this.jtable.getRowCount();
    }

    /**
     * @see wjhk.jupload2.gui.FilePanel#removeSelected()
     */
    public final void removeSelected() {
        int[] rows = this.jtable.getSelectedRows();
        for (int i = rows.length - 1; 0 <= i; i--) {
            this.model.removeRow(rows[i]);
        }
    }

    /**
     * @see java.awt.Container#removeAll()
     */
    @Override
    public final void removeAll() {
        for (int i = getFilesLength() - 1; 0 <= i; i--) {
            this.model.removeRow(i);
        }
    }

    /**
     * Removes all occurences of a file from the list. Each file should only
     * appear once here, but nobodody knows !
     * 
     * @param fileData The file to remove
     */
    public final void remove(FileData fileData) {
        this.model.removeRow(fileData);
    }

    /**
     * Clear the current selection in the JTable.
     */
    public final void clearSelection() {
        this.jtable.clearSelection();
    }

    /**
     * @see wjhk.jupload2.gui.FilePanel#focusTable()
     */
    public final void focusTable() {
        if (0 < this.jtable.getRowCount())
            this.jtable.requestFocus();
    }
}
