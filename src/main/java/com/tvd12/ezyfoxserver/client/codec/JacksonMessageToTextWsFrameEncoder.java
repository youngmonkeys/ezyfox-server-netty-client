package com.tvd12.ezyfoxserver.client.codec;

import com.tvd12.ezyfox.codec.EzyMessageSerializer;
import com.tvd12.ezyfox.entity.EzyArray;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class JacksonMessageToTextWsFrameEncoder extends MessageToMessageEncoder<EzyArray> {

    protected final Logger logger;
    protected final EzyMessageSerializer serializer;

    public JacksonMessageToTextWsFrameEncoder(EzyMessageSerializer serializer) {
        this.serializer = serializer;
        this.logger = LoggerFactory.getLogger(getClass());
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, EzyArray msg, List<Object> out) {
        writeMessage(serializer.serialize(msg), out);
    }

    private void writeMessage(byte[] message, List<Object> out) {
        out.add(new TextWebSocketFrame(Unpooled.wrappedBuffer(message)));
    }

}
