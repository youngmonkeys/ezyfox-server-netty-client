package com.tvd12.ezyfoxserver.client.codec;

import com.tvd12.ezyfox.codec.EzyMessageDeserializer;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

import java.util.List;

public class JacksonByteToMessageDecoder extends MessageToMessageDecoder<TextWebSocketFrame> {

    private final EzyMessageDeserializer deserializer;

    public JacksonByteToMessageDecoder(EzyMessageDeserializer deserializer) {
        this.deserializer = deserializer;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, TextWebSocketFrame in, List<Object> out) {
        String text = in.text();
        Object value = deserializer.deserialize(text);
        out.add(value);
    }
}
