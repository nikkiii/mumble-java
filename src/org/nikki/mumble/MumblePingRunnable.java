package org.nikki.mumble;

import MumbleProto.Mumble.Ping;


public class MumblePingRunnable implements Runnable {

	private Mumble mumble;
	
	private final byte[] udpBuffer = new byte[9];

	public MumblePingRunnable(Mumble mumble) {
		this.mumble = mumble;
		
		udpBuffer[0] = MumbleProtocol.UDPMESSAGETYPE_UDPPING << 5;
	}
	
	public void run() {
		while(true) {
			try {
				long timestamp = System.currentTimeMillis();
				
				Ping.Builder ping = Ping.newBuilder();
				ping.setTimestamp(timestamp);
				ping.setGood(mumble.cryptState.getGood());
				ping.setLate(mumble.cryptState.getLate());
				ping.setLost(0); // TODO lost
				ping.setTcpPackets(mumble.getProtocol().getTcpPackets());
				ping.setUdpPackets(mumble.getProtocol().getUdpPackets());
				mumble.write(ping.build());
				
				mumble.getProtocol().updateLastPing();
				
				udpBuffer[1] = (byte) ((timestamp >> 56) & 0xFF);
				udpBuffer[2] = (byte) ((timestamp >> 48) & 0xFF);
				udpBuffer[3] = (byte) ((timestamp >> 40) & 0xFF);
				udpBuffer[4] = (byte) ((timestamp >> 32) & 0xFF);
				udpBuffer[5] = (byte) ((timestamp >> 24) & 0xFF);
				udpBuffer[6] = (byte) ((timestamp >> 16) & 0xFF);
				udpBuffer[7] = (byte) ((timestamp >> 8) & 0xFF);
				udpBuffer[8] = (byte) ((timestamp) & 0xFF);

				mumble.writeUdp(udpBuffer, udpBuffer.length, true);
				
				Thread.sleep(5000);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
