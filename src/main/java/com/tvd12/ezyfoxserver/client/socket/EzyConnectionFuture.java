package com.tvd12.ezyfoxserver.client.socket;

import java.util.concurrent.TimeoutException;

public class EzyConnectionFuture {

	protected volatile Boolean success;
	
	public void setSuccess(boolean value) {
		synchronized (this) {
			success = value;
			notify();
		}
	}
	
	public boolean isSuccess() throws Exception {
		synchronized (this) {
			while(success == null) {
				wait(5000);
				break;
			}
			if(success != null)
				return success;
			throw new TimeoutException("wating for connection timeout");
		}
	}
	
}
