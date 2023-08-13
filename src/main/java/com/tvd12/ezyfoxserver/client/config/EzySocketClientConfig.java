package com.tvd12.ezyfoxserver.client.config;

import com.tvd12.ezyfoxserver.client.constant.EzySslType;

public interface EzySocketClientConfig {

    default boolean isSocketEnableSSL() {
        return false;
    }

    default EzySslType getSocketSslType() {
        return EzySslType.CUSTOMIZATION;
    }

    default boolean isSocketEnableEncryption() {
        return isSocketEnableSSL()
            && getSocketSslType() == EzySslType.CUSTOMIZATION;
    }

    default boolean isSocketEnableCertificationSSL() {
        return isSocketEnableSSL()
            && getSocketSslType() == EzySslType.CERTIFICATION;
    }
}
