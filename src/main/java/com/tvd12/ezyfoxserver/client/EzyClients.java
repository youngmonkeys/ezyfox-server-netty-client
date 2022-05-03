package com.tvd12.ezyfoxserver.client;

import com.tvd12.ezyfoxserver.client.config.EzyClientConfig;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class EzyClients {

    private final Map<String, EzyClient> clients;
    private String defaultClientName;

    private static final EzyClients INSTANCE = new EzyClients();

    private EzyClients() {
        this.clients = new HashMap<>();
    }

    public static EzyClients getInstance() {
        return INSTANCE;
    }

    public EzyClient newClient(EzyClientConfig config) {
        synchronized (clients) {
            return doNewClient(config);
        }
    }

    private EzyClient doNewClient(EzyClientConfig config) {
        String clientName = config.getClientName();
        EzyClient client = clients.get(clientName);
        if (client == null) {
            client = new EzyTcpClient(config);
            doAddClient(client);
            if (defaultClientName == null) {
                defaultClientName = client.getName();
            }
        }
        return client;
    }

    public EzyClient newDefaultClient(EzyClientConfig config) {
        synchronized (clients) {
            EzyClient client = doNewClient(config);
            defaultClientName = client.getName();
            return client;
        }
    }

    public void addClient(EzyClient client) {
        synchronized (clients) {
            doAddClient(client);
        }
    }

    private void doAddClient(EzyClient client) {
        this.clients.put(client.getName(), client);
        if (defaultClientName == null) {
            defaultClientName = client.getName();
        }
    }

    public EzyClient getClient(String name) {
        synchronized (clients) {
            return getClient0(name);
        }
    }

    private EzyClient getClient0(String name) {
        if (name == null) {
            throw new NullPointerException("can not get client with name: null");
        }
        return clients.get(name);
    }

    public EzyClient getDefaultClient() {
        synchronized (clients) {
            if (defaultClientName == null) {
                return null;
            }
            return getClient0(defaultClientName);
        }
    }

    public void getClients(List<EzyClient> cachedClients) {
        cachedClients.clear();
        synchronized (clients) {
            cachedClients.addAll(clients.values());
        }
    }

    public void removeClient(String name) {
        synchronized (clients) {
            clients.remove(name);
        }
    }

    public void clear() {
        synchronized (clients) {
            this.clients.clear();
        }
    }
}
