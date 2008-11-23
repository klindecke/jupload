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

package wjhk.jupload2.upload;

/**
 * This interface defines the methods of the various FileUploadThread classes.
 * These classes are kept in the CVS, as people often update them for their
 * needs: I don't want to remove them, when I do a 'big bang' within them.
 * Created on 21 nov. 06
 */
public interface FileUploadThread {

    /**
     * Get the server response message. In HTTP mode, it's the body part,
     * without the HTTP headers.<BR>
     * Note: was getResponseMsg until release 3.4.1.
     * 
     * @return The String that contains the HTTP response message (e.g.
     *         "SUCCESS")
     */
    public String getResponseMsg();

    /**
     * Closes the connection to the server and releases resources.
     */
    public void close();

    /**
     * @return true if the thread is currently working.
     * @see java.lang.Thread#isAlive()
     */
    public boolean isAlive();

    /**
     * @throws InterruptedException
     * @see java.lang.Thread#join()
     */
    public void join() throws InterruptedException;

    /**
     * @param millisec
     * @throws InterruptedException
     * @see java.lang.Thread#join(long)
     */
    public void join(long millisec) throws InterruptedException;

    /**
     * @see java.lang.Thread#start()
     */
    public void start();

    /** @see java.lang.Thread#interrupt() */
    public void interrupt();
}
