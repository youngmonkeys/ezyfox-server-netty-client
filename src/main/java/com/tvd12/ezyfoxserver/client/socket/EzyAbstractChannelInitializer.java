package com.tvd12.ezyfoxserver.client.socket;

import com.tvd12.ezyfox.builder.EzyBuilder;
import com.tvd12.ezyfox.codec.EzyCodecCreator;
import com.tvd12.ezyfoxserver.client.codec.EzyCombinedCodec;
import com.tvd12.ezyfoxserver.client.handler.EzyChannelHandler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOutboundHandler;
import io.netty.channel.ChannelPipeline;

public abstract class EzyAbstractChannelInitializer extends ChannelInitializer<Channel> {

	protected final EzyCodecCreator codecCreator;
	protected final EzySocketEventQueue socketEventQueue;
	
	protected EzyAbstractChannelInitializer(Builder<?> builder) {
		this.codecCreator = builder.codecCreator;
		this.socketEventQueue = builder.socketEventQueue;
	}
	
	@Override
	protected final void initChannel(Channel ch) throws Exception {
		initChannel0(ch);
		initChannel1(ch);
	}
	
	protected void initChannel0(Channel ch) throws Exception {
	}
	
	private void initChannel1(Channel ch) throws Exception {
		ChannelPipeline pipeline = ch.pipeline();
		ChannelOutboundHandler encoder = (ChannelOutboundHandler) codecCreator.newEncoder();
		ChannelInboundHandlerAdapter decoder = (ChannelInboundHandlerAdapter) codecCreator.newDecoder(65536);
		pipeline.addLast("codec-1", new EzyCombinedCodec(decoder, encoder));
		pipeline.addLast("handler", createDataHandler());
		pipeline.addLast("codec-2", new EzyCombinedCodec(decoder, encoder));
	}
	
	private EzyChannelHandler createDataHandler() {
		EzyChannelHandler.Builder<?> handlerBuilder = newDataHandlerBuilder();
		handlerBuilder.socketEventQueue(socketEventQueue);
		EzyChannelHandler handler = handlerBuilder.build();
		return handler;
	}
	
	protected abstract EzyChannelHandler.Builder<?> newDataHandlerBuilder();

	@SuppressWarnings("unchecked")
	public static abstract class Builder<B extends Builder<B>> 
			implements EzyBuilder<EzyAbstractChannelInitializer> {
		
		protected EzyCodecCreator codecCreator;
		protected EzySocketEventQueue socketEventQueue;
		
		public B codecCreator(EzyCodecCreator codecCreator) {
			this.codecCreator = codecCreator;
			return (B) this;
		}
		
		public B socketEventQueue(EzySocketEventQueue socketEventQueue) {
			this.socketEventQueue = socketEventQueue;
			return (B) this;
		}
	}
 }