package com.tvd12.ezyfoxserver.client.socket;

import io.netty.channel.ChannelHandlerContext;

import static io.netty.handler.codec.http.websocketx.WebSocketClientProtocolHandler.ClientHandshakeStateEvent.HANDSHAKE_COMPLETE;

public class EzyWebSocketChannelHandler extends EzyChannelHandler {

    public EzyWebSocketChannelHandler(Builder builder) {
        super(builder);
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
        logger.debug("channel: {} event: {} triggered", ctx.channel(), evt);
        if (evt.equals(HANDSHAKE_COMPLETE)) {
            connectionActive(ctx);
        }
    }

    public static class Builder extends EzyChannelHandler.Builder<Builder> {

        @Override
        public EzyChannelHandler build() {
            return new EzyWebSocketChannelHandler(this);
        }
    }
}
