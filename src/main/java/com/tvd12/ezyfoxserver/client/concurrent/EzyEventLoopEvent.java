package com.tvd12.ezyfoxserver.client.concurrent;

public interface EzyEventLoopEvent {

    boolean call();

    default void onFinished() {}

    default void onRemoved() {}
}
