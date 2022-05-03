package com.tvd12.ezyfoxserver.client;

import com.tvd12.ezyfox.entity.EzyArray;
import com.tvd12.ezyfoxserver.client.config.EzyClientConfig;
import com.tvd12.ezyfoxserver.client.constant.EzyCommand;
import com.tvd12.ezyfoxserver.client.constant.EzyConnectionStatus;
import com.tvd12.ezyfoxserver.client.constant.EzyConnectionType;
import com.tvd12.ezyfoxserver.client.constant.EzyDisconnectReason;
import com.tvd12.ezyfoxserver.client.entity.EzyApp;
import com.tvd12.ezyfoxserver.client.entity.EzyUser;
import com.tvd12.ezyfoxserver.client.entity.EzyZone;
import com.tvd12.ezyfoxserver.client.manager.EzyHandlerManager;
import com.tvd12.ezyfoxserver.client.manager.EzyPingManager;
import com.tvd12.ezyfoxserver.client.request.EzyRequest;
import com.tvd12.ezyfoxserver.client.setup.EzySetup;
import com.tvd12.ezyfoxserver.client.socket.EzyISocketClient;
import com.tvd12.ezyfoxserver.client.socket.EzyPingSchedule;

public interface EzyClient {

    EzySetup setup();

    void connect(String url);

    void connect(String host, int port);

    boolean reconnect();

    void send(EzyRequest request);

    void send(EzyCommand cmd, EzyArray data);

    void disconnect(int reason);

    default void disconnect() {
        disconnect(EzyDisconnectReason.CLOSE.getId());
    }

    void processEvents();

    void udpConnect(int port);

    void udpConnect(String host, int port);

    void udpSend(EzyRequest request);

    void udpSend(EzyCommand cmd, EzyArray data);

    String getName();

    EzyClientConfig getConfig();

    EzyUser getMe();

    EzyZone getZone();

    EzyConnectionStatus getStatus();

    void setStatus(EzyConnectionStatus status);

    EzyConnectionStatus getUdpStatus();

    void setUdpStatus(EzyConnectionStatus status);

    void setSessionId(long sessionId);

    void setSessionToken(String token);

    EzyISocketClient getSocket();

    EzyApp getApp();

    EzyApp getAppById(int appId);

    EzyPingManager getPingManager();

    EzyPingSchedule getPingSchedule();

    EzyHandlerManager getHandlerManager();

    EzyConnectionType getConnectionType();

    default boolean isConnected() {
        return getStatus() == EzyConnectionStatus.CONNECTED;
    }

    default boolean isUdpConnected() {
        return getUdpStatus() == EzyConnectionStatus.CONNECTED;
    }
}
