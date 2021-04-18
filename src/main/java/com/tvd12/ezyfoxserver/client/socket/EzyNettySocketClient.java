package com.tvd12.ezyfoxserver.client.socket;

import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import com.tvd12.ezyfox.codec.EzyCodecCreator;
import com.tvd12.ezyfox.concurrent.EzyExecutors;
import com.tvd12.ezyfoxserver.client.constant.EzyConnectionFailedReason;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

public abstract class EzyNettySocketClient extends EzySocketClient {

	protected Channel socket;
	protected EzyCodecCreator codecCreator;
	protected EzyConnectionFuture connectionFuture;
	
	{
		codecCreator = newCodecCreator();
	}
	
	@Override
	protected boolean connectNow() {
        boolean success = false;
        try {
    		connectionFuture = new EzyConnectionFuture();
    		EventLoopGroup eventLoopGroup = newLoopGroup();
	        Bootstrap b = newBootstrap(eventLoopGroup);
	        ChannelFuture channelFuture = b.connect();
	        channelFuture.syncUninterruptibly();
	        if(connectionFuture.isSuccess()) {
	            reconnectCount = 0;
	            socket = channelFuture.channel();
	            success = connectionFuture.isSuccess();
	        }
        } 
        catch (Exception e) {
	        if(e instanceof ConnectException) {
	            ConnectException c = (ConnectException)e;
	            if("Network is unreachable".equalsIgnoreCase(c.getMessage()))
	               connectionFailedReason = EzyConnectionFailedReason.NETWORK_UNREACHABLE;
	            else
	                connectionFailedReason = EzyConnectionFailedReason.CONNECTION_REFUSED;
	        }
	        else if(e instanceof  UnknownHostException) {
	            connectionFailedReason = EzyConnectionFailedReason.UNKNOWN_HOST;
	        }
	        else {
	            connectionFailedReason = EzyConnectionFailedReason.UNKNOWN;
	        }
	    }
	    return success;
    }
	
	protected Bootstrap newBootstrap(EventLoopGroup group) {
		Bootstrap bootstrap = new Bootstrap()
	        .group(group)
	        .channel(NioSocketChannel.class)
	        .remoteAddress(new InetSocketAddress(host, port))
	        .handler(newChannelInitializer())
	        .option(ChannelOption.TCP_NODELAY, false)
	        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5 * 1000);
		return bootstrap;
	}

	protected EventLoopGroup newLoopGroup() {
		return new NioEventLoopGroup(2, EzyExecutors.newThreadFactory("client-event-loop"));
	}
	
	protected ChannelInitializer<Channel> newChannelInitializer() {
		EzyAbstractChannelInitializer channelInitializer = newChannelInitializerBuilder()
				.codecCreator(codecCreator)
				.socketReader(socketReader)
				.connectionFuture(connectionFuture)
				.build();
		return channelInitializer;
	}
	
	protected abstract EzyCodecCreator newCodecCreator();
	protected abstract EzyAbstractChannelInitializer.Builder<?> newChannelInitializerBuilder();
	
	@Override
    protected void createAdapters() {
        socketWriter = new EzyNettySocketWriter();
    }

    @Override
    protected void startAdapters() {
        ((EzyNettySocketWriter)socketWriter).setSocket(socket);
        socketWriter.start();
    }

    @Override
    protected void resetSocket() {
        this.socket = null;
    }

    @Override
    protected void closeSocket() {
        try {
            if (socket != null) {
            		this.socket.disconnect();
                this.socket.close();
                this.socket.eventLoop().shutdownGracefully();
            }
        }
        catch (Exception e) {
        		logger.warn("close socket error");
        }
    }
}
