package com.tvd12.ezyfoxserver.client.socket;

import com.tvd12.ezyfox.codec.EzyCodecCreator;
import com.tvd12.ezyfoxserver.client.codec.JacksonCodecCreator;
import com.tvd12.ezyfoxserver.client.util.EzyURIs;

import java.net.URI;

public class EzyWebSocketClient extends EzyNettySocketClient {

    protected URI uri;

    @Override
    protected void parseConnectionArguments(Object... args) {
        Object arg0 = args[0];
        if (args.length >= 2) {
            arg0 = URI.create("ws://" + args[0] + ":" + args[1]);
        }
        uri = (arg0 instanceof URI) ? (URI) arg0 : URI.create(arg0.toString());
        this.host = uri.getHost();
        this.port = EzyURIs.getWsPort(uri);
    }

    @Override
    protected EzyCodecCreator newCodecCreator() {
        return new JacksonCodecCreator();
    }

    @Override
    protected EzyAbstractChannelInitializer.Builder<?> newChannelInitializerBuilder() {
        return EzyWebSocketChannelInitializer.builder().uri(uri);
    }
}
