package com.tvd12.ezyfoxserver.client.config;

import com.tvd12.ezyfox.builder.EzyBuilder;

public class EzyClientConfig {

    private final String zoneName;
    private final String clientName;
    private final EzyPingConfig ping;
    private final EzyReconnectConfig reconnect;

    protected EzyClientConfig(Builder builder) {
        this.zoneName = builder.zoneName;
        this.clientName = builder.clientName;
        this.ping = builder.pingConfigBuilder.build();
        this.reconnect = builder.reconnectConfigBuilder.build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getZoneName() {
        return zoneName;
    }

    public String getClientName() {
        if (clientName == null) {
            return zoneName;
        }
        return clientName;
    }

    public EzyPingConfig getPing() {
        return ping;
    }

    public EzyReconnectConfig getReconnect() {
        return reconnect;
    }

    public static class Builder implements EzyBuilder<EzyClientConfig> {

        private final EzyPingConfig.Builder pingConfigBuilder;
        private final EzyReconnectConfig.Builder reconnectConfigBuilder;
        private String zoneName;
        private String clientName;

        public Builder() {
            this.pingConfigBuilder = new EzyPingConfig.Builder(this);
            this.reconnectConfigBuilder = new EzyReconnectConfig.Builder(this);
        }

        public Builder zoneName(String zoneName) {
            this.zoneName = zoneName;
            return this;
        }

        public Builder clientName(String clientName) {
            this.clientName = clientName;
            return this;
        }

        public EzyPingConfig.Builder pingConfigBuilder() {
            return pingConfigBuilder;
        }

        public EzyReconnectConfig.Builder reconnectConfigBuilder() {
            return reconnectConfigBuilder;
        }

        @Override
        public EzyClientConfig build() {
            return new EzyClientConfig(this);
        }
    }
}
