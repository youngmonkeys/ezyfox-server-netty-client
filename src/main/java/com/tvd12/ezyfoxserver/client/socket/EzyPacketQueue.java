package com.tvd12.ezyfoxserver.client.socket;

public interface EzyPacketQueue {

    int size();

    void clear();

    EzyPackage poll();

    EzyPackage take() throws InterruptedException;

    boolean isFull();

    boolean isEmpty();

    boolean add(EzyPackage packet);

    void wakeup();
}
