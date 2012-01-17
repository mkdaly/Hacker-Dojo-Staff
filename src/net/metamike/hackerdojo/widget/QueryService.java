package net.metamike.hackerdojo.widget;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import net.metamike.hackerdojo.widget.DisplayActivity.DojoStatus;

import com.google.gson.stream.JsonReader;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

public class QueryService extends Service {
	public static final String INTENT_EXCEPTION_THROWN = "net.metamike.hackerdojo.widget.intents.Exception_Thrown";
	public static final String INTENT_QUERYING_DOJO = "net.metamike.hackerdojo.widget.intents.Querying_Dojo";
//	public static final String INTENT_STARTED_QUERYING_DOJO = "net.metamike.hackerdojo.widget.intents.Starting_Querying_Dojo";
//	public static final String INTENT_FINISHED_QUERYING_DOJO = "net.metamike.hackerdojo.widget.intents.Finished_Querying_Dojo";

	public static final String INTENT_EXTRA_PEOPLE = "net.metamike.hackerdojo.widget.intents.people";
	public static final String INTENT_EXTRA_STATUS = "net.metamike.hackerdojo.widget.intents.status";
	//public static final String INTENT_EXTRA_THROBBER = "net.metamike.hackerdojo.widget.intents.throbber";

	
	private static final String TAG = "QueryService";

	private String staffUrlString;
	private Boolean doFetchGravatar = Boolean.FALSE;

	private DojoStatus status;
	private ArrayList<Person> people;	

	@Override
	public void onCreate() {
		//From http://android-developers.blogspot.com/2011/09/androids-http-clients.html
		// 8 is the magic number for FROYO
		if (Build.VERSION.SDK_INT < 8) {
	        System.setProperty("http.keepAlive", "false");
	    }

	}

	@Override
	public void onStart(Intent intent, int startId) {
		setValuesFromPreferences();
		people = new ArrayList<Person>();
		status = DojoStatus.UNKNOWN;
		new QueryTask().execute((Void[])null);
	}
	
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
	
	private void addNewPerson(Person peep) {
		people.add(peep);
	}
	
	private void setStatus() {
		if (people.isEmpty())
			this.status =  DojoStatus.CLOSED;
		else 
			this.status = DojoStatus.OPEN;
	}
	
	private void setValuesFromPreferences() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		staffUrlString = prefs.getString(getString(R.string.PREF_WIDGET_URL), null);
		doFetchGravatar = prefs.getBoolean(getString(R.string.PREF_LOAD_GRAVATARS), false);
	}
	
	private class QueryTask extends AsyncTask<Void, Person, Void> {
		@Override
		protected void onPreExecute() {
			//Intent i = new Intent(QueryService.this, DisplayActivity.PersonReceiver.class);
			Intent i = new Intent(INTENT_QUERYING_DOJO);
			sendBroadcast(i);			
		}

		@Override
		protected void onPostExecute(Void result) {
			//Intent i = new Intent(QueryService.this, DisplayActivity.PersonReceiver.class);
			Intent i = new Intent(INTENT_QUERYING_DOJO);
			i.putExtra(INTENT_EXTRA_PEOPLE, people);
			i.putExtra(INTENT_EXTRA_STATUS, status);
			sendBroadcast(i);
		}

		@Override
		protected Void doInBackground(Void... params) {
			try {
				URL location = new URL(staffUrlString);
				HttpURLConnection connection = (HttpURLConnection)location.openConnection();
				connection.connect();
				if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
					JsonReader reader = null;
					try {
						reader = new JsonReader( new InputStreamReader(connection.getInputStream()));
						reader.setLenient(true);
						readStaff(reader);
					}
					finally {
						if (reader != null) 
							reader.close();
					}
				}
				connection.disconnect();
			} catch (MalformedURLException mfu) {
				Log.e(TAG, "Bad URL:"+staffUrlString, mfu);
				mfu.printStackTrace();
				Intent i = new Intent(INTENT_EXCEPTION_THROWN);
				i.putExtra("exception", mfu);
				sendBroadcast(i);				
			} catch (IOException ioe) {
				Log.e(TAG, "IO Error.", ioe);
				ioe.printStackTrace();
				Intent i = new Intent(INTENT_EXCEPTION_THROWN);
				i.putExtra("exception", ioe);
				sendBroadcast(i);
			}
			return null;
		}
	}

	//TODO: Handle IOE
	private void readStaff(JsonReader reader) throws IOException {
		reader.beginArray();
		while (reader.hasNext()) {
			addNewPerson(readPerson(reader));
		}
		reader.endArray();
		setStatus();
	}

	//TODO: Handle IOE
	private Person readPerson(JsonReader reader) throws IOException{
		String personName = null;
		String time = null;
		String imageURL = null;
		reader.beginObject();
		while(reader.hasNext()) {
			String name = reader.nextName();
			if (getString(R.string.JSON_NAME).equals(name)) {
				personName = reader.nextString();
			} else if (getString(R.string.JSON_LOG_IN_TIME).equals(name)) {
				time = reader.nextString();
			} else if ( this.doFetchGravatar && getString(R.string.JSON_IMAGE_URL).equals(name)){
				imageURL = reader.nextString();
			} else {
				reader.skipValue();
			}
		}
		reader.endObject();
		//TODO: Make size a setting
		return new Person(personName, time, imageURL+"?s=50");
	}

	public class QueryBinder extends Binder {
		QueryService getService() {
			return QueryService.this;
		}
	}
}