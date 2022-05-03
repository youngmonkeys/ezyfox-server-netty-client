package com.tvd12.ezyfoxserver.client.socket;

public class EzySocketChannelInitializer extends EzyAbstractChannelInitializer {

    protected EzySocketChannelInitializer(Builder builder) {
        super(builder);
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    protected EzyChannelHandler.Builder<?> newDataHandlerBuilder() {
        return EzySocketChannelHandler.builder();
    }

    public static class Builder extends EzyAbstractChannelInitializer.Builder<Builder> {

        @Override
        public EzySocketChannelInitializer build() {
            return new EzySocketChannelInitializer(this);
        }
    }
}