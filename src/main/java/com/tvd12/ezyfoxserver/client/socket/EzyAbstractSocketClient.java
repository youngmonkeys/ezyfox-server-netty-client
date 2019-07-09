package com.tvd12.ezyfoxserver.client.socket;

import java.net.ConnectException;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.util.Set;

import com.tvd12.ezyfox.builder.EzyBuilder;
import com.tvd12.ezyfox.codec.EzyCodecCreator;
import com.tvd12.ezyfox.concurrent.EzyExecutors;
import com.tvd12.ezyfox.entity.EzyArray;
import com.tvd12.ezyfox.entity.EzyData;
import com.tvd12.ezyfox.factory.EzyEntityFactory;
import com.tvd12.ezyfox.util.EzyLoggable;
import com.tvd12.ezyfoxserver.client.config.EzyReconnectConfig;
import com.tvd12.ezyfoxserver.client.constant.EzyCommand;
import com.tvd12.ezyfoxserver.client.event.EzyConnectionFailureEvent;
import com.tvd12.ezyfoxserver.client.event.EzyEvent;
import com.tvd12.ezyfoxserver.client.event.EzyTryConnectEvent;
import com.tvd12.ezyfoxserver.client.manager.EzyHandlerManager;
import com.tvd12.ezyfoxserver.client.manager.EzyPingManager;
import com.tvd12.ezyfoxserver.client.request.EzyRequest;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * Created by tavandung12 on 9/20/18.
 */

public abstract class EzyAbstractSocketClient
		extends EzyLoggable
		implements EzySocketClient, EzyDisconnectionDelegate {

 	protected int reconnectCount;
 	protected long startConnectTime;
 	protected Channel socketChannel;
 	protected EzySocketThread socketThread;
 	protected SocketAddress serverAddress;
 	protected final EzyPingManager pingManager;
    protected final EzyPingSchedule pingSchedule;
    protected final Set<Object> unloggableCommands;
    protected final EzyHandlerManager handlerManager;
    protected final EzyMainThreadQueue mainThreadQueue;
    protected final EzyReconnectConfig reconnectConfig;
    protected final EzySocketDataHandler dataHandler;
    protected final EzyCodecCreator codecCreator;
    protected final EzySocketEventQueue socketEventQueue;
    protected final EzySocketDataEventHandler socketDataEventHandler;
    protected final EzySocketDataEventLoopHandler socketDataEventLoopHandler;
    
    protected EventLoopGroup eventLoopGroup;
    protected ChannelFuture connectionFuture;

    protected EzyAbstractSocketClient(Builder<?> builder) {
        this.pingManager = builder.pingManager;
        this.pingSchedule = builder.pingSchedule;
        this.handlerManager = builder.handlerManager;
        this.reconnectConfig = builder.reconnectConfig;
        this.mainThreadQueue = builder.mainThreadQueue;
        this.unloggableCommands = builder.unloggableCommands;
        this.codecCreator = builder.codecCreator;
        this.socketEventQueue = new EzyLinkedBlockingEventQueue();
        this.dataHandler = newSocketDataHandler();
        this.pingSchedule.setDataHandler(dataHandler);
        this.socketDataEventHandler = newSocketDataEventHandler();
        this.socketDataEventLoopHandler = newSocketDataEventLoopHandler();
        this.startComponents();
    }

    private void startComponents() {
        try {
            this.socketDataEventLoopHandler.start();
        }
        catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
    
    private EzySocketDataHandler newSocketDataHandler() {
        return new EzySocketDataHandler(socketEventQueue, this);
    }

    private EzySocketDataEventHandler newSocketDataEventHandler() {
        return new EzySocketDataEventHandler(
                mainThreadQueue,
                dataHandler,
                pingManager,
                handlerManager,
                socketEventQueue, unloggableCommands);
    }

    private EzySocketDataEventLoopHandler newSocketDataEventLoopHandler() {
        EzySocketDataEventLoopHandler handler = new EzySocketDataEventLoopHandler();
        handler.setEventHandler(socketDataEventHandler);
        return handler;
    }

    @Override
    public void connect(Object... args) throws Exception {
        preConnect(args);
        connect();
    }
    
    protected void preConnect(Object... args) {}

    @Override
    public void connect() {
        reconnectCount = 0;
        handleConnection(0);
    }

    @Override
    public boolean reconnect() {
        int maxReconnectCount = reconnectConfig.getMaxReconnectCount();
        if(reconnectCount >= maxReconnectCount)
            return false;
        long reconnectSleepTime = getReconnectSleepTime();
        handleConnection(reconnectSleepTime);
        reconnectCount++;
        logger.info("try reconnect to server: {}, wating time: {}", reconnectCount, reconnectSleepTime);
        EzyEvent tryConnectEvent = new EzyTryConnectEvent(reconnectCount);
        EzySocketEvent tryConnectSocketEvent
                = new EzySimpleSocketEvent(EzySocketEventType.EVENT, tryConnectEvent);
        dataHandler.fireSocketEvent(tryConnectSocketEvent);
        return true;
    }
    
    private long getReconnectSleepTime() {
        long now = System.currentTimeMillis();
        long offset = now - startConnectTime;
        long reconnectPeriod = reconnectConfig.getReconnectPeriod();
        long sleepTime = reconnectPeriod - offset;
        return sleepTime;
    }

    private void handleConnection(long sleepTime) {
        if(socketThread != null)
            socketThread.cancel();
        disconnect();
        resetComponents();
        socketThread = new EzySocketThread(sleepTime);
        dataHandler.setDisconnected(false);
        socketThread.start();
    }

    protected boolean connect0() throws Exception {
        logger.info("connecting to server ...");
        boolean success = false;
        try {
        		startConnectTime = System.currentTimeMillis();
	    		eventLoopGroup = newLoopGroup();
	        Bootstrap b = newBootstrap(eventLoopGroup);
	        logger.info("connecting to server ...");
	        connectionFuture = b.connect();
	        connectionFuture.syncUninterruptibly();
	        if(connectionFuture.isSuccess()) {
		        	logger.info("connect to server successfully");
	            success = true;
	            reconnectCount = 0;
	            socketChannel = connectionFuture.channel();
	        }
        } 
        catch (Exception e) {
        		EzyEvent event = null;
	        if(e instanceof ConnectException) {
	            ConnectException c = (ConnectException)e;
	            if("Network is unreachable".equalsIgnoreCase(c.getMessage()))
	                event = EzyConnectionFailureEvent.networkUnreachable();
	            else
	                event = EzyConnectionFailureEvent.connectionRefused();
	        }
	        else if(e instanceof  UnknownHostException) {
	            event = EzyConnectionFailureEvent.unknownHost();
	        }
	        else {
	            event = EzyConnectionFailureEvent.unknown();
	        }
	        logger.info("connect to: {} error", getConnectionString(), e);
	        EzySocketEvent socketEvent = new EzySimpleSocketEvent(EzySocketEventType.EVENT, event);
		    dataHandler.fireSocketEvent(socketEvent);
	    }
	    return success;
    }
    
    protected Bootstrap newBootstrap(EventLoopGroup group) {
    		Bootstrap bootstrap = new Bootstrap()
            .group(group)
            .channel(NioSocketChannel.class)
            .remoteAddress(serverAddress)
            .handler(newChannelInitializer())
            .option(ChannelOption.TCP_NODELAY, false)
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5 * 1000);
    		return bootstrap;
    }
    
    protected EventLoopGroup newLoopGroup() {
		return new NioEventLoopGroup(1, EzyExecutors.newThreadFactory("client-event-loop-group"));
    }
    
    protected ChannelInitializer<Channel> newChannelInitializer() {
		EzyAbstractChannelInitializer channelInitializer = newChannelInitializerBuilder()
				.codecCreator(codecCreator)
				.socketEventQueue(socketEventQueue)
				.build();
		return channelInitializer;
    }

    protected abstract EzyAbstractChannelInitializer.Builder<?> newChannelInitializerBuilder();
    
    protected String getConnectionString() {
		return serverAddress.toString();
    }

    @Override
    public void send(EzyRequest request) {
        Object cmd = request.getCommand();
        EzyData data = request.serialize();
        send(cmd, data);
    }

    @Override
    public void send(Object cmd, EzyData data) {
        EzyArray array = EzyEntityFactory.newArrayBuilder()
                .append(((EzyCommand)cmd).getId())
                .append(data)
                .build();
        if(!unloggableCommands.contains(cmd))
            logger.debug("send command: {} and data: {}", cmd, data);
        try {
        		socketChannel.writeAndFlush(array);
        } catch (Exception e) {
            logger.error("send cmd: {} with data: {} error", cmd, data, e);
        }
    }
    
    @Override
    public void disconnect() {
        if(socketChannel != null)
            disconnect0();
        socketChannel = null;
        handleDisconnected();
        dataHandler.setDisconnected(true);
    }

    private void disconnect0() {
        try {
        		socketChannel.disconnect();
            socketChannel.close();
        } catch (Exception e) {
            logger.error("close socket channel: {} error", socketChannel, e);
        }
    }

    public void onDisconnected(int reason) {
        handleDisconnected();
    }

    private void handleDisconnected() {
        socketEventQueue.clear();
        pingSchedule.stop();
    }

    private void resetComponents() {
        socketEventQueue.clear();
        dataHandler.reset();
    }

    private class EzySocketThread {

        private final Thread thread;
        private volatile boolean cancelled;

        public EzySocketThread(final long sleepTime) {
            this.cancelled = false;
            this.thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    handleConnect(sleepTime);
                }
            });
            thread.setName("socket-connection");
        }

        private void handleConnect(long sleepTime) {
            try {
                logger.info("sleeping {} ms before connect to server", sleepTime);
                sleepBeforeConnect(sleepTime);
                if(!cancelled)
                    connect0();
            }
            catch (Exception e) {
                logger.error("start connect to server error", e);
            }
        }

        private void sleepBeforeConnect(long sleepTime) throws InterruptedException {
            if(sleepTime > 0)
                Thread.sleep(sleepTime);
        }


        public void start() {
            thread.start();
        }

        public void cancel() {
            this.cancelled = true;
        }
    }
    
    @SuppressWarnings("unchecked")
    public static abstract class Builder<B extends Builder<B>> implements EzyBuilder<EzySocketClient> {
    	
    		protected EzyPingManager pingManager;
    		protected EzyPingSchedule pingSchedule;
    		protected Set<Object> unloggableCommands;
    		protected EzyHandlerManager handlerManager;
    		protected EzyMainThreadQueue mainThreadQueue;
    		protected EzyReconnectConfig reconnectConfig;
    		protected EzyCodecCreator codecCreator;
    		
    		public B codecCreator(EzyCodecCreator codecCreator) {
    			this.codecCreator = codecCreator;
    			return (B)this;
    		}
        
		public B pingManager(EzyPingManager pingManager) {
        		this.pingManager = pingManager;
        		return (B)this;
        }
        
        public B pingSchedule(EzyPingSchedule pingSchedule) {
        		this.pingSchedule = pingSchedule;
        		return (B)this;
        }
        
        public B unloggableCommands(Set<Object> unloggableCommands) {
        		this.unloggableCommands = unloggableCommands;
        		return (B)this;
        }
        
        public B handlerManager(EzyHandlerManager handlerManager) {
        		this.handlerManager = handlerManager;
        		return (B)this;
        }
        
        public B mainThreadQueue(EzyMainThreadQueue mainThreadQueue) {
        		this.mainThreadQueue = mainThreadQueue;
        		return (B)this;
        }
        
        public B reconnectConfig(EzyReconnectConfig reconnectConfig) {
        		this.reconnectConfig = reconnectConfig;
        		return (B)this;
        }
    }

}
