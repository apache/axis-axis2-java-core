/**
 * Echo.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2RC1 Sep 29, 2004 (08:29:40 EDT) WSDL2Java emitter.
 */

package org.apache.axis.sample.echo;

public interface Echo extends java.rmi.Remote {
    public java.lang.String echoString(java.lang.String in) throws java.rmi.RemoteException;
    public java.lang.String[] echoStringArray(java.lang.String[] in) throws java.rmi.RemoteException;
    public org.apache.axis.sample.echo.EchoStruct echoEchoStruct(org.apache.axis.sample.echo.EchoStruct in) throws java.rmi.RemoteException;
    public org.apache.axis.sample.echo.EchoStruct[] echoEchoStructArray(org.apache.axis.sample.echo.EchoStruct[] in) throws java.rmi.RemoteException;
}
