package com.tvd12.ezyfoxserver.client;

import com.tvd12.ezyfoxserver.client.config.EzyClientConfig;
import com.tvd12.ezyfoxserver.client.constant.EzyConnectionType;
import com.tvd12.ezyfoxserver.client.socket.EzyNettySocketClient;
import com.tvd12.ezyfoxserver.client.socket.EzyWebSocketClient;

/**
 * Created by tavandung12 on 9/20/18.
 */

public class EzyWsClient extends EzyNettyClient {

    public EzyWsClient(EzyClientConfig config) {
        super(config);
    }
    
    @Override
    protected EzyNettySocketClient newNettySocketClient() {
    		return new EzyWebSocketClient();
    }

    @Override
    public EzyConnectionType getConnectionType() {
    		return EzyConnectionType.WEBSOCKET;
    }
    
}
