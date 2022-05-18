package com.tvd12.ezyfoxserver.client.socket;

import com.tvd12.ezyfox.util.EzyLoggable;
import com.tvd12.ezyfoxserver.client.EzyClient;
import com.tvd12.ezyfoxserver.client.concurrent.EzyEventLoopEvent;
import com.tvd12.ezyfoxserver.client.concurrent.EzyEventLoopGroup;
import com.tvd12.ezyfoxserver.client.constant.EzyDisconnectReason;
import com.tvd12.ezyfoxserver.client.event.EzyLostPingEvent;
import com.tvd12.ezyfoxserver.client.manager.EzyPingManager;
import com.tvd12.ezyfoxserver.client.request.EzyPingRequest;
import com.tvd12.ezyfoxserver.client.request.EzyRequest;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static com.tvd12.ezyfox.concurrent.EzyExecutors.newSingleThreadScheduledExecutor;

public class EzyPingSchedule
    extends EzyLoggable
    implements EzyEventLoopEvent {

    protected final EzyClient client;
    protected final EzyRequest request;
    protected final EzyPingManager pingManager;
    protected final EzyEventLoopGroup eventLoopGroup;
    protected final ScheduledExecutorService scheduledExecutor;
    protected ScheduledFuture<?> scheduledFuture;
    protected EzySocketEventQueue socketEventQueue;

    public EzyPingSchedule(
        EzyClient client,
        EzyEventLoopGroup eventLoopGroup
    ) {
        this.client = client;
        this.request = new EzyPingRequest();
        this.eventLoopGroup = eventLoopGroup;
        this.pingManager = client.getPingManager();
        this.scheduledExecutor = eventLoopGroup != null ? null : newScheduledExecutor();
    }

    protected ScheduledExecutorService newScheduledExecutor() {
        final ScheduledExecutorService answer = newSingleThreadScheduledExecutor("ping-schedule");
        Runtime.getRuntime().addShutdownHook(new Thread(answer::shutdown));
        return answer;
    }

    @Override
    public boolean fire() {
        sendPingRequest();
        return true;
    }

    public void start() {
        long periodMillis = pingManager.getPingPeriod();
        if (eventLoopGroup != null) {
            eventLoopGroup.addScheduleEvent(
                this,
                periodMillis,
                periodMillis
            );
        } else {
            synchronized (this) {
                scheduledFuture = scheduledExecutor.scheduleAtFixedRate(
                    this::sendPingRequest,
                    periodMillis,
                    periodMillis,
                    TimeUnit.MILLISECONDS
                );
            }
        }
    }

    public void stop() {
        if (eventLoopGroup != null) {
            eventLoopGroup.removeEvent(this);
        } else {
            synchronized (this) {
                if (scheduledFuture != null) {
                    this.scheduledFuture.cancel(true);
                }
                this.scheduledFuture = null;
            }
        }
    }

    public void shutdown() {
        this.scheduledExecutor.shutdown();
    }

    private void sendPingRequest() {
        int lostPingCount = pingManager.increaseLostPingCount();
        int maxLostPingCount = pingManager.getMaxLostPingCount();
        if (lostPingCount >= maxLostPingCount) {
            client.getSocket().disconnect(EzyDisconnectReason.SERVER_NOT_RESPONDING.getId());
        } else {
            EzyRequest request = new EzyPingRequest();
            client.send(request);
        }
        if (lostPingCount > 1) {
            logger.info("lost ping count: " + lostPingCount);
            EzyLostPingEvent event = new EzyLostPingEvent(lostPingCount);
            socketEventQueue.addEvent(event);
        }
    }

    public void setSocketEventQueue(EzySocketEventQueue socketEventQueue) {
        this.socketEventQueue = socketEventQueue;
    }
}
