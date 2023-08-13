package com.tvd12.ezyfoxserver.client.socket;

import com.tvd12.ezyfox.entity.EzyArray;
import lombok.Getter;

@Getter
public class EzySimplePackage implements EzyPackage {

    protected final EzyArray data;
    protected final boolean encrypted;
    protected final byte[] encryptionKey;

    public EzySimplePackage(
        EzyArray data,
        boolean encrypted,
        byte[] encryptionKey
    ) {
        this.data = data;
        this.encrypted = encrypted;
        this.encryptionKey = encryptionKey;
    }

    @Override
    public EzyArray getData() {
        return data;
    }

    @Override
    public boolean isEncrypted() {
        return encrypted;
    }

    @Override
    public byte[] getEncryptionKey() {
        return encryptionKey;
    }
}
