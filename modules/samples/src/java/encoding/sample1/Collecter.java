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
 
package encoding.sample1;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

/*
 * Created on Feb 10, 2005
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class Collecter {
    private static Writer writer = null;
    private long[] results;
    private int index = 0;
    private String message = null;

    public Collecter(int size, String message, Writer writer) throws IOException {
        this.message = message;
        Collecter.writer = writer;
        results = new long[size];
    }

    public void add(long value) {
        results[index] = value;
        index++;
    }

    public void printResult() throws IOException {
        int failed = 0;
        long totel = 0;

        for (int i = 0; i < results.length; i++) {

            if (results[i] <= 0) {
                failed++;
            } else {
                totel = totel + results[1];
            }

        }
        System.out.println("Calls =" + results.length);
        System.out.println("Faliure =" + failed);
        System.out.println("Average =" + totel / (results.length - failed));
        writer.write("--------------------------------------");
        writer.write(message + "\n");
        writer.write("Average =" + totel / (results.length - failed)+"\n");
        writer.write("--------------------------------------");
        writer.flush();
    }

}
