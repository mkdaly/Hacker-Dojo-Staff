package net.metamike.hackerdojo.widget;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import com.google.gson.stream.JsonReader;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.util.Log;

public abstract class QueryService extends Service {
	public static final String INTENT_EXCEPTION_THROWN = "net.metamike.hackerdojo.widget.intents.Exception_Thrown";
	public static final String INTENT_QUERYING_DOJO = "net.metamike.hackerdojo.widget.intents.Querying_Dojo";
//	public static final String INTENT_STARTED_QUERYING_DOJO = "net.metamike.hackerdojo.widget.intents.Starting_Querying_Dojo";
//	public static final String INTENT_FINISHED_QUERYING_DOJO = "net.metamike.hackerdojo.widget.intents.Finished_Querying_Dojo";

	public static final String INTENT_EXTRA_PEOPLE = "net.metamike.hackerdojo.widget.intents.people";
	public static final String INTENT_EXTRA_STATUS = "net.metamike.hackerdojo.widget.intents.status";
	//public static final String INTENT_EXTRA_THROBBER = "net.metamike.hackerdojo.widget.intents.throbber";

	
	private static final String TAG = "QueryService";
	protected Handler handler;

	@Override
	public void onCreate() {
		//From http://android-developers.blogspot.com/2011/09/androids-http-clients.html
		// 8 is the magic number for FROYO
		if (Build.VERSION.SDK_INT < 8) {
	        System.setProperty("http.keepAlive", "false");
	    }
	}
		
	protected class QueryTask extends AsyncTask<String, Void, Void> {
		@Override
		protected void onPreExecute() {
			//Intent i = new Intent(QueryService.this, DisplayActivity.PersonReceiver.class);
			//Intent i = new Intent(INTENT_QUERYING_DOJO);
			//sendBroadcast(i);
			handler.sendEmptyMessage(InfoActivity.MESSAGE_START);
		}

		@Override
		protected void onPostExecute(Void result) {
			//Intent i = new Intent(QueryService.this, DisplayActivity.PersonReceiver.class);
			Intent i = new Intent(INTENT_QUERYING_DOJO);
			//i.putExtra(INTENT_EXTRA_PEOPLE, people);
			//i.putExtra(INTENT_EXTRA_STATUS, );
			//sendBroadcast(i);
			handler.sendEmptyMessage(InfoActivity.MESSAGE_STOP);
		}

		@Override
		protected Void doInBackground(String... urls) {
			if (urls.length < 1) {
				return null;
			} 
			fetchJSONData(urls[0]);
			return null;
		}
	}
	
	private void fetchJSONData(String url) {
		try {
			URL staffLocation = new URL(url);
			HttpURLConnection connection = (HttpURLConnection)staffLocation.openConnection();
			connection.connect();
			if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
				JsonReader reader = null;
				try {
					reader = new JsonReader( new InputStreamReader(connection.getInputStream()));
					reader.setLenient(true);
					readJSONArray(reader);
				}
				finally {
					if (reader != null) 
						reader.close();
				}
			}
			connection.disconnect();
		} catch (MalformedURLException mfu) {
			Log.e(TAG, "Bad URL:"+url, mfu);
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
	}

	//TODO: Handle IOE
	private void readJSONArray(JsonReader reader ) throws IOException {
		reader.beginArray();
		while (reader.hasNext()) {
			readJSONObject(reader);
		}
		reader.endArray();
	}
	
	protected abstract void readJSONObject(JsonReader reader) throws IOException;
	public abstract void setHandler(Handler handler);
}