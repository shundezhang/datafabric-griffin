/*
 * ------------------------------------------------------------------------------
 * Hermes FTP Server
 * Copyright (c) 2005-2007 Lars Behnke
 * ------------------------------------------------------------------------------
 * 
 * This file is part of Hermes FTP Server.
 * 
 * Hermes FTP Server is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * Hermes FTP Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Hermes FTP Server; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 * ------------------------------------------------------------------------------
 */

package au.org.arcs.griffin.streams;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

import au.org.arcs.griffin.filesystem.FileObject;
import au.org.arcs.griffin.filesystem.RandomAccessFileObject;


/**
 * Wrapper class for reading a RandomAccessFile through the InputStream abstraction.
 * 
 * @author Lars Behnke
 * @author Shunde Zhang
 */
public class RafInputStream extends InputStream implements RecordReadSupport {

    private FileObject             file;

    private RandomAccessFileObject raf;

    private long             offset;

    /**
     * Constructor.
     * 
     * @param file The input file.
     */
    public RafInputStream(FileObject file) {
        super();
        this.file = file;
    }

    /**
     * Constructor.
     * 
     * @param file The input file.
     * @param offset The number of bytes to be skipped when reading the file.
     */
    public RafInputStream(FileObject file, long offset) {
        super();
        this.file = file;
        this.offset = offset;
    }

    /**
     * {@inheritDoc}
     */
    public int read() throws IOException {
        return getRaf().read();
    }

    /**
     * {@inheritDoc}
     */
    public int read(byte[] b) throws IOException {
        return getRaf().read(b);
    }

    /**
     * {@inheritDoc}
     */
    public int read(byte[] b, int off, int len) throws IOException {
        return getRaf().read(b, off, len);
    }

    /**
     * {@inheritDoc}
     */
    public void close() throws IOException {
        getRaf().close();
    }

    private RandomAccessFileObject getRaf() throws IOException {
        if (raf == null) {
            raf = file.getRandomAccessFileObject("r");
            if (offset > 0) {
                raf.seek(offset);
            }
        }
        return raf;
    }

    /**
     * {@inheritDoc}
     */
    public byte[] readRecord() throws IOException {
        String line = getRaf().readLine();
        if (line != null) {
            return line.getBytes();
        } else {
            return null;
        }
    }

}
