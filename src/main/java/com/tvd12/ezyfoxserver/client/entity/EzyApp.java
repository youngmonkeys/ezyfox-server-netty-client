package com.tvd12.ezyfoxserver.client.entity;

import com.tvd12.ezyfox.entity.EzyData;
import com.tvd12.ezyfoxserver.client.EzyClient;
import com.tvd12.ezyfoxserver.client.handler.EzyAppDataHandler;
import com.tvd12.ezyfoxserver.client.metrics.EzyMetricsRecorder;
import com.tvd12.ezyfoxserver.client.request.EzyRequest;

public interface EzyApp {

    int getId();

    String getName();

    EzyClient getClient();

    EzyZone getZone();

    default void send(EzyRequest request) {
        send(request, false);
    }

    void send(EzyRequest request, boolean encrypted);

    default void send(String cmd) {
        send(cmd, false);
    }

    void send(String cmd, boolean encrypted);

    default void send(String cmd, EzyData data) {
        send(cmd, data, false);
    }

    void send(String cmd, EzyData data, boolean encrypted);

    default void udpSend(EzyRequest request) {
        udpSend(request, false);
    }

    void udpSend(EzyRequest request, boolean encrypted);

    default void udpSend(String cmd) {
        udpSend(cmd, false);
    }

    void udpSend(String cmd, boolean encrypted);

    default void udpSend(String cmd, EzyData data) {
        udpSend(cmd, data, false);
    }

    void udpSend(String cmd, EzyData data, boolean encrypted);

    EzyAppDataHandler<?> getDataHandler(Object cmd);

    EzyMetricsRecorder getMetricsRecorder();
}
