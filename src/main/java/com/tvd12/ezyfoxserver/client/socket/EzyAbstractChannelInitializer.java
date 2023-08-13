package com.tvd12.ezyfoxserver.client.socket;

import com.tvd12.ezyfox.builder.EzyBuilder;
import com.tvd12.ezyfoxserver.client.codec.EzyCombinedCodec;
import com.tvd12.ezyfoxserver.client.codec.EzyNettyCodecCreator;
import com.tvd12.ezyfoxserver.client.constant.EzySocketConstants;
import io.netty.channel.*;

public abstract class EzyAbstractChannelInitializer extends ChannelInitializer<Channel> {

    protected final EzyNettyCodecCreator codecCreator;
    protected final EzySocketReader socketReader;
    protected final EzyConnectionFuture connectionFuture;

    protected EzyAbstractChannelInitializer(Builder<?> builder) {
        this.codecCreator = builder.codecCreator;
        this.socketReader = builder.socketReader;
        this.connectionFuture = builder.connectionFuture;
    }

    @Override
    protected final void initChannel(Channel ch) throws Exception {
        initChannel0(ch);
        initChannel1(ch);
    }

    protected void initChannel0(Channel ch) throws Exception {
    }

    private void initChannel1(Channel ch) {
        ChannelPipeline pipeline = ch.pipeline();
        ChannelOutboundHandler encoder = codecCreator.newNettyEncoder();
        ChannelInboundHandlerAdapter decoder = codecCreator.newNettyDecoder(
            EzySocketConstants.MAX_RESPONSE_SIZE
        );
        pipeline.addLast("codec-1", new EzyCombinedCodec(decoder, encoder));
        pipeline.addLast("handler", createDataHandler());
        pipeline.addLast("codec-2", new EzyCombinedCodec(decoder, encoder));
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

        protected EzyNettyCodecCreator codecCreator;
        protected EzySocketReader socketReader;
        protected EzyConnectionFuture connectionFuture;

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