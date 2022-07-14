package com.tvd12.ezyfoxserver.client;

import com.tvd12.ezyfox.concurrent.EzyEventLoopGroup;
import com.tvd12.ezyfoxserver.client.config.EzyClientConfig;
import com.tvd12.ezyfoxserver.client.constant.EzyConnectionType;
import com.tvd12.ezyfoxserver.client.socket.EzyNettySocketClient;
import com.tvd12.ezyfoxserver.client.socket.EzyWebSocketClient;
import io.netty.channel.EventLoopGroup;

public class EzyWsClient extends EzyNettyClient {

    public EzyWsClient(EzyClientConfig config) {
        super(config);
    }

    public EzyWsClient(
        EzyClientConfig config,
        EzyEventLoopGroup eventLoopGroup,
        EventLoopGroup nettyEventLoopGroup
    ) {
        super(config, eventLoopGroup, nettyEventLoopGroup);
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
