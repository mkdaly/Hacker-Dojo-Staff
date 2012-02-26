package net.metamike.hackerdojo.widget;

import java.util.Date;

import net.metamike.hackerdojo.widget.Event.RoomStatus;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class EventDBAdapter {
	private static final String EVENT_DB_NAME = "event.db";
	private static final String EVENT_TABLE = "event";
	private static final int DATABASE_VERSION = 1;
	
	public static final String KEY_ID = "_id";
	public static final String KEY_DOJO_ID = "dojo_id";
	public static final String KEY_NAME = "name";
	public static final String KEY_START = "start";
	public static final String KEY_END = "end";
	public static final String KEY_LOCATION = "location";
	public static final String KEY_STATUS = "status";
/*
	static final int ID_COLUMN = 0;
	static final int DOJO_ID_COLUMN = 1;
	static final int NAME_COLUMN = 2;
	static final int START_COLUMN = 3;
	static final int END_COLUMN = 4;
	static final int LOCATION_COLUMN = 5;
	static final int STATUS_COLUMN = 6;
*/	
	private static final String CREATE_DATABASE = "create table " +
			EVENT_TABLE + " (" + KEY_ID + " integer primary key autoincrement, " +
			KEY_DOJO_ID + " integer not null, " +
			KEY_START + " integer not null, " +
			KEY_END + " integer not null, " +
			KEY_NAME + " text not null, " +
			KEY_LOCATION + " text not null, " +
			KEY_STATUS + " integer not null);";
	
	private SQLiteDatabase eventDB;
	private EventDBHelper dbHelper;
	
	
	public EventDBAdapter(Context context) {
		dbHelper = new EventDBHelper(context, EVENT_DB_NAME, null, DATABASE_VERSION);
	}
	
	public EventDBAdapter open() throws SQLException  {
		try {
			eventDB = dbHelper.getWritableDatabase();
		} catch (SQLiteException sle) {
			Log.w("EventDBAdapter", sle.getMessage());
			eventDB = dbHelper.getReadableDatabase();
		}
		return this;
	}
	
	public void close() {
		eventDB.close();
	}

	//TODO: Need to do a insert or replace.
	public long insertEntry(Event e) {
		ContentValues values = new ContentValues();
		values.put(KEY_END, e.getEnd().getTime());
		values.put(KEY_DOJO_ID, e.getDojoID());
		values.put(KEY_LOCATION, e.getRoom());
		values.put(KEY_NAME, e.getName());
		values.put(KEY_START, e.getStart().getTime());
		values.put(KEY_STATUS, e.getStatus().ordinal());
		return eventDB.insert(EVENT_TABLE, null, values);
	}

	public boolean removeEntry(Long dojoID) {
		return eventDB.delete(EVENT_TABLE, KEY_DOJO_ID + "=" + dojoID, null) > 0;
	}
	
	public Cursor getAllEntries() {
		return eventDB.query(EVENT_TABLE, new String[] {KEY_ID,KEY_DOJO_ID,KEY_START,KEY_END,KEY_NAME,KEY_STATUS,KEY_LOCATION},
				null, null, null, null, null);
		//TODO: probably will want to specify more options.
	}
	
	public Event getEntry(Long dojoID) {
		Cursor c = eventDB.query(EVENT_TABLE, new String[] {KEY_ID,KEY_DOJO_ID,KEY_START,KEY_NAME,KEY_STATUS,KEY_LOCATION},
				KEY_DOJO_ID + "=" + dojoID, null, null, null, null);
		return getEventObjectFromCursor(c);
	}
	
	public int updateEntry(Long dojoID, Event e) {
		ContentValues values = new ContentValues();
		values.put(KEY_END, e.getEnd().getTime());
		values.put(KEY_DOJO_ID, e.getDojoID());
		values.put(KEY_LOCATION, e.getRoom());
		values.put(KEY_NAME, e.getName());
		values.put(KEY_START, e.getStart().getTime());
		values.put(KEY_STATUS, e.getStatus().ordinal());
		return eventDB.update(EVENT_TABLE, values, KEY_DOJO_ID + "=" + dojoID, null );
	}
	
	public static Event getEventObjectFromCursor(Cursor c) {
		return new Event(c.getLong(c.getColumnIndex(KEY_DOJO_ID)),
				c.getString(c.getColumnIndex(KEY_NAME)),
				new Date(c.getLong(c.getColumnIndex(KEY_START))),
				new Date(c.getLong(c.getColumnIndex(KEY_END))),
				RoomStatus.values()[c.getInt(c.getColumnIndex(KEY_STATUS))],
				c.getString(c.getColumnIndex(KEY_LOCATION)));
	}
	
	private static class EventDBHelper extends SQLiteOpenHelper {

		public EventDBHelper(Context context, String name,
				CursorFactory factory, int version) {
			super(context, name, factory, version);
		}
		
		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(CREATE_DATABASE);
		}


		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w("EventDBHelper", "Upgrading from version " + oldVersion 
					+ " to "+ newVersion +" which will destroy all old data.");
			db.execSQL("DROP TABLE IF EXISTS " + EVENT_DB_NAME);
			onCreate(db);
		}
	}
}
