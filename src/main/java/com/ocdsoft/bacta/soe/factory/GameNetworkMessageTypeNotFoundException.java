package com.ocdsoft.bacta.soe.factory;

/**
 * Created by kyle on 4/21/2016.
 */
public class GameNetworkMessageTypeNotFoundException extends RuntimeException {

    public GameNetworkMessageTypeNotFoundException(int gameMessageType) {
        super("Unable to create message with key: 0x" + Integer.toHexString(gameMessageType));
    }
}
