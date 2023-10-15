package com.tvd12.ezyfoxserver.client.codec;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tvd12.ezyfox.codec.*;
import com.tvd12.ezyfox.jackson.JacksonObjectMapperBuilder;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelOutboundHandler;

public class NettyJacksonCodecCreator implements EzyNettyCodecCreator {

    protected final ObjectMapper objectMapper;
    protected final EzyMessageDeserializer deserializer;
    protected final EzyMessageByTypeSerializer serializer;

    public NettyJacksonCodecCreator() {
        this.objectMapper = newObjectMapper();
        this.serializer = newSerializer();
        this.deserializer = newDeserializer();
    }

    protected ObjectMapper newObjectMapper() {
        return JacksonObjectMapperBuilder.newInstance().build();
    }

    protected EzyMessageDeserializer newDeserializer() {
        return new JacksonSimpleDeserializer(objectMapper);
    }

    protected EzyMessageByTypeSerializer newSerializer() {
        return new JacksonSimpleSerializer(objectMapper);
    }

    @Override
    public EzyObjectToByteEncoder newSocketEncoder() {
        throw new UnsupportedOperationException("unsupported");
    }

    @Override
    public EzyByteToObjectDecoder newSocketDecoder(int maxRequestSize) {
        throw new UnsupportedOperationException("unsupported");
    }

    @Override
    public ChannelOutboundHandler newNettyEncoder() {
        return new NettyJacksonMessageToByteEncoder(serializer);
    }

    @Override
    public ChannelInboundHandlerAdapter newNettyDecoder(int maxRequestSize) {
        return new NettyJacksonByteToMessageDecoder(deserializer);
    }
}
