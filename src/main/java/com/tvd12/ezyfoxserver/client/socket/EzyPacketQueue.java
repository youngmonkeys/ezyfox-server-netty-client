package com.tvd12.ezyfoxserver.client.socket;

import com.tvd12.ezyfox.entity.EzyArray;

public interface EzyPacketQueue {

    int size();
    
	void clear();
	
	EzyArray take() throws  InterruptedException;

	boolean isFull();
	
	boolean isEmpty();

	boolean add(EzyArray packet);

	void wakeup();
	
}
