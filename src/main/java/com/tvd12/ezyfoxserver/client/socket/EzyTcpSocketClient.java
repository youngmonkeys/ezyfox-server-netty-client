package com.tvd12.ezyfoxserver.client.socket;

import java.net.InetSocketAddress;

/**
 * Created by tavandung12 on 9/20/18.
 */

public class EzyTcpSocketClient extends EzyAbstractSocketClient {
	
	protected EzyTcpSocketClient(Builder builder) {
		super(builder);
	}

	@Override
	protected void preConnect(Object... args) {
		serverAddress = new InetSocketAddress((String)args[0], (Integer)args[1]);
	}
	
	@Override
	protected EzyAbstractChannelInitializer.Builder<?> newChannelInitializerBuilder() {
		return EzySocketChannelInitializer.builder();
	}
	
	public static Builder builder() {
		return new Builder();
	}
	
	public static class Builder extends EzyAbstractSocketClient.Builder<Builder> {

		@Override
		public EzySocketClient build() {
			return new EzyTcpSocketClient(this);
		}
		
	}
}
