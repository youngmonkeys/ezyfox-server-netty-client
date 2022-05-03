package com.tvd12.ezyfoxserver.client.constant;

import com.tvd12.ezyfox.constant.EzyConstant;
import com.tvd12.ezyfox.util.EzyEnums;
import lombok.Getter;

import java.util.Map;

@Getter
public enum EzyUserRemoveReason implements EzyConstant {

    EXIT_APP(300);

    private final int id;

    private static final Map<Integer, EzyUserRemoveReason> MAP =
        EzyEnums.enumMapInt(EzyUserRemoveReason.class);

    EzyUserRemoveReason(int id) {
        this.id = id;
    }

    public static EzyUserRemoveReason valueOf(int id) {
        return MAP.get(id);
    }

    @Override
    public String getName() {
        return toString();
    }
}
