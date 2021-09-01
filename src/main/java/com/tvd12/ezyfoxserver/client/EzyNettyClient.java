package com.tvd12.ezyfoxserver.client;

import static com.tvd12.ezyfoxserver.client.constant.EzyConnectionStatuses.isClientConnectable;
import static com.tvd12.ezyfoxserver.client.constant.EzyConnectionStatuses.isClientReconnectable;

import java.net.URI;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tvd12.ezyfox.entity.EzyArray;
import com.tvd12.ezyfox.entity.EzyData;
import com.tvd12.ezyfox.entity.EzyEntity;
import com.tvd12.ezyfoxserver.client.config.EzyClientConfig;
import com.tvd12.ezyfoxserver.client.constant.EzyCommand;
import com.tvd12.ezyfoxserver.client.constant.EzyConnectionStatus;
import com.tvd12.ezyfoxserver.client.entity.EzyApp;
import com.tvd12.ezyfoxserver.client.entity.EzyMeAware;
import com.tvd12.ezyfoxserver.client.entity.EzyUser;
import com.tvd12.ezyfoxserver.client.entity.EzyZone;
import com.tvd12.ezyfoxserver.client.entity.EzyZoneAware;
import com.tvd12.ezyfoxserver.client.manager.EzyAppManager;
import com.tvd12.ezyfoxserver.client.manager.EzyHandlerManager;
import com.tvd12.ezyfoxserver.client.manager.EzyPingManager;
import com.tvd12.ezyfoxserver.client.manager.EzySimpleHandlerManager;
import com.tvd12.ezyfoxserver.client.manager.EzySimplePingManager;
import com.tvd12.ezyfoxserver.client.request.EzyRequest;
import com.tvd12.ezyfoxserver.client.request.EzyRequestSerializer;
import com.tvd12.ezyfoxserver.client.request.EzySimpleRequestSerializer;
import com.tvd12.ezyfoxserver.client.setup.EzySetup;
import com.tvd12.ezyfoxserver.client.setup.EzySimpleSetup;
import com.tvd12.ezyfoxserver.client.socket.EzyISocketClient;
import com.tvd12.ezyfoxserver.client.socket.EzyNettySocketClient;
import com.tvd12.ezyfoxserver.client.socket.EzyPingSchedule;
import com.tvd12.ezyfoxserver.client.socket.EzySocketClient;

/**
 * Created by tavandung12 on 9/20/18.
 */

public abstract class EzyNettyClient
        extends EzyEntity
        implements EzyClient, EzyMeAware, EzyZoneAware {

	protected EzyUser me;
    protected EzyZone zone;
    protected long sessionId;
    protected String sessionToken;
    protected final String name;
    protected final EzySetup settingUp;
    protected final EzyClientConfig config;
    protected final EzyPingManager pingManager;
    protected final EzyHandlerManager handlerManager;
    protected final EzyRequestSerializer requestSerializer;

    protected EzyConnectionStatus status;
    protected EzyConnectionStatus udpStatus;
    protected final Set<Object> unloggableCommands;

    protected final EzySocketClient socketClient;
    protected final EzyPingSchedule pingSchedule;
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    public EzyNettyClient(EzyClientConfig config) {
    		this.config = config;
        this.name = config.getClientName();
        this.status = EzyConnectionStatus.NULL;
        this.pingManager = new EzySimplePingManager(config.getPing());
        this.pingSchedule = new EzyPingSchedule(this);
        this.handlerManager = new EzySimpleHandlerManager(this);
        this.requestSerializer = new EzySimpleRequestSerializer();
        this.settingUp = new EzySimpleSetup(handlerManager);
        this.unloggableCommands = newUnloggableCommands();
        this.socketClient = newSocketClient();
    }

    private Set<Object> newUnloggableCommands() {
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
        client.setReconnectConfig(config.getReconnect());
        client.setUnloggableCommands(unloggableCommands);
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
            	logger.warn("client has already connected to: {}", Arrays.toString(args));
                return;
            }
            preconnect();
            socketClient.connectTo(args);
            setStatus(EzyConnectionStatus.CONNECTING);
        } catch (Exception e) {
            logger.error("connect to server error", e);
        }
    }
    
    @Override
    public boolean reconnect() {
        if (!isClientReconnectable(status)) {
            String host = socketClient.getHost();
            int port = socketClient.getPort();
            logger.warn("client has already connected to: " + host + ":" + port);
            return false;
        }
        preconnect();
        boolean success = socketClient.reconnect();
        if (success)
            setStatus(EzyConnectionStatus.RECONNECTING);
        return success;
    }
    
    protected void preconnect() {
        this.me = null;
        this.zone = null;
    }

    @Override
    public void disconnect(int reason) {
        socketClient.disconnect(reason);
    }

    @Override
    public void send(EzyRequest request) {
        Object cmd = request.getCommand();
        EzyData data = request.serialize();
        send((EzyCommand) cmd, (EzyArray) data);
    }

    @Override
    public void send(EzyCommand cmd, EzyArray data) {
        EzyArray array = requestSerializer.serialize(cmd, data);
        if (socketClient != null) {
            socketClient.sendMessage(array);
            printSentData(cmd, data);
        }
    }

    @Override
    public void processEvents() {
        socketClient.processEventMessages();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public EzyClientConfig getConfig() {
        return config;
    }

    @Override
    public EzyZone getZone() {
        return zone;
    }

    @Override
    public void setZone(EzyZone zone) {
        this.zone = zone;
    }

    @Override
    public EzyUser getMe() {
        return me;
    }

    @Override
    public void setMe(EzyUser me) {
        this.me = me;
    }

    @Override
    public EzyConnectionStatus getStatus() {
        return status;
    }

    @Override
    public void setStatus(EzyConnectionStatus status) {
        this.status = status;
    }
    
    @Override
    public void setUdpStatus(EzyConnectionStatus status) {
        this.udpStatus = status;
    }
    
    @Override
    public EzyConnectionStatus getUdpStatus() {
    	return udpStatus;
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
    public EzyISocketClient getSocket() {
    	return socketClient;
    }
    
    @Override
	public EzyApp getApp() {
    	if(zone != null) {
    		EzyAppManager appManager = zone.getAppManager();
            EzyApp app = appManager.getApp();
            return app;
    	}
    	return null;
	}

    @Override
    public EzyApp getAppById(int appId) {
        if (zone != null) {
            EzyAppManager appManager = zone.getAppManager();
            EzyApp app = appManager.getAppById(appId);
            return app;
        }
        return null;
    }

    @Override
    public EzyPingManager getPingManager() {
        return pingManager;
    }
    
    @Override
    public EzyPingSchedule getPingSchedule() {
        return pingSchedule;
    }

    @Override
    public EzyHandlerManager getHandlerManager() {
        return handlerManager;
    }

    protected void printSentData(EzyCommand cmd, EzyArray data) {
        if (!unloggableCommands.contains(cmd))
        		logger.debug("send command: " + cmd + " and data: " + data);
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
    public void udpSend(EzyRequest request) {
    	throw new UnsupportedOperationException("only support TCP, use EzyUTClient instead");
    }
    
    @Override
	public void udpSend(EzyCommand cmd, EzyArray data) {
    	throw new UnsupportedOperationException("only support TCP, use EzyUTClient instead");
	}
    
    public void close() {
    	socketClient.close();
    }
}
