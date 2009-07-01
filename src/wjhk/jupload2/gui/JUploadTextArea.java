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


/**
 * This class represents the text area for debug output.
 */
public class JUploadTextArea extends JTextArea {
    /**
     * Duration, in millisecond, that the thread will wait before checking if
     * new messages are available.
     */
    private static int DURATION_BETWEEN_TWO_LOOPS = 100;

    /**
     * This constant defines the upper limit of lines, kept in the log window.
     */
    // public final static int MAX_DEBUG_LINES = 10000;
    /**
     * Maximum number of characters in the logWindow.
     */
    public final static int MAX_LOG_WINDOW_LENGTH = 800000;

    /**
     * The queue, that contains all messages to display. They will be displayed
     * by the {@link LogMessageThread} thread.
     */
    Queue<String> messages = null;

    /** A generated serialVersionUID, to avoid warning during compilation */
    private static final long serialVersionUID = -6037767344615468632L;

    /**
     * A thread, that will be called in the EventDispatcherThread, to have a
     * tread-safe update of the GUI. This thread is responsible to display one
     * String.
     */
    static class LogMessageThread extends Thread {

        /**
         * The ConcurrentLinkedQueue that'll contain the messages.
         */
        private Queue<String> messages;

        /**
         * The text area that'll contain the messages.
         */
        private JUploadTextArea textArea;

        /**
         * @param messages the queue, that will contain the messages to display.
         * @param textArea
         */
        LogMessageThread(Queue<String> messages, JUploadTextArea textArea) {
            this.messages = messages;
            this.textArea = textArea;
        }


        /** The run method of the Runnable Interface */
        public void run() {
            boolean someTextHasBeenAdded = false;
            String nextMessage = null;
            StringBuffer sbLogContent = new StringBuffer(this.textArea
                    .getText());
            String newLogContent = null;

            try {
                while (!isInterrupted()) {
                    // Let's add all available messages... if any. They may have
                    // been all consumed by a previous execution of this thread.
                    while ((nextMessage = this.messages.poll()) != null) {
                        someTextHasBeenAdded = true;
                        sbLogContent.append(nextMessage);
                    }
                    // If some text has been added, we may have to truncate the
                    // text, according to the max allowed size for it.
                    if (someTextHasBeenAdded) {
                        newLogContent = sbLogContent.toString();
                        int len = newLogContent.length();
                        if (len > JUploadTextArea.MAX_LOG_WINDOW_LENGTH) {
                            newLogContent = newLogContent.substring(len
                                    - MAX_LOG_WINDOW_LENGTH);
                            len = MAX_LOG_WINDOW_LENGTH;
                        }

                        this.textArea.setText(newLogContent);

                        // The end of the text, is the interesting part of it !
                        if (len > 0) {
                            this.textArea.setCaretPosition(len - 1);
                        }

                        // Let's display the changes to the user.
                        //this.textArea.repaint();
                    }

                    // Let's wait for a notification for next messages
                    sleep(DURATION_BETWEEN_TWO_LOOPS);

                }

            } catch (Exception e) {
                // This should not happen !
                e.printStackTrace();
            }
        }
    }

    /**
     * The thread, that will put messages in the debug log.
     */
    LogMessageThread logMessageThread = null;

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
        this.logMessageThread = new LogMessageThread(this.messages, this);
        this.logMessageThread.start();
    }

    /**
     * Add a string to the queue of string to be added to the logWindow. This is
     * necessary, to manage the non-thread-safe Swing environment.
     * 
     * @param str The string to add, at the end of the JUploadTextArea.
     */
    public final void displayMsg(String str) {
        this.messages.add(str);
    }

    /**
     * 
     * @see Object#finalize()
     */
    protected void finalize() {
        this.logMessageThread.interrupt();
    }

}