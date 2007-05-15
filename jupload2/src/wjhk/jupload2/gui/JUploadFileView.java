//
// $Id: JUploadFileView.java 112 2007-05-07 02:45:28 +0000 (lun., 07 mai 2007)
// felfert $
// 
// jupload - A file upload applet.
// Copyright 2007 The JUpload Team
// 
// Created: 2007-04-06
// Creator: Etienne Gauthier
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

import java.io.File;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.Icon;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileView;

import wjhk.jupload2.policies.UploadPolicy;

/**
 * The IconWorker class loads a icon from a file. It's called from a backup
 * thread created by the JUploadFileView class. This allows to load/calculate
 * icons in background. This prevent the applet to be freezed while icons are
 * loading.
 */
class IconWorker implements Runnable {

    /** The current upload policy */
    UploadPolicy uploadPolicy = null;

    /** The current file chooser. */
    JFileChooser fileChooser = null;

    /** The current file view */
    JUploadFileView fileView = null;

    /** The file whose icon must be loaded. */
    File file = null;

    /** The icon for this file. */
    Icon icon = null;

    /**
     * The constructor only stores the file. The background thread will call the
     * loadIcon method.
     * 
     * @param file The file whose icon must be loaded/calculated.
     */
    IconWorker(UploadPolicy uploadPolicy, JFileChooser fileChooser,
            JUploadFileView fileView, File file) {
        this.uploadPolicy = uploadPolicy;
        this.fileChooser = fileChooser;
        this.fileView = fileView;
        this.file = file;
    }

    /**
     * Returns the currently loaded icon for this file.
     * 
     * @return The Icon to be displayed for this file.
     */
    Icon getIcon() {
        this.uploadPolicy.displayDebug("In IconWorker.getIcon("
                + this.file.getName() + ")", 90);
        // We add the current worker to the task list.
        /*
         * ?? Hum, it should already be in the task list. I don't add it. if
         * (this.icon == null) { this.uploadPolicy.displayDebug(" Adding " +
         * this.file.getName() + " to the work list)", 90);
         * this.fileView.execute(this); }
         */
        return this.icon;
    }

    /** Get the icon from the current upload policy, for this file */
    void loadIcon() {
        this.uploadPolicy.displayDebug("In IconWorker.loadIcon("
                + this.file.getName() + ")", 90);
        File dir = null;
        File parent = null;
        try {
            // Maybe it has already been loaded.
            if (this.icon == null && !this.file.isDirectory()) {
                // Maybe the current directory changed. In this case, we
                // postpone the loading of this icon.
                dir = this.fileChooser.getCurrentDirectory();
                parent = this.file.getParentFile();
                // If dir and parent are null, they are equals, we calculate the
                // icon
                if (parent == null && dir == null) {
                    this.icon = this.uploadPolicy.fileViewGetIcon(this.file);
                    this.fileChooser.repaint();
                } else if (parent != null) {
                    if (dir.getAbsolutePath().equals(parent.getAbsolutePath())
                            || dir.isDirectory()) {
                        // If it's a directory, we instantly calculate the icon.
                        // The icon has not yet be loaded, and the user is still
                        // in this directory. Let's load the icon.
                        this.icon = this.uploadPolicy
                                .fileViewGetIcon(this.file);
                        this.fileChooser.repaint();
                    } else {
                        // We don't do it now, but we'll do it later.
                        this.uploadPolicy
                                .displayDebug("   Adding "
                                        + this.file.getName()
                                        + " to the task list", 90);
                        this.fileView.execute(this);
                    }
                }
                // Otherwise, one of 'parent' or 'dir' is null, we let the icon
                // to null.
            }
        } catch (NullPointerException e) {
            // No action, we mask the error
            this.uploadPolicy.displayWarn(e.getClass().getName()
                    + " in IconWorker.loadIcon. dir: " + dir + ", parent: "
                    + parent);
        }
    }

    /** Implementation of the Runnable interface */
    public void run() {
        loadIcon();
    }
}

/**
 * This class provides the icon view for the file selector.
 * 
 * @author Etienne Gauthier
 */
public class JUploadFileView extends FileView {

    /** The current upload policy. */
    UploadPolicy uploadPolicy = null;

    /** The current file chooser. */
    JFileChooser fileChooser = null;

    /** This map will contain all instances of {@link IconWorker}. */
    ConcurrentHashMap<String, IconWorker> hashMap = new ConcurrentHashMap<String, IconWorker>();

    ExecutorService executorService = null;

    /**
     * Creates a new instance.
     * 
     * @param uploadPolicy The upload policy to apply.
     * @param fileChooser The desired file chooser to use.
     */
    public JUploadFileView(UploadPolicy uploadPolicy, JFileChooser fileChooser) {
        this.uploadPolicy = uploadPolicy;
        this.fileChooser = fileChooser;
    }

    /**
     * @see javax.swing.filechooser.FileView#getIcon(java.io.File)
     */
    @Override
    public Icon getIcon(File file) {
        if (file.isDirectory()) {
            // we let the JVM display the system icon for directories.
            return null;
        }
        IconWorker iconWorker = this.hashMap.get(file.getAbsolutePath());
        if (iconWorker == null) {
            // This file has not been loaded.
            iconWorker = new IconWorker(this.uploadPolicy, this.fileChooser,
                    this, file);
            // We store it in the global Icon container.
            this.hashMap.put(file.getAbsolutePath(), iconWorker);
            // Then, we ask the current Thread to load its icon.
            execute(iconWorker);
            // We currently have no icon to display.
            return null;
        }
        return iconWorker.getIcon();
    }

    synchronized void execute(IconWorker iconWorker) {
        this.uploadPolicy.displayDebug("In JUploadFileView.execute for "
                + iconWorker.file.getAbsolutePath(), 90);
        if (this.executorService == null || this.executorService.isShutdown()) {
            this.uploadPolicy
                    .displayDebug(
                            "JUploadFileView.execute: creating the executorService",
                            90);
            this.executorService = Executors.newSingleThreadExecutor();
        }
        this.executorService.execute(iconWorker);
    }

    /**
     * Stop all current and to come thread. To be called when the file chooser
     * is closed.
     */
    synchronized public void shutdownNow() {
        if (this.executorService != null) {
            this.executorService.shutdownNow();
            this.uploadPolicy.displayDebug(
                    "JUploadFileView.shutdownNow (executorService->null)", 90);
            this.executorService = null;
        }
    }
}
