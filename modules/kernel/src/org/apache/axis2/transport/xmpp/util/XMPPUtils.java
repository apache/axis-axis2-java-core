package org.apache.axis2.transport.xmpp.util;

import org.apache.axis2.AxisFault;


public class XMPPUtils {
    
	/**
	 * Extract XMPP server accountName section from transport URL passed in.  
	 * @param transportUrl
	 * @return String 
	 * @throws AxisFault
	 */
    public static String getAccountName(String transportUrl) throws AxisFault{
    	String accountName = "";
    	if(transportUrl == null){
    		return null;
    	}
    	
        if (!transportUrl.startsWith(XMPPConstants.XMPP)) {
            throw new AxisFault ("Invalid XMPP URL : " + transportUrl +
                    " Must begin with the prefix xmpp");
        }
        //eg: transportUrl is similar to xmpp://axisserver@sumedha/Version
        int start = transportUrl.indexOf("://") + 3;
        int end = transportUrl.lastIndexOf("/"); //first index
        if(start != -1 && end != -1){
        	accountName = transportUrl.substring(start, end);
        }else{
        	accountName = transportUrl;
        }
        return accountName;
    }

    /**
     * Extract Service name from transport URL passed in
     * @param transportUrl
     * @return
     * @throws AxisFault
     */
    public static String getServiceName(String transportUrl) throws AxisFault{
    	String serviceName = "";
    	if(transportUrl == null){
    		return null;
    	}
        if (!transportUrl.startsWith(XMPPConstants.XMPP)) {
            throw new AxisFault ("Invalid XMPP URL : " + transportUrl +
                    " Must begin with the prefix xmpp");
        }
        //eg: transportUrl is similar to xmpp://axisserver@sumedha/Version
        int start = transportUrl.lastIndexOf("/") + 1;
        int end = transportUrl.length();
        if(start != -1 && end != -1){
        	serviceName = transportUrl.substring(start, end);
        }
        return serviceName;
    }
    
}

