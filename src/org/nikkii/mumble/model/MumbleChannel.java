package org.nikkii.mumble.model;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class MumbleChannel {
	private int id;
	private String name;
	
	private MumbleChannel parent;

	private Map<Integer, MumbleChannel> subChannels = new HashMap<Integer, MumbleChannel>();
	
	private List<MumbleUser> users = new LinkedList<MumbleUser>();
	
	public MumbleChannel(int id, String name) {
		this.id = id;
		this.name = name;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Map<Integer, MumbleChannel> getSubChannels() {
		return subChannels;
	}
	
	@Override
	public String toString() {
		StringBuilder bldr = new StringBuilder();
		bldr.append("Channel [").append("id=").append(id).append(",name=").append(name).append(",users=").append(users);
		if(parent != null) {
			bldr.append(",parent=").append(parent);
		}
		bldr.append(']');
		return bldr.toString();
	}
	
	public String toTree() {
		StringBuilder str = new StringBuilder();
		MumbleChannel c = this;
		while(c.hasParent()) {
			c = c.getParent();
			str.insert(0, c.getName() + " -> ");
		}
		str.append(name);
		return str.toString();
	}

	public void setParent(MumbleChannel parent) {
		this.parent = parent;
	}
	
	public MumbleChannel getParent() {
		return parent;
	}

	public void addSubChannel(MumbleChannel channel) {
		subChannels.put(channel.getId(), channel);
	}

	public boolean hasParent() {
		return parent != null;
	}

	public void removeSubChannel(MumbleChannel channel) {
		subChannels.remove(channel.getId());
	}

	public MumbleChannel getSubChannel(String name) {
		for(MumbleChannel channel : subChannels.values()) {
			if(channel.getName().trim().equals(name)) {
				return channel;
			}
		}
		return null;
	}

	public void addUser(MumbleUser user) {
		users.add(user);
	}

	public void removeUser(MumbleUser user) {
		users.remove(user);
	}
}
