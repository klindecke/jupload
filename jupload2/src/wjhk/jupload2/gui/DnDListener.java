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

import wjhk.jupload2.filedata.DefaultFileData;
import wjhk.jupload2.policies.UploadPolicy;

/**
 * Our implementation of DND.
 * 
 * @author William JinHua Kwong
 * @version $Release$
 */
public class DnDListener implements DropTargetListener {

    private JUploadPanel uploadPanel;

    private UploadPolicy uploadPolicy;

    /**
     * Creates a new instance.
     * 
     * @param uploadPanel The corresponding upload panel.
     * @param uploadPolicy
     */
    public DnDListener(JUploadPanel uploadPanel, UploadPolicy uploadPolicy) {
        this.uploadPanel = uploadPanel;
        this.uploadPolicy = uploadPolicy;
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
    @SuppressWarnings("unchecked")
    public void drop(DropTargetDropEvent e) {
        if (!e.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
            e.rejectDrop();
        } else {
            e.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
            try {
                List<File> fileList = (List<File>) e.getTransferable()
                        .getTransferData(DataFlavor.javaFileListFlavor);

                File[] fileArray = (File[]) fileList.toArray();
                this.uploadPanel.getFilePanel().addFiles(fileArray, DefaultFileData.getRoot(fileArray));

                e.getDropTargetContext().dropComplete(true);

                // Let's communicate this to the upload policy: there may be
                // something to do now.
                this.uploadPolicy.afterFileDropped(e);

            } catch (IOException ioe) {
                this.uploadPolicy.displayErr("DnDListener.drop()", ioe);
                e.rejectDrop();
            } catch (UnsupportedFlavorException ufe) {
                this.uploadPolicy.displayErr("DnDListener.drop()", ufe);
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
