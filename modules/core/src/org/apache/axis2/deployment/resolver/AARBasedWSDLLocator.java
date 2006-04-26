package org.apache.axis2.deployment.resolver;

import org.apache.axis2.deployment.DeploymentConstants;
import org.xml.sax.InputSource;

import javax.wsdl.xml.WSDLLocator;
import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
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

/**
 * Custom WSDL locator to load schemas from zip archives
 * Need to provide the aarFile and the baseInputStream for
 * the base WSDL file
 * <p/>
 * The logic here is that we only care about the import location
 * all imports must be relative to the META-INF folder
 */
public class AARBasedWSDLLocator implements WSDLLocator {

    private File aarFile;
    private InputStream baseInputStream;

    public AARBasedWSDLLocator(File zipFile, InputStream baseInputStream) {
        this.baseInputStream = baseInputStream;
        this.aarFile = zipFile;
    }

    /**
     * @return
     */
    public InputSource getBaseInputSource() {
        return new InputSource(baseInputStream);
    }

    /**
     * @param parentLocation
     * @param importLocation
     * @return
     */
    public InputSource getImportInputSource(String parentLocation, String importLocation) {
        //we don't care about the parent location
        ZipInputStream zin = null;
        try {

            zin = new ZipInputStream(new FileInputStream(aarFile));
            ZipEntry entry;
            byte[] buf = new byte[1024];
            int read;
            ByteArrayOutputStream out;
            while ((entry = zin.getNextEntry()) != null) {
                String entryName = entry.getName();
                if ((entryName.startsWith(DeploymentConstants.META_INF.toLowerCase())
                        || entryName.startsWith(DeploymentConstants.META_INF))
                        && entryName.endsWith(importLocation)) {
                    //read the item into a byte array to allow the
                    //stream to be closed
                    out = new ByteArrayOutputStream();
                    while ((read = zin.read(buf)) > 0) {
                        out.write(buf, 0, read);
                    }
                    ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
                    return new InputSource(in);
                }
            }


        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                if (zin != null) zin.close();
            } catch (IOException e) {
                //log this error
            }
        }

        return null;
    }

    /**
     * As for the zip there is no point in returning
     * a base URI
     *
     * @return
     */
    public String getBaseURI() {
        // we don't care
        return "";
    }

    /**
     * returns the latest import
     *
     * @return
     */
    public String getLatestImportURI() {
        //we don't care about this either
        return "";
    }
}
