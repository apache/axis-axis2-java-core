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
 
package encoding.sample2.benchMark;

public class Benchmark1Impl implements Benchmark1PortType{
    public void echoVoid() throws java.rmi.RemoteException {
    }

    public byte[] echoBase64(byte[] input) throws java.rmi.RemoteException {
        return input;
    }

    public java.lang.String[] echoStrings(java.lang.String[] input) throws java.rmi.RemoteException {
        return input;
    }

    public int[] echoInts(int[] input) throws java.rmi.RemoteException {
        return input;
    }

    public double[] echoDoubles(double[] input) throws java.rmi.RemoteException {
        return input;
    }


    public int receiveBase64(byte[] input) throws java.rmi.RemoteException {
        return input.length;
    }

    public int receiveStrings(String[] input) throws java.rmi.RemoteException {
        return input.length;
    }

    public int receiveInts(int[] input) throws java.rmi.RemoteException {
        return input.length;
    }

    public int receiveDoubles(double[] input) throws java.rmi.RemoteException {
        return input.length;
    }

    public byte[] sendBase64(int n) throws java.rmi.RemoteException {
        byte[] output = new byte[n];
        for (int i = 0; i < n; i++)
        {
            output[i] = (byte) i;
        }
        return output;
    }

    public String[] sendStrings(int n) throws java.rmi.RemoteException {
        String[] output = new String[n];
        for (int i = 0; i < n; i++)
        {
            output[i] = "s"+i;
        }
        return output;
    }

    public int[] sendInts(int n) throws java.rmi.RemoteException {
        int[] output = new int[n];
        for (int i = 0; i < n; i++)
        {
            output[i] = (int) i;
        }
        return output;
    }

    public double[] sendDoubles(int n) throws java.rmi.RemoteException {
        double[] output = new double[n];
        for (int i = 0; i < n; i++)
        {
            output[i] = (double) i;
        }
        return output;
    }

}
