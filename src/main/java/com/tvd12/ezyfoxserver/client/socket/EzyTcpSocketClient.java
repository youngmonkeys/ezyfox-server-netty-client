package com.tvd12.ezyfoxserver.client.socket;

import com.tvd12.ezyfoxserver.client.codec.EzyNettyCodecCreator;
import com.tvd12.ezyfoxserver.client.codec.NettyMsgPackCodecCreator;
import com.tvd12.ezyfoxserver.client.config.EzySocketClientConfig;
import io.netty.channel.Channel;
import io.netty.handler.ssl.SslHandler;

import java.util.concurrent.Future;

public class EzyTcpSocketClient extends EzyNettySocketClient {

    public EzyTcpSocketClient(EzySocketClientConfig config) {
        super(config);
    }

    @Override
    protected void parseConnectionArguments(Object... args) {
        this.host = (String) args[0];
        this.port = (Integer) args[1];
    }

    @Override
    protected void postConnectionSuccessfully() throws Exception {
        if (sslContext != null) {
            SslHandler sslHandler = socket.pipeline().get(SslHandler.class);
            Future<Channel> handshakeFuture = sslHandler.handshakeFuture();
            handshakeFuture.get();
        }
    }

    @Override
    protected EzyNettyCodecCreator newCodecCreator(boolean enableEncryption) {
        return new NettyMsgPackCodecCreator(
            enableEncryption,
            () -> sessionKey
        );
    }

    @Override
    protected EzyAbstractChannelInitializer.Builder<?> newChannelInitializerBuilder() {
        return EzySocketChannelInitializer
            .builder()
            .sslContext(sslContext);
    }
}
