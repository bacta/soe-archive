package com.ocdsoft.bacta.soe.dispatch;

import com.ocdsoft.bacta.soe.connection.ConnectionRole;
import com.ocdsoft.bacta.soe.controller.GameNetworkMessageController;
import lombok.Getter;

import java.util.List;

/**
 * Created by kyle on 4/22/2016.
 */
@Getter
public class ControllerData<T> {

    private final T controller;
    private final ConnectionRole[] roles;

    public ControllerData(final T controller,
                          final ConnectionRole[] roles) {
        this.controller = controller;
        this.roles = roles;
    }

    public boolean containsRoles(List<ConnectionRole> userRoles) {

        for(ConnectionRole role : roles) {
            if(userRoles.contains(role)) {
                return true;
            }
        }

        return roles.length == 0;
    }
}
