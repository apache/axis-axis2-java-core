/*
 * Copyright 2004,2005 The Apache Software Foundation.
 * Copyright 2006 International Business Machines Corp.
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
package org.apache.axis2.jaxws.provider;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.OutputStream;
import java.util.Iterator;

import javax.imageio.IIOImage;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;

/**
 * This will serve as a helper class for attachments utility methods. All methods
 * are intended to be referenced staticly.
 *
 */
public class AttachmentUtil {
 
    /**
     * Store a given image to an Image output stream
     * @param mimeType
     * @param image
     * @param os
     * @throws Exception
     */
    public static void storeImage(String mimeType, Image image, OutputStream os) throws Exception {
        ImageWriter imageWriter = null;
        BufferedImage bufferedImage = (BufferedImage) image;
        
        Iterator iterator = javax.imageio.ImageIO.getImageWritersByMIMEType(mimeType);
        if (iterator.hasNext()) {
        	imageWriter = (ImageWriter) iterator.next();
        }
        ImageOutputStream ios = javax.imageio.ImageIO.createImageOutputStream(os);
        imageWriter.setOutput(ios);

        imageWriter.write(new IIOImage(bufferedImage, null, null));
        ios.flush();
        imageWriter.dispose();
    }
}

