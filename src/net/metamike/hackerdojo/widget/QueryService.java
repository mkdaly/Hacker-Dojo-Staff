package net.metamike.hackerdojo.widget;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.parsers.FactoryConfigurationError;

import org.ccil.cowan.tagsoup.Parser;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;

public class QueryService extends Service {
	public static final String EXCEPTION_THROWN = "Exception_Thrown";
	public static final String FINISHED_QUERYING_DOJO = "Finished_Querying_Dojo";

	public static final String INTENT_EXTRA_PEOPLE = "people";
	public static final String INTENT_EXTRA_STATUS = "status";
	
	private static final String TAG = "QueryService";

	private String urlString;
	private Boolean doFetchGravatar = Boolean.FALSE;

	private Boolean status;  //TODO: Make tri-state, via a new class?
	private ArrayList<Person> people;
	
	private DojoContentHandlerImpl ch = new DojoContentHandlerImpl();
	

	@Override
	public void onCreate() {
	}

	@Override
	public void onStart(Intent intent, int startId) {
		setValuesFromPreferences();
		people = new ArrayList<Person>();
		status = null;
		new QueryTask().execute((Void[])null);
	}

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	
	private void addNewPerson(Person peep) {
		people.add(peep);
	}
	
	private void setStatus(Boolean status) {
		this.status = status;
	}
	
	
	private void setValuesFromPreferences() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		urlString = prefs.getString(getString(R.string.PREF_WIDGET_URL), null);
		doFetchGravatar = prefs.getBoolean(getString(R.string.PREF_LOAD_GRAVATARS), false);
	}
	
	private class QueryTask extends AsyncTask<Void, Person, Void> {
		@Override
		protected void onPreExecute() {
			/*
			 * TODO: Figure out how to communicate this....
			DisplayActivity.this.throbber.setVisibility(View.VISIBLE); 
			DisplayActivity.this.statusView.setText(R.string.fetching_status);
			DisplayActivity.this.statusView.setTextColor(Color.LTGRAY);
			*/
		}

		@Override
		protected void onPostExecute(Void result) {
			//TODO: Figure out how to communicate this....
			//DisplayActivity.this.setThrobber(View.INVISIBLE);

			Intent i = new Intent(FINISHED_QUERYING_DOJO);
			i.putExtra(INTENT_EXTRA_PEOPLE, people);
			i.putExtra(INTENT_EXTRA_STATUS, status);
			sendBroadcast(i);
		}

		@Override
		protected Void doInBackground(Void... params) {
			try {
				//isOpen = null; TODO: Need to figure out how to communicate to DisplayActivity
				URL location = new URL(urlString);
				HttpURLConnection connection = (HttpURLConnection)location.openConnection();
				connection.connect();
				if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
					InputStream is = connection.getInputStream();
					XMLReader reader = new Parser();
					reader.setContentHandler(ch);
					reader.parse( new InputSource(is));
					is.close();
				}
				connection.disconnect();
			} catch (MalformedURLException mfu) {
				Log.e(TAG, "Bad URL:"+urlString, mfu);
				mfu.printStackTrace();
				Intent i = new Intent(EXCEPTION_THROWN);
				i.putExtra("exception", mfu);
				sendBroadcast(i);				
			} catch (IOException ioe) {
				Log.e(TAG, "IO Error.", ioe);
				ioe.printStackTrace();
				Intent i = new Intent(EXCEPTION_THROWN);
				i.putExtra("exception", ioe);
				sendBroadcast(i);
			} catch (FactoryConfigurationError e) {
				//TODO: Provide feedback to user
				e.printStackTrace();
			} catch (SAXException e) {
				//TODO: Provide feedback to user
				e.printStackTrace();
			}
			return null;
		}
	}


	private class DojoContentHandlerImpl extends DefaultHandler {
		private Person peep;
		
		private boolean inName = false;
		private boolean inAgo = false;
		private String attr = "";
		private StringBuffer sb = new StringBuffer();

		@Override
		public void characters(char[] ch, int start, int length)
				throws SAXException {
			//the null check for this.peep should have been done in startElement() 
			if (inName || inAgo) {
				sb.append(ch, start, length);
			}
		}
		
		@Override
		public void endElement(String uri, String localName, String qName)
				throws SAXException {
			if ("tr".equals(localName)) {
				if (peep != null && peep.verify()) {
					QueryService.this.addNewPerson(peep);
				}
			} if ("span".equals(localName)) {
				if("name".equalsIgnoreCase(attr) && peep != null) {
					peep.setName(sb.toString());
					sb = new StringBuffer();
					attr = null;
					inName = false;
				} else if("ago".equalsIgnoreCase(attr) && peep != null) {
					peep.setTime(sb.toString());
					sb = new StringBuffer();
					attr = null;
					inAgo = false;
				}
			}
		}

		@Override
		public void startElement(String uri, String localName, String qName,
				Attributes atts) throws SAXException {
			if ("p".equals(localName)) {
				String cssClass = atts.getValue("class"); 
				if ( cssClass != null && "openline".equalsIgnoreCase(cssClass)) {
						setStatus(Boolean.TRUE);
				} else {
					//TODO: handle close case, need to see HTML when closed....
				}
			} if ("tr".equals(localName)) {
				//New person
				peep = new Person();
			} if ("span".equals(localName)) {
				String klass = atts.getValue("class"); 
				if ( klass == null) {
					return;
				} else if("name".equalsIgnoreCase(klass) && peep != null) {
					attr = klass;
					inName = true;
				} else if("ago".equalsIgnoreCase(klass) && peep != null) {
					attr = klass;
					inAgo = true;
				}
			} if ("img".equals(localName)) {
				//assume that the only img's are gravatar urls
				if (QueryService.this.doFetchGravatar) {
					String url = atts.getValue("src");
					if (url != null && peep != null)
						try {
							//TODO: Make size a setting
							peep.setGravatar( new URL(url+"?s=50"));
						} catch (MalformedURLException e) {
							//swallow it
						}
				}	
			}
		}		
	}	

}
