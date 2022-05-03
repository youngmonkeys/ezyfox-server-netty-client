package com.tvd12.ezyfoxserver.client.socket;

import com.tvd12.ezyfox.entity.EzyArray;

import java.util.LinkedList;
import java.util.Queue;

public class EzyBlockingPacketQueue implements EzyPacketQueue {

    protected final int capacity;
    protected final Queue<EzyArray> queue;

    public EzyBlockingPacketQueue() {
        this(10000);
    }

    public EzyBlockingPacketQueue(int capacity) {
        this.capacity = capacity;
        this.queue = new LinkedList<>();
    }

    @Override
    public int size() {
        synchronized (this) {
            return queue.size();
        }
    }

    @Override
    public void clear() {
        synchronized (this) {
            queue.clear();
        }
    }

    @Override
    public EzyArray take() throws InterruptedException {
        synchronized (this) {
            while (queue.isEmpty()) {
                wait();
            }
            return queue.poll();
        }

    }

    @Override
    public boolean isFull() {
        synchronized (this) {
            int size = queue.size();
            return size >= capacity;
        }
    }

    @Override
    public boolean isEmpty() {
        synchronized (this) {
            return queue.isEmpty();
        }
    }

    @Override
    public boolean add(EzyArray packet) {
        synchronized (this) {
            int size = queue.size();
            if (size >= capacity) {
                return false;
            }
            boolean success = queue.offer(packet);
            if (success) {
                notifyAll();
            }
            return success;
        }
    }

    @Override
    public void wakeup() {
        synchronized (this) {
            queue.offer(null);
            notifyAll();
        }
    }
}
