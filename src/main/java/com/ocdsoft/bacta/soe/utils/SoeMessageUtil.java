package com.ocdsoft.bacta.soe.utils;

import com.ocdsoft.bacta.engine.utils.BufferUtil;
import io.netty.buffer.ByteBuf;

public class SoeMessageUtil {

    public static String bytesToHex(ByteBuf buffer) {
        return BufferUtil.bytesToHex(buffer);
    }

    public static String makeMessageStruct(ByteBuf buffer) {
        StringBuilder builder = new StringBuilder();

        String bytes = SoeMessageUtil.bytesToHex(buffer);
        int length = (16 * 3) - 1;
        while (bytes.length() > (16 * 3) - 1) {
            builder.append("    " + bytes.substring(0, length) + "\n");
            bytes = bytes.substring(length + 1);
        }

        builder.append("    " + bytes + "\n");

        return builder.toString();
    }

}
