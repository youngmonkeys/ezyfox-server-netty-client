package com.tvd12.ezyfoxserver.client.testing;

import com.tvd12.ezyfoxserver.client.EzyTcpClient;
import com.tvd12.ezyfoxserver.client.config.EzyClientConfig;

public class EzyTcpClientTest {

    public static void main(String[] args) throws Exception {
        EzyTcpClientTest test = new EzyTcpClientTest();
        test.test();
    }

    public void test() throws Exception {
        EzyClientConfig config = EzyClientConfig.builder()
            .clientName("hello-word")
            .zoneName("example")
            .build();

        EzyTcpClient client = new EzyTcpClient(config);
        ClientSetup.getInstance().setup(client);
        client.connect("ws.tvd12.com", 3005);
        //noinspection InfiniteLoopStatement
        while (true) {
            client.processEvents();
            //noinspection BusyWait
            Thread.sleep(5);
        }
    }

}
