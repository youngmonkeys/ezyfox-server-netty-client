package com.tvd12.ezyfoxserver.client.socket;

import java.util.concurrent.TimeoutException;

public class EzyConnectionFuture {

    protected volatile Boolean success;

    public boolean isSuccess() throws Exception {
        synchronized (this) {
            //noinspection LoopStatementThatDoesntLoop
            while (success == null) {
                wait(5000);
                break;
            }
            if (success != null) {
                return success;
            }
            throw new TimeoutException("waiting for connection timeout");
        }
    }

    public void setSuccess(boolean value) {
        synchronized (this) {
            success = value;
            notify();
        }
    }
}
