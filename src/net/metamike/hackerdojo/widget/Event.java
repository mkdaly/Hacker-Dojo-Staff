package net.metamike.hackerdojo.widget;

import java.util.Date;

import android.text.format.Time;
import android.util.TimeFormatException;

public class Event {

	private Long dojoID;
	private String name;
	private Time start;
	private Time end;
	private EventStatus status;
	private String room;
	
	public Event() {
		dojoID = -1L;
		name = "";
		start = new Time();
		start.setToNow();
		end = new Time();
		end.setToNow();
		status = EventStatus.UNKNOWN;
		room = "";
	}

	public Event(Long dojoID, String name, Time start, Time end, EventStatus status, String room) {
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
	
	private Time getDateFromString(String time) {
		Time t = new Time();
		try {
			t.parse3339(time);
		} catch (TimeFormatException tfe) {
			t.setToNow();
		}
		return t;
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

	public Time getStart() {
		return start;
	}

	public void setStart(Time start) {
		this.start.set(start);
	}

	public void setStart(String startString) {
		start.set(getDateFromString(startString));
	}
	
	public Time getEnd() {
		return end;
	}

	public void setEnd(Time end) {
		this.end.set(end);
	}
	
	public void setEnd(String endString) {
		end.set(getDateFromString(endString));
	}

	public EventStatus getStatus() {
		return status;
	}

	public void setStatus(EventStatus status) {
		this.status = status;
	}
	
	public void setStatus(String statusString) {
		try {
			status = EventStatus.valueOf(statusString.toUpperCase());
		} catch (IllegalArgumentException iae) {
			status = EventStatus.UNKNOWN;
		} catch (NullPointerException npe) {
			status = EventStatus.UNKNOWN;
		}
	}

	public String getRoom() {
		return room;
	}

	public void setRoom(String room) {
		this.room = room;
	}

	public enum EventStatus {
		APPROVED,
		CANCELED,
		UNKNOWN,
		PAST;
	}
}