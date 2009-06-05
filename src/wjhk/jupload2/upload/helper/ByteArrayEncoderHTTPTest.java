package wjhk.jupload2.upload.helper;

import java.util.Arrays;

import wjhk.jupload2.JUploadDaemon;
import wjhk.jupload2.context.JUploadContext;
import wjhk.jupload2.exception.JUploadIOException;
import wjhk.jupload2.test.JUploadContextTest;
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

    final String testCase = "A string, with accents: שאיט\u00f8\u00e5\u00d8\u00e6";

    String boundary = "A boundary";

    String encoding = null;

    byte[] target = null;

    protected void setUp() throws Exception {
        super.setUp();
        juploadDaemon = new JUploadDaemon();
        juploadContext = new JUploadContextTest(juploadDaemon,
                "basicUploadPolicy.properties");
        byteArrayEncoderHTTP = new ByteArrayEncoderHTTP(juploadContext
                .getUploadPolicy(), boundary,
                ByteArrayEncoderHTTP.DEFAULT_ENCODING);
        encoding = this.byteArrayEncoderHTTP.getEncoding();
        target = testCase.getBytes(encoding);
    }

    protected void tearDown() throws Exception {
        super.tearDown();
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

    private void finishTestAppend() throws Exception {
        this.byteArrayEncoderHTTP.close();
        byte[] result = this.byteArrayEncoderHTTP.getEncodedByteArray();
        assertTrue(Arrays.equals(result, target));
    }

    /**
     * @throws Exception The test is wrong, if this exception is fired
     */
    public void testAppendString() throws Exception {
        this.byteArrayEncoderHTTP.append(testCase);
        finishTestAppend();
    }

    /**
     * @throws Exception The test is wrong, if this exception is fired
     */
    public void testAppendInt() throws Exception {
        this.byteArrayEncoderHTTP.append((int) 65);
        this.byteArrayEncoderHTTP.close();
        byte[] result = this.byteArrayEncoderHTTP.getEncodedByteArray();
        byte[] targetInt = {
            65
        };
        assertTrue(Arrays.equals(result, targetInt));
    }

    /**
     * @throws Exception The test is wrong, if this exception is fired
     */
    public void testAppendByteArray() throws Exception {
        this.byteArrayEncoderHTTP.append(target);
        finishTestAppend();
    }

    /**
     * @throws Exception The test is wrong, if this exception is fired
     */
    public void testAppendByteArrayEncoder() throws Exception {
        ByteArrayEncoderHTTP bae = new ByteArrayEncoderHTTP(this.juploadContext
                .getUploadPolicy());
        bae.append(this.testCase);
        bae.close();
        // append should throw an exception, if executed on a non-closed
        this.byteArrayEncoderHTTP.append(bae);
        finishTestAppend();
    }

    /**
     * @throws Exception The test is wrong, if this exception is fired
     */
    public void testAppendTextProperty() throws Exception {
        String name = "A name";
        String value = "A value";

        // First: calculate the result.
        StringBuffer sb = new StringBuffer();
        sb.append(this.byteArrayEncoderHTTP.getBoundary()).append("\r\n");
        sb.append("Content-Disposition: form-data; name=\"").append(name)
                .append("\"\r\n");
        sb.append("Content-Transfer-Encoding: 8bit\r\n");
        sb.append("Content-Type: text/plain; ").append(encoding).append("\r\n");
        sb.append("\r\n");
        sb.append(value).append("\r\n");
        this.target = sb.toString().getBytes(encoding);

        // Then, do the test.
        this.byteArrayEncoderHTTP.appendTextProperty(name, value);
        finishTestAppend();
    }

    /**
     * @throws Exception The test is wrong, if this exception is fired
     */
    public void testAppendEndPropertyList() throws Exception {
        // First: calculate the result.
        StringBuffer sb = new StringBuffer();
        sb.append(this.byteArrayEncoderHTTP.getBoundary()).append("--\r\n");
        this.target = sb.toString().getBytes(encoding);

        // Then, do the test.
        this.byteArrayEncoderHTTP.appendEndPropertyList();
        finishTestAppend();
    }

    /**
     * @throws Exception The test is wrong, if this exception is fired
     */
    public void testAppendFormVariables() throws Exception {
        fail("Not yet implemented"); // TODO
        finishTestAppend();
    }

    /**
     * @throws Exception The test is wrong, if this exception is fired
     */
    public void testGetBoundary() throws Exception {
        assertTrue("Boundary should be the one given on creation", boundary
                .equals(this.byteArrayEncoderHTTP.getBoundary()));
    }

    /**
     * @throws Exception The test is wrong, if this exception is fired
     */
    public void testGetDefaultEncoding() throws Exception {
        assertTrue("UTF-8".equals(ByteArrayEncoderHTTP.getDefaultEncoding()));
    }

    /**
     * @throws Exception The test is wrong, if this exception is fired
     */
    public void testIsClosed() throws Exception {
        assertTrue(this.byteArrayEncoderHTTP.isClosed() == this.byteArrayEncoderHTTP.closed);
    }

    /**
     * @throws Exception The test is wrong, if this exception is fired
     */
    public void testGetEncoding() throws Exception {
        assertTrue(this.byteArrayEncoderHTTP.getEncoding()
                .equals(this.encoding));
    }

    /**
     * @throws Exception The test is wrong, if this exception is fired
     */
    public void testGetEncodedLength() throws Exception {
        testAppendString();
        assertTrue(this.byteArrayEncoderHTTP.getEncodedLength() == this.target.length);
    }

    /**
     * @throws Exception The test is wrong, if this exception is fired
     */
    public void testGetEncodedByteArray() throws Exception {
        this.byteArrayEncoderHTTP.append("Any string");

        boolean aJUploadIOExceptionWasFired = false;
        try {
            this.byteArrayEncoderHTTP.getEncodedByteArray();
        } catch (JUploadIOException e) {
            aJUploadIOExceptionWasFired = true;
        }
        assertTrue(
                "An exception should be fired when getEncodedByteArray is called on a non closed ByteArray",
                aJUploadIOExceptionWasFired);
    }

}
