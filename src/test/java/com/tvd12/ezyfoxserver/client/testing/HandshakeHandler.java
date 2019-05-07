package com.tvd12.ezyfoxserver.client.testing;

import com.tvd12.ezyfoxserver.client.handler.EzyHandshakeHandler;
import com.tvd12.ezyfoxserver.client.request.EzyLoginRequest;
import com.tvd12.ezyfoxserver.client.request.EzyRequest;

public class HandshakeHandler extends EzyHandshakeHandler {

	@Override
	protected EzyRequest getLoginRequest() {
		return new EzyLoginRequest("example1", "DungTV", "123456");
	}

}
