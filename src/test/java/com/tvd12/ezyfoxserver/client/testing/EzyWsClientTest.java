package com.tvd12.ezyfoxserver.client.testing;

import com.tvd12.ezyfoxserver.client.EzyWsClient;
import com.tvd12.ezyfoxserver.client.config.EzyClientConfig;

public class EzyWsClientTest {

    public static void main(String[] args) throws Exception {
        EzyWsClientTest test = new EzyWsClientTest();
        test.test();
    }

    public void test() throws Exception {
        EzyClientConfig config = EzyClientConfig.builder()
            .clientName("hello-word")
            .zoneName("example")
            .build();

        EzyWsClient client = new EzyWsClient(config);
        ClientSetup.getInstance().setup(client);
		client.connect("ws://127.0.0.1:2208/ws");
//        client.connect("wss://ws.tvd12.com/ws");
        //noinspection InfiniteLoopStatement
        while (true) {
            client.processEvents();
            //noinspection BusyWait
            Thread.sleep(5);
        }
    }
}
