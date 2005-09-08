package org.apache.axis2.security;

public class AddressingMTOMSecurityTest extends InteropTestBase {
	protected void setUp() throws Exception {
		this.setClientRepo(COMPLETE_CLIENT_REPOSITORY);
		this.setServiceRepo(COMPLETE_SERVICE_REPOSITORY);
		super.setUp();
	}
}
