//
// $Id$
//
// jupload - A file upload applet.
// Copyright 2007 The JUpload Team
//
// Created: ?
// Creator: William JinHua Kwong
// Last modified: $Date$
//
// This program is free software; you can redistribute it and/or modify it under
// the terms of the GNU General Public License as published by the Free Software
// Foundation; either version 2 of the License, or (at your option) any later
// version. This program is distributed in the hope that it will be useful, but
// WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
// FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
// details. You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software Foundation, Inc.,
// 675 Mass Ave, Cambridge, MA 02139, USA.

package wjhk.jupload2.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Point;
import java.io.File;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.table.TableColumnModel;

import wjhk.jupload2.exception.JUploadExceptionStopAddingFiles;
import wjhk.jupload2.filedata.FileData;
import wjhk.jupload2.policies.UploadPolicy;

/**
 * Implementation of the FilePanel : it creates the
 * {@link wjhk.jupload2.gui.FilePanelJTable}, and handles the necessary
 * functionalities.
 * 
 * @author William JinHua Kwong
 * @version $Revision$
 */
public class FilePanelTableImp extends JPanel implements FilePanel {

    /** A generated serialVersionUID, to avoid warning during compilation */
    private static final long serialVersionUID = -8273990467324350526L;

    private FilePanelJTable jtable;

    private FilePanelDataModel2 model;

    /**
     * The current policy, always useful.
     */
    private UploadPolicy uploadPolicy = null;

    /**
     * The main panel of the applet.
     */
    private JUploadPanel juploadPanel = null;;

    /**
     * Creates a new instance.
     * 
     * @param juploadPanel The upload panel (parent).
     * @param uploadPolicy The upload policy to apply.
     */
    public FilePanelTableImp(JUploadPanel juploadPanel,
            UploadPolicy uploadPolicy) {
        this.juploadPanel = juploadPanel;
        this.uploadPolicy = uploadPolicy;

        setLayout(new BorderLayout());
        addMouseListener(juploadPanel);
        setTransferHandler(juploadPanel.getTransferHandler());

        this.jtable = new FilePanelJTable(juploadPanel, uploadPolicy);

        this.model = new FilePanelDataModel2(uploadPolicy);
        this.jtable.setModel(this.model);

        TableColumnModel colModel = this.jtable.getColumnModel();
        for (int i = 0; i < this.model.getColumnCount(); i++) {
            colModel.getColumn(i)
                    .setPreferredWidth(this.model.getColumnSize(i));
        }

        JScrollPane scrollPane = new JScrollPane(this.jtable);
        add(scrollPane, BorderLayout.CENTER);
        scrollPane.addMouseListener(juploadPanel);
    }

    /**
     * @see wjhk.jupload2.gui.FilePanel#addFiles(java.io.File[],java.io.File)
     */
    public final void addFiles(File[] f, File root) {

        if (null != f) {
            try {
                for (int i = 0; i < f.length; i++) {
                    addDirectoryFiles(f[i], root);
                }
            } catch (JUploadExceptionStopAddingFiles e) {
                // The user want to stop here. Nothing else to do.
                this.uploadPolicy.displayWarn(getClass().getName()
                        + ".addFiles() [" + e.getClass().getName() + "]: "
                        + e.getMessage());
            }
        }
        this.juploadPanel.updateButtonState();
    }

    /**
     * This method allows a recursive calls through the file hierarchy.
     * 
     * @param f
     * @param root
     * @throws JUploadExceptionStopAddingFiles
     */
    private final void addDirectoryFiles(File f, File root)
            throws JUploadExceptionStopAddingFiles {
        if (!f.isDirectory()) {
            addFileOnly(f, root);
        } else {
            File[] dirFiles = f.listFiles();
            for (int i = 0; i < dirFiles.length; i++) {
                if (dirFiles[i].isDirectory()) {
                    addDirectoryFiles(dirFiles[i], root);
                } else {
                    addFileOnly(dirFiles[i], root);
                }
            }
        }
    }

    /**
     * Adds a single file into the file list.
     * 
     * @param f
     * @param root
     * @throws JUploadExceptionStopAddingFiles
     */
    private final void addFileOnly(File f, File root)
            throws JUploadExceptionStopAddingFiles {
        // Make sure we don't select the same file twice.
        if (!this.model.contains(f)) {
            this.model.addFile(f, root);
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

    /** @see wjhk.jupload2.gui.FilePanel#focusTable() */
    public final void focusTable() {
        if (0 < this.jtable.getRowCount())
            this.jtable.requestFocus();
    }

    /** @see wjhk.jupload2.gui.FilePanel#getFileDataAt(Point) */
    public FileData getFileDataAt(Point point) {
        int row = this.jtable.rowAtPoint(point);
        return this.model.getFileDataAt(row);
    }

    /**
     * Return the component on which drop event can occur. Used by
     * {@link JUploadPanel}, when initializing the DropTarget.
     * 
     * @return Component on which the drop event can occur.
     */
    public Component getDropComponent() {
        return this;
    }
}
