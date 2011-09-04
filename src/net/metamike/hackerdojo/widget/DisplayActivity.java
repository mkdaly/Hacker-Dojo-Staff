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
import org.jdom.Document;
import org.jdom.input.SAXHandler;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import android.app.Activity;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class DisplayActivity extends Activity {
	private static final String TAG = "DisplayActivity";
	private Boolean isOpen = Boolean.FALSE;
	
	private TextView statusView;
	private ListView peopleView;
	private ProgressBar throbber;
	
	private static final int REFRESH_MENUITEM = Menu.FIRST;
	private static final int CLOSE_MENUITEM = Menu.FIRST+1;
	
	private DojoContentHandlerImpl ch = new DojoContentHandlerImpl();
	private List<Person> people = Collections.synchronizedList(new ArrayList<Person>());
	private PersonArrayAdapter personAdapter;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.i(TAG, "onCreate(" + savedInstanceState + ")");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		throbber = (ProgressBar) findViewById(R.id.throbber);
		personAdapter = new PersonArrayAdapter(this, R.layout.person, people);
		peopleView = (ListView)findViewById(R.id.view_people);
		peopleView.setAdapter(personAdapter);
		new QueryTask().execute((Void[])null);
	}
	
	private void queryDojo() {
		//TODO: Make the URL a settings value
		String url = getString(R.string.widget_url);
		try {
			URL location = new URL(url);
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
			Log.e(TAG, "Bad URL:"+url, mfu);
			mfu.printStackTrace();
		} catch (IOException ioe) {
			Log.e(TAG, "IO Error.", ioe);
			ioe.printStackTrace();
		} catch (FactoryConfigurationError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void setStatusLine() {
		statusView = (TextView)findViewById(R.id.view_dojo_status);
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
		menu.add(0, REFRESH_MENUITEM, Menu.NONE, R.string.refresh_menu);
		menu.add(0, CLOSE_MENUITEM, Menu.NONE, R.string.close_menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
			case (REFRESH_MENUITEM):
				refresh();
				break;
			case (CLOSE_MENUITEM):
				this.finish();
				break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	private void refresh() {
		this.people.clear();
		personAdapter.notifyDataSetChanged();
		new QueryTask().execute((Void[])null);
	}
	
	private class QueryTask extends AsyncTask<Void, Void, Void> {
		@Override
		protected void onPreExecute() {
			DisplayActivity.this.throbber.setVisibility(View.VISIBLE);
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
