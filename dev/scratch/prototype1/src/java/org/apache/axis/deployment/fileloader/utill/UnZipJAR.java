package org.apache.axis.deployment.fileloader.utill;

/**
 * Copyright 2001-2004 The Apache Software Foundation.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * @author Deepal Jayasinghe
 *         Oct 5, 2004
 *         2:54:57 PM
 *
 */

import org.apache.axis.deployment.DeployCons;
import org.apache.axis.deployment.DeploymentEngine;
import org.apache.axis.deployment.schemaparser.SchemaParser;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipFile;
import java.util.Enumeration;

public class UnZipJAR implements DeployCons {
    final int BUFFER = 2048;

    public void listZipcontent(String filename, DeploymentEngine engine) {
        // get attribute values
        String strArchive = filename;
        String tempfile = "C:/tem.txt";
        ZipInputStream zin;
        int entrysize = 0;
        try {

            zin = new ZipInputStream(new FileInputStream(strArchive));
            ZipEntry entry;
            while ((entry = zin.getNextEntry()) != null) {
                if (entry.getName().equals(SERVICEXML)) {
                    SchemaParser schme = new SchemaParser(zin, engine, filename);
                    schme.parseXML();
                }
            }
            zin.closeEntry();
            zin.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    public void listzip(String filename) {
        int BUFFER = 2048;
        try {
            BufferedOutputStream dest = null;
            BufferedInputStream is = null;
            ZipEntry entry;
            ZipFile zipfile = new ZipFile(filename);
            Enumeration e = zipfile.entries();
            while (e.hasMoreElements()) {
                entry = (ZipEntry) e.nextElement();
                System.out.println("Extracting: " + entry);
                is = new BufferedInputStream
                        (zipfile.getInputStream(entry));
                int count;
                byte data[] = new byte[BUFFER];
                FileOutputStream fos = new
                        FileOutputStream(entry.getName());
                dest = new
                        BufferedOutputStream(fos, BUFFER);
                while ((count = is.read(data, 0, BUFFER))
                        != -1) {
                    dest.write(data, 0, count);
                }
                dest.flush();
                dest.close();
                is.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}








