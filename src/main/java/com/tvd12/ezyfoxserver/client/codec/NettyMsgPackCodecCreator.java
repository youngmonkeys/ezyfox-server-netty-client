package com.tvd12.ezyfoxserver.client.codec;

import com.tvd12.ezyfox.codec.EzyByteToObjectDecoder;
import com.tvd12.ezyfox.codec.EzyCodecCreator;
import com.tvd12.ezyfox.codec.EzyObjectToByteEncoder;
import com.tvd12.ezyfox.codec.MsgPackCodecCreator;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelOutboundHandler;

import java.util.function.Supplier;

public class NettyMsgPackCodecCreator implements EzyNettyCodecCreator {

    private final EzyCodecCreator socketCodecCreator;
    private final Supplier<byte[]> decryptionKeySupplier;

    public NettyMsgPackCodecCreator(
        boolean enableEncryption,
        Supplier<byte[]> decryptionKeySupplier
    ) {
        this.decryptionKeySupplier = decryptionKeySupplier;
        this.socketCodecCreator = new MsgPackCodecCreator(enableEncryption);
    }

    @Override
    public EzyObjectToByteEncoder newSocketEncoder() {
        return (EzyObjectToByteEncoder) socketCodecCreator.newEncoder();
    }

    @Override
    public EzyByteToObjectDecoder newSocketDecoder(int maxRequestSize) {
        return (EzyByteToObjectDecoder) socketCodecCreator.newDecoder(
            maxRequestSize
        );
    }

    @Override
    public ChannelOutboundHandler newNettyEncoder() {
        return new NettyMsgPackMessageToByteEncoder(newSocketEncoder());
    }

    @Override
    public ChannelInboundHandlerAdapter newNettyDecoder(int maxRequestSize) {
        return new NettyMsgPackByteToMessageDecoder(
            newSocketDecoder(maxRequestSize),
            maxRequestSize,
            decryptionKeySupplier
        );
    }
}
