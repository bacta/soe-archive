package com.ocdsoft.bacta.soe.controller;

import com.ocdsoft.bacta.engine.network.controller.Controller;
import com.ocdsoft.bacta.soe.connection.SoeUdpConnection;

public interface CommandController<T> extends Controller {
	public void handleCommand(SoeUdpConnection connection, T invoker, T target, String params);
}
