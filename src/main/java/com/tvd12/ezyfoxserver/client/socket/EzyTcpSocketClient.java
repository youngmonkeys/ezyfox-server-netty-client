package com.tvd12.ezyfoxserver.client.socket;

import com.tvd12.ezyfox.codec.EzyCodecCreator;
import com.tvd12.ezyfoxserver.client.codec.MsgPackCodecCreator;

public class EzyTcpSocketClient extends EzyNettySocketClient {

    @Override
    protected void parseConnectionArguments(Object... args) {
        this.host = (String) args[0];
        this.port = (Integer) args[1];
    }

    @Override
    protected EzyCodecCreator newCodecCreator() {
        return new MsgPackCodecCreator();
    }

    @Override
    protected EzyAbstractChannelInitializer.Builder<?> newChannelInitializerBuilder() {
        return EzySocketChannelInitializer.builder();
    }
}
