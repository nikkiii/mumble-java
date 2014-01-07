package org.nikki.mumble.model;

public class MumbleServerConfig {

	private int maxBandwidth;
	private String welcomeText;
	private boolean allowHtml;
	private int messageLength;
	private int imageMessageLength;

	public MumbleServerConfig() {
	}

	public void setMaxBandwidth(int maxBandwidth) {
		this.maxBandwidth = maxBandwidth;
	}

	public void setWelcomeText(String welcomeText) {
		this.welcomeText = welcomeText;
	}

	public void setAllowHtml(boolean allowHtml) {
		this.allowHtml = allowHtml;
	}

	public void setMessageLength(int messageLength) {
		this.messageLength = messageLength;
	}

	public void setImageMessageLength(int imageMessageLength) {
		this.imageMessageLength = imageMessageLength;
	}

	public int getMaxBandwidth() {
		return maxBandwidth;
	}

	public String getWelcomeText() {
		return welcomeText;
	}

	public boolean isAllowHtml() {
		return allowHtml;
	}

	public int getMessageLength() {
		return messageLength;
	}

	public int getImageMessageLength() {
		return imageMessageLength;
	}

	@Override
	public String toString() {
		return "ServerConfig [maxBandwidth=" + maxBandwidth + ", welcomeText=" + welcomeText + ", allowHtml=" + allowHtml + ", messageLength=" + messageLength + ", imageMessageLength=" + imageMessageLength + "]";
	}
}
