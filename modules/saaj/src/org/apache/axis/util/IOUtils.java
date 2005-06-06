/*
 * Created on Apr 7, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.apache.axis.util;

import java.io.IOException;
import java.io.InputStream;

/**
 * Utility class containing IO helper methods
 */
public class IOUtils
{
    private IOUtils() {
    }

    /**
     * Read into a byte array; tries to ensure that the the
     * full buffer is read.
     *
     * Helper method, just calls <tt>readFully(in, b, 0, b.length)</tt>
     * @see #readFully(java.io.InputStream, byte[], int, int)
     */
    public static int readFully(InputStream in, byte[] b)
    throws IOException
    {
        return readFully(in, b, 0, b.length);
    }

    /**
     * Same as the normal <tt>in.read(b, off, len)</tt>, but tries to ensure that
     * the entire len number of bytes is read.
     * <p>
     * @returns the number of bytes read, or -1 if the end of file is
     *  reached before any bytes are read
     */
    public static int readFully(InputStream in, byte[] b, int off, int len)
    throws IOException
    {
        int total = 0;
        for (;;) {
            int got = in.read(b, off + total, len - total);
            if (got < 0) {
                return (total == 0) ? -1 : total;
            } else {
                total += got;
                if (total == len)
                    return total;
            }
        }
    }
}
