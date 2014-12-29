package com.ocdsoft.bacta.soe;

import lombok.Getter;

/**
 * Created by Kyle on 2/18/14.
 */
public enum ServerType {

    LOGIN ("login"),
    GAME ("game"),
    PING ("ping");

    @Getter
    private String group;

    ServerType(String group) {
        this.group = group;
    }
}
