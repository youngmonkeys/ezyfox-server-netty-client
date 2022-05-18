package com.tvd12.ezyfoxserver.client.socket;

import com.tvd12.ezyfox.util.EzyLoggable;
import com.tvd12.ezyfoxserver.client.concurrent.EzyEventLoopEvent;
import com.tvd12.ezyfoxserver.client.concurrent.EzyEventLoopGroup;
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
    public boolean fire() {
        return false;
    }

    @Override
    public void onFinished() {
        setStopped(true);
    }

    protected abstract String getThreadName();

    protected void loop() {
        update();
        setStopped(true);
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

    protected void setActive(boolean active) {
        synchronized (adapterLock) {
            this.active = active;
        }
    }

    public boolean isStopped() {
        synchronized (adapterLock) {
            return stopped;
        }
    }

    protected void setStopped(boolean stopped) {
        synchronized (adapterLock) {
            this.stopped = stopped;
        }
    }
}
