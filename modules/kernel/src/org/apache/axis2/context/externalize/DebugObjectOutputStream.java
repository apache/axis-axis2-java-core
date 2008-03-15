/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.axis2.context.externalize;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectStreamConstants;

/**
 * DebugObjectOutputStream delegates to an ObjectOutput object.
 * Each method logs in/out trace information
 */
public class DebugObjectOutputStream implements ObjectStreamConstants, ObjectOutput
        {
    private static final Log log = LogFactory.getLog(DebugObjectOutputStream.class);
    private static final boolean isDebug = log.isDebugEnabled();
    ObjectOutput out;
    
    DebugObjectOutputStream(ObjectOutput out) throws IOException {
        super();
        trace("--START DebugOutputStream--");
        this.out = out;
    }

    public void close() throws IOException {
        trace("--CLOSE DebugOutputStream--");
        out.close();
    }

    public void flush() throws IOException {
        trace ("start flush()");
        out.flush();
        trace ("end flush()");
    }

    public void write(byte[] b, int off, int len) throws IOException {
        trace ("start write(b, off, len) off=" + off + " len=" + len);
        if (len > 4) {
            trace (" first four bytes = '" + 
                   b[off] + "' '" + 
                   b[off+1] + "' '" +
                   b[off+2] + "' '" +
                   b[off+3] + "'");
        }
        out.write(b, off, len);
        trace ("end write(b, off, len)");
    }

    public void write(byte[] b) throws IOException {
        trace ("start write(byte) byte=" + b);
        out.write(b);
        trace ("end write(b)");
    }

    public void write(int b) throws IOException {
        trace ("start write(int) int=" + b);
        out.write(b);
        trace ("end write(int)");
    }

    public void writeBoolean(boolean v) throws IOException {
        trace ("start writeBoolean(v) v=" + v);
        out.writeBoolean(v);
        trace ("end writeBoolean(v)");
    }

    public void writeByte(int v) throws IOException {
        trace ("start writeByte(v) v=" + v);
        out.writeByte(v);
        trace ("end writeByte(v)");
    }

    public void writeBytes(String s) throws IOException {
        trace ("start writeBytes(s) s=" + s);
        out.writeBytes(s);
        trace ("end writeBytes(s)");
    }

    public void writeChar(int v) throws IOException {
        trace ("start writeChar(v) v=" + v);
        out.writeChar(v);
        trace ("end writeChar(v)");
    }

    public void writeChars(String s) throws IOException {
        trace ("start writeChars(s) s=" + s);
        out.writeChars(s);
        trace ("end writeChars(s)");
    }

    public void writeDouble(double v) throws IOException {
        trace ("start writeDouble(v) v=" + v);
        out.writeDouble(v);
        trace ("end writeDouble(v)");
    }

    public void writeFloat(float v) throws IOException {
        trace ("start writeFloat(v) v=" + v);
        out.writeFloat(v); 
        trace ("end writeFloat(v)");
    }

    public void writeInt(int v) throws IOException {
        trace ("start writeInt(v) v=" + v);
        out.writeInt(v);
        trace ("end writeInt(v)");
    }

    public void writeLong(long v) throws IOException {
        trace ("start writeLong(v) v=" + v);
        out.writeLong(v);
        trace ("end writeLong(v)");
    }

    public void writeObject(Object obj) throws IOException {
        trace ("start writeObject(v) v=" + valueName(obj));
        out.writeObject(obj);
        trace ("end writeObject(v)");
    }

    public void writeShort(int v) throws IOException {
        trace ("start writeShort(v) v=" + v);
        out.writeShort(v);
        trace ("end writeShort(v)");
    }

    public void writeUTF(String str) throws IOException {
        trace ("start writeUTF(v) v=" + str);
        out.writeUTF(str);
        trace ("end writeUTF(v)");
    }
    
    public void trace(String str) {
        if (isDebug) {
            log.debug(str);
        }
    }
    
    private String valueName(Object obj) {
        if (obj == null) {
            return "null";
        } else if (obj instanceof String) {
            return (String) obj;
        } else {
            return "Object of class = " + obj.getClass().getName();
        }
    }
}
