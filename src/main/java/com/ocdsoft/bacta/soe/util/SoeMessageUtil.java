package com.ocdsoft.bacta.soe.util;

import com.ocdsoft.bacta.engine.utils.BufferUtil;
import org.joda.time.DateTimeZone;

import java.nio.ByteBuffer;

public class SoeMessageUtil {

    public static String bytesToHex(ByteBuffer buffer) {
        return BufferUtil.bytesToHex(buffer);
    }

    public static String makeMessageStruct(ByteBuffer buffer) {
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

    public static int getTimeZoneValue() {
        return DateTimeZone.getDefault().getOffset(null) / 1000;
    }

    public static String getTimeZoneOffsetFromValue(int value) {
        DateTimeZone zone = DateTimeZone.forOffsetMillis(value * 1000);
        return zone.toString();
    }
}
