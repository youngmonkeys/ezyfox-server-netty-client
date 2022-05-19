package com.tvd12.ezyfoxserver.client.concurrent;

import io.netty.channel.nio.NioEventLoopGroup;

public class EzyNettyEventLoopGroup extends NioEventLoopGroup {

    public static final int DEFAULT_MAX_THREADS = 2;

    public EzyNettyEventLoopGroup() {
        this(DEFAULT_MAX_THREADS);
    }

    public EzyNettyEventLoopGroup(int numberOfThreads) {
        super(
            numberOfThreads,
            EzyNettyThreadFactory.create("netty-event-loop")
        );
    }
}
