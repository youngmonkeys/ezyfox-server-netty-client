package com.tvd12.ezyfoxserver.client.socket;

import com.tvd12.ezyfoxserver.client.handler.EzyChannelHandler;
import com.tvd12.ezyfoxserver.client.handler.EzySocketChannelHandler;

public class EzySocketChannelInitializer extends EzyAbstractChannelInitializer {

	protected EzySocketChannelInitializer(Builder builder) {
		super(builder);
	}
	
	@Override
	protected EzyChannelHandler.Builder<?> newDataHandlerBuilder() {
		return EzySocketChannelHandler.builder();
	}
	
	public static Builder builder() {
		return new Builder();
	}
	
	public static class Builder extends EzyAbstractChannelInitializer.Builder<Builder> {
		
		@Override
		public EzySocketChannelInitializer build() {
			return new EzySocketChannelInitializer(this);
		}
	    
	}
 }