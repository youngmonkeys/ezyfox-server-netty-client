package com.tvd12.ezyfoxserver.client.codec;

import com.tvd12.ezyfox.codec.EzyByteToObjectDecoder;
import com.tvd12.ezyfox.codec.EzyObjectToByteEncoder;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelOutboundHandler;

public interface EzyNettyCodecCreator {

    EzyObjectToByteEncoder newSocketEncoder();

    EzyByteToObjectDecoder newSocketDecoder(int maxRequestSize);

    ChannelOutboundHandler newNettyEncoder();

    ChannelInboundHandlerAdapter newNettyDecoder(int maxRequestSize);
}
