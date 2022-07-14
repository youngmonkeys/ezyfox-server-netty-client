package com.tvd12.ezyfoxserver.client.socket;

import com.tvd12.ezyfox.concurrent.EzyEventLoopEvent;
import com.tvd12.ezyfox.concurrent.EzyEventLoopGroup;
import com.tvd12.ezyfox.util.EzyLoggable;
import lombok.Setter;

public abstract class EzySocketAdapter
    extends EzyLoggable
    implements EzyEventLoopEvent {

    protected volatile boolean active;
    protected volatile boolean stopped;
    protected final Object adapterLock;
    @Setter
    protected EzyEventLoopGroup eventLoopGroup;

    public EzySocketAdapter() {
        this.active = false;
        this.stopped = false;
        this.adapterLock = new Object();
    }

    public void start() {
        synchronized (adapterLock) {
            if (active) {
                return;
            }
            active = true;
            stopped = false;
            if (eventLoopGroup != null) {
                eventLoopGroup.addEvent(this);
            } else {
                Thread newThread = new Thread(this::loop);
                newThread.setName(getThreadName());
                newThread.start();
            }
        }
    }

    @Override
    public boolean call() {
        return false;
    }

    @Override
    public void onFinished() {
        setStopped();
    }

    protected abstract String getThreadName();

    protected void loop() {
        update();
        setStopped();
    }

    protected abstract void update();

    public void stop() {
        synchronized (adapterLock) {
            active = false;
            if (eventLoopGroup != null) {
                eventLoopGroup.removeEvent(this);
            }
        }
    }

    public boolean isActive() {
        synchronized (adapterLock) {
            return active;
        }
    }

    public boolean isStopped() {
        synchronized (adapterLock) {
            return stopped;
        }
    }

    protected void setStopped() {
        synchronized (adapterLock) {
            this.stopped = true;
        }
    }
}
