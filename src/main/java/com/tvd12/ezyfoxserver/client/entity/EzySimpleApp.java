package com.tvd12.ezyfoxserver.client.entity;

import com.tvd12.ezyfox.builder.EzyArrayBuilder;
import com.tvd12.ezyfox.entity.EzyArray;
import com.tvd12.ezyfox.entity.EzyData;
import com.tvd12.ezyfox.entity.EzyEntity;
import com.tvd12.ezyfox.factory.EzyEntityFactory;
import com.tvd12.ezyfoxserver.client.EzyClient;
import com.tvd12.ezyfoxserver.client.constant.EzyCommand;
import com.tvd12.ezyfoxserver.client.handler.EzyAppDataHandler;
import com.tvd12.ezyfoxserver.client.handler.EzyAppDataHandlers;
import com.tvd12.ezyfoxserver.client.metrics.EzyMetricsRecorder;
import com.tvd12.ezyfoxserver.client.request.EzyRequest;
import lombok.Getter;
import lombok.Setter;

public class EzySimpleApp extends EzyEntity implements EzyApp {
    protected final int id;
    protected final String name;
    protected final EzyZone zone;
    protected final EzyClient client;
    protected final EzyAppDataHandlers dataHandlers;
    @Setter
    @Getter
    protected EzyMetricsRecorder metricsRecorder;

    public EzySimpleApp(EzyZone zone, int id, String name) {
        this.client = zone.getClient();
        this.zone = zone;
        this.id = id;
        this.name = name;
        this.dataHandlers = client.getHandlerManager().getAppDataHandlers(name);
        this.metricsRecorder = EzyMetricsRecorder.getDefault();
    }

    public void send(EzyRequest request) {
        String cmd = (String) request.getCommand();
        EzyData data = request.serialize();
        send(cmd, data);
    }

    public void send(String cmd) {
        send(cmd, EzyEntityFactory.EMPTY_OBJECT);
    }

    public void send(String cmd, EzyData data) {
        EzyArrayBuilder commandData = EzyEntityFactory.newArrayBuilder()
            .append(cmd)
            .append(data);
        EzyArray requestData = EzyEntityFactory.newArrayBuilder()
            .append(id)
            .append(commandData.build())
            .build();
        client.send(EzyCommand.APP_REQUEST, requestData);
        metricsRecorder.increaseAppRequestCount(cmd);
    }

    @Override
    public void udpSend(EzyRequest request) {
        String cmd = (String) request.getCommand();
        EzyData data = request.serialize();
        udpSend(cmd, data);
    }

    @Override
    public void udpSend(String cmd) {
        udpSend(cmd, EzyEntityFactory.EMPTY_OBJECT);
    }

    @Override
    public void udpSend(String cmd, EzyData data) {
        EzyArray commandData = EzyEntityFactory.newArray();
        commandData.add(cmd, data);
        EzyArray requestData = EzyEntityFactory.newArray();
        requestData.add(id, commandData);
        client.udpSend(EzyCommand.APP_REQUEST, requestData);
        metricsRecorder.increaseAppRequestCount(cmd);
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public EzyClient getClient() {
        return client;
    }

    public EzyZone getZone() {
        return zone;
    }

    public EzyAppDataHandler<?> getDataHandler(Object cmd) {
        return (EzyAppDataHandler<?>) dataHandlers.getHandler(cmd);
    }

    @Override
    public String toString() {
        return "App(" +
            "id: " + id + ", " +
            "name: " + name +
            ")";
    }
}
