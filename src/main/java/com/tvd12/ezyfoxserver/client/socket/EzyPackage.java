package com.tvd12.ezyfoxserver.client.socket;

import com.tvd12.ezyfox.entity.EzyArray;

public interface EzyPackage {

    EzyArray getData();

    boolean isEncrypted();

    byte[] getEncryptionKey();
}
