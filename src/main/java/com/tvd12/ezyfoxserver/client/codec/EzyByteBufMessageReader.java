package com.tvd12.ezyfoxserver.client.codec;

import com.tvd12.ezyfox.codec.EzyMessageReader;
import com.tvd12.ezyfox.io.EzyInts;
import com.tvd12.ezyfoxserver.client.io.EzyByteBufs;
import io.netty.buffer.ByteBuf;

public class EzyByteBufMessageReader extends EzyMessageReader<ByteBuf> {

    @Override
    protected byte readByte(ByteBuf buffer) {
        return buffer.readByte();
    }

    @Override
    protected int remaining(ByteBuf buffer) {
        return buffer.readableBytes();
    }

    @Override
    protected int readMessageSize(ByteBuf buffer) {
        return EzyInts.bin2uint(EzyByteBufs.getBytes(buffer, getSizeLength()));
    }

    @Override
    protected void readMessageContent(ByteBuf buffer, byte[] content, int offset, int length) {
        buffer.readBytes(content, offset, length);
    }
}
