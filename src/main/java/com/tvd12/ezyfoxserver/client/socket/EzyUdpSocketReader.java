package com.tvd12.ezyfoxserver.client.socket;

import com.tvd12.ezyfox.codec.EzyByteToObjectDecoder;
import com.tvd12.ezyfox.codec.EzyMessage;
import com.tvd12.ezyfox.codec.EzyMessageReaders;
import com.tvd12.ezyfox.entity.EzyArray;
import com.tvd12.ezyfoxserver.client.concurrent.EzySynchronizedQueue;
import com.tvd12.ezyfoxserver.client.constant.EzySocketConstants;
import com.tvd12.ezyfoxserver.client.util.EzyQueue;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.DatagramChannel;
import java.util.List;

public class EzyUdpSocketReader extends EzySocketAdapter {

    protected final ByteBuffer buffer;
    protected final int readBufferSize;
    protected final EzyQueue<EzyArray> dataQueue;
    protected EzyByteToObjectDecoder decoder;
    protected DatagramChannel datagramChannel;

    public EzyUdpSocketReader() {
        super();
        this.readBufferSize = EzySocketConstants.MAX_READ_BUFFER_SIZE;
        this.dataQueue = new EzySynchronizedQueue<>();
        this.buffer = ByteBuffer.allocateDirect(readBufferSize);
    }

    @Override
    protected void loop() {
        super.loop();
    }

    @Override
    protected void update() {
        while (true) {
            try {
                if (!active) {
                    return;
                }
                this.buffer.clear();
                int bytesToRead = readSocketData();
                if (bytesToRead <= 0) {
                    return;
                }
                buffer.flip();
                byte[] binary = new byte[buffer.limit()];
                buffer.get(binary);
                handleReceivedBytes(binary);
            } catch (Exception e) {
                logger.warn("I/O error at socket-reader", e);
                return;
            }
        }
    }

    protected int readSocketData() {
        try {
            datagramChannel.receive(buffer);
            return buffer.position();
        } catch (Exception e) {
            handleSocketReaderException(e);
            return -1;
        }
    }

    protected void handleReceivedBytes(byte[] bytes) {
        EzyMessage message = EzyMessageReaders.bytesToMessage(bytes);
        if (message == null) {
            return;
        }
        onMessageReceived(message);
    }

    protected void handleSocketReaderException(Exception e) {
        if (e instanceof AsynchronousCloseException) {
            logger.debug("Socket closed by another thread", e);
        } else {
            logger.warn("I/O error at socket-reader", e);
        }
    }

    protected void clear() {
        if (dataQueue != null) {
            dataQueue.clear();
        }
    }

    public void popMessages(List<EzyArray> buffer) {
        dataQueue.pollAll(buffer);
    }

    private void onMessageReceived(EzyMessage message) {
        try {
            Object data = decoder.decode(message);
            dataQueue.add((EzyArray) data);
        } catch (Exception e) {
            logger.warn("decode error at socket-reader", e);
        }
    }

    public void setDecoder(EzyByteToObjectDecoder decoder) {
        this.decoder = decoder;
    }

    public void setDatagramChannel(DatagramChannel datagramChannel) {
        this.datagramChannel = datagramChannel;
    }

    @Override
    protected String getThreadName() {
        return "udp-socket-reader";
    }

}
