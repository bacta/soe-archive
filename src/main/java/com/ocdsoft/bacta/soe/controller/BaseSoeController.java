package com.ocdsoft.bacta.soe.controller;

import com.ocdsoft.bacta.soe.router.SoeMessageRouter;
import com.ocdsoft.bacta.soe.router.SwgMessageRouter;

/**
 * Created by kburkhardt on 1/26/15.
 */
public abstract class BaseSoeController implements SoeMessageController{

    protected SoeMessageRouter soeMessageRouter;
    protected SwgMessageRouter swgMessageRouter;

    @Override
    public void setSoeMessageRouter(final SoeMessageRouter soeMessageRouter) {
        this.soeMessageRouter = soeMessageRouter;
    }

    @Override
    public void setSwgMessageRouter(final SwgMessageRouter swgMessageRouter) {
        this.swgMessageRouter = swgMessageRouter;
    }
}
