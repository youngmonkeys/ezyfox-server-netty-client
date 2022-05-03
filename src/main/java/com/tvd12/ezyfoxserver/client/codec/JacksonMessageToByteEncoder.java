package com.tvd12.ezyfoxserver.client.codec;

import com.tvd12.ezyfox.codec.EzyMessageByTypeSerializer;
import com.tvd12.ezyfox.entity.EzyArray;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

import java.util.List;

public class JacksonMessageToByteEncoder extends MessageToMessageEncoder<EzyArray> {

    protected final EzyMessageByTypeSerializer serializer;

    public JacksonMessageToByteEncoder(EzyMessageByTypeSerializer serializer) {
        this.serializer = serializer;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, EzyArray msg, List<Object> out) {
        String text = serializer.serialize(msg, String.class);
        TextWebSocketFrame frame = new TextWebSocketFrame(text);
        out.add(frame);
    }
}
