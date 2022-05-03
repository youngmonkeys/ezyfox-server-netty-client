package com.tvd12.ezyfoxserver.client.socket;

import com.tvd12.ezyfox.builder.EzyBuilder;
import com.tvd12.ezyfox.entity.EzyArray;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class EzyChannelHandler extends SimpleChannelInboundHandler<EzyArray> {

    protected final Logger logger;
    protected final EzySocketReader socketReader;
    protected final EzyConnectionFuture connectionFuture;

    public EzyChannelHandler(Builder<?> builder) {
        this.socketReader = builder.socketReader;
        this.connectionFuture = builder.connectionFuture;
        this.logger = LoggerFactory.getLogger(getClass());
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        logger.debug("channel {} active", ctx.channel());
    }

    protected void connectionActive(ChannelHandlerContext ctx) {
        logger.debug("connection of channel {} active", ctx.channel());
        connectionFuture.setSuccess(true);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        logger.debug("channel {} inactive", ctx.channel());
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) {
        logger.debug("channel {} register", ctx.channel());
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) {
        logger.debug("channel {} unregister", ctx.channel());
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        logger.debug("handler channel {} added", ctx.channel());
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) {
        logger.debug("handler channel {} removed", ctx.channel());
        socketReader.setActive(false);
        socketReader.setStopped(true);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, EzyArray msg) {
        socketReader.addMessage(msg);
    }

    @SuppressWarnings("unchecked")
    public abstract static class Builder<B extends Builder<B>> implements EzyBuilder<EzyChannelHandler> {

        protected EzySocketReader socketReader;
        protected EzyConnectionFuture connectionFuture;

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
