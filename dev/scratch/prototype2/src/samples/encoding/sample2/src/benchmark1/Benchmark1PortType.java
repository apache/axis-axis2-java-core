/**
 * Benchmark1PortType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2beta May 11, 2004 (03:05:50 EST) WSDL2Java emitter.
 */

package benchmark1;

public interface Benchmark1PortType extends java.rmi.Remote {

    /**
     * pings the server
     */
    public void echoVoid() throws java.rmi.RemoteException;

    /**
     * echos base64 content
     */
    //public byte[] echoBase64(byte[] input) throws java.rmi.RemoteException;

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
    //public int receiveBase64(byte[] input) throws java.rmi.RemoteException;

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
    //public byte[] sendBase64(int size) throws java.rmi.RemoteException;

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
