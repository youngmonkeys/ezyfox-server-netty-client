package com.tvd12.ezyfoxserver.client.socket;

import com.tvd12.ezyfoxserver.client.util.EzyURIs;
import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketClientProtocolHandler;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.handler.stream.ChunkedWriteHandler;

import java.net.URI;

public class EzyWebSocketChannelInitializer extends EzyAbstractChannelInitializer {

    protected URI uri;

    protected EzyWebSocketChannelInitializer(Builder builder) {
        super(builder);
        this.uri = builder.uri;
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    protected void initChannel0(Channel ch) throws Exception {
        String scheme = uri.getScheme();
        ChannelPipeline pipeline = ch.pipeline();
        if (scheme.equals("wss")) {
            SslContext sslCtx = SslContextBuilder.forClient()
                .trustManager(InsecureTrustManagerFactory.INSTANCE).build();
            int port = EzyURIs.getWsPort(uri);
            pipeline.addLast(sslCtx.newHandler(ch.alloc(), uri.getHost(), port));
        }
        pipeline.addLast("http-client-codec", new HttpClientCodec());
        pipeline.addLast("http-object-aggregator", new HttpObjectAggregator(64 * 1024));
        pipeline.addLast("chunked-write-handler", new ChunkedWriteHandler());
        pipeline.addLast("ws-client-protocol-handler", newWebSocketClientProtocolHandler());
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
            return new EzyWebSocketChannelInitializer(this);
        }
    }
}