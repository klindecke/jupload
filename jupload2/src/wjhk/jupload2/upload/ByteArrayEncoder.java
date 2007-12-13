//
// $Id: FileUploadThreadFTP.java 136 2007-05-12 20:15:36 +0000 (sam., 12 mai
// 2007) etienne_sf $
// 
// jupload - A file upload applet.
// Copyright 2007 The JUpload Team
// 
// Created: 2007-12-11
// Creator: Etienne Gauthier
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

package wjhk.jupload2.upload;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

import wjhk.jupload2.exception.JUploadIOException;

/**
 * This class is a utility, which provide easy encoding for HTTP queries. The
 * way to use this class is:
 * <OL TYPE=1>
 * <LI>Instantiate a new object
 * <LI>Append data to it, using the append methods. Available for: String,
 * byte[], other ByteArrayEncode...
 * <LI>Close the stream. This will prevent any new data to be appended to it.
 * The encoded length can now be calculated.
 * <LI>Get the encoded length.
 * <LI>Get the encoded byte array
 * </OL>
 * 
 * @author etienne_sf
 * 
 */

public class ByteArrayEncoder {

    /**
     * The default encoding. It can be retrieved with
     * {@link #getDefaultEncoding()}.
     */
    private final static String DEFAULT_ENCODING = "UTF-8";

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
    private boolean closed = false;

    /**
     * The actual array, which will collect the encoded bytes.
     */
    private ByteArrayOutputStream baos = new ByteArrayOutputStream();

    /**
     * The writer, that will encode the input parameters to {@link #baos}.
     */
    private Writer writer;

    /**
     * The byte array length. Calculated when the ByteArrayOutput is closed.
     */
    private int encodedLength = -1;

    /**
     * The encoded byte array. Calculated when the ByteArrayOutput is closed.
     */
    private byte[] encodedByteArray = null;

    // ///////////////////////////////////////////////////////////////////////
    // //////////////// CONSTRUCTORS /////////////////////////////////////////
    // ///////////////////////////////////////////////////////////////////////

    /**
     * Create an encoder, using the {@link #DEFAULT_ENCODING} encoding.
     */
    public ByteArrayEncoder() throws JUploadIOException {
        init(DEFAULT_ENCODING);
    }

    /**
     * Create an encoder, using the {@link #DEFAULT_ENCODING} encoding.
     */
    public ByteArrayEncoder(String encoding) throws JUploadIOException {
        init(encoding);
    }

    // ///////////////////////////////////////////////////////////////////////
    // //////////////// Public methods ///////////////////////////////////////
    // ///////////////////////////////////////////////////////////////////////

    /**
     * Closes the encoding writer, and prepares the encoded length and byte
     * array. This method must be called before call to
     * {@link #getEncodedLength()} and {@link #getEncodedByteArray()}. <B>Note:</B>
     * After a call to this method, you can not append any new data to the
     * encoder.
     */
    synchronized public void close() throws JUploadIOException {
        if (isClosed()) {
            throw new JUploadIOException(
                    "Trying to close an already closed ByteArrayEncoded");
        }
        try {
            writer.close();
        } catch (IOException e) {
            throw new JUploadIOException(e);
        }
        encodedByteArray = baos.toByteArray();
        encodedLength = encodedByteArray.length;
        closed = true;
    }

    /**
     * Append a string, to be encoded at the current end of the byte array.
     */
    public ByteArrayEncoder append(String str) throws JUploadIOException {
        try {
            writer.append(str);
        } catch (IOException e) {
            throw new JUploadIOException(e);
        }
        //Returning the encoder allows calls like:
        // bae.append("qdqd").append("qsldqd");  (like StringBuffer)
        return this;
    }

    /**
     * Append a stream, to be encoded at the current end of the byte array.
     */
    public ByteArrayEncoder append(byte[] b) throws JUploadIOException {
        try {
            writer.flush();
            baos.write(b);
        } catch (IOException e) {
            throw new JUploadIOException(e);
        }
        //Returning the encoder allows calls like:
        // bae.append("qdqd").append("qsldqd");  (like StringBuffer)
        return this;
    }

    /**
     * Append a string, to be encoded at the current end of the byte array.
     * 
     * @param bae The ByteArrayEncoder whose encoding result should be appended
     *            to the current encoder. bae must be closed, before being
     *            appended.
     * @throws JUploadIOException This exception is thrown when this method is
     *             called on a non-closed encoder.
     */
    public ByteArrayEncoder append(ByteArrayEncoder bae) throws JUploadIOException {
        this.append(bae.getEncodedByteArray());
        //Returning the encoder allows calls like:
        // bae.append("qdqd").append("qsldqd");  (like StringBuffer)
        return this;
    }

    /**
     * Returns the default encoding
     * 
     * @return value of the {@link #DEFAULT_ENCODING} attribute.
     */
    public static String getDefaultEncoding() {
        return DEFAULT_ENCODING;
    }

    /**
     * @return the closed
     */
    public boolean isClosed() {
        return closed;
    }

    /**
     * @return the encoding
     */
    public String getEncoding() {
        return encoding;
    }

    /**
     * Get the length of the encoded result. Can be called only once the
     * encoder has been closed.
     * 
     * @return the encodedLength
     * @throws JUploadIOException This exception is thrown when this method is
     *             called on a non-closed encoder.
     */
    public int getEncodedLength() throws JUploadIOException {
        if (!isClosed()) {
            throw new JUploadIOException(
                    "Trying to get length of a on non-closed ByteArrayEncoded");
        }
        return encodedLength;
    }

    /**
     * Get the encoded result. Can be called only once the encoder has been
     * closed.
     * 
     * @return the encodedByteArray
     * @throws JUploadIOException This exception is thrown when this method is
     *             called on a non-closed encoder.
     */
    public byte[] getEncodedByteArray() throws JUploadIOException {
        if (!isClosed()) {
            throw new JUploadIOException(
                    "Trying to get the byte array of a on non-closed ByteArrayEncoded");
        }
        return encodedByteArray;
    }

    /**
     * Get the String that matches the encoded result. Can be called only once the encoder has been
     * closed.
     * 
     * @return the String that has been encoded.
     * @throws JUploadIOException This exception is thrown when this method is
     *             called on a non-closed encoder.
     */
    public String getString() throws JUploadIOException {
        if (!isClosed()) {
            throw new JUploadIOException(
                    "Trying to get the byte array of a on non-closed ByteArrayEncoded");
        }
        try {
            return new String(encodedByteArray, getEncoding());
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
    private void init(String encoding) throws JUploadIOException {
        this.encoding = encoding;
        try {
            writer = new OutputStreamWriter(baos, encoding);
        } catch (UnsupportedEncodingException e) {
            throw new JUploadIOException(e);
        }
    }
}
