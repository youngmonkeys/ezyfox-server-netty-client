package com.tvd12.ezyfoxserver.client.socket;

/**
 * Created by tavandung12 on 9/30/18.
 */

public interface EzySocketClient extends EzySender {

    void connect(Object... args) throws Exception;

    void connect();

    boolean reconnect();

    void disconnect();

}
