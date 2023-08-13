package com.tvd12.ezyfoxserver.client.socket;

import com.tvd12.ezyfox.codec.EzyObjectToByteEncoder;
import lombok.Setter;

import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

import static com.tvd12.ezyfoxserver.client.codec.EzyPackageMessageCodecs.encodePackageToBytes;

@Setter
public class EzyUdpSocketWriter extends EzySocketWriter {

    protected DatagramChannel datagramChannel;
    protected EzyObjectToByteEncoder objectToByteEncoder;
    protected final ByteBuffer writeBuffer = ByteBuffer.allocate(4096);

    @Override
    protected void writePacketToSocket(EzyPackage packet) {
        try {
            byte[] bytes = encodePackageToBytes(objectToByteEncoder, packet);
            int bytesToWrite = bytes.length;
            ByteBuffer buffer = getWriteBuffer(writeBuffer, bytesToWrite);
            buffer.clear();
            buffer.put(bytes);
            buffer.flip();
            datagramChannel.write(buffer);
        } catch (Throwable e) {
            logger.info("I/O error at socket-writer", e);
        }
    }

    protected ByteBuffer getWriteBuffer(ByteBuffer fixed, int bytesToWrite) {
        return bytesToWrite > fixed.capacity() ? ByteBuffer.allocate(bytesToWrite) : fixed;
    }

    @Override
    protected String getThreadName() {
        return "udp-socket-writer";
    }
}
