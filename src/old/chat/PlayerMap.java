package com.ocdsoft.bacta.swg.precu.message.chat;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.ocdsoft.bacta.soe.connection.SoeUdpConnection;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public class PlayerMap {
	private Map<String, SoeUdpConnection> players = new ConcurrentHashMap<String, SoeUdpConnection>();
	
	@Inject
	public PlayerMap() {
	}
	
	public void add(String name, SoeUdpConnection client) {
		players.put(name, client);
	}
	
	public SoeUdpConnection remove(String name) {
		return players.remove(name);
	}
	
	public boolean containsKey(String name) {
		return players.containsKey(name);
	}
	
	public SoeUdpConnection get(String name) {
		return players.get(name);
	}
	
	public int size() {
		return players.size();
	}
}
