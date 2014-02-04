//
// $Id: JUploadPanel.java 303 2007-07-21 07:42:51 +0000 (sam., 21 juil. 2007)
// etienne_sf $
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

import java.awt.Frame;
import java.awt.dnd.DropTarget;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.TransferHandler;

import wjhk.jupload2.JUploadApplet;
import wjhk.jupload2.gui.filepanel.FilePanel;
import wjhk.jupload2.gui.filepanel.FilePanelTableImp;
import wjhk.jupload2.policies.UploadPolicy;
import wjhk.jupload2.upload.FileUploadManagerThread;

/**
 * Main code for the applet (or frame) creation. It contains all creation of
 * necessary elements, and calls to {@link wjhk.jupload2.policies.UploadPolicy}
 * methods to allow easy personalization. <BR>
 * This class remain in the current project structure, even if it's not really
 * used any more. The original reason for this class was that the code would
 * work from within a navigator (an applet) or from a standard java application. <BR>
 * This compatibility is no more maintained, as a lot of code suppose access to
 * navigator parameters. Hope it will be restored...
 *
 * @author William JinHua Kwong
 * @version $Revision$
 */
public class JUploadPanel extends JPanel implements ActionListener,
        MouseListener {

    /** A generated serialVersionUID, to avoid warning during compilation */
    private static final long serialVersionUID = -1212601012568225757L;

    /** The debug popup menu of the applet */
    private JUploadDebugPopupMenu jUploadDebugPopupMenu;

    /** The main popup menu of the applet */
    private JUploadMainPopupMenu jUploadMainPopupMenu;

    // ------------- VARIABLES ----------------------------------------------

    /**
     * The Drag and Drop listener, that will manage the drop event. All pplet
     * element should register this instance, so that the user see the whole
     * applet as a unique drop target.
     */
    private DnDListener dndListener = null;

    private JButton browseButton = null, removeButton = null,
            removeAllButton = null, uploadButton = null, stopButton = null;

    private JUploadFileChooser fileChooser = null;

    private FilePanel filePanel = null;

    private JProgressBar preparationProgressBar = null;

    private JProgressBar uploadProgressBar = null;

    private JLabel statusLabel = null;

    /**
     * The log window. It's created by {@link JUploadApplet}.
     */
    private JUploadTextArea logWindow = null;

    /**
     * The log window pane contains the log window, and the relevant scroll
     * bars. It's actually this pane that is displayed, as a view on the log
     * window.
     */
    private JScrollPane jLogWindowPane = null;

    private UploadPolicy uploadPolicy = null;

    private FileUploadManagerThread fileUploadManagerThread = null;

    // ------------- CONSTRUCTOR --------------------------------------------

    /**
     * Standard constructor.
     *
     * @param logWindow The log window that should already have been created.
     *            This allows putting text into it, before the effective
     *            creation of the layout.
     * @param uploadPolicyParam The current UploadPolicy. Null if a new one must
     *            be created.
     * @throws Exception
     */
    public JUploadPanel(JUploadTextArea logWindow,
            UploadPolicy uploadPolicyParam) throws Exception {
        this.logWindow = logWindow;
        this.uploadPolicy = uploadPolicyParam;
        this.jUploadDebugPopupMenu = new JUploadDebugPopupMenu(
                this.uploadPolicy);
        this.jUploadMainPopupMenu = new JUploadMainPopupMenu(this.uploadPolicy,
                this);

        // First: create standard components.
        createStandardComponents();
        logWindow.addMouseListener(this);

        // Then: display them on the applet
        this.uploadPolicy.addComponentsToJUploadPanel(this);

        // Define the drop target.
        this.dndListener = new DnDListener(this, this.uploadPolicy);
        new DropTarget(this, this.dndListener);
        new DropTarget(this.filePanel.getDropComponent(), this.dndListener);
        new DropTarget(this.logWindow, this.dndListener);

        // Define the TransfertHandler, to manage paste operations.
        JUploadTransferHandler jUploadTransfertHandler = new JUploadTransferHandler(
                this.uploadPolicy, this);
        this.setTransferHandler(jUploadTransfertHandler);
        this.filePanel.setTransferHandler(jUploadTransfertHandler);
        ActionMap map = this.getActionMap();
        map.put(TransferHandler.getPasteAction().getValue(Action.NAME),
                TransferHandler.getPasteAction());

        // The JUploadPanel will listen to Mouse messages for the standard
        // component. The current only application of this, it the CTRL+Righ
        // Click, that triggers the popup menu, which allow to switch debug on.
        this.browseButton.addMouseListener(this);
        this.removeAllButton.addMouseListener(this);
        this.removeButton.addMouseListener(this);
        this.stopButton.addMouseListener(this);
        this.uploadButton.addMouseListener(this);

        this.jLogWindowPane.addMouseListener(this);
        logWindow.addMouseListener(this);
        this.preparationProgressBar.addMouseListener(this);
        this.uploadProgressBar.addMouseListener(this);
        this.statusLabel.addMouseListener(this);

        // Setup File Chooser.
        try {
            this.fileChooser = uploadPolicyParam.createFileChooser();
        } catch (Exception e) {
            this.uploadPolicy.displayErr(e);
        }
    }

    // ----------------------------------------------------------------------

    /**
     * Creates all components used by the default upload policy. <BR>
     * You can change the component position of these components on the applet,
     * by creating a new upload policy, and override the
     * {@link UploadPolicy#addComponentsToJUploadPanel(JUploadPanel)} method.<BR>
     * You should keep these components, as there content is managed by the
     * internal code of the applet. <BR>
     * <U>Note:</U> this method will create component only if they were not
     * already created. That is only if the relevant attribute contain a null
     * value. If it's not the case, the already created component are keeped
     * unchanged.
     */
    private void createStandardComponents() {
        // -------- JButton browse --------
        if (this.browseButton == null) {
            this.browseButton = new JButton(this.uploadPolicy
                    .getLocalizedString("buttonBrowse"));
            this.browseButton.setIcon(new ImageIcon(getClass().getResource(
                    "/images/explorer.gif")));
        }
        this.browseButton.addActionListener(this);

        // -------- JButton remove --------
        if (this.removeButton == null) {
            this.removeButton = new JButton(this.uploadPolicy
                    .getLocalizedString("buttonRemoveSelected"));
            this.removeButton.setIcon(new ImageIcon(getClass().getResource(
                    "/images/recycle.gif")));
        }
        this.removeButton.setEnabled(false);
        this.removeButton.addActionListener(this);

        // -------- JButton removeAll --------
        if (this.removeAllButton == null) {
            this.removeAllButton = new JButton(this.uploadPolicy
                    .getLocalizedString("buttonRemoveAll"));
            this.removeAllButton.setIcon(new ImageIcon(getClass().getResource(
                    "/images/cross.gif")));
        }
        this.removeAllButton.setEnabled(false);
        this.removeAllButton.addActionListener(this);

        // -------- JButton upload --------
        if (null == this.uploadButton) {
            this.uploadButton = new JButton(this.uploadPolicy
                    .getLocalizedString("buttonUpload"));
            this.uploadButton.setIcon(new ImageIcon(getClass().getResource(
                    "/images/up.gif")));
        }
        this.uploadButton.setEnabled(false);
        this.uploadButton.addActionListener(this);

        // -------- The main thing: the file panel --------
        this.filePanel = new FilePanelTableImp(this, this.uploadPolicy);

        // -------- JProgressBar progress --------
        if (null == this.preparationProgressBar) {
            this.preparationProgressBar = new JProgressBar(
                    SwingConstants.HORIZONTAL);
            this.preparationProgressBar.setStringPainted(true);
        }
        if (null == this.uploadProgressBar) {
            this.uploadProgressBar = new JProgressBar(SwingConstants.HORIZONTAL);
            this.uploadProgressBar.setStringPainted(true);
        }

        // -------- JButton stop --------
        if (null == this.stopButton) {
            this.stopButton = new JButton(this.uploadPolicy
                    .getLocalizedString("buttonStop"));
            this.stopButton.setIcon(new ImageIcon(getClass().getResource(
                    "/images/cross.gif")));
        }
        this.stopButton.setEnabled(false);
        this.stopButton.addActionListener(this);

        // -------- JButton stop --------
        if (this.jLogWindowPane == null) {
            this.jLogWindowPane = new JScrollPane();
            this.jLogWindowPane
                    .setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
            this.jLogWindowPane
                    .setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        }
        this.jLogWindowPane.getViewport().add(this.logWindow);
        this.jLogWindowPane.setPreferredSize(null);

        // -------- statusLabel --------
        this.statusLabel = new JLabel("JUpload applet V"
                + this.uploadPolicy.getContext().getVersion());
    }

    /**
     * This methods show or hides the logWindow, depending on the following
     * applet parameters. The following conditions must be met, to hide the log
     * window: <DIR> <LI>showLogWindow (must be False) <LI>debugLevel (must be 0
     * or less) </DIR>
     */
    public void showOrHideLogWindow() {
        if ((this.uploadPolicy.getShowLogWindow()
                .equals(UploadPolicy.SHOWLOGWINDOW_TRUE))
                || (this.uploadPolicy.getShowLogWindow().equals(
                        UploadPolicy.SHOWLOGWINDOW_ONERROR) && this.uploadPolicy
                        .getLastException() != null)) {
            // The log window should be visible.
            this.jLogWindowPane.setVisible(true);
        } else {
            // It should be hidden.
            this.jLogWindowPane.setVisible(false);
        }
        // Let's recalculate the component display
        validate();
    }

    // ///////////////////////////////////////////////////////////////////////////////
    // ///////////////// Action methods
    // ///////////////////////////////////////////////////////////////////////////////

    /**
     * Reaction to a click on the browse button.
     */
    public void doBrowse() {
        // Browse clicked
        if (null != this.fileChooser) {
            try {
                int ret = this.fileChooser.showOpenDialog(new Frame());
                if (JFileChooser.APPROVE_OPTION == ret)
                    this.filePanel.addFiles(
                            this.fileChooser.getSelectedFiles(),
                            this.fileChooser.getCurrentDirectory());
                // We stop any running task for the JUploadFileView
                this.uploadPolicy.setCurrentBrowsingDirectory(this.fileChooser
                        .getCurrentDirectory().getAbsolutePath());
                this.fileChooser.shutdownNow();
            } catch (Exception ex) {
                this.uploadPolicy.displayErr(ex);
            }
        }
    }

    /**
     * Reaction to a click on the remove button. This method actually removes
     * the selected files in the file list.
     */
    public void doRemove() {
        this.filePanel.removeSelected();
        if (0 >= this.filePanel.getFilesLength()) {
            this.removeButton.setEnabled(false);
            this.removeAllButton.setEnabled(false);
            this.uploadButton.setEnabled(false);
        }
    }

    /**
     * Reaction to a click on the removeAll button. This method actually removes
     * all the files in the file list.
     */
    public void doRemoveAll() {
        this.filePanel.removeAll();
        this.removeButton.setEnabled(false);
        this.removeAllButton.setEnabled(false);
        this.uploadButton.setEnabled(false);
    }

    /**
     * Reaction to a click on the upload button. This method can be called from
     * outside to start the upload.
     */
    public void doStartUpload() {
        // Check that the upload is ready (we ask the uploadPolicy. Then,
        // we'll call beforeUpload for each
        // FileData instance, that exists in allFiles[].

        // ///////////////////////////////////////////////////////////////////////////////////////////////
        // IMPORTANT: It's up to the UploadPolicy to explain to the user
        // that the upload is not ready!
        // ///////////////////////////////////////////////////////////////////////////////////////////////
        try {
            if (this.uploadPolicy.beforeUpload()) {
                // The FileUploadManagerThread will manage everything around
                // upload, including GUI part.
                this.fileUploadManagerThread = new FileUploadManagerThread(
                        this.uploadPolicy);
                this.fileUploadManagerThread.start();
            } // if isIploadReady()
        } catch (Exception e) {
            // If an exception occurs here, it fails silently. The exception is
            // thrown to the AWT event dispatcher.
            this.uploadPolicy.displayErr(e.getClass().getName()
                    + " in JUploadPanel.doStartUpload()", e);
        }
    }

    /**
     * Reaction to a click on the stop button. This stops the running on upload.
     * This method can be called from outside to start the upload.
     */
    public void doStopUpload() {
        this.fileUploadManagerThread.stopUpload();
    }

    // ///////////////////////////////////////////////////////////////////////////////
    // ///////////////// Implementation of the ActionListener
    // ///////////////////////////////////////////////////////////////////////////////

    /**
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e) {
        // Let's log some info.
        this.uploadPolicy.displayDebug("Action : " + e.getActionCommand(), 1);

        final String actionPaste = (String) TransferHandler.getPasteAction()
                .getValue(Action.NAME);

        if (e.getActionCommand().equals(actionPaste)) {
            Action a = getActionMap().get(actionPaste);
            if (a != null) {
                a.actionPerformed(new ActionEvent(this.filePanel,
                        ActionEvent.ACTION_PERFORMED, null));
            }
        } else if (e.getActionCommand() == this.browseButton.getActionCommand()) {
            doBrowse();
        } else if (e.getActionCommand() == this.removeButton.getActionCommand()) {
            // Remove clicked
            doRemove();
        } else if (e.getActionCommand() == this.removeAllButton
                .getActionCommand()) {
            // Remove All clicked
            doRemoveAll();
        } else if (e.getActionCommand() == this.uploadButton.getActionCommand()) {
            // Upload clicked
            doStartUpload();
        } else if (e.getActionCommand() == this.stopButton.getActionCommand()) {
            // We request the thread to stop its job.
            doStopUpload();
        }
        // focus the table. This is necessary in order to enable mouse
        // events
        // for triggering tooltips.
        this.filePanel.focusTable();
    }

    // ///////////////////////////////////////////////////////////////////////////////
    // ///////////////// Implementation of the MouseListener
    // ///////////////////////////////////////////////////////////////////////////////

    /**
     * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
     */
    public void mouseClicked(MouseEvent mouseEvent) {
        maybeOpenPopupMenu(mouseEvent);
    }

    /**
     * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
     */
    public void mouseEntered(MouseEvent mouseEvent) {
        maybeOpenPopupMenu(mouseEvent);
    }

    /**
     * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
     */
    public void mouseExited(MouseEvent mouseEvent) {
        maybeOpenPopupMenu(mouseEvent);
    }

    /**
     * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
     */
    public void mousePressed(MouseEvent mouseEvent) {
        maybeOpenPopupMenu(mouseEvent);
    }

    /**
     * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
     */
    public void mouseReleased(MouseEvent mouseEvent) {
        if (mouseEvent.getClickCount() == 2) {
            // We have a double-click. Let's tell it to the current upload
            // policy...
            this.uploadPolicy.onFileDoubleClicked(this.filePanel
                    .getFileDataAt(mouseEvent.getPoint()));
        } else {
            maybeOpenPopupMenu(mouseEvent);
        }
    }

    /**
     * This method opens the popup menu, if the mouseEvent is relevant. In this
     * case it returns true. Otherwise, it does nothing and returns false.
     *
     * @param mouseEvent The triggered mouse event.
     * @return true if the popup menu was opened, false otherwise.
     */
    public boolean maybeOpenPopupMenu(MouseEvent mouseEvent) {
        // Should we open one out of the numerous (2!) popup menus ?
        if (mouseEvent.isPopupTrigger()) {
            if ((mouseEvent.getModifiersEx() & InputEvent.CTRL_DOWN_MASK) == InputEvent.CTRL_DOWN_MASK) {
                // We open the debug menu
                if (this.jUploadDebugPopupMenu != null) {
                    this.jUploadDebugPopupMenu.show(mouseEvent.getComponent(),
                            mouseEvent.getX(), mouseEvent.getY());
                    return true;
                }
            } else {
                // Let's open the main popup menu
                if (this.jUploadMainPopupMenu != null) {
                    this.jUploadMainPopupMenu.show(mouseEvent.getComponent(),
                            mouseEvent.getX(), mouseEvent.getY());
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Select or unselect the applet buttons
     */
    public void updateButtonState() {
        if (this.fileUploadManagerThread != null
                && this.fileUploadManagerThread.isAlive()
                && !this.fileUploadManagerThread.isUploadFinished()) {
            // An upload is running on.
            this.browseButton.setEnabled(false);
            this.removeButton.setEnabled(false);
            this.removeAllButton.setEnabled(false);
            this.uploadButton.setEnabled(false);
            this.stopButton.setEnabled(true);
        } else {
            // No upload running on.
            this.browseButton.setEnabled(true);
            this.stopButton.setEnabled(false);

            boolean enabled = (this.filePanel.getFilesLength() > 0);
            this.removeButton.setEnabled(enabled);
            this.removeAllButton.setEnabled(enabled);
            this.uploadButton.setEnabled(enabled);
        }

    }

    /** Clear the current log window content. */
    public void clearLogWindow() {
        this.logWindow.setText("");
    }

    /**
     * Copy the log window content into the clipboard. Allows easy access to the
     * debug output.
     *
     */
    public void copyLogWindow() {
        this.logWindow.selectAll();
        this.logWindow.copy();
    }

    /**
     * @return the browseButton
     */
    public JButton getBrowseButton() {
        return this.browseButton;
    }

    /**
     * @return the dndListener
     */
    public DnDListener getDndListener() {
        return this.dndListener;
    }

    /**
     * @return the filePanel
     */
    public FilePanel getFilePanel() {
        return this.filePanel;
    }

    /**
     * The component that contains the log window. It is used to display the
     * content of the log window, with the relevant scroll bars.
     *
     * @return the jLogWindowPane
     */
    public JScrollPane getJLogWindowPane() {
        return this.jLogWindowPane;
    }

    /**
     * Get the log window, that is: the component where messages (debug, info,
     * error...) are written. You should not use this component directly, but:
     * <UL>
     * <LI>To display messages: use the UploadPolicy.displayXxx methods.
     * <LI>To place this component on the applet, when overriding the
     * {@link UploadPolicy#addComponentsToJUploadPanel(JUploadPanel)} method:
     * use the {@link #getJLogWindowPane()} method instead. The
     * {@link #logWindow} is embbeded in it.
     * </UL>
     *
     * @return the logWindow
     */
    protected JUploadTextArea getLogWindow() {
        return this.logWindow;
    }

    /**
     * @return the preparationProgressBar
     */
    public JProgressBar getPreparationProgressBar() {
        return this.preparationProgressBar;
    }

    /**
     * @return the uploadProgressBar
     */
    public JProgressBar getUploadProgressBar() {
        return this.uploadProgressBar;
    }

    /**
     * @return the removeAllButton
     */
    public JButton getRemoveAllButton() {
        return this.removeAllButton;
    }

    /**
     * @return the removeButton
     */
    public JButton getRemoveButton() {
        return this.removeButton;
    }

    /**
     * @return the statusLabel
     */
    public JLabel getStatusLabel() {
        return this.statusLabel;
    }

    /**
     * @return the stopButton
     */
    public JButton getStopButton() {
        return this.stopButton;
    }

    /**
     * @return the uploadButton
     */
    public JButton getUploadButton() {
        return this.uploadButton;
    }

    /**
     * Standard setter for filePanel.
     *
     * @param filePanel
     */
    public void setFilePanel(FilePanel filePanel) {
        this.filePanel = filePanel;
    }

}
