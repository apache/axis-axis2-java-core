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

public interface Benchmark1PortType extends java.rmi.Remote {

    /**
     * pings the server
     */
    public void echoVoid() throws java.rmi.RemoteException;

    /**
     * echos base64 content
     */
    public byte[] echoBase64(byte[] input) throws java.rmi.RemoteException;

    /**
     * echos string arrays
     */
    public java.lang.String[] echoStrings(java.lang.String[] input) throws java.rmi.RemoteException;

    /**
     * echos int arrays
     */
    public int[] echoInts(int[] input) throws java.rmi.RemoteException;

    /**
     * echos double arrays
     */
    public double[] echoDoubles(double[] input) throws java.rmi.RemoteException;

    /**
     * receives base64 content
     */
    public int receiveBase64(byte[] input) throws java.rmi.RemoteException;

    /**
     * receives strings
     */
    public int receiveStrings(java.lang.String[] input) throws java.rmi.RemoteException;

    /**
     * receives ints
     */
    public int receiveInts(int[] input) throws java.rmi.RemoteException;

    /**
     * receives doubles
     */
    public int receiveDoubles(double[] input) throws java.rmi.RemoteException;

    /**
     * sends base64 content
     */
    public byte[] sendBase64(int size) throws java.rmi.RemoteException;

    /**
     * sends strings
     */
    public java.lang.String[] sendStrings(int size) throws java.rmi.RemoteException;

    /**
     * sends ints
     */
    public int[] sendInts(int size) throws java.rmi.RemoteException;

    /**
     * sends doubles
     */
    public double[] sendDoubles(int size) throws java.rmi.RemoteException;
}
