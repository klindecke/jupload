//
// $Id: JUploadFileView.java 112 2007-05-07 02:45:28 +0000 (lun., 07 mai 2007)
// felfert $
//
// jupload - A file upload applet.
// Copyright 2007 The JUpload Team
//
// Created: 2007-04-06
// Creator: etienne_sf
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

import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.Enumeration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileView;

import wjhk.jupload2.filedata.PictureFileData;
import wjhk.jupload2.policies.DefaultUploadPolicy;
import wjhk.jupload2.policies.PictureUploadPolicy;
import wjhk.jupload2.policies.UploadPolicy;

// //////////////////////////////////////////////////////////////////////////////////////////////////
// ///////////////////////////// local class: JUploadFileView
// //////////////////////////////////////////////////////////////////////////////////////////////////

/**
 * The IconWorker class loads a icon from a file. It's called from a backup
 * thread created by the JUploadFileView class. This allows to load/calculate
 * icons in background. This prevent the applet to be freezed while icons are
 * loading. <BR>
 * Instances of this class can have the following status, in this order: <DIR>
 * <LI>STATUS_NOT_LOADED: This icon is not loaded, and its loading is not
 * requested. This status is the default one, on creation. <LI>
 * STATUS_TO_BE_LOADED: This icon is on the list of icon to load. This status is
 * written by the JUploadFileView#execute(IconWorker) method. <LI>
 * STATUS_LOADING: Indicates the IconWorker#loadIcon() has been called, but is
 * not finished. <LI>STATUS_LOADED: The icon is loaded, and ready to be
 * displayed. <LI>STATUS_ERROR_WHILE_LOADING: Too bad, the applet could not load
 * the icon. It won't be tried again. </DIR>
 */
class IconWorker implements Runnable {

    /** Indicates that an error occurs, during the icon creation */
    final static int STATUS_ERROR_WHILE_LOADING = -1;

    /** Indicates that the icon for this file has been loaded */
    final static int STATUS_LOADED = 1;

    /**
     * Indicated that the creation of the icon for this file has started. But it
     * is not ready yet.
     */
    final static int STATUS_LOADING = 2;

    /**
     * Indicates the loading of the icon for this file has been requested, but
     * has not started yet.
     */
    final static int STATUS_TO_BE_LOADED = 3;

    /**
     * Indicates the loading of the icon for this file is not currently
     * requested. The loading may have been requested, then cancelled, for
     * instance of the user changes the current directory or closes the file
     * chooser.
     */
    final static int STATUS_NOT_LOADED = 4;

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

    /** Current loading status for this worker */
    int status = STATUS_NOT_LOADED;

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
        switch (this.status) {
            case STATUS_LOADED:
                return this.icon;
            case STATUS_NOT_LOADED:
                // ?? This picture should not be in this state. Perhaps the user
                // changes of directory, then went back to it.
                // We ask again to calculate its icon.
                this.fileView.execute(this);
                return JUploadFileView.emptyIcon;
            default:
                return JUploadFileView.emptyIcon;
        }// switch
    }// getIcon

    /**
     * Get the icon from the current upload policy, for this file. This methods
     * does something only if the current status for the icon is
     * {@link #STATUS_TO_BE_LOADED}. If not, this method does nothing.
     */
    void loadIcon() {
        try {
            if (this.status == STATUS_TO_BE_LOADED) {
                this.status = STATUS_LOADING;

                // This thread is of the lower possible priority. So we first
                // give a change for other thread to work
                Thread.yield();

                // This class is used only to do this call, in a separate
                // thread.
                this.icon = this.uploadPolicy.fileViewGetIcon(this.file);
                this.status = STATUS_LOADED;

                // This thread is of the lower possible priority. So we first
                // give a change for other thread to work
                Thread.yield();

                // Let's notify the fact the work is done.
                this.fileChooser.repaint();

                // A try to minimize memory footprint
                PictureFileData.freeMemory(this.getClass().getName()
                        + ".loadIcon()", this.uploadPolicy);
            }
        } catch (OutOfMemoryError e) {
            this.uploadPolicy
                    .displayWarn("OutOfMemoryError in IconWorker.loadIcon()");
            this.status = STATUS_ERROR_WHILE_LOADING;
            this.icon = null;
        }
    }

    /** Implementation of the Runnable interface */
    public void run() {
        loadIcon();
    }
}

// //////////////////////////////////////////////////////////////////////////////////////////////////
// ///////////////// JUploadFileView
// //////////////////////////////////////////////////////////////////////////////////////////////////

/**
 * This class provides the icon view for the file selector.
 *
 * @author etienne_sf
 */
public class JUploadFileView extends FileView implements
        PropertyChangeListener, ThreadFactory {

    /**
     * This thread group is used to contain all icon worker threads. Its
     * priority is the MIN_PRIORITY, to try to minimize CPU footprint. Its
     * thread max priority is set in the
     * {@link JUploadFileView#JUploadFileView(UploadPolicy, JFileChooser)}
     * constructor.
     */
    ThreadGroup iconWorkerThreadGroup = new ThreadGroup("JUpload ThreadGroup");

    /** The current upload policy. */
    UploadPolicy uploadPolicy = null;

    /** The current file chooser. */
    JFileChooser fileChooser = null;

    /** This map will contain all instances of {@link IconWorker}. */
    ConcurrentHashMap<String, IconWorker> hashMap = new ConcurrentHashMap<String, IconWorker>(
            1000, (float) 0.5, 3);

    /**
     * This executor will crate icons from files, one at a time. It is used to
     * create these icon asynchronously.
     *
     * @see #execute(IconWorker)
     */
    ExecutorService executorService = null;

    /**
     * An empty icon, having the good file size.
     */
    public static ImageIcon emptyIcon = null;

    /**
     * Creates a new instance.
     *
     * @param uploadPolicy The upload policy to apply.
     * @param fileChooser The desired file chooser to use.
     */
    public JUploadFileView(UploadPolicy uploadPolicy, JFileChooser fileChooser) {
        this.uploadPolicy = uploadPolicy;
        this.fileChooser = fileChooser;
        this.fileChooser.addPropertyChangeListener(this);

        // The real interest of the thread group, here, is to lower the priority
        // of the icon workers threads:
        this.iconWorkerThreadGroup.setMaxPriority(Thread.MIN_PRIORITY);

        // emptyIcon needs an upload policy, to be set, but we'll create it
        // only once.
        if (emptyIcon == null
                || emptyIcon.getIconHeight() != uploadPolicy
                        .getFileChooserIconSize()) {
            // The empty icon has not been calculated yet, or its size changed
            // since the icon creation. This can happen when the applet is
            // reloaded, and the applet parameter changed: the static attribute
            // are not recalculated.
            // Let's construct the resized picture.
            emptyIcon = new ImageIcon(new BufferedImage(uploadPolicy
                    .getFileChooserIconSize(), uploadPolicy
                    .getFileChooserIconSize(), BufferedImage.TYPE_INT_ARGB_PRE));
        }
    }

    synchronized void execute(IconWorker iconWorker) {
        if (this.executorService == null || this.executorService.isShutdown()) {
            this.executorService = Executors.newSingleThreadExecutor();
        }
        iconWorker.status = IconWorker.STATUS_TO_BE_LOADED;
        this.executorService.execute(iconWorker);
    }

    /**
     * Stop all current and to come thread. To be called when the file chooser
     * is closed.
     */
    synchronized public void shutdownNow() {
        if (this.executorService != null) {
            stopRunningJobs();

            this.executorService.shutdownNow();
            this.executorService = null;
        }
    }

    /**
     * Lazily mark all jobs as not done. No particular thread management.
     */
    private void stopRunningJobs() {
        Enumeration<IconWorker> e = this.hashMap.elements();
        IconWorker iw = null;
        while (e.hasMoreElements()) {
            iw = e.nextElement();
            if (iw.status == IconWorker.STATUS_TO_BE_LOADED) {
                iw.status = IconWorker.STATUS_NOT_LOADED;
            }
        }
    }

    /**
     * Waiting for JFileChooser events. Currently managed:
     * DIRECTORY_CHANGED_PROPERTY, to stop the to be loaded icons.
     *
     * @param e
     */
    public void propertyChange(PropertyChangeEvent e) {
        String prop = e.getPropertyName();
        // If the directory changed, don't show an image.
        if (JFileChooser.DIRECTORY_CHANGED_PROPERTY.equals(prop)) {
            // We stops all running job. If the user gets back to this
            // directory, the non calculated icons will be added to the job
            // list.
            this.uploadPolicy.displayDebug(
                    "[JUploadFileView] Directory changed", 50);
            stopRunningJobs();
        }
    }

    // ///////////////////////////////////////////////////////////////////////:
    // /////////////////////// Methods from the FileView class
    // ///////////////////////////////////////////////////////////////////////:

    /** #see javax.swing.filechooser.FileView#getDescription(File)) */
    @Override
    public String getDescription(File f) {
        return null; // let the L&F FileView figure this out
    }

    /**
     * The fileChooserIconFromFileContent applet parameter defies which icon is
     * to be returned here.
     *
     * @see javax.swing.filechooser.FileView#getIcon(java.io.File)
     * @see UploadPolicy#PROP_FILE_CHOOSER_ICON_FROM_FILE_CONTENT
     */
    @Override
    public Icon getIcon(File file) {
        // For DefaultUploadPolicy, a value of 1 means calculating the icon.
        // For PictureUploadPolicy and sisters, a value of 0 also means
        // calculating the icon
        // Otherwise: return null, for default icon.
        if (!((this.uploadPolicy.getFileChooserIconFromFileContent() == 1 && this.uploadPolicy instanceof DefaultUploadPolicy) || (this.uploadPolicy
                .getFileChooserIconFromFileContent() == 0 && this.uploadPolicy instanceof PictureUploadPolicy))) {
            return null;
        }
        // For PictureUploadPolicy and sisters, a value of 0
        if (file.isDirectory()) {
            // We let the L&F display the system icon for directories.
            return null;
        }
        IconWorker iconWorker = this.hashMap.get(file.getAbsolutePath());
        if (iconWorker == null) {
            // This file has not been loaded.
            iconWorker = new IconWorker(this.uploadPolicy, this.fileChooser,
                    this, file);
            // We store it in the global Icon container.
            this.hashMap.put(file.getAbsolutePath(), iconWorker);
            // Then, we ask the current Thread to load its icon. It will be done
            // later.
            execute(iconWorker);
            // We currently have no icon to display.
            return emptyIcon;
        }
        // Ok, let's take the icon.
        return iconWorker.getIcon();
    }

    /** #see {@link javax.swing.filechooser.FileView#getName(File)} */
    @Override
    public String getName(File f) {
        return null; // let the L&F FileView figure this out
    }

    /** #see {@link javax.swing.filechooser.FileView#getTypeDescription(File)} */
    @Override
    public String getTypeDescription(File f) {
        return null; // let the L&F FileView figure this out
    }

    /** #see {@link javax.swing.filechooser.FileView#isTraversable(File)} */
    @Override
    public Boolean isTraversable(File f) {
        return null; // let the L&F FileView figure this out
    }

    /**
     * Implementation of ThreadFactory. Creates a thread in the
     * iconWorkerThreadGroup thread group. This thread group has the lower
     * available priority.
     *
     * @param runnable The runnable instance to start.
     * @return The newly created thread
     */
    public Thread newThread(Runnable runnable) {
        Thread thread = new Thread(this.iconWorkerThreadGroup, runnable);
        thread.setPriority(Thread.MIN_PRIORITY);
        return thread;
    }
}
