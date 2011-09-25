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

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class DisplayActivity extends Activity {
	private static final String TAG = "DisplayActivity";
	private static final int PREFERENCE_ACTIVITY = 1;

	private DojoContentHandlerImpl ch = new DojoContentHandlerImpl();

	//Views
	private TextView statusView;
	private ListView peopleView;
	private ProgressBar throbber;

	//State vars
	private Boolean isOpen = Boolean.FALSE;
	private List<Person> people = Collections.synchronizedList(new ArrayList<Person>());
	private PersonArrayAdapter personAdapter;
	private String urlString;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.i(TAG, "onCreate(" + savedInstanceState + ")");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		PreferenceManager.setDefaultValues(getApplicationContext(), R.xml.preferences, false);

		throbber = (ProgressBar) findViewById(R.id.throbber);
		statusView = (TextView)findViewById(R.id.view_dojo_status);
		personAdapter = new PersonArrayAdapter(this, R.layout.person, people);
		peopleView = (ListView)findViewById(R.id.view_people);
		peopleView.setAdapter(personAdapter);
		setURLStringFromPreferences();

		new QueryTask().execute((Void[])null);
	}
	
	private void setURLStringFromPreferences() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		urlString = prefs.getString(getString(R.string.WIDGET_URL), null);
	}
	
	private void queryDojo() {
		try {
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
			//TODO: Provide feedback to user
			Log.e(TAG, "Bad URL:"+urlString, mfu);
			mfu.printStackTrace();
		} catch (IOException ioe) {
			//TODO: Provide feedback to user
			Log.e(TAG, "IO Error.", ioe);
			ioe.printStackTrace();
		} catch (FactoryConfigurationError e) {
			//TODO: Provide feedback to user
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			//TODO: Provide feedback to user
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void setStatusLine() {
		////TODO: Add an "unknown" status
		if (isOpen) {
			statusView.setText("The Dojo is open.");
			statusView.setTextColor(Color.GREEN);
		} else {
			statusView.setText("The Dojo is closed.");
			statusView.setTextColor(Color.RED);
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		switch(item.getItemId()) {
			case (R.id.menu_refresh):
				refresh();
				return true;
			case (R.id.menu_exit):
				this.finish();
				return true;
			case (R.id.menu_preferences):
				startActivityForResult(new Intent(this, PreferencesActivity.class), DisplayActivity.PREFERENCE_ACTIVITY);
				return true;
		}
		return false;
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == PREFERENCE_ACTIVITY)
			/*Since the back button is used to exit the PA,
			  the resultCode will be Activity.RESULT_CANCELLED */
			//if (resultCode == Activity.RESULT_OK)
			updateFromPreferences();
	}
		
	private void refresh() {
		this.people.clear();
		personAdapter.notifyDataSetChanged();
		new QueryTask().execute((Void[])null);
	}
	
	private void updateFromPreferences() {
		//TODO: Implement auto-refresh
		
		//TODO: check for change and refresh IFF the url changed
		setURLStringFromPreferences();
		refresh();
	}
	
	private class QueryTask extends AsyncTask<Void, Void, Void> {
		@Override
		protected void onPreExecute() {
			DisplayActivity.this.throbber.setVisibility(View.VISIBLE); 
			DisplayActivity.this.statusView.setText(R.string.fetching_status);
			DisplayActivity.this.statusView.setTextColor(Color.LTGRAY);
		}

		@Override
		protected void onPostExecute(Void result) {
			DisplayActivity.this.throbber.setVisibility(View.INVISIBLE);
			setStatusLine();
			personAdapter.notifyDataSetChanged();
		}

		@Override
		protected Void doInBackground(Void... params) {
			DisplayActivity.this.queryDojo();
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
					DisplayActivity.this.people.add(peep);
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
						DisplayActivity.this.isOpen = Boolean.TRUE;
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
