/**
 * Benchmark1Impl.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2beta Apr 25, 2004 (11:37:32 EST) WSDL2Java emitter.
 */

package benchmark1;

public class Benchmark1Impl implements benchmark1.Benchmark1PortType{
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
