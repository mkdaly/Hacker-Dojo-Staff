package net.metamike.hackerdojo.widget;

import java.io.IOException;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;

import com.google.gson.stream.JsonReader;

public class EventFetchService extends QueryService {

	private String eventsUrlString = "";
	
	private final IBinder binder = new EventBinder();
	
	@Override
	public void onStart(Intent i, int startid) {
		go();
	}
	
	public void go() {
		setValuesFromPreferences();
		new QueryTask().execute(new String[]{ eventsUrlString });
	}

	public IBinder onBind(Intent i) {
		return binder;
	}
	
	private void setValuesFromPreferences() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		eventsUrlString = prefs.getString(getString(R.string.PREF_EVENTS_URL), null);
	}

	//TODO: It doesn't look like the times are being set properly.
	protected void readJSONObject(JsonReader reader) throws IOException {
		reader.beginObject();
		Event e = new Event();
		while(reader.hasNext()) {
			String name = reader.nextName();
			if (getString(R.string.JSON_EVENT_TITLE).equals(name)) {
				e.setName(reader.nextString());
			} else if (getString(R.string.JSON_EVENT_START_TIME).equals(name)) {
				e.setStart(reader.nextString());
			} else if (getString(R.string.JSON_EVENT_END_TIME).equals(name)) {
				e.setEnd(reader.nextString());
			} else if ( getString(R.string.JSON_EVENT_STATUS).equals(name)){
				e.setStatus(reader.nextString());
			} else if ( getString(R.string.JSON_EVENT_ID).equals(name)){
				e.setId(reader.nextLong());
			} else {
				reader.skipValue();
			}
		}
		reader.endObject();
		Message msg = new Message();
		msg.what = EventActivity.MESSAGE_EVENT;
		msg.obj = e;
		handler.sendMessage(msg);
	}


	@Override
	public void setHandler(Handler handler) {
		this.handler = handler; 
	}


	//TODO: Handle IOE
	public class EventBinder extends Binder {
		EventFetchService getService() {
			return EventFetchService.this;
		}
	}
	
}
