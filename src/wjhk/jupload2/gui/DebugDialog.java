//
// $Id: DebugDialog.java 298 2007-07-12 10:17:32 +0000 (jeu., 12 juil. 2007)
// etienne_sf $
//
// jupload - A file upload applet.
// Copyright 2007 The JUpload Team
//
// Created: 2006-07-10
// Creator: etienne_sf
// Last modified: $Date: 2008-04-16 09:58:02 +0200 (mer., 16 avr. 2008) $
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
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.text.Document;
import javax.swing.text.html.HTMLEditorKit;

import wjhk.jupload2.exception.JUploadIOException;
import wjhk.jupload2.policies.UploadPolicy;

/**
 * A maximized modal dialog box, that display the selected picture.
 * 
 * @author etienne_sf
 */
public class DebugDialog extends JDialog implements ActionListener {

    /** A generated serialVersionUID, to avoid warning during compilation */
    private static final long serialVersionUID = 7802205907550854333L;

    /**
     * The close button, which closes this dialog window.
     */
    JButton buttonClose;

    /**
     * The temporary file, that will contain the HTML response body.
     */
    File lastReponseBodyFile = null;

    /**
     * The current upload policy.
     */
    UploadPolicy uploadPolicy = null;

    /**
     * Creates a new instance.
     * 
     * @param owner The parent frame.
     * @param text The text to display. It can be HTML.
     * @param uploadPolicy The upload policy which applies.
     * @throws JUploadIOException
     */
    public DebugDialog(Frame owner, String text, UploadPolicy uploadPolicy)
            throws JUploadIOException {
        this.uploadPolicy = uploadPolicy;

        // Creation of the buttonClose button.
        this.buttonClose = new JButton(uploadPolicy
                .getLocalizedString("buttonClose"));
        this.buttonClose.setMaximumSize(new Dimension(100, 100));
        this.buttonClose.addActionListener(this);

        // Creation of the text (HTML) area
        JEditorPane editorPane = new JEditorPane();
        JScrollPane editorScrollPane = new JScrollPane(editorPane);
        editorScrollPane
                .setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        editorScrollPane.setPreferredSize(new Dimension(250, 145));
        editorScrollPane.setMinimumSize(new Dimension(10, 10));

        setText(editorPane, text);

        getContentPane().add(this.buttonClose, BorderLayout.SOUTH);
        getContentPane().add(editorScrollPane);

        try {
            pack();
        } catch (IllegalArgumentException e) {
            // This can happen, while parsing HTML.
            uploadPolicy
                    .displayWarn("IllegalArgumentException while packing the DebugWindow (bad HTML ?)");
            uploadPolicy.displayErr(e);
        }
        // Correction given by
        // setSize(getMaximumSize()); generate very high number under MAC OSX ->
        // Applet Crash
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setBounds(0, 0, screenSize.width, screenSize.height);

        // The dialog is modal: the next line will return when the DialogPicture
        // is hidden (to be closed, in our case)
        setTitle("JUpload DebugDialog: last response body");
        setVisible(true);
    }

    /**
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent event) {
        if (event.getActionCommand() == this.buttonClose.getActionCommand()) {
            this.uploadPolicy.displayDebug(
                    "[DebugDialog] Before this.dispose()", 50);
            this.dispose();
        }
    }

    /**
     * Set the text to display. If it's full HTML, beginning with a <!DOC tag,
     * this first tag and empty lines at the beginning are removed.
     * 
     * @param editorPane The target JEditorPane
     * @param text The text to save.
     * @throws JUploadIOException
     */
    public void setText(JEditorPane editorPane, String text)
            throws JUploadIOException {
        this.uploadPolicy.getContext().registerUnload(this, "deleteLog");
        try {
            // First: creation of a temporary file. This is necessary, as html
            // output is not correctly displayed in the JEditorPane, when using
            // the setText method. We need an URL to call the setPage one.
            this.lastReponseBodyFile = File.createTempFile("jupload_",
                    "_LRB.html");
            // Let's put our output within this temp file.
            FileOutputStream fos = new FileOutputStream(
                    this.lastReponseBodyFile);
            fos.write(text.getBytes());
            fos.close();
            // We can now call setPage(URL).
            java.net.URL lastResponseBodyLocalPage = this.lastReponseBodyFile
                    .toURI().toURL();
            editorPane.setEditable(false);
            editorPane.setPage(lastResponseBodyLocalPage);
            HTMLEditorKit ek = (HTMLEditorKit) editorPane.getEditorKit();
            Document doc = ek.createDefaultDocument();
            doc.putProperty("Base", "http://localhost/coppermine/");
        } catch (IOException e) {
            throw new JUploadIOException(e);
        }
    }

    /**
     * Delete the current log. (called upon applet termination)
     */
    public void deleteLog() {
        try {
            if (null != this.lastReponseBodyFile) {
                if (!this.lastReponseBodyFile.delete()) {
                    this.uploadPolicy
                            .displayWarn("Unable to delete this.lastReponseBodyFile ("
                                    + this.lastReponseBodyFile.getName() + ")");
                }
                this.lastReponseBodyFile = null;
            }
        } catch (Exception e) {
            // nothing to do
        }
    }

    /**
     * dispose all internal resources. Mainly: the temporary file.
     */
    @Override
    public void dispose() {
        super.dispose();
        deleteLog();
    }
}
