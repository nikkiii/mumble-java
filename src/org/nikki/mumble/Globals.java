package org.nikki.mumble;


public class Globals {
	public static final String LOG_TAG = "mumbleclient";
	public static final int PROTOCOL_VERSION = (1 << 16) | (2 << 8) |
											   (3 & 0xFF);
	public static final int CELT_VERSION = 0x8000000b;
	
	public static final int SAMPLE_RATE = 48000;
	
	public static final int UDPVoiceCELTAlpha = 0;
	public static final int UDPPing = 1;
	public static final int UDPVoiceSpeex = 2;
	public static final int UDPVoiceCELTBeta = 3;
	public static final int UDPVoiceOpus = 4;
}