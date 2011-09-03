package net.metamike.hackerdojo.widget;

import java.net.URL;

public class Person {
	private String name;
	private String time;
	private URL gravatar;
	
	public boolean verify() {
		//Check that there is something set for name
		if (name == null || "".equals(name)) { return false; }
		
		return true;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public URL getGravatar() {
		return gravatar;
	}

	public void setGravatar(URL gravatar) {
		this.gravatar = gravatar;
	}
}
