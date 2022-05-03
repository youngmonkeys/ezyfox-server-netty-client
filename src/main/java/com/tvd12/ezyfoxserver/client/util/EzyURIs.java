package com.tvd12.ezyfoxserver.client.util;

import java.net.URI;

public final class EzyURIs {

    private EzyURIs() {}

    public static int getWsPort(URI uri) {
        String scheme = uri.getScheme();
        int port = uri.getPort();
        if (port <= 0) {
            port = scheme.equals("ws") ? 80 : 443;
        }
        return port;
    }
}
