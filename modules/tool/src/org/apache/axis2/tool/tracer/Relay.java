/**
 *
 * Copyright 2000-2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.axis2.tool.tracer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * A <code>Relay</code> object is used by <code>TcpTunnel</code>
 * and <code>TcpTunnelGui</code> to relay bytes from an
 * <code>InputStream</code> to a <code>OutputStream</code>.
 *
 * @author Sanjiva Weerawarana (sanjiva@watson.ibm.com)
 * @author Scott Nichol (snichol@computer.org)
 */
public class Relay extends Thread {
    final static int BUFSIZ = 1000;
    InputStream in;
    OutputStream out;
    byte buf[] = new byte[BUFSIZ];
    OutputStream os;
    String enc = "8859_1";
    Relay(InputStream in, OutputStream out, OutputStream os, String enc) {
        this.in = in;
        this.out = out;
        this.os = os;
        this.enc = enc;
    }
    public String getEncoding() {
        return enc;
    }
    public void run() {
        int n;
        try {
            while ((n = in.read(buf)) > 0) {
                out.write(buf, 0, n);
                out.flush();
                if (os != null) {
                    os.write(buf, 0, n);
                    os.flush();
                }
            }
        } catch (IOException e) {
        } finally {
            try {
                in.close();
                out.close();
            } catch (IOException e) {
            }
        }
    }
    public void setEncoding(String enc) {
        this.enc = enc;
    }
}
