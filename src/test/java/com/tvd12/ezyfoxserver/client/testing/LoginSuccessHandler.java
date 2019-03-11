package com.tvd12.ezyfoxserver.client.testing;

import com.tvd12.ezyfox.entity.EzyData;
import com.tvd12.ezyfoxserver.client.handler.EzyLoginSuccessHandler;
import com.tvd12.ezyfoxserver.client.request.EzyAccessAppRequest;

public class LoginSuccessHandler extends EzyLoginSuccessHandler {

	@Override
	protected void handleLoginSuccess(EzyData responseData) {
		client.send(new EzyAccessAppRequest("hello-world"));
	}
	
}
