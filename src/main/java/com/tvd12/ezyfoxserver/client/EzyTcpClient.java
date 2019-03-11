package com.tvd12.ezyfoxserver.client;

import com.tvd12.ezyfox.codec.EzyCodecCreator;
import com.tvd12.ezyfoxserver.client.codec.MsgPackCodecCreator;
import com.tvd12.ezyfoxserver.client.config.EzyClientConfig;
import com.tvd12.ezyfoxserver.client.socket.EzyAbstractSocketClient;
import com.tvd12.ezyfoxserver.client.socket.EzyTcpSocketClient;

/**
 * Created by tavandung12 on 9/20/18.
 */

public class EzyTcpClient extends EzyAbstractClient {

    public EzyTcpClient(EzyClientConfig config) {
        super(config);
    }
    
    public void connect(String host, int port) {
		connect(new Object[] {host, port});
    }

    @Override
    protected EzyCodecCreator newCodecCreator() {
    		return new MsgPackCodecCreator();
    }
    
    @Override
    protected EzyAbstractSocketClient.Builder<?> newSocketClientBuilder() {
    		return EzyTcpSocketClient.builder();
    }
    
}
