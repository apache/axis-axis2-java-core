package org.apache.axis2.cluster;

import org.apache.axis2.AxisFault;
import org.apache.axis2.cluster.tribes.ContextManager;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.transport.http.SimpleHTTPServer;
import org.apache.catalina.tribes.Channel;
import org.apache.catalina.tribes.ChannelException;
import org.apache.catalina.tribes.group.GroupChannel;
import org.apache.catalina.tribes.tipis.ReplicatedMap;

public class NodeThree {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String path = "/usr/share/tomcat5/webapps/axis2/WEB-INF";
		
		try {
			ConfigurationContext ctx = ConfigurationContextFactory
			.createConfigurationContextFromFileSystem(
					path, path + "/conf/axis2.xml");
			SimpleHTTPServer server = new SimpleHTTPServer(ctx,10700);
			server.start();
		} catch (AxisFault e) {
			e.printStackTrace();
		}
		
		try {
			ConfigurationContext ctx = ConfigurationContextFactory
			.createConfigurationContextFromFileSystem(
					path, path + "/conf/axis2.xml");
			SimpleHTTPServer server = new SimpleHTTPServer(ctx,10800);
			server.start();
		} catch (AxisFault e) {
			e.printStackTrace();
		}
		
		try {
			ConfigurationContext ctx = ConfigurationContextFactory
			.createConfigurationContextFromFileSystem(
					path, path + "/conf/axis2.xml");
			SimpleHTTPServer server = new SimpleHTTPServer(ctx,10900);
			server.start();
		} catch (AxisFault e) {
			e.printStackTrace();
		}
		
		
		/*
		Channel channel = new GroupChannel();
		try {			
			channel.start(Channel.DEFAULT);
			
			ReplicatedMap map = new ReplicatedMap(NodeOne.class, 
                    channel,
                    1000,
                    "NodeMap",
                    new ClassLoader[]{Thread.currentThread().getContextClassLoader()}
                    );
			
			Thread.sleep(1000);
			
			map.put("name","rajith");
			map.replicate("name", true);
			
			Thread.sleep(1000);
			
			map.put("age","23");
			map.replicate("age", true);
			
			while(true){
				Thread.sleep(2000);
				map.replicate(true);
				System.out.println("Replicating");
			}
			
		} catch (Exception e) {
			System.out.println("Error starting Tribes channel");
			e.printStackTrace();
		}*/

	}

}
