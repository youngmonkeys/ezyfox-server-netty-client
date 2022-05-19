package com.tvd12.ezyfoxserver.client.socket;

import com.tvd12.ezyfox.entity.EzyArray;
import com.tvd12.ezyfoxserver.client.constant.EzySocketStatus;

public class EzyUTSocketClient extends EzyTcpSocketClient {

    protected final EzyUdpSocketClient udpClient;

    public EzyUTSocketClient() {
        super();
        this.udpClient = new EzyUdpSocketClient(codecCreator);
    }

    public void udpConnect(int port) {
        udpConnect(host, port);
    }

    public void udpConnect(String host, int port) {
        this.udpClient.setSessionId(sessionId);
        this.udpClient.setSessionToken(sessionToken);
        this.udpClient.setEventLoopGroup(eventLoopGroup);
        this.udpClient.connectTo(host, port);
    }

    public void udpSendMessage(EzyArray message) {
        this.udpClient.sendMessage(message);
    }

    public void udpSetStatus(EzySocketStatus status) {
        this.udpClient.setStatus(status);
    }

    @Override
    protected void popReadMessages() {
        super.popReadMessages();
        this.udpClient.popReadMessages(localMessageQueue);
    }

    @Override
    protected void clearComponents(int disconnectReason) {
        this.udpClient.disconnect();
    }
}
