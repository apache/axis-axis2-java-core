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

package org.apache.axis2.attachments;

import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;

public class MIMEBodyPartInputStream extends InputStream {
    PushbackInputStream inStream;

    boolean boundaryFound;

    MIMEHelper parent;

    byte[] boundary;

    public MIMEBodyPartInputStream(PushbackInputStream inStream, byte[] boundary) {
        super();
        this.inStream = inStream;
        this.boundary = boundary;
    }

    public MIMEBodyPartInputStream(PushbackInputStream inStream,
            byte[] boundary, MIMEHelper parent) {
        this(inStream, boundary);
        this.parent = parent;
    }

    public int read() throws IOException {
        if (boundaryFound) {
            return -1;
        }
        // read the next value from stream
        int value = inStream.read();

        // A problem occured because all the mime parts tends to have a /r/n at
        // the end. Making it hard to transform them to correct DataSources.
        // This logic introduced to handle it
        //TODO look more in to this && for a better way to do this
        if (value == 13) {
            value = inStream.read();
            if (value != 10) {
                inStream.unread(value);
                return 13;
            } else {
                value = inStream.read();
                if ((byte) value != boundary[0]) {
                    inStream.unread(value);
                    inStream.unread(10);
                    return 13;
                }
            }
        } else if ((byte) value != boundary[0]) {
            return value;
        }

        // read value is the first byte of the boundary. Start matching the
        // next characters to find a boundary
        int boundaryIndex = 0;
        while ((boundaryIndex < (boundary.length - 1))
                && ((byte) value == boundary[boundaryIndex])) {
            value = inStream.read();
            boundaryIndex++;
        }

        if (boundaryIndex == (boundary.length - 1)) { // boundary found
            boundaryFound = true;
            // read the end of line character
            if ((value = inStream.read()) == 45) {
                //check whether end of stream
                //Last mime boundary should have a succeeding "--"
                if ((value = inStream.read()) == 45 && parent!=null) {
                    parent.setEndOfStream(true);
                }
            } else {
                inStream.read();
            }

            return -1;
        }

        // Boundary not found. Restoring bytes skipped.
        // write first skipped byte, push back the rest

        if (value != -1) { // Stream might have ended
            inStream.unread(value);
        }
        inStream.unread(boundary, 1, boundaryIndex - 1);
        return boundary[0];
    }
    
    public boolean getBoundaryStatus()
    {
        return boundaryFound;
    }
}