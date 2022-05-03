package com.tvd12.ezyfoxserver.client;

import com.tvd12.ezyfoxserver.client.config.EzyClientConfig;
import com.tvd12.ezyfoxserver.client.constant.EzyConnectionType;
import com.tvd12.ezyfoxserver.client.socket.EzyNettySocketClient;
import com.tvd12.ezyfoxserver.client.socket.EzyWebSocketClient;

public class EzyWsClient extends EzyNettyClient {

    public EzyWsClient(EzyClientConfig config) {
        super(config);
    }

    @Override
    protected EzyNettySocketClient newNettySocketClient() {
        return new EzyWebSocketClient();
    }

    @Override
    public void connect(String url) {
        this.connectTo(url);
    }

    @Override
    public EzyConnectionType getConnectionType() {
        return EzyConnectionType.WEBSOCKET;
    }
}
