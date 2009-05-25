//
// $Id: FileUploadThreadFTP.java 136 2007-05-12 20:15:36 +0000 (sam., 12 mai
// 2007) etienne_sf $
//
// jupload - A file upload applet.
// Copyright 2007 The JUpload Team
//
// Created: 2007-12-11
// Creator: etienne_sf
// Last modified: $Date: 2007-07-21 09:42:51 +0200 (sam., 21 juil. 2007) $
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

package wjhk.jupload2.upload.helper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

import wjhk.jupload2.exception.JUploadIOException;
import wjhk.jupload2.policies.UploadPolicy;

/**
 * This class is a utility, which provide easy encoding for HTTP queries. The
 * way to use this class is: <OL TYPE=1> <LI>Instantiate a new object <LI>Append
 * data to it, using the append methods. Available for: String, byte[], other
 * ByteArrayEncode... <LI>Close the stream. This will prevent any new data to be
 * appended to it. The encoded length can now be calculated. <LI>Get the encoded
 * length. <LI>Get the encoded byte array </OL>
 * 
 * @author etienne_sf
 * 
 */

public class ByteArrayEncoderHTTP implements ByteArrayEncoder {

    /**
     * The default encoding. It can be retrieved with
     * {@link #getDefaultEncoding()}.
     */
    final static String DEFAULT_ENCODING = "UTF-8";

    /**
     * The boundary, to put between to post variables. Can not be changed during
     * the object 'life'.
     */
    private String bound = null;

    /**
     * The current encoding. Can not be changed during the object 'life'.
     */
    private String encoding = DEFAULT_ENCODING;

    /**
     * Indicate whether the encoder is closed or not. If closed, it's impossible
     * to append new data to it. If not closed, it's impossible to get the
     * encoded length or the encoded byte array.<BR>
     * <B>Note:</B> a closed byte array can not be re-opened.
     */
    boolean closed = false;

    /**
     * The actual array, which will collect the encoded bytes.
     */
    private ByteArrayOutputStream baos = new ByteArrayOutputStream();

    /**
     * The byte array length. Calculated when the ByteArrayOutput is closed.
     */
    private int encodedLength = -1;

    /**
     * The encoded byte array. Calculated when the ByteArrayOutput is closed.
     */
    private byte[] encodedByteArray = null;

    /**
     * The current upload policy.
     */
    private UploadPolicy uploadPolicy;

    /**
     * The writer, that will encode the input parameters to {@link #baos}.
     */
    private Writer writer;

    // ///////////////////////////////////////////////////////////////////////
    // //////////////// VARIOUS UTILITIES ////////////////////////////////////
    // ///////////////////////////////////////////////////////////////////////

    // ///////////////////////////////////////////////////////////////////////
    // //////////////// CONSTRUCTORS /////////////////////////////////////////
    // ///////////////////////////////////////////////////////////////////////

    /**
     * Create an encoder, using the DEFAULT_ENCODING encoding.
     * 
     * @param uploadPolicy The current upload policy
     * @throws JUploadIOException Any IO exception
     */
    public ByteArrayEncoderHTTP(UploadPolicy uploadPolicy)
            throws JUploadIOException {
        init(uploadPolicy, null, DEFAULT_ENCODING);
    }

    /**
     * Create an encoder, and specifies the encoding to use.
     * 
     * @param uploadPolicy The current upload policy
     * @param bound Any specific boundary. May be null: in this case a default
     *            boundary is used.
     * @throws JUploadIOException Any IO exception
     */
    public ByteArrayEncoderHTTP(UploadPolicy uploadPolicy, String bound)
            throws JUploadIOException {
        init(uploadPolicy, bound, DEFAULT_ENCODING);
    }

    /**
     * Create an encoder, and specifies the boundary and encoding to use.
     * 
     * @param uploadPolicy The current upload policy
     * @param bound Any specific boundary. May be null: in this case a default
     *            boundary is used.
     * @param encoding The encoding to use. For instance, "UTF-8".
     * @throws JUploadIOException Any IO exception
     */
    public ByteArrayEncoderHTTP(UploadPolicy uploadPolicy, String bound,
            String encoding) throws JUploadIOException {
        init(uploadPolicy, bound, encoding);
    }

    // ///////////////////////////////////////////////////////////////////////
    // //////////////// Public methods ///////////////////////////////////////
    // ///////////////////////////////////////////////////////////////////////

    /**
     * @throws JUploadIOException
     * @see wjhk.jupload2.upload.helper.ByteArrayEncoder#close()
     */
    synchronized public void close() throws JUploadIOException {
        if (isClosed()) {
            throw new JUploadIOException(
                    "Trying to close an already closed ByteArrayEncoded");
        }
        try {
            this.writer.close();
        } catch (IOException e) {
            throw new JUploadIOException(e);
        }
        this.encodedByteArray = this.baos.toByteArray();
        this.encodedLength = this.encodedByteArray.length;
        this.closed = true;
    }

    /** {@inheritDoc} */
    public ByteArrayEncoder append(String str) throws JUploadIOException {
        try {
            this.writer.append(str);
        } catch (IOException e) {
            throw new JUploadIOException(e);
        }
        // Returning the encoder allows calls like:
        // bae.append("qdqd").append("qsldqd"); (like StringBuffer)
        return this;
    }

    /** {@inheritDoc} */
    public ByteArrayEncoder append(int b) throws JUploadIOException {
        try {
            this.writer.flush();
            this.baos.write(b);
        } catch (IOException e) {
            throw new JUploadIOException(e);
        }
        // Returning the encoder allows calls like:
        // bae.append("qdqd").append("qsldqd"); (like StringBuffer)
        return this;
    }

    /** {@inheritDoc} */
    public ByteArrayEncoder append(byte[] b) throws JUploadIOException {
        try {
            this.writer.flush();
            this.baos.write(b);
        } catch (IOException e) {
            throw new JUploadIOException(e);
        }
        // Returning the encoder allows calls like:
        // bae.append("qdqd").append("qsldqd"); (like StringBuffer)
        return this;
    }

    /** {@inheritDoc} */
    public ByteArrayEncoder append(ByteArrayEncoder bae)
            throws JUploadIOException {
        this.append(bae.getEncodedByteArray());

        return this;
    }

    /** {@inheritDoc} */
    public ByteArrayEncoder appendTextProperty(String name, String value)
            throws JUploadIOException {
        this.append(this.bound).append("\r\n");
        this.append("Content-Disposition: form-data; name=\"").append(name)
                .append("\"\r\n");
        this.append("Content-Transfer-Encoding: 8bit\r\n");
        this.append("Content-Type: text/plain; ").append(this.getEncoding())
                .append("\r\n");
        // An empty line before the actual value.
        this.append("\r\n");
        // And then, the value!
        this.append(value).append("\r\n");

        return this;
    }

    /** {@inheritDoc} */
    public ByteArrayEncoder appendEndPropertyList() throws JUploadIOException {
        this.append(this.bound).append("--\r\n");
        return this;
    }

    /** {@inheritDoc} */
    public ByteArrayEncoder appendFormVariables(String formname)
            throws JUploadIOException {
        String action = "Entering ByteArrayEncoderHTTP.appendFormVariables()";
        try {
            this.uploadPolicy.displayDebug(
                    "Entering ByteArrayEncoderHTTP.appendFormVariables() [html form: "
                            + formname + "]", 80);
            // TODO Do not use getApplet() anymore, here.
            action = "win = netscape.javascript.JSObject.getWindow";
            netscape.javascript.JSObject win = netscape.javascript.JSObject
                    .getWindow(this.uploadPolicy.getContext().getApplet());

            action = "o = win.eval";
            Object o = win.eval("document." + formname + ".elements.length");
            if (o instanceof Number) {
                int len = ((Number) o).intValue();
                if (len <= 0) {
                    this.uploadPolicy.displayWarn("The specified form \""
                            + formname + "\" does not contain any elements.");
                }
                int i;
                for (i = 0; i < len; i++) {
                    try {
                        action = "name = win.eval";
                        Object name = win.eval("document." + formname + "[" + i
                                + "].name");
                        action = "value = win.eval";
                        Object value = win.eval("document." + formname + "["
                                + i + "].value");
                        action = "elementType = win.eval";
                        Object elementType = win.eval("document." + formname
                                + "[" + i + "].type");
                        action = "elementClass = win.eval";
                        // elementClass seems to be not supported by IE7
                        // The next line prevents formData to be sent to the
                        // server, when formData is used on IE7. Works fine with
                        // firefox.
                        // Object elementClass = win.eval("document." + formname
                        // + "[" + i + "].class");
                        action = "etype = win.eval";
                        Object etype = win.eval("document." + formname + "["
                                + i + "].type");
                        if (etype instanceof String) {
                            String t = (String) etype;
                            if (t.equals("checkbox") || t.equals("radio")) {
                                action = "on = win.eval";
                                Object on = win.eval("document." + formname
                                        + "[" + i + "].checked");
                                if (on instanceof Boolean) {
                                    // Skip unchecked checkboxes and
                                    // radiobuttons
                                    if (!((Boolean) on).booleanValue())
                                        continue;
                                }

                            }
                        }
                        if (name instanceof String) {
                            if (value instanceof String) {
                                this.uploadPolicy.displayDebug(
                                        "  Trying to add formdata element num "
                                                + i + " (name: " + name
                                                + ", value: " + value
                                                + ", type: " + elementType
                                                /* + ", class: " + elementClass */
                                                + ")", 80);
                                this.appendTextProperty((String) name,
                                        (String) value);
                            } else {
                                this.uploadPolicy
                                        .displayWarn("[ByteArrayEncoder.appendFormVariables] value must be an instance of String (name: "
                                                + name
                                                + ", value: )"
                                                + value
                                                + ")");
                            }
                        } else {
                            this.uploadPolicy
                                    .displayWarn("[ByteArrayEncoder.appendFormVariables] name must be an instance of String (name: "
                                            + name + ", value: )" + value + ")");
                        }
                    } catch (netscape.javascript.JSException e1) {
                        this.uploadPolicy
                                .displayWarn(e1.getStackTrace()[1]
                                        + ": got JSException in ByteArrayEncoderHTTP.appendFormVariables() [html form: "
                                        + formname
                                        + "] - bailing out (action: " + action
                                        + ")");
                        i = len;
                    }
                }
            } else {
                this.uploadPolicy.displayWarn("The specified form \""
                        + formname + "\" could not be found.");
            }
        } catch (netscape.javascript.JSException e) {
            this.uploadPolicy.displayDebug(e.getStackTrace()[1]
                    + ": No JavaScript available (action: " + action + ")", 10);
        }
        return this;
    }

    /** {@inheritDoc} */
    public String getBoundary() {
        return this.bound;
    }

    /**
     * @return value of the DEFAULT_ENCODING constant.
     */
    public static String getDefaultEncoding() {
        return DEFAULT_ENCODING;
    }

    /** {@inheritDoc} */
    public boolean isClosed() {
        return this.closed;
    }

    /** {@inheritDoc} */
    public String getEncoding() {
        return this.encoding;
    }

    /** {@inheritDoc} */
    public int getEncodedLength() throws JUploadIOException {
        if (!isClosed()) {
            throw new JUploadIOException(
                    "Trying to get length of a on non-closed ByteArrayEncoded");
        }
        return this.encodedLength;
    }

    /** {@inheritDoc} */
    public byte[] getEncodedByteArray() throws JUploadIOException {
        if (!isClosed()) {
            throw new JUploadIOException(
                    "Trying to get the byte array of a on non-closed ByteArrayEncoded");
        }
        return this.encodedByteArray;
    }

    /** {@inheritDoc} */
    public String getString() throws JUploadIOException {
        if (!isClosed()) {
            throw new JUploadIOException(
                    "Trying to get the byte array of a on non-closed ByteArrayEncoded");
        }
        try {
            return new String(this.encodedByteArray, getEncoding());
        } catch (UnsupportedEncodingException e) {
            throw new JUploadIOException(e);
        }
    }

    // ///////////////////////////////////////////////////////////////////////
    // //////////////// Private methods //////////////////////////////////////
    // ///////////////////////////////////////////////////////////////////////

    /**
     * Initialization: called by the constructors.
     * 
     * @throws JUploadIOException
     */
    private void init(UploadPolicy uploadPolicy, String bound, String encoding)
            throws JUploadIOException {
        this.uploadPolicy = uploadPolicy;
        this.encoding = encoding;
        this.bound = bound;

        try {
            this.writer = new OutputStreamWriter(this.baos, encoding);
        } catch (UnsupportedEncodingException e) {
            throw new JUploadIOException(e);
        }
    }
}
