package com.tvd12.ezyfoxserver.client.codec;

import com.tvd12.ezyfox.codec.EzyByteToObjectDecoder;
import com.tvd12.ezyfox.codec.EzyDecodeState;
import com.tvd12.ezyfox.codec.EzyIDecodeState;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.AllArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static com.tvd12.ezyfox.codec.EzyDecodeState.*;
import static com.tvd12.ezyfoxserver.client.codec.EzyPackageMessageCodecs.decodeMessageToObject;

public class NettyMsgPackByteToMessageDecoder extends ByteToMessageDecoder {

    protected final Handlers handlers;
    protected final EzyByteToObjectDecoder byteToObjectDecoder;
    protected final Supplier<byte[]> decryptionKeySupplier;

    public NettyMsgPackByteToMessageDecoder(
        EzyByteToObjectDecoder byteToObjectDecoder,
        int maxSize,
        Supplier<byte[]> decryptionKeySupplier
    ) {
        this.byteToObjectDecoder = byteToObjectDecoder;
        this.decryptionKeySupplier = decryptionKeySupplier;
        this.handlers = Handlers.builder()
            .maxSize(maxSize)
            .byteToObjectDecoder(byteToObjectDecoder)
            .build();
    }

    @Override
    protected void decode(
        ChannelHandlerContext ctx,
        ByteBuf in,
        List<Object> out
    ) throws Exception {
        byte[] decryptionKey = decryptionKeySupplier.get();
        handlers.handle(in, decryptionKey, out);
    }

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

        protected EzyByteToObjectDecoder byteToObjectDecoder;

        @Override
        public EzyIDecodeState nextState() {
            return PREPARE_MESSAGE;
        }

        @Override
        public boolean handle(
            ByteBuf in,
            byte[] decryptionKey,
            List<Object> out
        ) throws Exception {
            if (!messageReader.readContent(in)) {
                return false;
            }
            Object data = decodeMessageToObject(
                byteToObjectDecoder,
                messageReader.get(),
                decryptionKey
            );
            out.add(data);
            return true;
        }
    }

    public static class Handlers extends EzyDecodeHandlers {

        protected final EzyByteToObjectDecoder byteToObjectDecoder;

        protected Handlers(Builder builder) {
            super(builder);
            this.byteToObjectDecoder = builder.byteToObjectDecoder;
        }

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder extends AbstractBuilder {
            protected int maxSize;
            protected EzyByteToObjectDecoder byteToObjectDecoder;
            protected EzyByteBufMessageReader messageReader = new EzyByteBufMessageReader();

            public Builder maxSize(int maxSize) {
                this.maxSize = maxSize;
                return this;
            }

            public Builder byteToObjectDecoder(EzyByteToObjectDecoder byteToObjectDecoder) {
                this.byteToObjectDecoder = byteToObjectDecoder;
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
                EzyDecodeHandler readMessageContent = new ReadMessageContent(byteToObjectDecoder);
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
