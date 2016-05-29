package com.ocdsoft.bacta.soe.io.udp;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.ocdsoft.bacta.engine.conf.BactaConfiguration;
import com.ocdsoft.bacta.soe.connection.EncryptMethod;
import lombok.Getter;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collection;

/**
 * Created by kburkhardt on 2/7/15.
 */
public interface NetworkConfiguration {
    int getMaxMultiPayload();
    int getMaxReliablePayload();
    InetAddress getBindAddress();
    InetAddress getPublicAddress();
    int getUdpPort();
    Collection<String> getTrustedClients();
    int getProtocolVersion();
    int getMaxRawPacketSize();
    byte getCrcBytes();
    EncryptMethod getEncryptMethod();
    boolean isCompression();
    int getNetworkThreadSleepTimeMs();
    boolean isReportUdpDisconnects();
    int getResendDelayAdjust();
    int getResendDelayPercent();
    int getNoDataTimeout();
    int getMaxInstandingPackets();
    int getMaxOutstandingPackets();
    boolean isMultiSoeMessages();
    boolean isMultiGameMessages();
    int getConnectionsPerAccount();
    boolean isDisableInstrumentation();
    String getBasePackage();
    String getRequiredClientVersion();
}
