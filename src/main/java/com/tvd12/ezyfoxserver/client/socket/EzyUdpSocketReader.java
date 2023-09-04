package com.tvd12.ezyfoxserver.client.socket;

import com.tvd12.ezyfox.codec.EzyByteToObjectDecoder;
import com.tvd12.ezyfox.codec.EzyMessage;
import com.tvd12.ezyfox.codec.EzyMessageReaders;
import com.tvd12.ezyfox.entity.EzyArray;
import com.tvd12.ezyfoxserver.client.concurrent.EzySynchronizedQueue;
import com.tvd12.ezyfoxserver.client.constant.EzySocketConstants;
import com.tvd12.ezyfoxserver.client.util.EzyQueue;
import lombok.Setter;

import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.DatagramChannel;
import java.util.List;

import static com.tvd12.ezyfoxserver.client.codec.EzyPackageMessageCodecs.decodeMessageToObject;

public class EzyUdpSocketReader extends EzySocketAdapter {

    protected final ByteBuffer buffer;
    protected final int readBufferSize;
    protected final EzyQueue<EzyArray> dataQueue;
    @Setter
    protected byte[] decryptionKey;
    @Setter
    protected EzyByteToObjectDecoder decoder;
    @Setter
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
            } catch (Throwable e) {
                logger.info("I/O error at socket-reader", e);
                return;
            }
        }
    }

    @Override
    public boolean call() {
        try {
            if (!active) {
                return false;
            }
            this.buffer.clear();
            Integer bytesToRead = nonBlockingReadSocketData();
            if (bytesToRead == null) {
                return true;
            } else if (bytesToRead <= 0) {
                return false;
            }
            buffer.flip();
            byte[] binary = new byte[buffer.limit()];
            buffer.get(binary);
            handleReceivedBytes(binary);
        } catch (Throwable e) {
            logger.info("I/O error at socket-reader event loop", e);
            return false;
        }
        return true;
    }

    protected int readSocketData() {
        try {
            datagramChannel.receive(buffer);
            return buffer.position();
        } catch (Throwable e) {
            handleSocketReaderException(e);
            return -1;
        }
    }

    protected Integer nonBlockingReadSocketData() {
        try {
            SocketAddress serverAddress = datagramChannel.receive(buffer);
            if (serverAddress == null) {
                return null;
            }
            return buffer.position();
        } catch (Throwable e) {
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

    protected void handleSocketReaderException(Throwable e) {
        if (e instanceof AsynchronousCloseException) {
            logger.debug("Socket closed by another thread", e);
        } else {
            logger.info("I/O error at socket-reader", e);
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
            Object data = decodeMessageToObject(
                decoder,
                message,
                decryptionKey
            );
            dataQueue.add((EzyArray) data);
        } catch (Throwable e) {
            logger.info("decode error at socket-reader", e);
        }
    }

    @Override
    protected String getThreadName() {
        return "udp-socket-reader";
    }

}
