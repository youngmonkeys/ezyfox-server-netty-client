/**
 * 
 */
package com.tvd12.ezyfoxserver.client.socket;

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
