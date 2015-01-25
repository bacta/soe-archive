package com.ocdsoft.bacta.soe.message.bacta;


import com.ocdsoft.bacta.soe.io.udp.game.GameServerState;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class GameServerStatusUpdate  {

    private MessageDigest md = MessageDigest.getInstance("MD5");

    public GameServerStatusUpdate(GameServerState serverState) throws NoSuchAlgorithmException {
//        writeShort(BactaController.GAMESERVERUPDATE);
//        writeAscii(new String(md.digest(serverState.getSecret().getBytes())));
//        writeAscii(serverState.getName());
//        writeAscii(serverState.getAddress());
//        writeInt(serverState.getPort());
//        writeInt(serverState.getPingPort());
//        writeInt(serverState.getPopulation());
//        writeInt(serverState.getMaximumPopulation());
//        writeInt(serverState.getMaximumCharacters());
//        writeInt(serverState.getTimezone());
//        writeInt(serverState.getConnectionState().getValue());
//        writeBoolean(serverState.isRecommended());
	}

}
