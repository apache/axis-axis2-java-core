This is a new JMS transport implementation for Axis2. The transport receiver must be configured as follows, with
one or more connection factories:

Sample axis2.xml

    <transportReceiver name="jms" class="org.apache.axis2.transport.njms.JMSListener">
        <parameter name="myTopicConnectionFactory" locked="false">        	        	
        	<parameter name="java.naming.factory.initial" locked="false">org.apache.activemq.jndi.ActiveMQInitialContextFactory</parameter>
        	<parameter name="java.naming.provider.url" locked="false">tcp://localhost:61616</parameter>        	
        	<parameter name="transport.jms.ConnectionFactoryJNDIName" locked="false">TopicConnectionFactory</parameter>
        </parameter>
        <parameter name="myQueueConnectionFactory" locked="false">
        	<parameter name="java.naming.factory.initial" locked="false">org.apache.activemq.jndi.ActiveMQInitialContextFactory</parameter>
        	<parameter name="java.naming.provider.url" locked="false">tcp://localhost:61616</parameter>        	
        	<parameter name="transport.jms.ConnectionFactoryJNDIName" locked="false">QueueConnectionFactory</parameter>
        </parameter>
        <parameter name="default" locked="false">        	        	
        	<parameter name="java.naming.factory.initial" locked="false">org.apache.activemq.jndi.ActiveMQInitialContextFactory</parameter>
        	<parameter name="java.naming.provider.url" locked="false">tcp://localhost:61616</parameter>        	
        	<parameter name="transport.jms.ConnectionFactoryJNDIName" locked="false">QueueConnectionFactory</parameter>
        </parameter>
    </transportReceiver>
    
If a connection factory named "default" (as shown above) is defined, this would be used for Services which does
not explicitly specify the connection factory that should be used. The services.xml of a Service should indicate
the connection factory and the destination name to be associated with. If a destination is not specified, the
implementation would create a JMS Queue with the service name. The JMS destination should ideally be created
and administered through the JMS provider utilities.

Sample services.xml

<service name="echo">
		<transports>
				....
		    <transport>jms</transport>
		</transports>
    ...
    <parameter name="transport.jms.ConnectionFactory" locked="true">myTopicConnectionFactory</parameter>
    <parameter name="transport.jms.Destination" locked="true">dynamicTopics/something.TestTopic</parameter>
</service>

Files making up this JMS implementation
JMSListener.java
JMSConnectionFactory.java
JMSMessageReceiver.java
JMSOutTransportInfo.java
JMSSender.java
JMSConstants.java
JMSUtils.java
AxisJMSException.java
DefaultThreadFactory.java
