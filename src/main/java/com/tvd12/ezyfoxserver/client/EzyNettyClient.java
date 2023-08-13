package com.tvd12.ezyfoxserver.client;

import com.tvd12.ezyfox.concurrent.EzyEventLoopGroup;
import com.tvd12.ezyfox.entity.EzyArray;
import com.tvd12.ezyfox.entity.EzyData;
import com.tvd12.ezyfox.entity.EzyEntity;
import com.tvd12.ezyfoxserver.client.config.EzyClientConfig;
import com.tvd12.ezyfoxserver.client.constant.EzyCommand;
import com.tvd12.ezyfoxserver.client.constant.EzyConnectionStatus;
import com.tvd12.ezyfoxserver.client.constant.EzySslType;
import com.tvd12.ezyfoxserver.client.entity.*;
import com.tvd12.ezyfoxserver.client.manager.*;
import com.tvd12.ezyfoxserver.client.metrics.EzyMetricsRecorder;
import com.tvd12.ezyfoxserver.client.request.EzyRequest;
import com.tvd12.ezyfoxserver.client.request.EzyRequestSerializer;
import com.tvd12.ezyfoxserver.client.request.EzySimpleRequestSerializer;
import com.tvd12.ezyfoxserver.client.setup.EzySetup;
import com.tvd12.ezyfoxserver.client.setup.EzySimpleSetup;
import com.tvd12.ezyfoxserver.client.socket.EzyISocketClient;
import com.tvd12.ezyfoxserver.client.socket.EzyNettySocketClient;
import com.tvd12.ezyfoxserver.client.socket.EzyPingSchedule;
import com.tvd12.ezyfoxserver.client.socket.EzySocketClient;
import io.netty.channel.EventLoopGroup;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static com.tvd12.ezyfoxserver.client.constant.EzyConnectionStatuses.isClientConnectable;
import static com.tvd12.ezyfoxserver.client.constant.EzyConnectionStatuses.isClientReconnectable;

public abstract class EzyNettyClient
    extends EzyEntity
    implements EzyClient, EzyMeAware, EzyZoneAware {

    @Setter
    @Getter
    protected EzyUser me;
    @Setter
    @Getter
    protected EzyZone zone;
    @Getter
    protected long sessionId;
    @Setter
    @Getter
    protected byte[] publicKey;
    @Setter
    @Getter
    protected byte[] privateKey;
    @Getter
    protected byte[] sessionKey;
    @Getter
    protected String sessionToken;
    @Setter
    @Getter
    protected EzyConnectionStatus status;
    @Setter
    @Getter
    protected EzyConnectionStatus udpStatus;
    @Setter
    @Getter
    protected EzyMetricsRecorder metricsRecorder;
    @Getter
    protected final String name;
    protected final EzySetup settingUp;
    @Getter
    protected final EzyClientConfig config;
    @Getter
    protected final EzyPingManager pingManager;
    @Getter
    protected final EzyHandlerManager handlerManager;
    protected final EzyRequestSerializer requestSerializer;
    protected final Set<Object> ignoredLogCommands;
    protected final EzySocketClient socketClient;
    @Getter
    protected final EzyPingSchedule pingSchedule;
    protected final EzyEventLoopGroup eventLoopGroup;
    protected final EventLoopGroup nettyEventLoopGroup;
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    public EzyNettyClient(EzyClientConfig config) {
        this(config, null, null);
    }

    public EzyNettyClient(
        EzyClientConfig config,
        EzyEventLoopGroup eventLoopGroup,
        EventLoopGroup nettyEventLoopGroup
    ) {
        this.config = config;
        this.name = config.getClientName();
        this.status = EzyConnectionStatus.NULL;
        this.eventLoopGroup = eventLoopGroup;
        this.nettyEventLoopGroup = nettyEventLoopGroup;
        this.metricsRecorder = EzyMetricsRecorder.getDefault();
        this.pingManager = new EzySimplePingManager(config.getPing());
        this.pingSchedule = new EzyPingSchedule(this, eventLoopGroup);
        this.handlerManager = new EzySimpleHandlerManager(this);
        this.requestSerializer = new EzySimpleRequestSerializer();
        this.settingUp = new EzySimpleSetup(handlerManager);
        this.ignoredLogCommands = newIgnoredLogCommands();
        this.socketClient = newSocketClient();
    }

    private Set<Object> newIgnoredLogCommands() {
        Set<Object> set = new HashSet<>();
        set.add(EzyCommand.PING);
        set.add(EzyCommand.PONG);
        return set;
    }

    protected EzySocketClient newSocketClient() {
        EzyNettySocketClient client = newNettySocketClient();
        client.setPingSchedule(pingSchedule);
        client.setPingManager(pingManager);
        client.setHandlerManager(handlerManager);
        client.setEventLoopGroup(eventLoopGroup);
        client.setNettyEventLoopGroup(nettyEventLoopGroup);
        client.setReconnectConfig(config.getReconnect());
        client.setIgnoredLogCommands(ignoredLogCommands);
        return client;
    }

    protected abstract EzyNettySocketClient newNettySocketClient();

    @Override
    public EzySetup setup() {
        return settingUp;
    }

    @Override
    public void connect(String url) {
        URI uri = URI.create(url);
        connect(uri.getHost(), uri.getPort());
    }

    @Override
    public void connect(String host, int port) {
        connectTo(host, port);
    }

    protected void connectTo(Object... args) {
        try {
            if (!isClientConnectable(status)) {
                logger.info("client has already connected to: {}", Arrays.toString(args));
                return;
            }
            preConnect();
            socketClient.connectTo(args);
            setStatus(EzyConnectionStatus.CONNECTING);
        } catch (Throwable e) {
            logger.error("connect to server error", e);
        }
    }

    @Override
    public boolean reconnect() {
        if (!isClientReconnectable(status)) {
            String host = socketClient.getHost();
            int port = socketClient.getPort();
            logger.info("client has already connected to: " + host + ":" + port);
            return false;
        }
        preConnect();
        boolean success = socketClient.reconnect();
        if (success) {
            setStatus(EzyConnectionStatus.RECONNECTING);
        }
        return success;
    }

    protected void preConnect() {
        this.me = null;
        this.zone = null;
    }

    @Override
    public void disconnect(int reason) {
        socketClient.disconnect(reason);
    }

    @Override
    public void send(EzyRequest request, boolean encrypted) {
        Object cmd = request.getCommand();
        EzyData data = request.serialize();
        send((EzyCommand) cmd, (EzyArray) data, encrypted);
    }

    @Override
    public void send(EzyCommand cmd, EzyArray data, boolean encrypted) {
        EzyArray array = requestSerializer.serialize(cmd, data);
        if (socketClient != null) {
            socketClient.sendMessage(array, encrypted);
            metricsRecorder.increaseSystemRequestCount(cmd);
            printSentData(cmd, data);
        }
    }

    @Override
    public void processEvents() {
        socketClient.processEventMessages();
    }

    @Override
    public boolean isSocketEnableSSL() {
        return config.isSocketEnableSSL();
    }

    @Override
    public EzySslType getSocketSslType() {
        return config.getSocketSslType();
    }

    @Override
    public boolean isSocketEnableEncryption() {
        return config.isSocketEnableEncryption();
    }

    @Override
    public boolean isSocketEnableCertificationSSL() {
        return config.isSocketEnableCertificationSSL();
    }

    @Override
    public boolean isEnableDebug() {
        return config.isEnableDebug();
    }

    @Override
    public void setSessionId(long sessionId) {
        this.sessionId = sessionId;
        this.socketClient.setSessionId(sessionId);
    }

    @Override
    public void setSessionToken(String token) {
        this.sessionToken = token;
        this.socketClient.setSessionToken(sessionToken);
    }

    @Override
    public void setSessionKey(byte[] sessionKey) {
        this.sessionKey = sessionKey;
        this.socketClient.setSessionKey(sessionKey);
    }

    @Override
    public EzyISocketClient getSocket() {
        return socketClient;
    }

    @Override
    public EzyApp getApp() {
        if (zone != null) {
            EzyAppManager appManager = zone.getAppManager();
            return appManager.getApp();
        }
        return null;
    }

    @Override
    public EzyApp getAppById(int appId) {
        if (zone != null) {
            EzyAppManager appManager = zone.getAppManager();
            return appManager.getAppById(appId);
        }
        return null;
    }

    protected void printSentData(EzyCommand cmd, EzyArray data) {
        if (!ignoredLogCommands.contains(cmd)) {
            logger.debug("send command: " + cmd + " and data: " + data);
        }
    }

    @Override
    public void udpConnect(int port) {
        throw new UnsupportedOperationException("only support TCP, use EzyUTClient instead");
    }

    @Override
    public void udpConnect(String host, int port) {
        throw new UnsupportedOperationException("only support TCP, use EzyUTClient instead");
    }

    @Override
    public void udpSend(EzyRequest request, boolean encrypted) {
        throw new UnsupportedOperationException("only support TCP, use EzyUTClient instead");
    }

    @Override
    public void udpSend(EzyCommand cmd, EzyArray data, boolean encrypted) {
        throw new UnsupportedOperationException("only support TCP, use EzyUTClient instead");
    }

    @Override
    public void close() {
        socketClient.close();
    }
}
