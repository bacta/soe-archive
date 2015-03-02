package com.ocdsoft.bacta.soe.io.udp;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.ocdsoft.bacta.engine.conf.BactaConfiguration;
import com.ocdsoft.bacta.soe.connection.EncryptMethod;
import lombok.Getter;

/**
 * Created by kburkhardt on 2/7/15.
 */
@Singleton
public final class NetworkConfiguration {
    
    @Getter private final int protocolVersion;
    @Getter private final int maxRawPacketSize;
    @Getter private final byte crcBytes;
    @Getter private final EncryptMethod encryptMethod;

    @Getter private final boolean compression;
    @Getter private final int networkThreadSleepTimeMs;

    @Getter private final boolean reportUdpDisconnects;

    @Getter private final int resendDelayAdjust;
    @Getter private final int resendDelayPercent;

    @Getter private final int noDataTimeout;
    @Getter private final int maxInstandingPackets;
    @Getter private final int maxOutstandingPackets;

    @Getter private final boolean multiSoeMessages; 
    @Getter private final boolean multiGameMessages;

    @Getter private final boolean disableInstrumentation;
    
//    logAllNetworkTraffic = false
//    incomingBufferSize = 4194304
//    outgoingBufferSize = 4194304
//    maxConnections = 1000
//
//    maxOutstandingBytes = 204800
//    fragmentSize = 496
//    pooledPacketMax = 1024
//    packettHistoryMax = 100
//    oldestUnacknowledgedTimeout = 90000
//    reportStatisticsInterval = 60000
//
//    pooledPAcketInitial = 1024
//
//
//    reliableOverflowBytes = 2097152
//    logConnectionConstructionDestruction = false
//    logConnectionOpenedClosed = false
    
    @Inject
    public NetworkConfiguration(final BactaConfiguration configuration) {
        protocolVersion = configuration.getIntWithDefault("SharedNetwork", "protocolVersion", 2);
        maxRawPacketSize = configuration.getIntWithDefault("SharedNetwork", "maxRawPacketSize", 496);
        crcBytes = configuration.getByteWithDefault("SharedNetwork", "crcBytes", (byte) 2);
        compression = configuration.getBooleanWithDefault("SharedNetwork", "compression", true);
        networkThreadSleepTimeMs = configuration.getIntWithDefault("SharedNetwork", "networkThreadSleepTimeMs", 20);
        reportUdpDisconnects = configuration.getBooleanWithDefault("SharedNetwork", "reportUdpDisconnects", false);
        String method = configuration.getStringWithDefault("SharedNetwork", "encryptMethod", "XOR");
        encryptMethod = EncryptMethod.valueOf(method != null ? method : "NONE");
        resendDelayAdjust = configuration.getIntWithDefault("SharedNetwork", "resendDelayAdjust", 500);
        resendDelayPercent = configuration.getIntWithDefault("SharedNetwork", "resendDelayPercent", 125);
        noDataTimeout = configuration.getIntWithDefault("SharedNetwork", "noDataTimeout", 46000);
        maxInstandingPackets = configuration.getIntWithDefault("SharedNetwork", "maxInstandingPackets", 400);
        maxOutstandingPackets = configuration.getIntWithDefault("SharedNetwork", "maxOutstandingPackets", 400);
        multiSoeMessages = configuration.getBooleanWithDefault("SharedNetwork", "multiSoeMessages", true);
        multiGameMessages = configuration.getBooleanWithDefault("SharedNetwork", "multiGameMessages", true);
        disableInstrumentation = configuration.getBooleanWithDefault("SharedNetwork", "disableInstrumentation", false);

    }

    public int getMaxMultiPayload() {
        return maxRawPacketSize - crcBytes - 5;
    }

    public int getMaxReliablePayload() {
        return maxRawPacketSize - crcBytes - 5;
    }

}
