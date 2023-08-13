package com.tvd12.ezyfoxserver.client.socket;

import com.tvd12.ezyfoxserver.client.codec.EzyNettyCodecCreator;
import com.tvd12.ezyfoxserver.client.concurrent.EzyNettyEventLoopGroup;
import com.tvd12.ezyfoxserver.client.config.EzySocketClientConfig;
import com.tvd12.ezyfoxserver.client.constant.EzyConnectionFailedReason;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.Setter;

import javax.net.ssl.SSLContext;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

public abstract class EzyNettySocketClient extends EzySocketClient {

    protected Channel socket;
    @Setter
    protected SSLContext sslContext;
    @Setter
    protected EventLoopGroup nettyEventLoopGroup;
    protected EventLoopGroup internalEventLoopGroup;
    protected EzyConnectionFuture connectionFuture;
    protected final EzyNettyCodecCreator codecCreator;

    public EzyNettySocketClient(EzySocketClientConfig config) {
        this.codecCreator = newCodecCreator(
            config.isSocketEnableEncryption()
        );
    }

    @Override
    protected boolean connectNow() {
        boolean success = false;
        try {
            if (nettyEventLoopGroup == null) {
                internalEventLoopGroup = new EzyNettyEventLoopGroup();
                nettyEventLoopGroup = internalEventLoopGroup;
            }
            connectionFuture = new EzyConnectionFuture();
            Bootstrap b = newBootstrap(nettyEventLoopGroup);
            ChannelFuture channelFuture = b.connect();
            channelFuture.syncUninterruptibly();
            if (connectionFuture.isSuccess()) {
                reconnectCount = 0;
                socket = channelFuture.channel();
                success = connectionFuture.isSuccess();
                if (success) {
                    postConnectionSuccessfully();
                }
            }
        } catch (Throwable e) {
            if (e instanceof ConnectException) {
                ConnectException c = (ConnectException) e;
                if ("Network is unreachable".equalsIgnoreCase(c.getMessage())) {
                    connectionFailedReason = EzyConnectionFailedReason.NETWORK_UNREACHABLE;
                } else {
                    connectionFailedReason = EzyConnectionFailedReason.CONNECTION_REFUSED;
                }
            } else if (e instanceof UnknownHostException) {
                connectionFailedReason = EzyConnectionFailedReason.UNKNOWN_HOST;
            } else {
                connectionFailedReason = EzyConnectionFailedReason.UNKNOWN;
            }
        }
        return success;
    }

    protected void postConnectionSuccessfully() throws Exception {}

    protected Bootstrap newBootstrap(EventLoopGroup group) {
        return new Bootstrap()
            .group(group)
            .channel(NioSocketChannel.class)
            .remoteAddress(new InetSocketAddress(host, port))
            .handler(newChannelInitializer())
            .option(ChannelOption.TCP_NODELAY, false)
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5 * 1000);
    }

    protected ChannelInitializer<Channel> newChannelInitializer() {
        return newChannelInitializerBuilder()
            .sslContext(sslContext)
            .codecCreator(codecCreator)
            .socketReader(socketReader)
            .connectionFuture(connectionFuture)
            .build();
    }

    protected abstract EzyNettyCodecCreator newCodecCreator(boolean enableEncryption);

    protected abstract EzyAbstractChannelInitializer.Builder<?> newChannelInitializerBuilder();

    @Override
    protected void createAdapters() {
        socketWriter = new EzyNettySocketWriter();
    }

    @Override
    protected void startAdapters() {
        ((EzyNettySocketWriter) socketWriter).setSocket(socket);
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
                if (internalEventLoopGroup != null) {
                    internalEventLoopGroup.shutdownGracefully();
                }
            }
        } catch (Throwable e) {
            logger.info("close socket error");
        }
    }
}
