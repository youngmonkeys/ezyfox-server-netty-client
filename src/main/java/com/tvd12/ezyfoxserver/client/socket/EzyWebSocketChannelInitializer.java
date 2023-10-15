package com.tvd12.ezyfoxserver.client.socket;

import com.tvd12.ezyfoxserver.client.ssl.EzySslContextFactory;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketClientProtocolHandler;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;
import io.netty.handler.stream.ChunkedWriteHandler;

import java.net.URI;

public class EzyWebSocketChannelInitializer extends EzyAbstractChannelInitializer {

    protected final URI uri;

    protected EzyWebSocketChannelInitializer(Builder builder) {
        super(builder);
        this.uri = builder.uri;
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    protected void initChannel0(Channel ch) {
        ch.pipeline()
            .addLast("http-client-codec", new HttpClientCodec())
            .addLast("http-object-aggregator", new HttpObjectAggregator(64 * 1024))
            .addLast("chunked-write-handler", new ChunkedWriteHandler())
            .addLast("ws-client-protocol-handler", newWebSocketClientProtocolHandler());
    }

    @Override
    protected EzyChannelHandler.Builder<?> newDataHandlerBuilder() {
        return EzyWebSocketChannelHandler.builder();
    }

    protected WebSocketClientProtocolHandler newWebSocketClientProtocolHandler() {
        WebSocketClientHandshaker handshaker = WebSocketClientHandshakerFactory.newHandshaker(
            uri,
            WebSocketVersion.V13,
            null,
            false,
            new DefaultHttpHeaders()
        );
        return new WebSocketClientProtocolHandler(handshaker);
    }

    public static class Builder extends EzyAbstractChannelInitializer.Builder<Builder> {

        protected URI uri;

        public Builder uri(URI uri) {
            this.uri = uri;
            return this;
        }

        @Override
        public EzyWebSocketChannelInitializer build() {
            if (sslContext == null) {
                String scheme = uri.getScheme();
                if (scheme.equals("wss")) {
                    sslContext = EzySslContextFactory
                        .getInstance()
                        .newSslContext();
                }
            }
            return new EzyWebSocketChannelInitializer(this);
        }
    }
}