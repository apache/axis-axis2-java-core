package org.apache.axis2.cluster;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.transport.http.SimpleHTTPServer;
import org.apache.catalina.tribes.Channel;
import org.apache.catalina.tribes.group.GroupChannel;
import org.apache.catalina.tribes.tipis.ReplicatedMap;

public class NodeTwo {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String path = "/usr/share/tomcat5/webapps/axis2/WEB-INF";
		
		try {
			ConfigurationContext ctx2 = ConfigurationContextFactory
			.createConfigurationContextFromFileSystem(
					path, path + "/conf/axis2.xml");
			SimpleHTTPServer server2 = new SimpleHTTPServer(ctx2,10600);
			server2.start();
		} catch (AxisFault e) {
			e.printStackTrace();
		}
		
		/*Channel channel = new GroupChannel();
		try {			
			channel.start(Channel.DEFAULT);
			
			ReplicatedMap map = new ReplicatedMap(NodeOne.class, 
                    channel,
                    1000,
                    "NodeMap",
                    new ClassLoader[]{Thread.currentThread().getContextClassLoader()}
                    );
			
			Thread.sleep(5000);
			
			System.out.println(map);
			              
			while(true){
			  Thread.sleep(3000);
			  System.out.println(map);
			}
			
		} catch (Exception e) {
			System.out.println("Error starting Tribes channel");
			e.printStackTrace();
		}*/


	}

}
