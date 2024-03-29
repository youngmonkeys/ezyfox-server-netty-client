package com.tvd12.ezyfoxserver.client;

import com.tvd12.ezyfox.entity.EzyArray;
import com.tvd12.ezyfoxserver.client.config.EzyClientConfig;
import com.tvd12.ezyfoxserver.client.constant.*;
import com.tvd12.ezyfoxserver.client.entity.EzyApp;
import com.tvd12.ezyfoxserver.client.entity.EzyUser;
import com.tvd12.ezyfoxserver.client.entity.EzyZone;
import com.tvd12.ezyfoxserver.client.manager.EzyHandlerManager;
import com.tvd12.ezyfoxserver.client.manager.EzyPingManager;
import com.tvd12.ezyfoxserver.client.metrics.EzyMetricsRecorder;
import com.tvd12.ezyfoxserver.client.request.EzyRequest;
import com.tvd12.ezyfoxserver.client.setup.EzySetup;
import com.tvd12.ezyfoxserver.client.socket.EzyISocketClient;
import com.tvd12.ezyfoxserver.client.socket.EzyPingSchedule;

public interface EzyClient {

    EzySetup setup();

    void connect(String url);

    void connect(String host, int port);

    boolean reconnect();

    default void send(EzyRequest request) {
        send(request, false);
    }

    void send(EzyRequest request, boolean encrypted);

    default void send(EzyCommand cmd, EzyArray data) {
        send(cmd, data, false);
    }

    void send(EzyCommand cmd, EzyArray data, boolean encrypted);

    void disconnect(int reason);

    default void disconnect() {
        disconnect(EzyDisconnectReason.CLOSE.getId());
    }

    default void close() {
        disconnect();
    }

    void processEvents();

    void udpConnect(int port);

    void udpConnect(String host, int port);

    default void udpSend(EzyRequest request) {
        udpSend(request, false);
    }

    void udpSend(EzyRequest request, boolean encrypted);

    default void udpSend(EzyCommand cmd, EzyArray data) {
        udpSend(cmd, data, false);
    }

    void udpSend(EzyCommand cmd, EzyArray data, boolean encrypted);

    String getName();

    EzyClientConfig getConfig();

    boolean isSocketEnableSSL();

    EzySslType getSocketSslType();

    boolean isSocketEnableEncryption();

    boolean isSocketEnableCertificationSSL();

    boolean isEnableDebug();

    EzyUser getMe();

    EzyZone getZone();

    EzyConnectionStatus getStatus();

    void setStatus(EzyConnectionStatus status);

    EzyConnectionStatus getUdpStatus();

    void setUdpStatus(EzyConnectionStatus status);

    long getSessionId();

    void setSessionId(long sessionId);

    String getSessionToken();

    void setSessionToken(String token);

    byte[] getSessionKey();

    void setSessionKey(byte[] sessionKey);

    byte[] getPrivateKey();

    void setPrivateKey(byte[] privateKey);

    byte[] getPublicKey();

    void setPublicKey(byte[] publicKey);

    EzyISocketClient getSocket();

    EzyApp getApp();

    EzyApp getAppById(int appId);

    EzyPingManager getPingManager();

    EzyPingSchedule getPingSchedule();

    EzyHandlerManager getHandlerManager();

    EzyConnectionType getConnectionType();

    EzyMetricsRecorder getMetricsRecorder();

    default boolean isConnected() {
        return getStatus() == EzyConnectionStatus.CONNECTED;
    }

    default boolean isUdpConnected() {
        return getUdpStatus() == EzyConnectionStatus.CONNECTED;
    }
}
