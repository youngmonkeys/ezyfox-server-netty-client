package com.tvd12.ezyfoxserver.client.socket;

import java.net.InetSocketAddress;
import java.net.URI;

import com.tvd12.ezyfoxserver.client.util.EzyURIs;

/**
 * Created by tavandung12 on 9/20/18.
 */

public class EzyWebSocketClient extends EzyAbstractSocketClient {

	protected URI uri;
	
	protected EzyWebSocketClient(Builder builder) {
		super(builder);
	}
	
	@Override
	protected void preConnect(Object... args) {
		Object arg0 = args[0];
		uri = (arg0 instanceof URI) ? (URI)arg0 : URI.create(arg0.toString());
		int port = EzyURIs.getWsPort(uri);
		serverAddress = new InetSocketAddress(uri.getHost(), port);
	}

	@Override
    protected EzyAbstractChannelInitializer.Builder<?> newChannelInitializerBuilder() {
    		return EzyWebSocketChannelInitializer.builder().uri(uri);
    }
	
	public static Builder builder() {
		return new Builder();
	}
	
	public static class Builder extends EzyAbstractSocketClient.Builder<Builder> {
		
		@Override
		public EzySocketClient build() {
			return new EzyWebSocketClient(this);
		}
		
	}
}
