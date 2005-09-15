/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.axis2.transport.http;


import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;


/**
 * @author Rick Rineholt
 */
public class ChunkedOutputStream extends FilterOutputStream {

    boolean eos = false;

    private ChunkedOutputStream() {
        super(null);
    }

    public ChunkedOutputStream(OutputStream os) {
        super(os);
    }

    public void write(int b)
            throws IOException {
        write(new byte[]{(byte) b}, 0, 1);
    }

    public void write(byte[] b)

            throws IOException {
        write(b, 0, b.length);
    }

    static final byte[] crlf = "\r\n".getBytes();

    public void write(byte[] b,
                      int off,
                      int len)
            throws IOException {
        if (len == 0) return;

        out.write((Integer.toHexString(len)).getBytes());
        out.write(crlf);
        out.write(b, off, len);
        out.write(crlf);
    }

    /*
     public void flush()
     throws IOException {
     out.flush();
     }
     */

    public void eos() throws IOException {
        synchronized (this) {
            if (eos) return;
            eos = true;
        }
        out.write("0\r\n\r\n".getBytes());
        out.flush();
    }

    public void close()
            throws IOException {
        eos();
        out.close();
    }

}
