package com.tvd12.ezyfoxserver.client.codec;

import com.tvd12.ezyfox.codec.EzyObjectToByteEncoder;
import com.tvd12.ezyfoxserver.client.socket.EzyPackage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import static com.tvd12.ezyfoxserver.client.codec.EzyPackageMessageCodecs.encodePackageToBytes;

public class NettyMsgPackMessageToByteEncoder extends MessageToByteEncoder<EzyPackage> {

    protected final EzyObjectToByteEncoder objectToByteEncoder;

    public NettyMsgPackMessageToByteEncoder(
        EzyObjectToByteEncoder objectToByteEncoder
    ) {
        this.objectToByteEncoder = objectToByteEncoder;
    }

    @Override
    protected void encode(
        ChannelHandlerContext ctx,
        EzyPackage msg,
        ByteBuf out
    ) throws Exception {
        byte[] bytes = encodePackageToBytes(objectToByteEncoder, msg);
        out.writeBytes(bytes);
    }
}
