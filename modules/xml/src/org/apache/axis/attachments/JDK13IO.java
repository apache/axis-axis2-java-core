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

package org.apache.axis.attachments;


import java.awt.Component;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import sun.awt.image.codec.JPEGImageEncoderImpl;

/**
 * JDK1.3 based Image I/O
 *
 * @author <a href="mailto:butek@us.ibm.com">Russell Butek</a>
 */
public class JDK13IO extends Component implements ImageIO {
    /**
     * Save an image.
     * @param mimeType the mime-type of the format to save the image
     * @param image the image to save
     * @param os the stream to write to
     * @exception Exception if an error prevents image encoding
     */
    public void saveImage(String mimeType, Image image, OutputStream os)
            throws Exception {

        BufferedImage rendImage = null;

        // Create a BufferedImage
        if (image instanceof BufferedImage) {
            rendImage = (BufferedImage) image;
        } else {
            MediaTracker tracker = new MediaTracker(this);
            tracker.addImage(image, 0);
            tracker.waitForAll();
            rendImage = new BufferedImage(image.getWidth(null), image.getHeight(null), 1);
            Graphics g = rendImage.createGraphics();
            g.drawImage(image, 0, 0, null);
        }

        // Write the image to the output stream
        if ("image/jpeg".equals(mimeType)) {
            JPEGImageEncoderImpl j = new JPEGImageEncoderImpl(os);
            j.encode(rendImage);
        }
        else {
            throw new IOException("Supports Jpeg Only");
        }
    } // saveImage

    /**
     * Load an Image.
     * @param in the stream to load the image
     * @return the Image
     */
    public Image loadImage(InputStream in) throws Exception {
        if (in.available() <= 0) {
            return null;
        }
        else {
            byte[] bytes = new byte[in.available()];
            org.apache.axis.attachments.IOUtils.readFully(in,bytes);
            return Toolkit.getDefaultToolkit().createImage(bytes);
        }
    } // loadImage
}

