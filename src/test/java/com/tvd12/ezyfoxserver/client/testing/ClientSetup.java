package com.tvd12.ezyfoxserver.client.testing;

import com.tvd12.ezyfox.entity.EzyObject;
import com.tvd12.ezyfoxserver.client.EzyClient;
import com.tvd12.ezyfoxserver.client.constant.EzyCommand;
import com.tvd12.ezyfoxserver.client.handler.EzyAppDataHandlers;
import com.tvd12.ezyfoxserver.client.manager.EzyHandlerManager;

public final class ClientSetup {

    private static final ClientSetup INSTANCE = new ClientSetup();

    private ClientSetup() {}

    public static ClientSetup getInstance() {
        return INSTANCE;
    }

    public void setup(EzyClient client) {
        EzyHandlerManager handlerManager = client.getHandlerManager();
        handlerManager.addDataHandler(EzyCommand.HANDSHAKE, new HandshakeHandler());
        handlerManager.addDataHandler(EzyCommand.LOGIN, new LoginSuccessHandler());
        handlerManager.addDataHandler(EzyCommand.APP_ACCESS, new AccessAppHandler());

        EzyAppDataHandlers appDataHandlers = handlerManager.getAppDataHandlers("hello-world");
        appDataHandlers.addHandler("greet", (app, data) -> {
            String message = ((EzyObject) data).get("message", String.class);
            System.out.println("server response me: " + message);
        });
    }
}
