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

package wjhk.jupload2.gui.filepanel;

import java.awt.Component;
import java.awt.Point;
import java.io.File;

import javax.swing.ActionMap;
import javax.swing.JComponent;
import javax.swing.TransferHandler;

import wjhk.jupload2.filedata.FileData;
import wjhk.jupload2.gui.JUploadPanel;

/**
 * Defines the interface used in the applet, when dealing with the file panel.
 */
public interface FilePanel {
    /**
     * Add multiple files to this panel.
     *
     * @param f An array of files to add.
     * @param root The top level of a directory hierarchy to add
     */
    public void addFiles(File[] f, File root);

    /**
     * Retrieve all currently stored files.
     *
     * @return an array of files, currently managed by this instance.
     */
    public FileData[] getFiles();

    /**
     * Retrieve the number of file entries in the JTable.
     *
     * @return the current number of files, held by this instance.
     */
    public int getFilesLength();

    /**
     * Removes all currently selected file entries.
     */
    public void removeSelected();

    /**
     * Removes all file entries.
     */
    public void removeAll();

    /**
     * Remove a specified file entry.
     *
     * @param fileData The file to be removed.
     */
    public void remove(FileData fileData);

    /**
     * Clears the current selection of the JTable.
     */
    public void clearSelection();

    /**
     * Requests focus for the JTable.
     */
    public void focusTable();

    /**
     * Ask for the file contained below the specific point on the screen.
     *
     * @param point The point
     * @return The return instance of File.
     */
    public FileData getFileDataAt(Point point);

    /**
     * Return the component on which drop event can occur. Used by
     * {@link JUploadPanel}, when initializing the DropTarget.
     *
     * @return The drop component target
     */
    public Component getDropComponent();

    /**
     * Transfer handler, to manage copy/paste operations.
     *
     * @param newHandler
     * @see JComponent#setTransferHandler(TransferHandler)
     */
    public void setTransferHandler(TransferHandler newHandler);

    /**
     * Allows to get standard action map, like paste action.
     *
     * @return Get the current actionMap
     * @see JComponent#getActionMap()
     */
    public ActionMap getActionMap();

}