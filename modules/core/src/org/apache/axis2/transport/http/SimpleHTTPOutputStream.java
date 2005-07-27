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
 *
 *  Runtime state of the engine
 */
package org.apache.axis2.transport.http;

import org.apache.axis2.AxisFault;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class SimpleHTTPOutputStream extends FilterOutputStream {
    private boolean written = false;
    private boolean chuncked = false;
    private String contentType = null;

    public SimpleHTTPOutputStream(OutputStream out, boolean chuncked)
            throws AxisFault {
        super(out);
        this.chuncked = chuncked;
    }

    public void write(byte[] b) throws IOException {
        if (!written) {
            writeHeader();
        }
        out.write(b);
    }

    /**
     * @param b
     * @param off
     * @param len
     * @throws java.io.IOException
     */
    public void write(byte[] b, int off, int len) throws IOException {
        if (!written) {
            writeHeader();
        }
        out.write(b, off, len);
    }

    /**
     * @param b
     * @throws java.io.IOException
     */
    public void write(int b) throws IOException {
        if (!written) {
            writeHeader();
        }
        out.write(b);
    }

    public void writeHeader() throws IOException {
        StringBuffer buf = new StringBuffer();
        if (chuncked) {
            buf.append(new String(HTTPConstants.HEADER_PROTOCOL_11)).append(
                    " ");
            buf.append(new String(HTTPConstants.OK)).append("\n");
            buf.append(HTTPConstants.HEADER_TRANSFER_ENCODING).append(": ");
            buf.append(HTTPConstants.HEADER_TRANSFER_ENCODING_CHUNKED).append(
                    "\n");
            if (contentType != null) {
                buf.append(HTTPConstants.HEADER_CONTENT_TYPE).append(": ");
                buf.append(contentType).append("\n");
            }
            buf.append("\n");
        } else {
            buf.append(new String(HTTPConstants.HTTP));
            buf.append(new String(HTTPConstants.OK)).append("\n");
            if (contentType != null) {
                buf.append(HTTPConstants.HEADER_CONTENT_TYPE).append(": ");
                buf.append(contentType).append("\n");
            }
            buf.append("\n");
        }
        out.write(buf.toString().getBytes());
        written = true;
        if (chuncked) {
            out.flush();
            out = new ChunkedOutputStream(out);
        }

    }

    public void finalize() throws IOException {
        if (!written) {
            out.write(new String(HTTPConstants.NOCONTENT).getBytes());
            written = true;
        } else {
            out.flush();
        }
        if (chuncked) {
            //TODO sometimes the out stream is closed by the client
            try {
                ((ChunkedOutputStream) out).eos();
            } catch (IOException e) {
            }
        }
    }

    /* (non-Javadoc)
     * @see java.io.OutputStream#close()
     */
    public void close() throws IOException {
        if (!written) {
            finalize();
        }
        super.close();
    }

    /**
     * @param string
     */
    public void setContentType(String string) {
        contentType = string;
    }

}
