package org.apache.axis2.transport.http;

import junit.framework.TestCase;

public class NonProxyHostTest extends TestCase {
    public void testForAxis2_3453() {
        String nonProxyHosts = "sealbook.ncl.ac.uk|*.sealbook.ncl.ac.uk|eskdale.ncl.ac.uk|*.eskdale.ncl.ac.uk|giga25.ncl.ac.uk|*.giga25.ncl.ac.uk";
        assertTrue(ProxyConfiguration.isHostInNonProxyList("sealbook.ncl.ac.uk", nonProxyHosts));
        assertFalse(ProxyConfiguration.isHostInNonProxyList("xsealbook.ncl.ac.uk", nonProxyHosts));
        assertTrue(ProxyConfiguration.isHostInNonProxyList("local","local|*.local|169.254/16|*.169.254/16"));
        assertFalse(ProxyConfiguration.isHostInNonProxyList("localhost","local|*.local|169.254/16|*.169.254/16"));
    }
}
