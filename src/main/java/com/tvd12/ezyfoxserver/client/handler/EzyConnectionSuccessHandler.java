package com.tvd12.ezyfoxserver.client.handler;

import com.tvd12.ezyfoxserver.client.constant.EzyConnectionStatus;
import com.tvd12.ezyfoxserver.client.event.EzyEvent;
import com.tvd12.ezyfoxserver.client.request.EzyHandshakeRequest;
import com.tvd12.ezyfoxserver.client.request.EzyRequest;

import java.util.UUID;

@SuppressWarnings("rawtypes")
public class EzyConnectionSuccessHandler extends EzyAbstractEventHandler {

    @Override
    public final void handle(EzyEvent event) {
        client.setStatus(EzyConnectionStatus.CONNECTED);
        sendHandshakeRequest();
        postHandle();
    }

    protected void postHandle() {}

    protected void sendHandshakeRequest() {
        EzyRequest request = newHandshakeRequest();
        client.send(request);
    }

    protected final EzyRequest newHandshakeRequest() {
        return new EzyHandshakeRequest(
            getClientId(),
            generateClientKey(),
            "NETTY",
            "1.1.4",
            false,
            getStoredToken()
        );
    }

    protected String getClientId() {
        return UUID.randomUUID().toString();
    }

    protected byte[] generateClientKey() {
        return null;
    }

    protected boolean isEnableEncryption() {
        return false;
    }

    protected String getStoredToken() {
        return "";
    }
}
