//
// $Id: JUploadTextArea.java 95 2007-05-02 03:27:05Z
// /C=DE/ST=Baden-Wuerttemberg/O=ISDN4Linux/OU=Fritz
// Elfert/CN=svn-felfert@isdn4linux.de/emailAddress=fritz@fritz-elfert.de $
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

import java.awt.Color;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;

/**
 * This class represents the text area for debug output.
 */
public class JUploadTextArea extends JTextArea {
    /**
     * This constant defines the upper limit of lines, kept in the log window.
     */
    public final static int MAX_DEBUG_LINES = 10000;

    /**
     * The queue, that contains all messages to display. They will be displayed
     * by the {@link DisplayOneMessageThread} thread.
     */
    Queue<String> messages = null;

    /** A generated serialVersionUID, to avoid warning during compilation */
    private static final long serialVersionUID = -6037767344615468632L;

    /**
     * A thread, that will be called in the EventDispatcherThread, to have a
     * tread-safe update of the GUI. This thread is responsible to display one
     * String.
     */
    class DisplayOneMessageThread implements Runnable {
        Queue<String> messages;

        JUploadTextArea textArea;

        /**
         * @param messages the queue, that will contain the messages to display.
         * @param textArea
         */
        DisplayOneMessageThread(Queue<String> messages, JUploadTextArea textArea) {
            this.messages = messages;
            this.textArea = textArea;
        }

        /** The run method of the Runnable Interface */
        public void run() {
            boolean someTextHasBeenAdded = false;
            String nextMessage = null;
            try {
                // Let's add all available messages... if any. They may have
                // been all consumed by a previous execution of this thread.
                while ((nextMessage = this.messages.poll()) != null) {
                    someTextHasBeenAdded = true;
                    this.textArea.append(nextMessage);
                }
                // If some text has been added, we may have to truncate the
                // text, according to the max allowed size for it.
                if (someTextHasBeenAdded) {
                    int lc = this.textArea.getLineCount();
                    if (lc > JUploadTextArea.MAX_DEBUG_LINES) {
                        int end;
                        try {
                            end = this.textArea.getLineEndOffset(lc
                                    - JUploadTextArea.MAX_DEBUG_LINES);
                            this.textArea.replaceRange("", 0, end);
                        } catch (BadLocationException e) {
                            e.printStackTrace();
                        }
                    }

                    // The end of the text, is the interesting part of it !
                    int len = this.textArea.getText().length();
                    if (len > 0) {
                        this.textArea.setCaretPosition(len - 1);
                    }

                    // Let's display the changes to the user.
                    this.textArea.repaint();
                }

            } catch (Exception e) {
                // This should not happen !
                e.printStackTrace();
            }
        }
    }

    /**
     * Constructs a new empty TextArea with the specified number of rows and
     * columns.
     * 
     * @param rows The desired number of text rows (lines).
     * @param columns The desired number of columns.
     */
    public JUploadTextArea(int rows, int columns) {
        super(rows, columns);
        setBackground(new Color(255, 255, 203));
        setEditable(false);
        setLineWrap(true);
        setWrapStyleWord(true);

        // The queue, where messages to display will be posted.
        this.messages = new ConcurrentLinkedQueue<String>();
    }

    /**
     * Add a string to the queue of string to be added to the logWindow. This is
     * necessary, to manage the non-thread-safe Swing environment.
     * 
     * @param str The string to add, at the end of the JUploadTextArea.
     */
    public final synchronized void displayMsg(String str) {
        // this.displayMessageThread.queueMessage(str);
        this.messages.add(str);
        DisplayOneMessageThread displayOneMessageThread = new DisplayOneMessageThread(
                this.messages, this);
        SwingUtilities.invokeLater(displayOneMessageThread);
    }
}