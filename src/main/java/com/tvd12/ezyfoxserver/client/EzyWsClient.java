package com.tvd12.ezyfoxserver.client;

import java.net.URI;

import com.tvd12.ezyfox.codec.EzyCodecCreator;
import com.tvd12.ezyfoxserver.client.codec.JacksonCodecCreator;
import com.tvd12.ezyfoxserver.client.config.EzyClientConfig;
import com.tvd12.ezyfoxserver.client.socket.EzyAbstractSocketClient;
import com.tvd12.ezyfoxserver.client.socket.EzyWebSocketClient;

/**
 * Created by tavandung12 on 9/20/18.
 */

public class EzyWsClient extends EzyAbstractClient {

    public EzyWsClient(EzyClientConfig config) {
        super(config);
    }
    
    public void connect(URI uri) {
    		connect(new Object[] {uri});
    }
    
    public void connect(String uri) {
		connect(new Object[] {uri});
}
    
    @Override
    protected EzyCodecCreator newCodecCreator() {
    		return new JacksonCodecCreator();
    }

    @Override
    protected EzyAbstractSocketClient.Builder<?> newSocketClientBuilder() {
    		return EzyWebSocketClient.builder();
    }
    
}
