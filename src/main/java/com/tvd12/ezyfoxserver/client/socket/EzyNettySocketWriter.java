package com.tvd12.ezyfoxserver.client.socket;

import com.tvd12.ezyfox.entity.EzyArray;

import io.netty.channel.Channel;
import lombok.Setter;

public class EzyNettySocketWriter extends EzySocketWriter {

	@Setter
	protected Channel socket;
	
	@Override
	protected void writePacketToSocket(EzyArray packet) {
		socket.writeAndFlush(packet);
	}
	
}
