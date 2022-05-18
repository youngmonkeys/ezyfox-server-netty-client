package com.tvd12.ezyfoxserver.client.concurrent;

public interface EzyEventLoopEvent {

    boolean fire();

    default void onFinished() {}
}
