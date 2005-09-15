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

public class PushbackFilePartInputStream extends InputStream {

    MIMEBodyPartInputStream inStream;

    byte[] buffer;

    int count;

    /**
     * @param inStream
     * @param buffer
     */
    public PushbackFilePartInputStream(MIMEBodyPartInputStream inStream,
            byte[] buffer) {
        super();
        this.inStream = inStream;
        this.buffer = buffer;
        count = buffer.length;
    }

    public int read() throws IOException {
        int data;
        if (count > 0) {
            data = buffer[buffer.length - count];
            count--;
        } else {
            data = inStream.read();
        }
        return data;
    }
    
    public boolean getBoundaryStatus()
    {
        return inStream.getBoundaryStatus();
    }

}