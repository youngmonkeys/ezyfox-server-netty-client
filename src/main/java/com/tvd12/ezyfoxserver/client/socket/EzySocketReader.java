package com.tvd12.ezyfoxserver.client.socket;

import com.tvd12.ezyfox.entity.EzyArray;
import com.tvd12.ezyfoxserver.client.concurrent.EzySynchronizedQueue;
import com.tvd12.ezyfoxserver.client.util.EzyQueue;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

public class EzySocketReader {

    protected final EzyQueue<EzyArray> dataQueue;
    @Getter
    @Setter
    protected volatile boolean active;
    @Getter
    @Setter
    protected volatile boolean stopped;

    public EzySocketReader() {
        this.active = true;
        this.stopped = false;
        this.dataQueue = new EzySynchronizedQueue<>();
    }

    public void addMessage(EzyArray message) {
        dataQueue.add(message);
    }

    public void popMessages(List<EzyArray> buffer) {
        dataQueue.pollAll(buffer);
    }

    public void clear() {
        this.dataQueue.clear();
    }

    public void reset() {
        this.dataQueue.clear();
        this.active = true;
        this.stopped = false;
    }

}
