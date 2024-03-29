package com.tvd12.ezyfoxserver.client.socket;

import com.tvd12.ezyfox.codec.EzyByteToObjectDecoder;
import com.tvd12.ezyfox.concurrent.EzyEventLoopGroup;
import com.tvd12.ezyfox.entity.EzyArray;
import com.tvd12.ezyfox.util.EzyLoggable;
import com.tvd12.ezyfoxserver.client.codec.EzyNettyCodecCreator;
import com.tvd12.ezyfoxserver.client.constant.EzyDisconnectReason;
import com.tvd12.ezyfoxserver.client.constant.EzySocketConstants;
import com.tvd12.ezyfoxserver.client.constant.EzySocketStatus;
import com.tvd12.ezyfoxserver.client.util.EzyValueStack;
import lombok.Setter;

import java.net.BindException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.List;

import static com.tvd12.ezyfoxserver.client.constant.EzySocketStatuses.isSocketConnectable;

public class EzyUdpSocketClient extends EzyLoggable {

    @Setter
    protected long sessionId;
    @Setter
    protected String sessionToken;
    @Setter
    protected byte[] sessionKey;
    @Setter
    protected EzyEventLoopGroup eventLoopGroup;
    protected InetSocketAddress serverAddress;
    protected DatagramChannel datagramChannel;
    protected EzyUdpSocketReader socketReader;
    protected EzyUdpSocketWriter socketWriter;
    protected final EzyPacketQueue packetQueue;
    protected final EzyNettyCodecCreator codecCreator;
    protected final EzyValueStack<EzySocketStatus> socketStatuses;

    public EzyUdpSocketClient(EzyNettyCodecCreator codecCreator) {
        this.codecCreator = codecCreator;
        this.packetQueue = new EzyBlockingPacketQueue();
        this.socketStatuses = new EzyValueStack<>(EzySocketStatus.NOT_CONNECT);
    }

    public void connectTo(String host, int port) {
        EzySocketStatus status = socketStatuses.last();
        if (!isSocketConnectable(status)) {
            logger.info("udp socket is connecting...");
            return;
        }
        serverAddress = new InetSocketAddress(host, port);
        connect0();
    }

    public boolean reconnect() {
        EzySocketStatus status = socketStatuses.last();
        if (status != EzySocketStatus.CONNECT_FAILED) {
            return false;
        }
        logger.info("udp socket is re-connecting...");
        connect0();
        return true;
    }

    public void setStatus(EzySocketStatus status) {
        socketStatuses.push(status);
    }

    protected void connect0() {
        while (true) {
            try {
                connect1();
                break;
            } catch (BindException e) {
                logger.info("fail to open udp port, re-try again");
            }
        }
    }

    protected void connect1() throws BindException {
        try {
            clearAdapters();
            createAdapters();
            updateAdapters();
            closeSocket();
            packetQueue.clear();
            socketStatuses.clear();
            datagramChannel = DatagramChannel.open();
            datagramChannel.configureBlocking(eventLoopGroup == null);
            datagramChannel.bind(null);
            datagramChannel.connect(serverAddress);
            startAdapters();
            socketStatuses.push(EzySocketStatus.CONNECTING);
            sendHandshakeRequest();
            if (eventLoopGroup != null) {
                eventLoopGroup.addOneTimeEvent(
                    this::reconnectIfNeed,
                    sleepTimeBeforeReconnect()
                );
            } else {
                Thread newThread = new Thread(() -> {
                    try {
                        Thread.sleep(sleepTimeBeforeReconnect());
                        reconnectIfNeed();
                    } catch (InterruptedException e) {
                        logger.info("udp reconnect interrupted", e);
                    } catch (Throwable e) {
                        logger.info("reconnect to server: {} error", serverAddress, e);
                    }
                });
                newThread.setName("udp-reconnect");
                newThread.start();
            }
        } catch (BindException e) {
            throw e;
        } catch (Throwable e) {
            throw new IllegalStateException("udp can't connect to: " + serverAddress, e);
        }
    }

    private void reconnectIfNeed() {
        EzySocketStatus status = socketStatuses.last();
        if (status == EzySocketStatus.CONNECTING) {
            socketStatuses.push(EzySocketStatus.CONNECT_FAILED);
        }
        reconnect();
    }

    protected int sleepTimeBeforeReconnect() {
        return 3000;
    }

    public void disconnect(@SuppressWarnings("unused") int reason) {
        packetQueue.clear();
        packetQueue.wakeup();
        closeSocket();
        clearAdapters();
        socketStatuses.push(EzySocketStatus.DISCONNECTED);
    }

    public void close() {
        disconnect(EzyDisconnectReason.CLOSE.getId());
    }

    public void sendMessage(EzyArray message, boolean encrypted) {
        EzyPackage pack = new EzySimplePackage(
            message,
            encrypted,
            sessionKey
        );
        packetQueue.add(pack);
    }

    public void popReadMessages(List<EzyArray> buffer) {
        EzySocketStatus status = socketStatuses.last();
        if (status == EzySocketStatus.CONNECTING || status == EzySocketStatus.CONNECTED) {
            this.socketReader.popMessages(buffer);
        }
    }

    protected void createAdapters() {
        this.socketReader = new EzyUdpSocketReader();
        this.socketWriter = new EzyUdpSocketWriter();
    }

    protected void updateAdapters() {
        EzyByteToObjectDecoder decoder = codecCreator.newSocketDecoder(
            EzySocketConstants.MAX_RESPONSE_SIZE
        );
        this.socketReader.setDecoder(decoder);
        this.socketReader.setEventLoopGroup(eventLoopGroup);
        this.socketWriter.setPacketQueue(packetQueue);
        this.socketWriter.setEventLoopGroup(eventLoopGroup);
        this.socketWriter.setObjectToByteEncoder(codecCreator.newSocketEncoder());
    }

    protected void startAdapters() {
        this.socketReader.setDecryptionKey(sessionKey);
        this.socketReader.setDatagramChannel(datagramChannel);
        this.socketReader.start();
        this.socketWriter.setDatagramChannel(datagramChannel);
        this.socketWriter.start();
    }

    protected void clearAdapters() {
        this.clearAdapter(socketReader);
        this.socketReader = null;
        this.clearAdapter(socketWriter);
        this.socketWriter = null;
    }

    protected void clearAdapter(EzySocketAdapter adapter) {
        if (adapter != null) {
            adapter.stop();
        }
    }

    protected void closeSocket() {
        try {
            if (datagramChannel != null) {
                datagramChannel.close();
            }
        } catch (Throwable e) {
            logger.info("close udp socket error", e);
        }
    }

    protected void sendHandshakeRequest() throws Exception {
        int tokenSize = sessionToken.length();
        int messageSize = 0;
        messageSize += 8; // sessionIdSize
        messageSize += 2; // tokenLengthSize
        messageSize += tokenSize; // messageSize
        ByteBuffer buffer = ByteBuffer.allocate(1 + 2 + messageSize);
        byte header = 0;
        header |= 1 << 5;
        buffer.put(header);
        buffer.putShort((short) messageSize);
        buffer.putLong(sessionId);
        buffer.putShort((short) tokenSize);
        buffer.put(sessionToken.getBytes());
        buffer.flip();
        datagramChannel.send(buffer, serverAddress);
    }
}
