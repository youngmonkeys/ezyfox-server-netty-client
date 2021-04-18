package com.tvd12.ezyfoxserver.client.socket;

import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

import com.tvd12.ezyfox.codec.EzyMessage;
import com.tvd12.ezyfox.codec.EzyMessageToBytes;
import com.tvd12.ezyfox.codec.EzyObjectToMessage;
import com.tvd12.ezyfox.entity.EzyArray;

import io.netty.buffer.ByteBuf;

public class EzyUdpSocketWriter extends EzySocketWriter {

	protected DatagramChannel datagramChannel;
	protected EzyMessageToBytes messageToBytes;
	protected EzyObjectToMessage objectToMessage; 
	protected ByteBuffer writeBuffer = ByteBuffer.allocate(4096);
	
	@Override
	protected void writePacketToSocket(EzyArray packet) {
		try {
			EzyMessage message = objectToMessage.convert(packet);
			ByteBuf byteBuf = messageToBytes.convert(message);
			byte[] bytes = byteBuf.array();
			int bytesToWrite = bytes.length;
			ByteBuffer buffer = getWriteBuffer((ByteBuffer)writeBuffer, bytesToWrite);
			buffer.clear();
			buffer.put(bytes);
			buffer.flip();
			datagramChannel.write(buffer);
		}
		catch (Exception e) {
        	logger.warn("I/O error at socket-writer", e);
        }
	}
	
	protected ByteBuffer getWriteBuffer(ByteBuffer fixed, int bytesToWrite) {
		return bytesToWrite > fixed.capacity() ? ByteBuffer.allocate(bytesToWrite) : fixed;
	}
	
	@Override
	protected String getThreadName() {
		return "udp-socket-writer";
	}
	
	public void setMessageToBytes(EzyMessageToBytes messageToBytes) {
		this.messageToBytes = messageToBytes;
	}
	
	public void setObjectToMessage(EzyObjectToMessage objectToMessage) {
		this.objectToMessage = objectToMessage;
	}
	
	public void setDatagramChannel(DatagramChannel datagramChannel) {
		this.datagramChannel = datagramChannel;
	}
	
}
