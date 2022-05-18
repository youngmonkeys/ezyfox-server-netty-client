package com.tvd12.ezyfoxserver.client.concurrent;

import com.tvd12.ezyfox.concurrent.EzyThreadFactory;

public class EzyNettyThreadFactory extends EzyThreadFactory {

    protected EzyNettyThreadFactory(Builder builder) {
        super(builder);
    }

    public static EzyNettyThreadFactory create(String poolName) {
        return (EzyNettyThreadFactory) builder()
            .poolName(poolName)
            .build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends EzyThreadFactory.Builder {

        protected Builder() {
            super();
            this.prefix = "";
        }

        @Override
        public EzyNettyThreadFactory build() {
            return new EzyNettyThreadFactory(this);
        }
    }
}
