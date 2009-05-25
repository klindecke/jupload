package wjhk.jupload2.upload.helper;

import wjhk.jupload2.JUploadDaemon;
import wjhk.jupload2.context.JUploadContext;
import wjhk.jupload2.context.JUploadContextExecutable;
import wjhk.jupload2.context.JUploadContextTest;
import junit.framework.TestCase;

/**
 * The JUnit test for the ByteArrayEncoderHTTP. Allows automation of tests, to
 * avoid regressions.
 * 
 * @author etienne_sf
 */
public class ByteArrayEncoderHTTPTest extends TestCase {
    JUploadDaemon juploadDaemon;

    JUploadContext juploadContext = null;

    ByteArrayEncoderHTTP byteArrayEncoderHTTP = null;

    protected void setUp() throws Exception {
        super.setUp();
        juploadDaemon = new JUploadDaemon();
        juploadContext = new JUploadContextTest(juploadDaemon,
                "basicUploadPolicy.properties");
        byteArrayEncoderHTTP = new ByteArrayEncoderHTTP(juploadContext
                .getUploadPolicy(), null, ByteArrayEncoderHTTP.DEFAULT_ENCODING);
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        byteArrayEncoderHTTP.close();
    }

    /**
     * @throws Exception The test is wrong, if this exception is fired
     */
    public void testByteArrayEncoderHTTPUploadPolicy() throws Exception {
        byteArrayEncoderHTTP.close();
        byteArrayEncoderHTTP = new ByteArrayEncoderHTTP(juploadContext
                .getUploadPolicy());
        // Nothing else to do, we just check the instance creation.
    }

    /**
     * @throws Exception The test is wrong, if this exception is fired
     */
    public void testByteArrayEncoderHTTPUploadPolicyString() throws Exception {
        byteArrayEncoderHTTP.close();
        byteArrayEncoderHTTP = new ByteArrayEncoderHTTP(juploadContext
                .getUploadPolicy(), null);
        byteArrayEncoderHTTP.close();
        byteArrayEncoderHTTP = new ByteArrayEncoderHTTP(juploadContext
                .getUploadPolicy(), "A boundary");
        // Nothing else to do, we just check the instance creation.
    }

    /**
     * @throws Exception The test is wrong, if this exception is fired
     */
    public void testByteArrayEncoderHTTPUploadPolicyStringString()
            throws Exception {
        byteArrayEncoderHTTP.close();
        byteArrayEncoderHTTP = new ByteArrayEncoderHTTP(juploadContext
                .getUploadPolicy(), "A boundary",
                ByteArrayEncoderHTTP.DEFAULT_ENCODING);
        // Nothing else to do, we just check the instance creation.
    }

    /**
     * @throws Exception The test is wrong, if this exception is fired
     */
    public void testAppendString() throws Exception {
        final String testCase="A string, with accents: שאיטü";
        String encoding = this.byteArrayEncoderHTTP.getEncoding();
        byte[] target = testCase.getBytes(encoding);
        
        // String
        this.byteArrayEncoderHTTP.append(testCase);
        this.byteArrayEncoderHTTP.close();
        assertTrue(this.byteArrayEncoderHTTP.getEncodedByteArray().equals(target));
    }

    /**
     * @throws Exception The test is wrong, if this exception is fired
     */
    public void testAppendInt() throws Exception {

        // int
        this.byteArrayEncoderHTTP.append((int) 65);
        fail("Not yet implemented"); // TODO
    }

    /**
     * @throws Exception The test is wrong, if this exception is fired
     */
    public void testAppendByteArray() throws Exception {
        byte[] b = new byte[1];
        b[0] = 66;
        this.byteArrayEncoderHTTP.append(b);
        fail("Not yet implemented"); // TODO
    }

    /**
     * @throws Exception The test is wrong, if this exception is fired
     */
    public void testAppendByteArrayEncoder() throws Exception {
        ByteArrayEncoderHTTP bae = new ByteArrayEncoderHTTP(this.juploadContext
                .getUploadPolicy());
        bae.append("Another ByteArrayEncoderHTTP");
        // append should throw an exception, if executed on a non-closed
        this.byteArrayEncoderHTTP.append(bae);

        fail("Not yet implemented"); // TODO
    }

    /**
     * @throws Exception The test is wrong, if this exception is fired
     */
    public void testAppendTextProperty() throws Exception {
        fail("Not yet implemented"); // TODO
    }

    /**
     * @throws Exception The test is wrong, if this exception is fired
     */
    public void testAppendEndPropertyList() throws Exception {
        fail("Not yet implemented"); // TODO
    }

    /**
     * @throws Exception The test is wrong, if this exception is fired
     */
    public void testAppendFormVariables() throws Exception {
        fail("Not yet implemented"); // TODO
    }

    /**
     * @throws Exception The test is wrong, if this exception is fired
     */
    public void testGetBoundary() throws Exception {
        fail("Not yet implemented"); // TODO
    }

    /**
     * @throws Exception The test is wrong, if this exception is fired
     */
    public void testGetDefaultEncoding() throws Exception {
        fail("Not yet implemented"); // TODO
    }

    /**
     * @throws Exception The test is wrong, if this exception is fired
     */
    public void testIsClosed() throws Exception {
        fail("Not yet implemented"); // TODO
    }

    /**
     * @throws Exception The test is wrong, if this exception is fired
     */
    public void testGetEncoding() throws Exception {
        fail("Not yet implemented"); // TODO
    }

    /**
     * @throws Exception The test is wrong, if this exception is fired
     */
    public void testGetEncodedLength() throws Exception {
        fail("Not yet implemented"); // TODO
    }

    /**
     * @throws Exception The test is wrong, if this exception is fired
     */
    public void testGetEncodedByteArray() throws Exception {
        fail("Not yet implemented"); // TODO
    }

    /**
     * @throws Exception The test is wrong, if this exception is fired
     */
    public void testGetString() throws Exception {
        fail("Not yet implemented"); // TODO
    }

    /**
     * @throws Exception The test is wrong, if this exception is fired
     */
    public void testObject() throws Exception {
        fail("Not yet implemented"); // TODO
    }

    /**
     * @throws Exception The test is wrong, if this exception is fired
     */
    public void testGetClass() throws Exception {
        fail("Not yet implemented"); // TODO
    }

    /**
     * @throws Exception The test is wrong, if this exception is fired
     */
    public void testHashCode() throws Exception {
        fail("Not yet implemented"); // TODO
    }

    /**
     * @throws Exception The test is wrong, if this exception is fired
     */
    public void testEquals() throws Exception {
        fail("Not yet implemented"); // TODO
    }

    /**
     * @throws Exception The test is wrong, if this exception is fired
     */
    public void testClone() throws Exception {
        fail("Not yet implemented"); // TODO
    }

    /**
     * @throws Exception The test is wrong, if this exception is fired
     */
    public void testToString() throws Exception {
        fail("Not yet implemented"); // TODO
    }

    /**
     * @throws Exception The test is wrong, if this exception is fired
     */
    public void testNotify() throws Exception {
        fail("Not yet implemented"); // TODO
    }

    /**
     * @throws Exception The test is wrong, if this exception is fired
     */
    public void testNotifyAll() throws Exception {
        fail("Not yet implemented"); // TODO
    }

    /**
     * @throws Exception The test is wrong, if this exception is fired
     */
    public void testWaitLong() throws Exception {
        fail("Not yet implemented"); // TODO
    }

    /**
     * @throws Exception The test is wrong, if this exception is fired
     */
    public void testWaitLongInt() throws Exception {
        fail("Not yet implemented"); // TODO
    }

    /**
     * @throws Exception The test is wrong, if this exception is fired
     */
    public void testWait() throws Exception {
        fail("Not yet implemented"); // TODO
    }

    /**
     * @throws Exception The test is wrong, if this exception is fired
     */
    public void testFinalize() throws Exception {
        fail("Not yet implemented"); // TODO
    }

}
