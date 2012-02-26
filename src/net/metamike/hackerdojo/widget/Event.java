package net.metamike.hackerdojo.widget;

import java.util.Date;

import android.text.format.Time;
import android.util.TimeFormatException;

public class Event {

	private Long dojoID;
	private String name;
	private Date start;
	private Date end;
	private RoomStatus status;
	private String room;
	
	public Event() {
		dojoID = -1L;
		name = "";
		start = new Date();
		end = new Date();
		status = RoomStatus.UNKNOWN;
		room = "";
	}

	public Event(Long dojoID, String name, Date start, Date end, RoomStatus status, String room) {
		this.dojoID = dojoID;
		this.name = name;
		this.start = start;
		this.end = end;
		this.status = status;
		this.room = room;
	}
	
	public Boolean verify() {
		if (dojoID != null && name != null && start != null) {
			return true;
		}
		return false;
	}
	
	private Date getDateFromString(String time) {
		try {
			Time t = new Time();
			t.parse3339(time);
			return new Date(t.toMillis(false));
		} catch (TimeFormatException tfe) {
			return new Date();
		}
	}
		
	public Long getDojoID() {
		return dojoID;
	}

	public void setDojoID(Long dojoID) {
		this.dojoID = dojoID;
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

	public RoomStatus getStatus() {
		return status;
	}

	public void setStatus(RoomStatus status) {
		this.status = status;
	}
	
	public void setStatus(String statusString) {
		try {
			status = RoomStatus.valueOf(statusString.toUpperCase());
		} catch (IllegalArgumentException iae) {
			status = RoomStatus.UNKNOWN;
		} catch (NullPointerException npe) {
			status = RoomStatus.UNKNOWN;
		}
	}

	public String getRoom() {
		return room;
	}

	public void setRoom(String room) {
		this.room = room;
	}

	public enum RoomStatus {
		APPROVED,
		CANCELLED,
		UNKNOWN;
	}
}