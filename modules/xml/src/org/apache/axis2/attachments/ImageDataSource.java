/*
 * Copyright 2001-2004 The Apache Software Foundation.
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

import javax.activation.DataSource;
import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ImageDataSource implements DataSource {

    public static final String CONTENT_TYPE = "image/jpeg";

    private final String name;

    private final String contentType;

    private byte[] data;

    private ByteArrayOutputStream os;

    public ImageDataSource(String name, Image data) {
        this(name, CONTENT_TYPE, data);
    } // ctor

    public ImageDataSource(String name, String contentType, Image data) {
        this.name = name;
        this.contentType = contentType == null ? CONTENT_TYPE : contentType;
        os = new ByteArrayOutputStream();
        try {
            if (data != null) {
                new JDK13IO().saveImage(this.contentType, data, os);
            }
        } catch (Exception e) {
            // log.error(Messages.getMessage("exception00"), e);
        }
    }

    public String getName() {
        return name;
    } // getName

    public String getContentType() {
        return contentType;
    } // getContentType

    public InputStream getInputStream() throws IOException {
        if (os.size() != 0) {
            data = os.toByteArray();
            os.reset();
        }
        return new ByteArrayInputStream(data == null ? new byte[0] : data);
    } // getInputStream

    public OutputStream getOutputStream() throws IOException {
        if (os.size() != 0) {
            data = os.toByteArray();
            os.reset();
        }
        return os;
    } // getOutputStream
} // class ImageDataSource
