/**
 * EchoService.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2RC1 Sep 29, 2004 (08:29:40 EDT) WSDL2Java emitter.
 */

package org.apache.axis.sample.echo;

public interface EchoService extends javax.xml.rpc.Service {
    public java.lang.String getechoAddress();

    public org.apache.axis.sample.echo.Echo getecho() throws javax.xml.rpc.ServiceException;

    public org.apache.axis.sample.echo.Echo getecho(java.net.URL portAddress) throws javax.xml.rpc.ServiceException;
}
