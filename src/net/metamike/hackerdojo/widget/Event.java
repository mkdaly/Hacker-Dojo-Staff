package net.metamike.hackerdojo.widget;

import java.util.Date;

import android.text.format.Time;

public class Event {
	private static final String TAG = "Event";

	private Long id;
	private String name;
	private Date start;
	private Date end;
	private Status status;
	private String room;
	
	public Event() {
		id = -1L;
		name = "";
		start = new Date();
		end = new Date();
		status = Status.UNKNOWN;
		room = "";
	}

	public Event(Long id, String name, Date start, Date end, Status status, String room) {
		this.id = id;
		this.name = name;
		this.start = start;
		this.end = end;
		this.status = status;
		this.room = room;
	}
	
	public Boolean verify() {
		if (id != null && name != null && start != null) {
			return true;
		}
		return false;
	}
	
	private Date getDateFromString(String time) {
		Time t = new Time();
		if (t.parse3339(time)) {
			return new Date(t.toMillis(false));
		} else {
			return new Date();
		}
	}
		
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Date getStart() {
		return start;
	}

	public void setStart(Date start) {
		this.start = start;
	}

	public void setStart(String startString) {
		start = getDateFromString(startString);
	}
	
	public Date getEnd() {
		return end;
	}

	public void setEnd(Date end) {
		this.end = end;
	}
	
	public void setEnd(String endString) {
		end = getDateFromString(endString);
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}
	
	public void setStatus(String statusString) {
		try {
			status = Status.valueOf(statusString.toUpperCase());
		} catch (IllegalArgumentException iae) {
			status = Status.UNKNOWN;
		} catch (NullPointerException npe) {
			status = Status.UNKNOWN;
		}
	}

	public String getRoom() {
		return room;
	}

	public void setRoom(String room) {
		this.room = room;
	}

	public enum Status {
		APPROVED,
		CANCELLED,
		UNKNOWN;
	}
}