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
import java.net.ServerSocket;
import java.net.Socket;

/**
 * A <code>TcpTunnel</code> object listens on the given port,
 * and once <code>Start</code> is pressed, will forward all bytes
 * to the given host and port.
 *
 * @author Sanjiva Weerawarana (sanjiva@watson.ibm.com)
 */
public class TcpTunnel {
    public static void main(String args[]) throws IOException {
        if (args.length != 3 && args.length != 4) {
            System.err.println("Usage: java TcpTunnel listenport tunnelhost tunnelport [encoding]");
            System.exit(1);
        }
        int listenport = Integer.parseInt(args[0]);
        String tunnelhost = args[1];
        int tunnelport = Integer.parseInt(args[2]);
        String enc;
        if (args.length == 4) {
            enc = args[3];
        } else {
            enc = "8859_1";
        }
        System.out.println("TcpTunnel: ready to rock and roll on port " + listenport);
        ServerSocket ss = new ServerSocket(listenport);
        while (true) {
            // accept the connection from my client
            Socket sc = ss.accept();

            // connect to the thing I'm tunnelling for
            Socket st = new Socket(tunnelhost, tunnelport);
            System.out.println("TcpTunnel: tunnelling port " + listenport + " to port " + tunnelport + " on host " + tunnelhost);

            // relay the stuff thru
            new Relay(sc.getInputStream(), st.getOutputStream(), System.out, enc).start();
            new Relay(st.getInputStream(), sc.getOutputStream(), System.out, enc).start();
            // that's it .. they're off; now I go back to my stuff.
        }
    }
}
