package com.tvd12.ezyfoxserver.client.socket;

import java.nio.channels.SocketChannel;

import com.tvd12.ezyfox.util.EzyLoggable;
import com.tvd12.ezyfox.util.EzyResettable;
import com.tvd12.ezyfoxserver.client.event.EzyDisconnectionEvent;
import com.tvd12.ezyfoxserver.client.event.EzyEvent;

/**
 * Created by tavandung12 on 9/21/18.
 */

public class EzySocketDataHandler extends EzyLoggable implements EzyResettable {
    protected SocketChannel socketChannel;
    protected volatile boolean disconnected;
    protected final EzySocketEventQueue socketEventQueue;
    protected final EzyDisconnectionDelegate disconnectionDelegate;

    public EzySocketDataHandler(EzySocketEventQueue eventQueue,
                                EzyDisconnectionDelegate disconnectionDelegate) {
        this.socketEventQueue = eventQueue;
        this.disconnectionDelegate = disconnectionDelegate;
    }

    public void setDisconnected(boolean disconnected) {
        this.disconnected = disconnected;
    }

    public void setSocketChannel(SocketChannel socketChannel) {
        this.socketChannel = socketChannel;
    }

    public void fireSocketDisconnected(int reason) {
        if(disconnected)
            return;
        disconnected = true;
        disconnectionDelegate.onDisconnected(reason);
        EzyEvent event = new EzyDisconnectionEvent(reason);
        EzySocketEvent socketEvent = new EzySimpleSocketEvent(
                EzySocketEventType.EVENT, event);
        fireSocketEvent(socketEvent);
    }

    public void fireSocketEvent(EzySocketEvent socketEvent) {
        socketEventQueue.add(socketEvent);
    }

    @Override
    public void reset() {
    }
}
