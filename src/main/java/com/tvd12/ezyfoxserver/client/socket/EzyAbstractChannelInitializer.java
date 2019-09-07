package com.tvd12.ezyfoxserver.client.socket;

import com.tvd12.ezyfox.builder.EzyBuilder;
import com.tvd12.ezyfox.codec.EzyCodecCreator;
import com.tvd12.ezyfoxserver.client.codec.EzyCombinedCodec;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOutboundHandler;
import io.netty.channel.ChannelPipeline;

public abstract class EzyAbstractChannelInitializer extends ChannelInitializer<Channel> {

	protected final EzyCodecCreator codecCreator;
	protected final EzySocketReader socketReader;
	protected final EzyConnectionFuture connectionFuture;
	
	protected EzyAbstractChannelInitializer(Builder<?> builder) {
		this.codecCreator = builder.codecCreator;
		this.socketReader = builder.socketReader;
		this.connectionFuture = builder.connectionFuture; 
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
		EzyChannelHandler handler = newDataHandlerBuilder()
				.socketReader(socketReader)
				.connectionFuture(connectionFuture)
				.build();
		return handler;
	}
	
	protected abstract EzyChannelHandler.Builder<?> newDataHandlerBuilder();

	@SuppressWarnings("unchecked")
	public static abstract class Builder<B extends Builder<B>> 
			implements EzyBuilder<EzyAbstractChannelInitializer> {
		
		protected EzyCodecCreator codecCreator;
		protected EzySocketReader socketReader;
		protected EzyConnectionFuture connectionFuture;
		
		public B codecCreator(EzyCodecCreator codecCreator) {
			this.codecCreator = codecCreator;
			return (B) this;
		}
		
		public B socketReader(EzySocketReader socketReader) {
			this.socketReader = socketReader;
			return (B) this;
		}
		
		public B connectionFuture(EzyConnectionFuture connectionFuture) {
			this.connectionFuture = connectionFuture;
			return (B)this;
		}
	}
 }