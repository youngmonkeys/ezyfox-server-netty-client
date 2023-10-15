package com.tvd12.ezyfoxserver.client.codec;

import com.tvd12.ezyfox.codec.EzyMessageByTypeSerializer;
import com.tvd12.ezyfoxserver.client.socket.EzyPackage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

import java.util.List;

public class NettyJacksonMessageToByteEncoder extends MessageToMessageEncoder<EzyPackage> {

    protected final EzyMessageByTypeSerializer serializer;

    public NettyJacksonMessageToByteEncoder(EzyMessageByTypeSerializer serializer) {
        this.serializer = serializer;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, EzyPackage msg, List<Object> out) {
        String text = serializer.serialize(msg.getData(), String.class);
        TextWebSocketFrame frame = new TextWebSocketFrame(text);
        out.add(frame);
    }
}
