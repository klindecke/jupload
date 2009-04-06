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
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;

/**
 * This class represents the text area for debug output.
 */
public class JUploadTextArea extends JTextArea implements ComponentListener {
    /**
     * This constant defines the upper limit of lines, kept in the log window.
     */
    public final static int MAX_DEBUG_LINES = 10000;

    /** A generated serialVersionUID, to avoid warning during compilation */
    private static final long serialVersionUID = -6037767344615468632L;

    /**
     * A thread, that will be called in the EventDispatcherThread, to have a
     * tread-safe update of the GUI. This thread is responsible to display one
     * String.
     */
    class DisplayOneMessageThread extends Thread {
        String message;

        JUploadTextArea textArea;

        /**
         * @param message the String to display. May be null (nothing will be
         *            displayed). Just position the caret at the good place.
         * @param textArea
         */
        DisplayOneMessageThread(String message, JUploadTextArea textArea) {
            this.message = message;
            this.textArea = textArea;
        }

        public void run() {
            try {
                if (this.message != null) {
                    this.textArea.append(this.message);

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
                }

                // The end of the text, is the interesting part of it !
                this.textArea
                        .setCaretPosition(this.textArea.getText().length() - 1);

                // Let's display the changes to the user.
                this.textArea.repaint();

            } catch (Exception e) {
                // This should not happen !
                e.printStackTrace();
            }
        }
    }

    /**
     * An internal class, to manage the fact that Swing is not Thread safe: all
     * update to the GUI must be done in the EventDispatchThread. <BR>
     * This thread is responsible for collecting all messages to display on a
     * queue, and add them to the TextArea within the EventDispatchThread.
     */
    class DisplayMessageThread extends Thread {
        /**
         * The current text area, where text must be written.
         */
        JUploadTextArea jUploadTextArea;

        /**
         * The queue, that contains all messages to display.
         */
        Queue<String> messages = null;

        /**
         * Indicates whether the thread is working, or waiting for an incoming
         * message.
         */
        boolean waiting = false;

        DisplayMessageThread(JUploadTextArea textArea) {
            this.jUploadTextArea = textArea;
            messages = new ConcurrentLinkedQueue<String>();
        }

        /**
         * Add a string at the end of the queue of messages to display.
         * 
         * @param str the String to display. May be null (nothing will be
         *            displayed)
         */
        public void queueMessage(String str) {
            this.messages.add(str);
            // If the thread is currently 'blocked' in the sleep statement, we
            // interrupt it.
            if (waiting) {
                waiting = false;
                this.interrupt();
            }
        }

        public void run() {
            String nextMessage;
            while (!isInterrupted()) {
                nextMessage = this.messages.poll();
                if (nextMessage == null) {
                    // Currently: no message. We stop using CPU.
                    // If necessary, we'll get interrupted by the append method,
                    // here above.
                    waiting = true;
                    try {
                        sleep(1000);
                    } catch (InterruptedException e1) {
                        // Nothing to do here: we just have a message to
                        // display. This will be done in the loop of this
                        // while.
                    }
                    waiting = false;
                } else {
                    try {
                        DisplayOneMessageThread displayOneMessageThread = new DisplayOneMessageThread(
                                nextMessage, this.jUploadTextArea);
                        SwingUtilities.invokeAndWait(displayOneMessageThread);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }// while
        }
    };

    /**
     * The instance of the DisplayMessageThread class.
     */
    private DisplayMessageThread displayMessageThread = null;

    /*
     * 
     * final Runnable doHelloWorld = new Runnable() { public void run() {
     * System.out.println("Hello World on " + Thread.currentThread()); } };
     * 
     * Thread appThread = new Thread() { public void run() { try {
     * SwingUtilities.invokeAndWait(doHelloWorld); } catch (Exception e) {
     * e.printStackTrace(); } System.out.println("Finished on " +
     * Thread.currentThread()); } }; appThread.start();
     */

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

        addComponentListener(this);

        // Let's start the tread that will actually add text to the GUI, in a
        // thread safe mode.
        displayMessageThread = new DisplayMessageThread(this);
        displayMessageThread.start();
    }

    /**
     * Add a string to the queue of string to be added to the logWindow. This is
     * necessary, to manage the non-thread-safe Swing environment.
     * 
     * @param str The string to add, at the end of the JUploadTextArea.
     */
    public final synchronized void displayMsg(String str) {
        this.displayMessageThread.queueMessage(str);
        // System.out.println("  To display: "+str);
    }

    /** @see ComponentListener#componentHidden(ComponentEvent) */
    public void componentHidden(ComponentEvent e) {
        // Nothing to do
    }

    /** @see ComponentListener#componentMoved(ComponentEvent) */
    public void componentMoved(ComponentEvent e) {
        // Nothing to do
    }

    /** @see ComponentListener#componentResized(ComponentEvent) */
    public void componentResized(ComponentEvent e) {
        // Nothing to do
    }

    /** @see ComponentListener#componentShown(ComponentEvent) */
    public void componentShown(ComponentEvent e) {
        // The caret must be placed. A call to repaint doesn't help.
        // So: we just display an empty string.
        // Not clean: if anyone has a better solution. The problem is:
        // Comment the line below, and open the applet with logWindow visible.
        // You'll see the beginning of the text. Un-comment the next line, and
        // restart the applet: you'll see the last line (which is the interesting
        // one).
        displayMsg("");
    }

}