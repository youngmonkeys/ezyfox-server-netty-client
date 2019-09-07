package com.tvd12.ezyfoxserver.client.socket;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.tvd12.ezyfox.entity.EzyArray;

public class EzyBlockingPacketQueue implements EzyPacketQueue {

	private final int capacity;
	private final BlockingQueue<EzyArray> queue;

	public EzyBlockingPacketQueue() {
		this(10000);
	}

	public EzyBlockingPacketQueue(int capacity) {
		this.capacity = capacity;
		this.queue = new LinkedBlockingQueue<>(capacity);
	}

	@Override
	public int size() {
		int size = queue.size();
		return size;
	}

	@Override
	public void clear() {
		queue.clear();
	}

	@Override
	public EzyArray take() throws InterruptedException {
		EzyArray packet = queue.take();
		return packet;
	}

	@Override
	public boolean isFull() {
		int size = queue.size();
		boolean full = size >= capacity;
		return full;
	}

	@Override
	public boolean isEmpty() {
		boolean empty = queue.isEmpty();
		return empty;
	}

	@Override
	public boolean add(EzyArray packet) {
		int size = queue.size();
		if(size >= capacity)
			return false;
		boolean success = queue.offer(packet);
		return success;
	}


	@Override
	public void wakeup() {
		queue.offer(null);
	}
}
