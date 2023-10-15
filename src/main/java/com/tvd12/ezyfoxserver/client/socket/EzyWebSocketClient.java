package com.tvd12.ezyfoxserver.client.socket;

import com.tvd12.ezyfoxserver.client.codec.EzyNettyCodecCreator;
import com.tvd12.ezyfoxserver.client.codec.NettyJacksonCodecCreator;
import com.tvd12.ezyfoxserver.client.config.EzySocketClientConfig;
import com.tvd12.ezyfoxserver.client.util.EzyURIs;

import java.net.URI;

public class EzyWebSocketClient extends EzyNettySocketClient {

    protected URI uri;

    public EzyWebSocketClient(EzySocketClientConfig config) {
        super(config);
    }

    @Override
    protected void parseConnectionArguments(Object... args) {
        Object arg0 = args[0];
        if (args.length >= 2) {
            arg0 = URI.create("ws://" + args[0] + ":" + args[1]);
        }
        this.uri = (arg0 instanceof URI)
            ? (URI) arg0
            : URI.create(arg0.toString());
        this.host = uri.getHost();
        this.port = EzyURIs.getWsPort(uri);
    }

    @Override
    protected EzyNettyCodecCreator newCodecCreator(boolean enableEncryption) {
        return new NettyJacksonCodecCreator();
    }

    @Override
    protected EzyAbstractChannelInitializer.Builder<?> newChannelInitializerBuilder() {
        return EzyWebSocketChannelInitializer.builder().uri(uri);
    }
}
