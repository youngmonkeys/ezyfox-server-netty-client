package com.tvd12.ezyfoxserver.client.socket;

import com.tvd12.ezyfox.codec.EzyCodecCreator;
import com.tvd12.ezyfoxserver.client.codec.MsgPackCodecCreator;
import io.netty.channel.Channel;
import io.netty.handler.ssl.SslHandler;
import lombok.Setter;

import javax.net.ssl.SSLContext;
import java.util.concurrent.Future;

public class EzyTcpSocketClient extends EzyNettySocketClient {

    @Setter
    private SSLContext sslContext;

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
    protected EzyCodecCreator newCodecCreator() {
        return new MsgPackCodecCreator();
    }

    @Override
    protected EzyAbstractChannelInitializer.Builder<?> newChannelInitializerBuilder() {
        return EzySocketChannelInitializer
            .builder()
            .sslContext(sslContext);
    }
}
