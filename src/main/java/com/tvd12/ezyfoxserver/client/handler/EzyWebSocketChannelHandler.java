/**
 * 
 */
package com.tvd12.ezyfoxserver.client.handler;

import com.tvd12.ezyfoxserver.client.event.EzyConnectionSuccessEvent;
import com.tvd12.ezyfoxserver.client.event.EzyEvent;
import com.tvd12.ezyfoxserver.client.socket.EzySimpleSocketEvent;
import com.tvd12.ezyfoxserver.client.socket.EzySocketEvent;
import com.tvd12.ezyfoxserver.client.socket.EzySocketEventType;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.WebSocketClientProtocolHandler;

/**
 * @author tavandung12
 *
 */
public class EzyWebSocketChannelHandler extends EzyChannelHandler {

	public EzyWebSocketChannelHandler(Builder builder) {
		super(builder);
	}
	
	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
		logger.debug("channel: {} event: {} trigged", ctx.channel(), evt);
		if(evt.equals(WebSocketClientProtocolHandler.ClientHandshakeStateEvent.HANDSHAKE_COMPLETE))
			connectionActive(ctx);
	}
	
	@Override
	protected void connectionActive(ChannelHandlerContext ctx) {
		super.connectionActive(ctx);
		EzyEvent event = new EzyConnectionSuccessEvent();
		EzySocketEvent socketEvent = new EzySimpleSocketEvent(EzySocketEventType.EVENT, event);
		socketEventQueue.add(socketEvent);
	}
	
	public static Builder builder() {
		return new Builder();
	}
	
	public static class Builder extends EzyChannelHandler.Builder<Builder> {

		@Override
		public EzyChannelHandler build() {
			return new EzyWebSocketChannelHandler(this);
		}
		
	}

}
