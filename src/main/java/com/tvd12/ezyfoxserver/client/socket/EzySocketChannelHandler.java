package com.tvd12.ezyfoxserver.client.socket;

import io.netty.channel.ChannelHandlerContext;

public class EzySocketChannelHandler extends EzyChannelHandler {

    public EzySocketChannelHandler(Builder builder) {
        super(builder);
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        connectionActive(ctx);
    }

    public static class Builder extends EzyChannelHandler.Builder<Builder> {

        @Override
        public EzyChannelHandler build() {
            return new EzySocketChannelHandler(this);
        }
    }
}
