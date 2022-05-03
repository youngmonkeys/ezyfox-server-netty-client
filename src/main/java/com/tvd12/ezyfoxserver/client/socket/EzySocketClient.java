package com.tvd12.ezyfoxserver.client.socket;

import com.tvd12.ezyfox.entity.EzyArray;
import com.tvd12.ezyfox.util.EzyLoggable;
import com.tvd12.ezyfoxserver.client.config.EzyReconnectConfig;
import com.tvd12.ezyfoxserver.client.constant.EzyCommand;
import com.tvd12.ezyfoxserver.client.constant.EzyConnectionFailedReason;
import com.tvd12.ezyfoxserver.client.constant.EzyDisconnectReason;
import com.tvd12.ezyfoxserver.client.constant.EzySocketStatus;
import com.tvd12.ezyfoxserver.client.event.*;
import com.tvd12.ezyfoxserver.client.handler.EzyDataHandlers;
import com.tvd12.ezyfoxserver.client.handler.EzyEventHandlers;
import com.tvd12.ezyfoxserver.client.manager.EzyHandlerManager;
import com.tvd12.ezyfoxserver.client.manager.EzyPingManager;
import com.tvd12.ezyfoxserver.client.util.EzyValueStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.tvd12.ezyfoxserver.client.constant.EzySocketStatuses.*;

public abstract class EzySocketClient
    extends EzyLoggable
    implements EzyISocketClient, EzySocketDelegate {
    protected final EzySocketReader socketReader;
    protected final EzyPacketQueue packetQueue;
    protected final EzySocketEventQueue socketEventQueue;
    protected final List<EzyEvent> localEventQueue;
    protected final List<EzyArray> localMessageQueue;
    protected final List<EzySocketStatus> localSocketStatuses;
    protected final EzyValueStack<EzySocketStatus> socketStatuses;
    protected String host;
    protected int port;
    protected int reconnectCount;
    protected long connectTime;
    protected int disconnectReason;
    protected long sessionId;
    protected String sessionToken;
    protected EzyReconnectConfig reconnectConfig;
    protected EzyHandlerManager handlerManager;
    protected Set<Object> ignoredLogCommands;
    protected EzyPingManager pingManager;
    protected EzyPingSchedule pingSchedule;
    protected EzyEventHandlers eventHandlers;
    protected EzyDataHandlers dataHandlers;
    protected EzySocketWriter socketWriter;
    protected EzyConnectionFailedReason connectionFailedReason;

    public EzySocketClient() {
        this.socketReader = new EzySocketReader();
        this.packetQueue = new EzyBlockingPacketQueue();
        this.socketEventQueue = new EzySocketEventQueue();
        this.localEventQueue = new ArrayList<>();
        this.localMessageQueue = new ArrayList<>();
        this.localSocketStatuses = new ArrayList<>();
        this.socketStatuses = new EzyValueStack<>(EzySocketStatus.NOT_CONNECT);
    }

    @Override
    public void connectTo(Object... args) {
        EzySocketStatus status = socketStatuses.last();
        if (!isSocketConnectable(status)) {
            logger.warn("socket is connecting...");
            return;
        }
        this.socketStatuses.push(EzySocketStatus.CONNECTING);
        parseConnectionArguments(args);
        this.reconnectCount = 0;
        this.connect0(0);
    }

    protected abstract void parseConnectionArguments(Object... args);

    @Override
    public boolean reconnect() {
        EzySocketStatus status = socketStatuses.last();
        if (!isSocketReconnectable(status)) {
            logger.warn("socket is not in a reconnectable status");
            return false;
        }
        int maxReconnectCount = reconnectConfig.getMaxReconnectCount();
        if (reconnectCount >= maxReconnectCount) {
            return false;
        }
        socketStatuses.push(EzySocketStatus.RECONNECTING);
        int reconnectSleepTime = reconnectConfig.getReconnectPeriod();
        connect0(reconnectSleepTime);
        reconnectCount++;
        logger.info("try reconnect to server: " + reconnectCount + ", waiting time: " + reconnectSleepTime);
        EzyEvent tryConnectEvent = new EzyTryConnectEvent(reconnectCount);
        socketEventQueue.addEvent(tryConnectEvent);
        return true;
    }

    protected void connect0(final int sleepTime) {
        clearAdapters();
        createAdapters();
        updateAdapters();
        closeSocket();
        packetQueue.clear();
        socketEventQueue.clear();
        socketStatuses.clear();
        disconnectReason = EzyDisconnectReason.UNKNOWN.getId();
        connectionFailedReason = EzyConnectionFailedReason.UNKNOWN;
        Thread newThread = new Thread(() -> connect1(sleepTime));
        newThread.setName("ezyfox-connection");
        newThread.start();
    }

    protected void connect1(int sleepTime) {
        long currentTime = System.currentTimeMillis();
        long dt = currentTime - connectTime;
        long realSleepTime = sleepTime;
        if (sleepTime <= 0) {
            //delay 2000ms
            if (dt < 2000) {
                realSleepTime = 2000 - dt;
            }
        }
        if (realSleepTime >= 0) {
            sleepBeforeConnect(realSleepTime);
        }
        socketStatuses.push(EzySocketStatus.CONNECTING);
        boolean success = this.connectNow();
        connectTime = System.currentTimeMillis();

        if (success) {
            this.reconnectCount = 0;
            this.startAdapters();
            this.socketStatuses.push(EzySocketStatus.CONNECTED);
        } else {
            this.resetSocket();
            this.socketStatuses.push(EzySocketStatus.CONNECT_FAILED);
        }
    }

    protected void sleepBeforeConnect(long sleepTime) {
        try {
            Thread.sleep(sleepTime);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    protected abstract boolean connectNow();

    protected abstract void createAdapters();

    protected void updateAdapters() {
        socketReader.reset();
        socketWriter.setPacketQueue(packetQueue);
    }

    protected abstract void startAdapters();

    protected void clearAdapters() {
        clearAdapter(socketWriter);
        socketReader.clear();
        socketWriter = null;
    }

    protected void clearAdapter(EzySocketAdapter adapter) {
        if (adapter != null) {
            adapter.stop();
        }
    }

    protected void clearComponents(int disconnectReason) {}

    protected abstract void resetSocket();

    protected abstract void closeSocket();

    @Override
    public void onDisconnected(int reason) {
        pingSchedule.stop();
        packetQueue.clear();
        packetQueue.wakeup();
        socketEventQueue.clear();
        closeSocket();
        clearAdapters();
        clearComponents(reason);
        socketStatuses.push(EzySocketStatus.DISCONNECTED);
    }

    @Override
    public void disconnect(int reason) {
        if (socketStatuses.last() != EzySocketStatus.CONNECTED) {
            return;
        }
        onDisconnected(disconnectReason = reason);
    }

    @Override
    public void close() {
        disconnect(EzyDisconnectReason.CLOSE.getId());
        pingSchedule.shutdown();
    }

    public void sendMessage(EzyArray message) {
        packetQueue.add(message);
    }

    public void processEventMessages() {
        processReceivedMessages();
        processStatuses();
        processEvents();
    }

    protected void processStatuses() {
        socketStatuses.popAll(localSocketStatuses);
        for (EzySocketStatus status : localSocketStatuses) {
            if (status == EzySocketStatus.CONNECTED) {
                EzyEvent evt = new EzyConnectionSuccessEvent();
                socketEventQueue.addEvent(evt);
            } else if (status == EzySocketStatus.CONNECT_FAILED) {
                EzyEvent evt = new EzyConnectionFailureEvent(connectionFailedReason);
                socketEventQueue.addEvent(evt);
                break;
            } else if (status == EzySocketStatus.DISCONNECTED) {
                EzyEvent evt = new EzyDisconnectionEvent(disconnectReason);
                socketEventQueue.addEvent(evt);
                break;
            }
        }
        localSocketStatuses.clear();
    }

    protected void processEvents() {
        socketEventQueue.popAll(localEventQueue);
        try {
            for (EzyEvent evt : localEventQueue) {
                eventHandlers.handle(evt);
            }
        } finally {
            localEventQueue.clear();
        }
    }

    protected void processReceivedMessages() {
        EzySocketStatus status = socketStatuses.last();
        if (status == EzySocketStatus.CONNECTED) {
            if (socketReader.isActive()) {
                processReceivedMessages0();
            }
        }
        EzySocketStatus statusLast = socketStatuses.last();
        if (isSocketDisconnectable(statusLast)) {
            if (socketReader.isStopped()) {
                onDisconnected(disconnectReason);
            } else if (socketWriter.isStopped()) {
                onDisconnected(disconnectReason);
            }
        }
    }

    protected void processReceivedMessages0() {
        popReadMessages();
        try {
            if (localMessageQueue.size() > 0) {
                pingManager.setLostPingCount(0);
            }
            for (EzyArray ezyArray : localMessageQueue) {
                processReceivedMessage(ezyArray);
            }
        } finally {
            localMessageQueue.clear();
        }
    }

    protected void popReadMessages() {
        socketReader.popMessages(localMessageQueue);
    }

    protected void processReceivedMessage(EzyArray message) {
        int cmdId = message.get(0, int.class);
        EzyArray data = message.get(1, EzyArray.class, null);
        EzyCommand cmd = EzyCommand.valueOf(cmdId);
        printReceivedData(cmd, data);
        if (cmd == EzyCommand.DISCONNECT) {
            disconnectReason = data.get(0, int.class);
            socketStatuses.push(EzySocketStatus.DISCONNECTING);
        } else {
            dataHandlers.handle(cmd, data);
        }
    }

    protected void printReceivedData(EzyCommand cmd, EzyArray data) {
        if (!ignoredLogCommands.contains(cmd)) {
            logger.debug("received command: " + cmd + " and data: " + data);
        }
    }

    public String getHost() {
        return this.host;
    }

    public int getPort() {
        return this.port;
    }

    public void setSessionId(long sessionId) {
        this.sessionId = sessionId;
    }

    public void setSessionToken(String sessionToken) {
        this.sessionToken = sessionToken;
    }

    public void setPingManager(EzyPingManager pingManager) {
        this.pingManager = pingManager;
    }

    public void setPingSchedule(EzyPingSchedule pingSchedule) {
        this.pingSchedule = pingSchedule;
        this.pingSchedule.setSocketEventQueue(socketEventQueue);
    }

    public void setHandlerManager(EzyHandlerManager handlerManager) {
        this.handlerManager = handlerManager;
        this.dataHandlers = handlerManager.getDataHandlers();
        this.eventHandlers = handlerManager.getEventHandlers();
    }

    public void setReconnectConfig(EzyReconnectConfig reconnectConfig) {
        this.reconnectConfig = reconnectConfig;
    }

    public void setIgnoredLogCommands(Set<Object> ignoredLogCommands) {
        this.ignoredLogCommands = ignoredLogCommands;
    }
}
