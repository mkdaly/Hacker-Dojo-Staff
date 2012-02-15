package net.metamike.hackerdojo.widget;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class StaffDBAdapter {
	private static final String STAFF_DB_NAME = "staff.db";
	private static final String STAFF_TABLE = "staff";
	private static final int DATABASE_VERSION = 1;
	
	private static final String KEY_ID = "_id";
	private static final String KEY_NAME = "name";
	private static final int NAME_COLUMN = 1;
	
	private static final String CREATE_DATABASE = "create table " +
			STAFF_TABLE + " (" + KEY_ID + " integer primary key autoincrement, " +
			KEY_NAME + " text not null);";
	
	private SQLiteDatabase staffDB;
	private final Context context;
	private StaffDBHelper dbHelper;
	
	
	public StaffDBAdapter(Context context) {
		this.context = context;
		dbHelper = new StaffDBHelper(context, STAFF_DB_NAME, null, DATABASE_VERSION);
	}
	
	public StaffDBAdapter open() throws SQLException  {
		try {
			staffDB = dbHelper.getWritableDatabase();
		} catch (SQLiteException sle) {
			Log.w("StaffDBAdapter", sle.getMessage());
			staffDB = dbHelper.getReadableDatabase();
		}
		return this;
	}
	
	public void close() {
		staffDB.close();
	}
	
	public int insertEntry(Person p) {
		//TODO: this
		return 1;
	}

	public boolean removeEntry(long id) {
		return staffDB.delete(STAFF_TABLE, KEY_ID + "=" + id, null) > 0;
	}
	
	public Cursor getAllEntries() {
		return staffDB.query(STAFF_TABLE, new String[] {KEY_ID,KEY_NAME},
				null, null, null, null, null);
		//TODO: probably will want to specifiy more options.
	}
	
	public Person getEntry(long id) {
		return null;
	}
	
	public boolean updateEntry(long id, Person p) {
		return true;
	}
	
	private static class StaffDBHelper extends SQLiteOpenHelper {

		public StaffDBHelper(Context context, String name,
				CursorFactory factory, int version) {
			super(context, name, factory, version);
		}
		
		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(CREATE_DATABASE);
		}


		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w("StaffDBHelper", "Upgrading from version " + oldVersion 
					+ " to "+ newVersion +" whcih will destroy all old data.");
			db.execSQL("DROP TABLE IF EXISTS " + STAFF_DB_NAME);
			onCreate(db);
		}
		
		

	}
}
