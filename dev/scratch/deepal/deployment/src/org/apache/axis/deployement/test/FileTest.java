package org.apache.axis.deployement.test;

import org.apache.axis.deployement.FileLoader.utill.UnZipJAR;

import java.util.Vector;
import java.util.Date;
import java.util.Locale;
import java.io.File;
import java.text.SimpleDateFormat;

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
public class FileTest {
    public static void main(String args[]) { // application's entry point
          String files[];
          Vector directories = new Vector();
          File root = new File("D:/Axis 2.0/projects/Deployement/test-data/");

          // initialise the agenda to contain just the root directory
          directories.addElement(root);

          while (directories.size() > 0) {         // loop until empty
             File dir = (File)directories.elementAt(0); // get first dir
             // System.out.println(dir);
             directories.remove(0);       // remove it
             files = dir.list();              // get list of files

             for (int i = 0; i < files.length; i++) { // iterate
                File f = new File(dir, files[i]);
                 long timestamp = f.lastModified();
                 Date when = new Date(timestamp);
                 System.out.println("last modified:  " + when);

               /*  SimpleDateFormat formatter;
                 Date currentdate=new Date();
                 long newtimestam = currentdate.getTime() ;
                 System.out.println("old time satm : " + timestamp + "  new tiem satm : " + newtimestam );
                 System.out.println("old Date : " + new Date(timestamp) + "  new Date : " +  new Date(newtimestam) );
                 long dif = newtimestam - timestamp;
                 System.out.println("time dif "  + new Date(dif));

               //  System.out.println("new tiem : "  + currentdate);   */




                if (f.isDirectory()) {        // see if it's a directory
                   directories.insertElementAt(f, 0); } // add dir to start of agenda
                else {//if (f.getName().equals(args[0])) { // test for target
                if (f.getName().equals("junit.jar")) {
                    UnZipJAR tem = new UnZipJAR();
                    tem.listZipcontent(f.getPath());
                }
                //   System.out.println(f.getPath());     // print out the full path
                   System.out.println(f.getName());     // print out the full path
                }
             }
          }
       }



}
