package com.tvd12.ezyfoxserver.client.entity;

import com.tvd12.ezyfoxserver.client.EzyClient;
import com.tvd12.ezyfoxserver.client.manager.EzyAppManager;

public interface EzyZone {

    int getId();

    String getName();

    EzyClient getClient();

    EzyAppManager getAppManager();

    EzyApp getApp();
}
