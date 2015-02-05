package com.ocdsoft.bacta.soe.connection;

import com.ocdsoft.bacta.soe.io.udp.SoeTransceiver;

/**
 * Created by kburkhardt on 1/26/15.
 */
public interface ConnectionServerAgent extends Runnable {
    void update();
    void setTransceiver(final SoeTransceiver transceiver);
}
