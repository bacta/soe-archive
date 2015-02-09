package com.ocdsoft.bacta.soe.connection;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by kburkhardt on 2/7/15.
 enum Configuration 
 {
     int encryptCode;
     int crcBytes;
     UdpManager::EncryptMethod encryptMethod[2];
     int maxRawPacketSize;
 }; 
 */


public class Configuration {

    @Getter
    @Setter
    private int encryptCode;
    
    @Getter
    @Setter
    private int crcBytes;
    
    @Getter
    @Setter
    private EncryptMethod encryptMethod;
    
    @Getter
    @Setter
    private int maxRawPacketSize;

    @Getter
    @Setter
    private boolean compression;
    
    public Configuration(final int crcBytes,
                         final EncryptMethod encryptMethod,
                         final int maxRawPacketSize,
                         final boolean compression) {
        
        this.crcBytes = crcBytes;
        this.encryptMethod = encryptMethod;
        this.maxRawPacketSize = maxRawPacketSize;
        this.compression = compression;
    }
}
