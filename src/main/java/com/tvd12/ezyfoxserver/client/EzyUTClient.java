package com.tvd12.ezyfoxserver.client;

import com.tvd12.ezyfox.concurrent.EzyEventLoopGroup;
import com.tvd12.ezyfox.entity.EzyArray;
import com.tvd12.ezyfox.entity.EzyData;
import com.tvd12.ezyfoxserver.client.config.EzyClientConfig;
import com.tvd12.ezyfoxserver.client.constant.EzyCommand;
import com.tvd12.ezyfoxserver.client.request.EzyRequest;
import com.tvd12.ezyfoxserver.client.socket.EzyTcpSocketClient;
import com.tvd12.ezyfoxserver.client.socket.EzyUTSocketClient;
import io.netty.channel.EventLoopGroup;

public class EzyUTClient extends EzyTcpClient {

    public EzyUTClient(EzyClientConfig config) {
        super(config);
    }

    public EzyUTClient(
        EzyClientConfig config,
        EzyEventLoopGroup eventLoopGroup,
        EventLoopGroup nettyEventLoopGroup
    ) {
        super(config, eventLoopGroup, nettyEventLoopGroup);
    }

    @Override
    protected EzyTcpSocketClient newNettySocketClient() {
        return new EzyUTSocketClient();
    }

    @Override
    public void udpConnect(int port) {
        ((EzyUTSocketClient) socketClient).udpConnect(port);
    }

    @Override
    public void udpConnect(String host, int port) {
        ((EzyUTSocketClient) socketClient).udpConnect(host, port);
    }

    @Override
    public void udpSend(EzyRequest request) {
        Object cmd = request.getCommand();
        EzyData data = request.serialize();
        udpSend((EzyCommand) cmd, (EzyArray) data);
    }

    @Override
    public void udpSend(EzyCommand cmd, EzyArray data) {
        EzyArray array = requestSerializer.serialize(cmd, data);
        ((EzyUTSocketClient) socketClient).udpSendMessage(array);
        metricsRecorder.increaseSystemRequestCount(cmd);
        printSentData(cmd, data);
    }
}
