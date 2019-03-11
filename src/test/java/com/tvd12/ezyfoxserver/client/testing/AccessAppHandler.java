package com.tvd12.ezyfoxserver.client.testing;

import com.tvd12.ezyfox.entity.EzyArray;
import com.tvd12.ezyfox.factory.EzyEntityFactory;
import com.tvd12.ezyfoxserver.client.entity.EzyApp;
import com.tvd12.ezyfoxserver.client.handler.EzyAccessAppHandler;

public class AccessAppHandler extends EzyAccessAppHandler {

	@Override
	protected void postHandle(EzyApp app, EzyArray data) {
		app.send("greet", EzyEntityFactory.newObjectBuilder()
				.append("who", client.getMe().getName())
				.build());
	}
	
}
