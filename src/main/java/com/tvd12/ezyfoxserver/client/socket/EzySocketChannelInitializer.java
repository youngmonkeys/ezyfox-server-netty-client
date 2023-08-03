package com.tvd12.ezyfoxserver.client.socket;

import io.netty.channel.Channel;
import io.netty.handler.ssl.SslHandler;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;

public class EzySocketChannelInitializer extends EzyAbstractChannelInitializer {

    private final SSLContext sslContext;

    protected EzySocketChannelInitializer(Builder builder) {
        super(builder);
        this.sslContext = builder.sslContext;
    }

    @Override
    protected void initChannel0(Channel ch) {
        if (sslContext != null) {
            SSLEngine engine = sslContext.createSSLEngine();
            engine.setUseClientMode(true);
            ch.pipeline().addLast("ssl", new SslHandler(engine));
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    protected EzyChannelHandler.Builder<?> newDataHandlerBuilder() {
        return EzySocketChannelHandler.builder();
    }

    public static class Builder extends EzyAbstractChannelInitializer.Builder<Builder> {

        private SSLContext sslContext;

        public Builder sslContext(SSLContext sslContext) {
            this.sslContext = sslContext;
            return this;
        }

        @Override
        public EzySocketChannelInitializer build() {
            return new EzySocketChannelInitializer(this);
        }
    }
}