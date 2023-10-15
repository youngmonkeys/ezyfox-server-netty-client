package com.tvd12.ezyfoxserver.client.socket;

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
                EzyPackage packet = packetQueue.take();
                if (packet == null) {
                    return;
                }
                writePacketToSocket(packet);
            } catch (InterruptedException e) {
                logger.info("socket-writer thread interrupted", e);
                return;
            } catch (Throwable e) {
                logger.info("problems in socket-writer", e);
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
            EzyPackage packet = packetQueue.poll();
            if (packet == null) {
                return true;
            }
            writePacketToSocket(packet);
        } catch (Throwable e) {
            logger.info("problems in socket-writer event loop", e);
            return false;
        }
        return true;
    }

    protected abstract void writePacketToSocket(EzyPackage packet);

    @Override
    protected String getThreadName() {
        return "ezyfox-socket-writer";
    }
}
