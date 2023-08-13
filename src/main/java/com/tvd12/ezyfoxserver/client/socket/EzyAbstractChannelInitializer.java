package com.tvd12.ezyfoxserver.client.socket;

import com.tvd12.ezyfox.builder.EzyBuilder;
import com.tvd12.ezyfoxserver.client.codec.EzyCombinedCodec;
import com.tvd12.ezyfoxserver.client.codec.EzyNettyCodecCreator;
import com.tvd12.ezyfoxserver.client.constant.EzySocketConstants;
import io.netty.channel.*;
import io.netty.handler.ssl.SslHandler;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;

public abstract class EzyAbstractChannelInitializer extends ChannelInitializer<Channel> {

    protected final SSLContext sslContext;
    protected final EzyNettyCodecCreator codecCreator;
    protected final EzySocketReader socketReader;
    protected final EzyConnectionFuture connectionFuture;

    protected EzyAbstractChannelInitializer(Builder<?> builder) {
        this.sslContext = builder.sslContext;
        this.codecCreator = builder.codecCreator;
        this.socketReader = builder.socketReader;
        this.connectionFuture = builder.connectionFuture;
    }

    @Override
    protected final void initChannel(Channel ch) {
        initChannel0(ch);
        initChannel1(ch);
    }

    protected void initChannel0(Channel ch) {}

    private void initChannel1(Channel ch) {
        ChannelPipeline pipeline = ch.pipeline();
        ChannelOutboundHandler encoder = codecCreator.newNettyEncoder();
        ChannelInboundHandlerAdapter decoder = codecCreator.newNettyDecoder(
            EzySocketConstants.MAX_RESPONSE_SIZE
        );
        if (sslContext != null) {
            SSLEngine engine = sslContext.createSSLEngine();
            engine.setUseClientMode(true);
            pipeline.addLast("ssl", new SslHandler(engine));
        }
        pipeline
            .addLast("codec-1", new EzyCombinedCodec(decoder, encoder))
            .addLast("handler", createDataHandler())
            .addLast("codec-2", new EzyCombinedCodec(decoder, encoder));
    }

    private EzyChannelHandler createDataHandler() {
        return newDataHandlerBuilder()
            .socketReader(socketReader)
            .connectionFuture(connectionFuture)
            .build();
    }

    protected abstract EzyChannelHandler.Builder<?> newDataHandlerBuilder();

    @SuppressWarnings("unchecked")
    public abstract static class Builder<B extends Builder<B>>
        implements EzyBuilder<EzyAbstractChannelInitializer> {

        protected SSLContext sslContext;
        protected EzyNettyCodecCreator codecCreator;
        protected EzySocketReader socketReader;
        protected EzyConnectionFuture connectionFuture;

        public B sslContext(SSLContext sslContext) {
            this.sslContext = sslContext;
            return (B) this;
        }

        public B codecCreator(EzyNettyCodecCreator codecCreator) {
            this.codecCreator = codecCreator;
            return (B) this;
        }

        public B socketReader(EzySocketReader socketReader) {
            this.socketReader = socketReader;
            return (B) this;
        }

        public B connectionFuture(EzyConnectionFuture connectionFuture) {
            this.connectionFuture = connectionFuture;
            return (B) this;
        }
    }
}