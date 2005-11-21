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

package org.apache.axis2.attachments.utils;


import javax.imageio.IIOImage;
import javax.imageio.ImageWriter;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;

/**
 * JDK1.4 based Image I/O
 */
public class ImageIO extends Component {
    /**
     * Saves an image.
     *
     * @param mimeType the mime-type of the format to save the image
     * @param image    the image to save
     * @param os       the stream to write to
     * @throws Exception if an error prevents image encoding
     */
    public void saveImage(String mimeType, Image image, OutputStream os)
            throws Exception {

        ImageWriter writer = null;
        Iterator iter = javax.imageio.ImageIO.getImageWritersByMIMEType(mimeType);
        if (iter.hasNext()) {
            writer = (ImageWriter) iter.next();
        }
        writer.setOutput(javax.imageio.ImageIO.createImageOutputStream(os));
        BufferedImage rendImage = null;
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
        writer.write(new IIOImage(rendImage, null, null));
        writer.dispose();
    } // saveImage

    /**
     * Loads an Image.
     *
     * @param in the stream to load the image
     * @return the Image
     */
    public Image loadImage(InputStream in) throws Exception {
        return javax.imageio.ImageIO.read(in);
    } // loadImage
}

