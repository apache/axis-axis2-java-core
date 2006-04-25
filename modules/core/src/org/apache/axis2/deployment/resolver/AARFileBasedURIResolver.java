package org.apache.axis2.deployment.resolver;

import org.apache.ws.commons.schema.resolver.DefaultURIResolver;
import org.apache.axis2.deployment.DeploymentConstants;
import org.xml.sax.InputSource;

import java.util.zip.ZipInputStream;
import java.util.zip.ZipEntry;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
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
 * A custom URI resolver that can
 */
public class AARFileBasedURIResolver extends DefaultURIResolver{

    private String aarFileName;
    private File aarFile;

    public AARFileBasedURIResolver(String aarFileName) {
        this.aarFileName = aarFileName;
    }

    public AARFileBasedURIResolver(File aarFile) {
        this.aarFile = aarFile;
    }
    
    public AARFileBasedURIResolver() {
    }

    public void setAarFileName(String aarFileName) {
        this.aarFileName = aarFileName;
    }

    public void setAarFileName(File aarFile) {
        this.aarFile = aarFile;
    }
    public InputSource resolveEntity(
            String targetNamespace,
            String schemaLocation,
            String baseUri) {
        //no issue with
        if (isAbsolute(schemaLocation)){
            return super.resolveEntity(
                    targetNamespace,schemaLocation,baseUri);
        }else{
            //validate
            if (schemaLocation.startsWith("..")){
                throw new RuntimeException(
                        "Unsupported schema location "+ schemaLocation);
            }

            ZipInputStream zin = null;
            try {
                if (aarFile!=null){
                    zin = new ZipInputStream(new FileInputStream(aarFile));
                } else{
                    zin = new ZipInputStream(new FileInputStream(aarFileName));
                }

                ZipEntry entry;
                byte[] buf = new byte[1024];
                int read;
                ByteArrayOutputStream out;
                while ((entry = zin.getNextEntry()) != null) {
                    String entryName = entry.getName().toLowerCase();
                    if (entryName.startsWith(DeploymentConstants.META_INF.toLowerCase())
                            && entryName.endsWith(schemaLocation)) {
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
            } finally{
                try {
                    if (zin!=null) zin.close();
                } catch (IOException e) {
                    //log this error
                }
            }

        }

        return null;
    }
}
