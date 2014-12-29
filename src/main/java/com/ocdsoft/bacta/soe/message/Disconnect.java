package com.ocdsoft.bacta.soe.message;

import java.util.HashMap;
import java.util.Map;

public final class Disconnect extends SoeMessage {

	public static Map<Byte, String> reasons = new HashMap<Byte, String>();
	
	public static byte NONE = 0;
	public static byte ICMPERROR = 1;
	public static byte TIMEOUT = 2;
	public static byte OTHERSIDETERMINATED = 3;
	public static byte MANAGERDELETED = 4;
	public static byte CONNECTFAIL = 5;
	public static byte APPLICATION = 6;
	public static byte UNREACHABLE = 7;
	public static byte UNACKTIMEOUT = 8;
	public static byte NEWATTEMPT = 9;
	public static byte REFUSED = 10;
	public static byte MUTUALERROR = 11;
	public static byte SELFCONNECT = 12;
	public static byte RELIABLEOVERFLOW = 13;
	
	static {
		reasons.put(NONE, "None");
		reasons.put(ICMPERROR, "ICMP Error");
		reasons.put(TIMEOUT, "Timeout");
		reasons.put(OTHERSIDETERMINATED, "Other side terminated");
		reasons.put(MANAGERDELETED, "Manager deleted");
		reasons.put(CONNECTFAIL, "Connect fail");
		reasons.put(APPLICATION, "Application");
		reasons.put(UNREACHABLE, "Unreachable connection");
		reasons.put(UNACKTIMEOUT, "Unacknowledged timeout");
		reasons.put(NEWATTEMPT, "New connection attempt");
		reasons.put(REFUSED, "Connection refused");
		reasons.put(MUTUALERROR, "Mutual connect error");
		reasons.put(SELFCONNECT, "Connecting to self");
		reasons.put(RELIABLEOVERFLOW, "Reliable Overflow");
	}
	
	public Disconnect(int connectionId, int i) {
		super(0x5);
				
		writeInt(connectionId);
		writeShort(i);
	}
}
