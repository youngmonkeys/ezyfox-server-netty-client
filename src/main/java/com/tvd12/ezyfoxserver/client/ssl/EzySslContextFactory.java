package com.tvd12.ezyfoxserver.client.ssl;

import com.tvd12.ezyfox.util.EzyLoggable;
import lombok.Setter;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import java.security.SecureRandom;

@Setter
public class EzySslContextFactory extends EzyLoggable {

    private static final String PROTOCOL = "TLS";

    private static final EzySslContextFactory INSTANCE =
        new EzySslContextFactory();

    private EzySslContextFactory() {}

    public static EzySslContextFactory getInstance() {
        return INSTANCE;
    }

    public SSLContext newSslContext() {
        try {
            TrustManager[] trustManagers = EzySslTrustManagerFactory
                .getInstance()
                .engineGetTrustManagers();
            SSLContext context = SSLContext.getInstance(getProtocol());
            context.init(null, trustManagers, new SecureRandom());
            return context;
        } catch (Exception e) {
            throw new IllegalStateException("create ssl context error", e);
        }
    }

    protected String getProtocol() {
        return PROTOCOL;
    }
}
