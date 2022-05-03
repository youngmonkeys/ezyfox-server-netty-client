package com.tvd12.ezyfoxserver.client;

import com.tvd12.ezyfoxserver.client.config.EzyClientConfig;
import com.tvd12.ezyfoxserver.client.constant.EzyConnectionType;
import com.tvd12.ezyfoxserver.client.socket.EzyNettySocketClient;
import com.tvd12.ezyfoxserver.client.socket.EzyTcpSocketClient;

public class EzyTcpClient extends EzyNettyClient {


    public EzyTcpClient(EzyClientConfig config) {
        super(config);
    }

    @Override
    protected EzyNettySocketClient newNettySocketClient() {
        return new EzyTcpSocketClient();
    }

    @Override
    public EzyConnectionType getConnectionType() {
        return EzyConnectionType.SOCKET;
    }
}
