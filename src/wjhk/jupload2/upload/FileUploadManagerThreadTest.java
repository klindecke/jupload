//
// $Id$
//
// jupload - A file upload applet.
//
// Copyright 2009 The JUpload Team
//
// Created: 28 mai 2009
// Creator: etienne_sf
// Last modified: $Date$
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.

package wjhk.jupload2.upload;

import java.awt.event.ActionEvent;
import java.io.File;

import junit.framework.TestCase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import wjhk.jupload2.JUploadDaemon;
import wjhk.jupload2.context.JUploadContext;
import wjhk.jupload2.exception.JUploadException;
import wjhk.jupload2.policies.UploadPolicy;
import wjhk.jupload2.test.FileUploadThreadStopDuringUpload;
import wjhk.jupload2.test.FileUploadThreadTestSuccess;
import wjhk.jupload2.test.JUploadContextTest;

/**
 * @author etienne_sf
 * 
 */
public class FileUploadManagerThreadTest extends TestCase {
    JUploadDaemon juploadDaemon;

    JUploadContext juploadContext = null;

    FileUploadThread fileUploadThread = null;

    FileUploadManagerThread fileUploadManagerThread = null;

    File fileroot = null;

    /**
     * The list of files that will be loaded. Initialized in {@link #setUp()}.
     */
    File[] filesToUpload = null;

    long uploadStartTime = -1;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        super.setUp();
        juploadDaemon = new JUploadDaemon();
        juploadContext = new JUploadContextTest(juploadDaemon,
                "basicUploadPolicy.properties");

        // Default setup is to upload a unique file.
        fileroot = new File(JUploadContextTest.TEST_FILES_FOLDER);
        setupFileArray(1);
        juploadContext.getUploadPanel().getFilePanel().addFiles(filesToUpload,
                fileroot);

        // Let's create the fake upload threads: if it was not created, or if it
        // has already run.
        if (fileUploadThread == null
                || Thread.State.TERMINATED.equals(fileUploadThread.getState())) {
            // If no fileUploadThread has been created, let's create a simple
            // one, which is the default for unit tests
            fileUploadThread = new FileUploadThreadTestSuccess(
                    this.juploadContext.getUploadPolicy());
        }
        fileUploadManagerThread = new FileUploadManagerThread(
                this.juploadContext.getUploadPolicy(), this.fileUploadThread);

        // Let's note the current system time. It should be almost the upload
        // start time.
        this.uploadStartTime = System.currentTimeMillis();
    }

    /**
     * Starts the upload, and wait for the {@link FileUploadThreadManager} to
     * finish.
     */
    private void executeUpload() throws Exception {
        fileUploadManagerThread.start();
        Thread.yield();
        Thread.sleep(50);
        fileUploadManagerThread.join();
    }

    /**
     * 
     */
    private void setupFileArray(int nbFiles) {
        filesToUpload = new File[nbFiles];
        String filename;
        for (int i = 0; i < nbFiles; i += 1) {
            filename = JUploadContextTest.TEST_FILES_FOLDER + "fichier_"
                    + (i + 1) + ".txt";
            filesToUpload[i] = new File(filename);
            // We must be able to load the file. Otherwise, it's useless to
            // start.
            // And there seems to be problem with user dir, depending on the
            // java tool used.

            assertTrue(filename + " must be readable !", filesToUpload[i]
                    .canRead());
        }
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
    }

    /**
     * Test method for
     * {@link wjhk.jupload2.upload.FileUploadManagerThread#run()}.
     * 
     * @throws Exception
     */
    @Test
    public void testRun() throws Exception {
        executeUpload();
        assertNull(this.fileUploadManagerThread.getUploadException());
    }

    /**
     * Test method for
     * {@link wjhk.jupload2.upload.FileUploadManagerThread#getNbUploadedFiles()}
     * .
     * 
     * @throws Exception
     */
    @Test
    public void testGetNbUploadedFiles() throws Exception {
        executeUpload();
        int n = this.fileUploadManagerThread.getNbUploadedFiles();
        boolean isUploadFinished = this.fileUploadManagerThread
                .isUploadFinished();
        assertTrue("Upload should be finished", isUploadFinished);
        assertEquals(1, n);
    }

    /**
     * Test method for
     * {@link wjhk.jupload2.upload.FileUploadManagerThread#getUploadStartTime()}
     * .
     * 
     * @throws Exception
     */
    @Test
    public void testGetUploadStartTime() throws Exception {
        executeUpload();
        final long errorMargin = DefaultFileUploadThread.TIME_BEFORE_CHECKING_NEXT_PACKET + 150;
        long realUploadStartTime = this.fileUploadManagerThread
                .getUploadStartTime();
        assertTrue("realUploadStartTime=" + realUploadStartTime
                + ", uploadStartTime=" + uploadStartTime,
                ((this.uploadStartTime - errorMargin) <= realUploadStartTime)
                        && (realUploadStartTime <= this.uploadStartTime
                                + errorMargin));
    }

    /**
     * Test method for
     * {@link wjhk.jupload2.upload.FileUploadManagerThread#setUploadException(wjhk.jupload2.exception.JUploadException)}
     * .
     */
    @Test
    public void testSetUploadException() {
        JUploadException jue = new JUploadException(
                "A test exception, to test FileUploadManagerThread.setUploadException()");
        fileUploadManagerThread.setUploadException(jue);
        assertTrue(jue == this.fileUploadManagerThread.getUploadException());
    }

    /**
     * Test method for
     * {@link wjhk.jupload2.upload.FileUploadManagerThread#getUploadException()}
     * .
     */
    @Test
    public void testGetUploadException() {
        testSetUploadException();
    }

    /**
     * Test method for
     * {@link wjhk.jupload2.upload.FileUploadManagerThread#isUploadFinished()}.
     * 
     * @throws Exception
     */
    @Test
    public void testIsUploadFinished() throws Exception {
        assertFalse(this.fileUploadManagerThread.isUploadFinished());
        executeUpload();
        assertTrue(this.fileUploadManagerThread.isUploadFinished());
    }

    /**
     * Test method for
     * {@link wjhk.jupload2.upload.FileUploadManagerThread#nbBytesUploaded(long)}
     * .
     * 
     * @throws Exception
     */
    @Test
    public void testNbBytesUploaded() throws Exception {
        UploadPolicy up = this.juploadContext.getUploadPolicy();
        this.fileUploadThread = new FileUploadThreadStopDuringUpload(up);
        setUp();
        fileUploadManagerThread.start();
        Thread
                .sleep(DefaultFileUploadThread.TIME_BEFORE_CHECKING_NEXT_PACKET + 150);

        // The simulated upload will top after one byte.
        long nbBytesAlreadyUploaded = fileUploadManagerThread.nbUploadedBytes;
        assertEquals(1, nbBytesAlreadyUploaded);

        // Then, the rest of the file is sent.
        this.fileUploadThread.join();// Wait for the simulated upload to finish.
        nbBytesAlreadyUploaded = fileUploadManagerThread.nbUploadedBytes;
        assertEquals(this.filesToUpload[0].length(), nbBytesAlreadyUploaded);
    }

    /**
     * Test method for
     * {@link wjhk.jupload2.upload.FileUploadManagerThread#stopUpload()}.
     * 
     * @throws Exception
     */
    @Test
    public void testStopUpload() throws Exception {
        // TODO Use FileUploadThreadStopDuringUpload
        this.fileUploadThread = new FileUploadThreadStopDuringUpload(
                this.juploadContext.getUploadPolicy());
        setUp();
        fileUploadManagerThread.start();
        fileUploadManagerThread.stopUpload();
        fileUploadManagerThread.join();
        assertFalse(this.filesToUpload[0].length() == fileUploadManagerThread.nbUploadedBytes);
    }

    /**
     * Test method for
     * {@link wjhk.jupload2.upload.FileUploadManagerThread#actionPerformed(java.awt.event.ActionEvent)}
     * .
     * 
     * @throws Exception
     */
    @Test
    public void testActionPerformed() throws Exception {
        final String dummyText = "A dummy text";
        final int dummyValue = 99;

        // Test update of the progress bar
        fileUploadManagerThread.uploadProgressBar.setString(dummyText);
        fileUploadManagerThread.uploadProgressBar.setValue(dummyValue);
        ActionEvent ae = new ActionEvent(
                fileUploadManagerThread.timerProgressBar, 1, "dummy event");
        fileUploadManagerThread.actionPerformed(ae);
        assertFalse(dummyText.equals(fileUploadManagerThread.uploadProgressBar
                .getString()));
        assertFalse(dummyValue == fileUploadManagerThread.uploadProgressBar
                .getValue());

        // Test update of the status bar
        this.fileUploadThread = new FileUploadThreadStopDuringUpload(
                this.juploadContext.getUploadPolicy());
        setUp();
        fileUploadManagerThread.start();
        fileUploadManagerThread.uploadPanel.getStatusLabel().setText(dummyText);
        assertEquals(dummyText, fileUploadManagerThread.uploadPanel
                .getStatusLabel().getText());
        ae = new ActionEvent(fileUploadManagerThread.timerStatusBar, 1,
                "dummy event");
        // fileUploadManagerThread.nbUploadedBytes must be more than 0
        fileUploadManagerThread.nbUploadedBytes = 1;
        fileUploadManagerThread.actionPerformed(ae);
        String currentStatusText = fileUploadManagerThread.uploadPanel
                .getStatusLabel().getText();
        assertFalse(dummyText.equals(currentStatusText));
    }

    /**
     * Test method for
     * {@link wjhk.jupload2.upload.FileUploadManagerThread#anotherFileHasBeenSent(wjhk.jupload2.filedata.FileData)}
     * .
     * 
     * @throws Exception
     */
    @Test
    public void testAnotherFileHasBeenSent() throws Exception {
        int nbSentFiles = fileUploadManagerThread.nbSentFiles;
        // int nbFilesBeingUploaded =
        // fileUploadManagerThread.nbFilesBeingUploaded;
        // long nbBytesUploadedForCurrentFile =
        // fileUploadManagerThread.nbBytesUploadedForCurrentFile;
        long nbBytesReadyForUpload = fileUploadManagerThread.nbBytesReadyForUpload;
        fileUploadManagerThread.uploadStatus = FileUploadManagerThread.UPLOAD_STATUS_UPLOADING;

        // TODO Use FileUploadThreadStopDuringUpload

        // We are finished with this one. Let's display it.
        executeUpload();
        assertTrue(nbSentFiles + 1 == fileUploadManagerThread.nbSentFiles);
        // In this test case, the upload is instantaneous. We can't check the
        // values when the file is being uploaded. So, no test on
        // nbFilesBeingUploaded and nbBytesUploadedForCurrentFile.
        assertEquals(0, fileUploadManagerThread.nbBytesReadyForUpload);
        assertEquals(fileUploadManagerThread.uploadStatus,
                FileUploadManagerThread.UPLOAD_STATUS_UPLOADED);
    }

    /**
     * Test method for
     * {@link wjhk.jupload2.upload.FileUploadManagerThread#currentRequestIsFinished(wjhk.jupload2.upload.UploadFileData[])}
     * .
     * 
     * @throws Exception
     */
    @Test
    public void testCurrentRequestIsFinished() throws Exception {
        // Test with one file.
        assertFalse(this.fileUploadManagerThread.isUploadFinished());
        executeUpload();
        assertTrue(this.fileUploadManagerThread.isUploadFinished());

        // Test with two files.
        setUp();
        setupFileArray(2);
        assertFalse(this.fileUploadManagerThread.isUploadFinished());
        executeUpload();
        assertTrue(this.fileUploadManagerThread.isUploadFinished());
    }

    /**
     * Test method for
     * {@link wjhk.jupload2.upload.FileUploadManagerThread#getNextPacket()}.
     * 
     * @throws Exception
     */
    @Test
    public void testGetNextPacket() throws Exception {
        UploadFileData[] fileDataTmp = this.fileUploadManagerThread
                .getNextPacket();
        assertNull(fileDataTmp);
        this.fileUploadManagerThread.start();
        // Let's wait for the file to be prepared.
        Thread.sleep(100);
        fileDataTmp = this.fileUploadManagerThread.getNextPacket();
        assertNotNull(fileDataTmp);
    }

}
