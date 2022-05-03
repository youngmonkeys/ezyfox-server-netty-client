package com.tvd12.ezyfoxserver.client.socket;

import com.tvd12.ezyfox.entity.EzyArray;
import lombok.Setter;

public abstract class EzySocketWriter extends EzySocketAdapter {

    @Setter
    protected EzyPacketQueue packetQueue;

    @Override
    protected void update() {
        while (true) {
            try {
                if (!active) {
                    return;
                }
                EzyArray packet = packetQueue.take();
                if (packet == null) {
                    return;
                }
                writePacketToSocket(packet);
            } catch (InterruptedException e) {
                logger.warn("socket-writer thread interrupted", e);
                return;
            } catch (Exception e) {
                logger.warn("problems in socket-writer main loop, thread", e);
                return;
            }
        }
    }

    protected abstract void writePacketToSocket(EzyArray packet);

    @Override
    protected String getThreadName() {
        return "ezyfox-socket-writer";
    }
}
