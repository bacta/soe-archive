package com.ocdsoft.bacta.soe.io.udp.game;

import com.ocdsoft.bacta.engine.network.client.ConnectionState;
import com.ocdsoft.bacta.engine.object.NetworkObject;
import com.ocdsoft.bacta.soe.connection.SoeUdpConnection;
import lombok.Getter;
import lombok.Setter;

public class GameConnection extends SoeUdpConnection {

//    @Getter
//    @Setter
//    private ChatServerAgent chatServerAgent;

    @Setter
    @Getter
    private NetworkObject character;

    @Override
    public void setState(ConnectionState state) {
        super.setState(state);

//        if(state == ConnectionState.DISCONNECTED) {
//            if (character != null) {
//                Zone zone = character.getZone();
//
//                if (zone != null) {
//                    zone.remove(character);
//                }
//            }
//        }
    }

}
