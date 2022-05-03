package com.tvd12.ezyfoxserver.client.entity;

import lombok.Getter;

@Getter
public class EzySimpleUser implements EzyUser {

    protected final long id;
    protected final String name;

    public EzySimpleUser(long id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public String toString() {
        return "User(" +
            "id: " + id + ", " +
            "name: " + name +
            ")";
    }
}
