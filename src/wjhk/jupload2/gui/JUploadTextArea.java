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
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.swing.JTextArea;

import wjhk.jupload2.policies.UploadPolicy;

/**
 * This class represents the text area for debug output.
 */
@SuppressWarnings("serial")
public class JUploadTextArea extends JTextArea {

    /**
     * Maximum number of characters in the logWindow.
     */
    public final static int MAX_LOG_WINDOW_LENGTH = 800000;

    /**
     * The size we truncate the output to, when the maximum size of debug output
     * is reach. We remove 20%.
     */
    public final static int SIZE_TO_TRUNCATE_TO = (int) (MAX_LOG_WINDOW_LENGTH * 0.8);

    /**
     * The current upload policy
     */
    UploadPolicy uploadPolicy;

    /**
     * The ConcurrentLinkedQueue that'll contain the messages.
     */
    private BlockingQueue<String> messages;

    /**
     * A thread, that will be called in the EventDispatcherThread, to have a
     * tread-safe update of the GUI. This thread is responsible to display one
     * String.
     */
    static class LogMessageThread extends Thread {

        /**
         * The text area that'll contain the messages.
         */
        private JUploadTextArea textArea;

        /**
         * Indicates whether the {@link LogMessageThread} should go on. Cleared
         * by the {@link #unload()} method.
         */
        boolean isRunning = true;

        /**
         * @param textArea
         */
        LogMessageThread(JUploadTextArea textArea) {
            this.textArea = textArea;
            setDaemon(true);
        }

        /**
         * The length of the current content of the JUploadTextArea.
         */
        int textLength = 0;

        /** The run method of the Runnable Interface */
        @Override
        public void run() {
            String nextMessage = null;

            while (true) {
                try {
                    nextMessage = this.textArea.messages.take();

                    // Ah, a new message has been delivered...

                    // If the current content is too long, we truncate it.
                    if (textLength > JUploadTextArea.MAX_LOG_WINDOW_LENGTH) {
                        String content = this.textArea.getText() + nextMessage;
                        String newContent = content.substring(content.length()
                                - SIZE_TO_TRUNCATE_TO, content.length());
                        this.textArea.setText(newContent);
                        textLength = SIZE_TO_TRUNCATE_TO;
                    } else {
                        // The result is not too long
                        this.textArea.append(nextMessage);
                        textLength += nextMessage.length();
                    }
                    this.textArea.setCaretPosition(textLength - 1);
                } catch (InterruptedException e) {
                    // If we're not running any more, then this 'stop' is not a
                    // problem any more. We're then just notified we must stop
                    // the thread.
                    if (this.isRunning) {
                        // This should not happen, and we can not put in the
                        // standard JUpload output, as this thread is
                        // responsible for it.
                        e.printStackTrace();
                    }
                }// try
            }// while
        }

        /**
         * Free any used ressources. Actually close the LogMessageThread thread.
         */
        public void unload() {
            this.isRunning = false;
            this.interrupt();
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
     * @param uploadPolicy The current uploadPolicy
     */
    public JUploadTextArea(int rows, int columns, UploadPolicy uploadPolicy) {
        super(rows, columns);
        this.uploadPolicy = uploadPolicy;
        this.messages = new LinkedBlockingQueue<String>();
        setBackground(new Color(255, 255, 203));
        setEditable(false);
        setLineWrap(true);
        setWrapStyleWord(true);

        // The queue, where messages to display will be posted.
        this.logMessageThread = new LogMessageThread(this);
        this.logMessageThread.setName(this.logMessageThread.getClass()
                .getName());
        this.logMessageThread.start();

        // The unload callback will be registered, once the uploadPolicy has
        // been built, by DefaultJUploadContext.init(JUploadApplet)
    }

    /**
     * Add a string to the queue of string to be added to the logWindow. This is
     * necessary, to manage the non-thread-safe Swing environment.
     *
     * @param str The string to add, at the end of the JUploadTextArea.
     */
    public final void displayMsg(String str) {
        try {
            // messages is a BlockingQueue. So the next line may 'block' the
            // applet main thread. But, we're optimistic: this should not happen
            // as we instanciate an unbound LinkedBlockingQueue. We'll be
            // blocked at Integer.MAX_VALUE, that is ... much after an
            // OutOfMemory is thrown !
            this.messages.put(str);
        } catch (InterruptedException e) {
            System.out.println("WARNING - [" + this.getClass().getName()
                    + "] Message lost due to " + e.getClass().getName() + " ("
                    + str + ")");
        }
    }

    /**
     * Free any used ressources. Actually close the LogMessageThread thread.
     */
    public void unload() {
        this.logMessageThread.unload();
    }
}