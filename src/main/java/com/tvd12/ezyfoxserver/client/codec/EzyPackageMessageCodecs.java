package com.tvd12.ezyfoxserver.client.codec;

import com.tvd12.ezyfox.codec.EzyByteToObjectDecoder;
import com.tvd12.ezyfox.codec.EzyMessage;
import com.tvd12.ezyfox.codec.EzyMessageHeader;
import com.tvd12.ezyfox.codec.EzyObjectToByteEncoder;
import com.tvd12.ezyfoxserver.client.socket.EzyPackage;

public final class EzyPackageMessageCodecs {

    private EzyPackageMessageCodecs() {}

    public static byte[] encodePackageToBytes(
        EzyObjectToByteEncoder objectToByteEncoder,
        EzyPackage packet
    ) throws Exception {
        Object data = packet.getData();
        return packet.isEncrypted()
            ? objectToByteEncoder.encryptMessageContent(
                objectToByteEncoder.toMessageContent(data),
                packet.getEncryptionKey()
            )
            : objectToByteEncoder.encode(data);
    }

    public static Object decodeMessageToObject(
        EzyByteToObjectDecoder byteToObjectDecoder,
        EzyMessage message,
        byte[] decryptionKey
    ) throws Exception {
        EzyMessageHeader messageHeader = message.getHeader();
        return messageHeader.isEncrypted()
            ? byteToObjectDecoder.decode(
                message,
                decryptionKey
            )
            : byteToObjectDecoder.decode(message);
    }
}
