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
 * Created on Feb 9, 2005
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
/**
 * @author hemapani
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class EndToEndTest {
    private static int sizeOfArray = 10;
    private static int numberOfRequests = 100;
    private int no;
    private static int THREAD_COUNT = 100;
    private Collecter c; 
    public EndToEndTest(int no,Collecter c){
        this.no = no;
    }

    public static void main(String[] args) throws IOException {
        
        Writer writer = new FileWriter("results/results.txt");
        writer.write("Axis2 Perf\n#########################\n");
        int[] vals = new int[]{1, 10,100,1000};
        for (int j = 0; j < vals.length; j++) {
            Collecter c = new Collecter(100,"Array Size =" + vals[j],writer);
            for (int i = 0; i < numberOfRequests; i++) {
                try {
                    Sampler sampler = new Sampler(vals[j], c);
                    System.out.print(i + " = ");
                    sampler.invokeService();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            c.printResult();
        }
        writer.write("#########################\n");
        writer.close();

    }
}
