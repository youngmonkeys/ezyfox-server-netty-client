package com.tvd12.ezyfoxserver.client.testing;

import com.tvd12.ezyfoxserver.client.EzyTcpClient;
import com.tvd12.ezyfoxserver.client.config.EzyClientConfig;

public class EzyTcpClientTest {
	
	public void test() throws Exception {
		EzyClientConfig config = EzyClientConfig.builder()
				.clientName("hello-word")
				.zoneName("example")
				.build();
		
		EzyTcpClient client = new EzyTcpClient(config);
		ClientSetup.getInstance().setup(client);
		client.connect("ws.tvd12.com", 3005);
		while(true) {
			client.processEvents();
			Thread.sleep(5);
		}
	}
	
	public static void main(String[] args) throws Exception {
		EzyTcpClientTest test = new EzyTcpClientTest();
		test.test();
	}
	
}
