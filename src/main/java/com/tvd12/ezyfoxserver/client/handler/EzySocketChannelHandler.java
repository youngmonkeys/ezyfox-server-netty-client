package com.tvd12.ezyfoxserver.client.handler;

public class EzySocketChannelHandler extends EzyChannelHandler {
	
	public EzySocketChannelHandler(Builder builder) {
		super(builder);
	}
	
	public static Builder builder() {
		return new Builder();
	}

	public static class Builder extends EzyChannelHandler.Builder<Builder> {

		@Override
		public EzyChannelHandler build() {
			return new EzySocketChannelHandler(this);
		}
		
	}
}
