package net.metamike.hackerdojo.widget;

import java.io.IOException;
import java.util.ArrayList;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;

import com.google.gson.stream.JsonReader;

public class StaffFetchService extends QueryService {

	private Boolean doFetchGravatar = Boolean.FALSE;
	private String staffUrlString = "";

	private final IBinder binder = new StaffBinder();
	
	@Override
	public void onStart(Intent i, int startID) {
		go();
	}

	public void go() {
		setValuesFromPreferences();
		new QueryTask().execute(new String[]{ staffUrlString });
	}
	
	@Override
	public IBinder onBind(Intent i) {
		return binder;
	}
	
	private void setValuesFromPreferences() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		staffUrlString = prefs.getString(getString(R.string.PREF_STAFF_URL), null);
		doFetchGravatar = prefs.getBoolean(getString(R.string.PREF_LOAD_GRAVATARS), false);
	}
	
	protected void readJSONObject(JsonReader reader) throws IOException{
		reader.beginObject();
		Person p = new Person();
		while(reader.hasNext()) {
			String name = reader.nextName();
			if (getString(R.string.JSON_PERSON_NAME).equals(name)) {
				p.setName(reader.nextString());
			} else if (getString(R.string.JSON_PERSON_LOG_IN_TIME).equals(name)) {
				p.setTime(reader.nextString());
			} else if ( this.doFetchGravatar && getString(R.string.JSON_PERSON_IMAGE_URL).equals(name)){
				p.setGravatar(reader.nextString());
			} else {
				reader.skipValue();
			}
		}
		reader.endObject();
		Message msg = new Message();
		msg.what = StaffActivity.MESSAGE_PERSON;
		msg.obj = p;
		handler.sendMessage(msg);
	}
	
	@Override
	public void setHandler(Handler handler) {
		this.handler = handler; 
	}


	//TODO: Handle IOE
	public class StaffBinder extends Binder {
		StaffFetchService getService() {
			return StaffFetchService.this;
		}
	}
	
}
