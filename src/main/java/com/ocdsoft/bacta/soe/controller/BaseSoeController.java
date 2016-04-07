package com.ocdsoft.bacta.soe.controller;

import com.ocdsoft.bacta.soe.dispatch.SoeMessageDispatcher;
import com.ocdsoft.bacta.soe.dispatch.SwgMessageDispatcher;

/**
 * Created by kburkhardt on 1/26/15.
 */
public abstract class BaseSoeController implements SoeMessageController {

    protected SoeMessageDispatcher soeMessageDispatcher;
    protected SwgMessageDispatcher swgMessageDispatcher;

    public void setSoeMessageDispatcher(final SoeMessageDispatcher soeMessageDispatcher) {
        this.soeMessageDispatcher = soeMessageDispatcher;
    }

    public void setSwgMessageDispatcher(final SwgMessageDispatcher swgMessageDispatcher) {
        this.swgMessageDispatcher = swgMessageDispatcher;
    }
}
