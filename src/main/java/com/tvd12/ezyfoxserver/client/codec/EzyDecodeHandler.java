package com.tvd12.ezyfoxserver.client.codec;

import com.tvd12.ezyfox.codec.EzyIDecodeState;
import com.tvd12.ezyfox.exception.EzyNotImplementedException;
import io.netty.buffer.ByteBuf;

import java.util.List;

public interface EzyDecodeHandler {

    /**
     * Get next state.
     *
     * @return the next state
     */
    EzyIDecodeState nextState();

    /**
     * Get next handler corresponding the next state.
     *
     * @return the next handler
     */
    EzyDecodeHandler nextHandler();

    /**
     * Handler decoding.
     *
     * @param in the input.
     * @param decryptionKey the key to decrypt message content.
     * @param out the output.
     * @return true if decoding is successful or not.
     */
    default boolean handle(
        ByteBuf in,
        byte[] decryptionKey,
        List<Object> out
    ) throws Exception {
        return handle(in, out);
    }

    /**
     * Handler decoding.
     *
     * @param in the input.
     * @param out the output.
     * @return true if decoding is successful or not.
     */
    default boolean handle(ByteBuf in, List<Object> out) {
        throw new EzyNotImplementedException("not implemented");
    }
}
