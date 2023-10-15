package com.tvd12.ezyfoxserver.client.socket;

import io.netty.channel.Channel;
import lombok.Setter;

public class EzyNettySocketWriter extends EzySocketWriter {

    @Setter
    protected Channel socket;

    @Override
    protected void writePacketToSocket(EzyPackage packet) {
        socket.writeAndFlush(packet);
    }
}
