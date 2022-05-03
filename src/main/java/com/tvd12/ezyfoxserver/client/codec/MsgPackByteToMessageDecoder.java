package com.tvd12.ezyfoxserver.client.codec;

import com.tvd12.ezyfox.codec.*;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.AllArgsConstructor;
import lombok.Setter;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import static com.tvd12.ezyfox.codec.EzyDecodeState.*;

public class MsgPackByteToMessageDecoder
    extends ByteToMessageDecoder
    implements EzyByteToObjectDecoder {

    protected final Handlers handlers;

    public MsgPackByteToMessageDecoder(
        EzyMessageDeserializer deserializer,
        int maxSize
    ) {
        this.handlers = Handlers.builder()
            .maxSize(maxSize)
            .deserializer(deserializer)
            .build();
    }

    @Override
    protected void decode(
        ChannelHandlerContext ctx,
        ByteBuf in,
        List<Object> out
    ) {
        handlers.handle(ctx, in, out);
    }

    @Override
    public Object decode(EzyMessage message) {
        return handlers.decode(message);
    }

    @Override
    public void decode(ByteBuffer bytes, Queue<EzyMessage> queue) {
        throw new UnsupportedOperationException("unsupported");
    }

    @Override
    public void reset() {}


    @Setter
    public abstract static class AbstractHandler implements EzyDecodeHandler {

        protected EzyDecodeHandler nextHandler;
        protected EzyByteBufMessageReader messageReader;

        @Override
        public EzyDecodeHandler nextHandler() {
            return nextHandler;
        }
    }

    public static class PrepareMessage extends AbstractHandler {

        @Override
        public EzyIDecodeState nextState() {
            return READ_MESSAGE_HEADER;
        }

        @Override
        public boolean handle(ByteBuf in, List<Object> out) {
            messageReader.clear();
            return true;
        }
    }

    public static class ReadMessageHeader extends AbstractHandler {

        @Override
        public EzyIDecodeState nextState() {
            return EzyDecodeState.READ_MESSAGE_SIZE;
        }

        @Override
        public boolean handle(ByteBuf in, List<Object> out) {
            return messageReader.readHeader(in);
        }

    }

    public static class ReadMessageSize extends AbstractHandler {

        protected final int maxSize;

        public ReadMessageSize(int maxSize) {
            this.maxSize = maxSize;
        }

        @Override
        public EzyIDecodeState nextState() {
            return READ_MESSAGE_CONTENT;
        }

        @Override
        public boolean handle(ByteBuf in, List<Object> out) {
            return messageReader.readSize(in, maxSize);
        }
    }

    @AllArgsConstructor
    public static class ReadMessageContent extends AbstractHandler {

        protected EzyMessageDeserializer deserializer;

        @Override
        public EzyIDecodeState nextState() {
            return PREPARE_MESSAGE;
        }

        @Override
        public boolean handle(ByteBuf in, List<Object> out) {
            if (!messageReader.readContent(in)) {
                return false;
            }
            processMessage(messageReader.get(), out);
            return true;
        }

        private void processMessage(EzyMessage msg, List<Object> out) {
            Object contentBytes = readMessageContent(msg.getContent());
            out.add(contentBytes);
        }

        private Object readMessageContent(byte[] content) {
            return deserializer.deserialize(content);
        }

    }

    public static class Handlers extends EzyDecodeHandlers {

        protected final EzyMessageDeserializer deserializer;

        protected Handlers(Builder builder) {
            super(builder);
            this.deserializer = builder.deserializer;
        }

        public static Builder builder() {
            return new Builder();
        }

        public Object decode(EzyMessage message) {
            return deserializer.deserialize(message.getContent());
        }

        public static class Builder extends AbstractBuilder {
            protected int maxSize;
            protected EzyMessageDeserializer deserializer;
            protected EzyByteBufMessageReader messageReader = new EzyByteBufMessageReader();

            public Builder maxSize(int maxSize) {
                this.maxSize = maxSize;
                return this;
            }

            public Builder deserializer(EzyMessageDeserializer deserializer) {
                this.deserializer = deserializer;
                return this;
            }

            public Handlers build() {
                return new Handlers(this);
            }

            @Override
            protected void addHandlers(
                Map<EzyIDecodeState, EzyDecodeHandler> answer) {
                EzyDecodeHandler readMessageHeader = new ReadMessageHeader();
                EzyDecodeHandler prepareMessage = new PrepareMessage();
                EzyDecodeHandler readMessageSize = new ReadMessageSize(maxSize);
                EzyDecodeHandler readMessageContent = new ReadMessageContent(deserializer);
                answer.put(PREPARE_MESSAGE, newHandler(prepareMessage, readMessageHeader));
                answer.put(READ_MESSAGE_HEADER, newHandler(readMessageHeader, readMessageSize));
                answer.put(READ_MESSAGE_SIZE, newHandler(readMessageSize, readMessageContent));
                answer.put(READ_MESSAGE_CONTENT, newHandler(readMessageContent));
            }


            private EzyDecodeHandler newHandler(EzyDecodeHandler handler) {
                return newHandler(handler, null);
            }

            private EzyDecodeHandler newHandler(EzyDecodeHandler handler, EzyDecodeHandler next) {
                return newHandler((AbstractHandler) handler, next);
            }

            private EzyDecodeHandler newHandler(AbstractHandler handler, EzyDecodeHandler next) {
                handler.setNextHandler(next);
                handler.setMessageReader(messageReader);
                return handler;
            }
        }
    }
}
