package com.tvd12.ezyfoxserver.client.socket;

import static com.tvd12.ezyfoxserver.client.constant.EzySocketStatuses.isSocketConnectable;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.List;

import com.tvd12.ezyfox.codec.EzyByteToObjectDecoder;
import com.tvd12.ezyfox.codec.EzyCodecCreator;
import com.tvd12.ezyfox.entity.EzyArray;
import com.tvd12.ezyfox.util.EzyLoggable;
import com.tvd12.ezyfoxserver.client.codec.EzyMessageToBytesFetcher;
import com.tvd12.ezyfoxserver.client.codec.EzyObjectToMessageFetcher;
import com.tvd12.ezyfoxserver.client.constant.EzyDisconnectReason;
import com.tvd12.ezyfoxserver.client.constant.EzySocketConstants;
import com.tvd12.ezyfoxserver.client.constant.EzySocketStatus;
import com.tvd12.ezyfoxserver.client.util.EzyValueStack;

public class EzyUdpSocketClient extends EzyLoggable {

	protected long sessionId;
	protected String sessionToken;
	protected InetSocketAddress serverAddress;
	protected DatagramChannel datagramChannel;
	protected EzyUdpSocketReader socketReader;
	protected EzyUdpSocketWriter socketWriter;
	protected final EzyPacketQueue packetQueue;
	protected final EzyCodecCreator codecCreator;
	protected final EzyValueStack<EzySocketStatus> socketStatuses;
	
	public EzyUdpSocketClient(EzyCodecCreator codecCreator) {
		this.codecCreator = codecCreator;
		this.packetQueue = new EzyBlockingPacketQueue();
		this.socketStatuses = new EzyValueStack<>(EzySocketStatus.NOT_CONNECT);
	}
	
	public void connectTo(String host, int port) {
		EzySocketStatus status = socketStatuses.last();
        if (!isSocketConnectable(status)) {
        	logger.warn("udp socket is connecting...");
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
		try {
			clearAdapters();
	        createAdapters();
	        updateAdapters();
	        closeSocket();
	        packetQueue.clear();
	        socketStatuses.clear();
	        datagramChannel = DatagramChannel.open();
			datagramChannel.bind(null);
			datagramChannel.connect(serverAddress);
			startAdapters();
			socketStatuses.push(EzySocketStatus.CONNECTING);
			sendHandshakeRequest();
			Thread newThread = new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						Thread.sleep(sleepTimeBeforeReconnect());
						EzySocketStatus status = socketStatuses.last();
						if(status == EzySocketStatus.CONNECTING)
							socketStatuses.push(EzySocketStatus.CONNECT_FAILED);
						reconnect();
					}
					catch (InterruptedException e) {
						logger.error("udp reconnect interrupted", e);
					}
				}
			});
			newThread.setName("udp-reconnect");
			newThread.start();
		}
		catch (Exception e) {
			throw new IllegalStateException("udp can't connect to: " + serverAddress,  e);
		}
	}
	
	protected int sleepTimeBeforeReconnect() {
		return 3000;
	}
	
	public void disconnect(int reason) {
		packetQueue.clear();
        packetQueue.wakeup();
        closeSocket();
		clearAdapters();
		socketStatuses.push(EzySocketStatus.DISCONNECTED);
	}
	
	public void close() {
		disconnect(EzyDisconnectReason.CLOSE.getId());
	}

    public void sendMessage(EzyArray message) {
        packetQueue.add(message);
    }
	
	public void popReadMessages(List<EzyArray> buffer) {
		EzySocketStatus status = socketStatuses.last();
		if(status == EzySocketStatus.CONNECTING || status == EzySocketStatus.CONNECTED)
			this.socketReader.popMessages(buffer);
	}
	
	protected void createAdapters() {
		this.socketReader = new EzyUdpSocketReader();
		this.socketWriter = new EzyUdpSocketWriter();
	}
	
	protected void updateAdapters() {
		Object decoder = codecCreator.newDecoder(EzySocketConstants.MAX_RESPONSE_SIZE);
        this.socketReader.setDecoder((EzyByteToObjectDecoder) decoder);
        this.socketWriter.setPacketQueue(packetQueue);
        this.socketWriter.setObjectToMessage(((EzyObjectToMessageFetcher)codecCreator).getObjectToMessage());
        this.socketWriter.setMessageToBytes(((EzyMessageToBytesFetcher)codecCreator).getMessageToBytes());
	}
	
	protected void startAdapters() {
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
        if (adapter != null)
            adapter.stop();
    }
	
	protected void closeSocket() {
		try {
			if(datagramChannel != null)
				datagramChannel.close();
		}
		catch(Exception e) {
			logger.warn("close udp socket error", e);
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
		buffer.putShort((short)messageSize);
		buffer.putLong(sessionId);
		buffer.putShort((short)tokenSize);
		buffer.put(sessionToken.getBytes());
		buffer.flip();
		datagramChannel.send(buffer, serverAddress);
	}
	
	public void setSessionId(long sessionId) {
		this.sessionId = sessionId;
	}
	
	public void setSessionToken(String sessionToken) {
		this.sessionToken = sessionToken;
	}
	
}
