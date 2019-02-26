/**
 * 
 */
package com.tvd12.ezyfoxserver.client.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tvd12.ezyfox.builder.EzyBuilder;
import com.tvd12.ezyfox.entity.EzyArray;
import com.tvd12.ezyfoxserver.client.socket.EzyResponse;
import com.tvd12.ezyfoxserver.client.socket.EzySimpleResponse;
import com.tvd12.ezyfoxserver.client.socket.EzySimpleSocketEvent;
import com.tvd12.ezyfoxserver.client.socket.EzySocketEvent;
import com.tvd12.ezyfoxserver.client.socket.EzySocketEventQueue;
import com.tvd12.ezyfoxserver.client.socket.EzySocketEventType;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * @author tavandung12
 *
 */
public abstract class EzyChannelHandler extends SimpleChannelInboundHandler<EzyArray> {

	protected final EzySocketEventQueue socketEventQueue;
	protected final Logger logger;
	
	public EzyChannelHandler(Builder<?> builder) {
		this.socketEventQueue = builder.socketEventQueue;
		this.logger = LoggerFactory.getLogger(getClass());
	}
	
	@Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
		logger.debug("channel {} active", ctx.channel());
    }
	
	protected void connectionActive(ChannelHandlerContext ctx) {
		logger.debug("connection of channel {} active", ctx.channel());
	}
	
	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		logger.debug("channel {} inactive", ctx.channel());
	}
	
	@Override
	public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
		logger.debug("channel {} register", ctx.channel());
	}
	
	@Override
	public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
		logger.debug("channel {} unregister", ctx.channel());
	}

	@Override
	public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
		logger.debug("handler channel {} added", ctx.channel());
	}
	
	@Override
	public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
		logger.debug("handler removed");
	}
	
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, EzyArray msg) throws Exception {
    		EzyResponse response = new EzySimpleResponse(msg);
    		EzySocketEvent socketEvent = new EzySimpleSocketEvent(EzySocketEventType.RESPONSE, response);
    		socketEventQueue.add(socketEvent);
    }
    
    @SuppressWarnings("unchecked")
    public abstract static class Builder<B extends Builder<B>> implements EzyBuilder<EzyChannelHandler> {
    	
    		protected EzySocketEventQueue socketEventQueue;
    		
		public B socketEventQueue(EzySocketEventQueue socketEventQueue) {
    			this.socketEventQueue = socketEventQueue;
    			return (B)this;
    		}
    }
}
