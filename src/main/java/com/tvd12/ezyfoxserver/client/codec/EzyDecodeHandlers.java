package com.tvd12.ezyfoxserver.client.codec;

import com.tvd12.ezyfox.codec.EzyDecodeState;
import com.tvd12.ezyfox.codec.EzyIDecodeState;
import io.netty.buffer.ByteBuf;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class EzyDecodeHandlers {

    protected EzyIDecodeState state;
    protected Map<EzyIDecodeState, EzyDecodeHandler> handers;

    protected EzyDecodeHandlers(AbstractBuilder builder) {
        this.state = firstState();
        this.handers = builder.newHandlers();
    }

    protected void handle(
        ByteBuf in,
        byte[] decryptionKey,
        List<Object> out
    ) throws Exception {
        EzyDecodeHandler handler = handers.get(state);
        while (handler != null && handler.handle(in, decryptionKey, out)) {
            state = handler.nextState();
            handler = handler.nextHandler();
        }
    }

    protected EzyIDecodeState firstState() {
        return EzyDecodeState.PREPARE_MESSAGE;
    }

    public abstract static class AbstractBuilder {
        protected Map<EzyIDecodeState, EzyDecodeHandler> newHandlers() {
            Map<EzyIDecodeState, EzyDecodeHandler> answer = new HashMap<>();
            addHandlers(answer);
            return answer;
        }

        protected abstract void addHandlers(Map<EzyIDecodeState, EzyDecodeHandler> answer);
    }
}
