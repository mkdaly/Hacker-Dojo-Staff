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
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
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
	private static final int MALFORMED_URL_DIALOG = 1;
	private static final int IO_EXECPTION_DIALOG = 2;

	private DojoContentHandlerImpl ch = new DojoContentHandlerImpl();

	//Views
	private TextView statusView;
	private ListView peopleView;
	private ProgressBar throbber;

	//State vars
	private Boolean isOpen = null; //Use null when status is unknown
	private List<Person> people = Collections.synchronizedList(new ArrayList<Person>());
	private PersonArrayAdapter personAdapter;
	private String urlString;
	private Boolean doFetchGravatar = Boolean.FALSE;
	
	private Exception exception; //Don't like this....
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.i(TAG, "onCreate(" + savedInstanceState + ")");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		PreferenceManager.setDefaultValues(getApplicationContext(), R.xml.preferences, true);
		setValuesFromPreferences();

		throbber = (ProgressBar) findViewById(R.id.throbber);
		statusView = (TextView)findViewById(R.id.view_dojo_status);
		personAdapter = new PersonArrayAdapter(this, R.layout.person, people);
		peopleView = (ListView)findViewById(R.id.view_people);
		peopleView.setAdapter(personAdapter);

		new QueryTask().execute((Void[])null);
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		AlertDialog.Builder builder = new AlertDialog.Builder(DisplayActivity.this);
		switch (id) {
		case MALFORMED_URL_DIALOG:
			builder.setTitle(R.string.DIALOG_malformed_url_title)
				.setCancelable(false)
				.setMessage(R.string.DIALOG_malformed_url_body)
				.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						DisplayActivity.this.resetURLString();
						DisplayActivity.this.refresh();
					}})
				.setNegativeButton(R.string.no, null);
			return builder.create();
		case IO_EXECPTION_DIALOG:
			builder.setTitle(R.string.DIALOG_io_exception_title)
				.setCancelable(false)
				.setMessage(R.string.DIALOG_io_exception_body)
				.setPositiveButton("OK", null);
			return builder.create();
		default:
			return super.onCreateDialog(id);
		}
	}

	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		super.onPrepareDialog(id, dialog);
		switch (id) {
			case IO_EXECPTION_DIALOG:
				if (exception != null && exception instanceof IOException) {
					((AlertDialog)dialog).setMessage(exception.toString());
				}
				break;
		}
	}

	private void setValuesFromPreferences() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		urlString = prefs.getString(getString(R.string.PREF_WIDGET_URL), null);
		doFetchGravatar = prefs.getBoolean(getString(R.string.PREF_LOAD_GRAVATARS), false);
	}
	
	private void resetURLString() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		urlString = getString(R.string.widget_url);
		prefs.edit().putString(getString(R.string.PREF_WIDGET_URL), urlString).commit();		
	}
	
	private int queryDojo() {
		try {
			isOpen = null;
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
			return DisplayActivity.RESULT_OK;
		} catch (MalformedURLException mfu) {
			Log.e(TAG, "Bad URL:"+urlString, mfu);
			mfu.printStackTrace();
			return DisplayActivity.MALFORMED_URL_DIALOG;
		} catch (IOException ioe) {
			Log.e(TAG, "IO Error.", ioe);
			ioe.printStackTrace();
			exception = ioe;
			return DisplayActivity.IO_EXECPTION_DIALOG;
		} catch (FactoryConfigurationError e) {
			//TODO: Provide feedback to user
			e.printStackTrace();
			return -1;
		} catch (SAXException e) {
			//TODO: Provide feedback to user
			e.printStackTrace();
			return -1;
		}
	}
	
	private void setStatusLine() {
		if (isOpen == null) {
			statusView.setText(R.string.dojo_unknown);
			statusView.setTextColor(Color.LTGRAY);			
		} else {
			if (isOpen) {
				statusView.setText(R.string.dojo_open);
				statusView.setTextColor(Color.GREEN);
			} else {
				statusView.setText(R.string.dojo_closed);
				statusView.setTextColor(Color.RED);
			}
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
		setValuesFromPreferences();
		refresh();
	}
	
	private class QueryTask extends AsyncTask<Void, Void, Integer> {
		@Override
		protected void onPreExecute() {
			DisplayActivity.this.throbber.setVisibility(View.VISIBLE); 
			DisplayActivity.this.statusView.setText(R.string.fetching_status);
			DisplayActivity.this.statusView.setTextColor(Color.LTGRAY);
		}

		@Override
		protected void onPostExecute(Integer result) {
			int r = (result != null) ? r = result.intValue() : -1;
			DisplayActivity.this.throbber.setVisibility(View.INVISIBLE);
			setStatusLine();
			personAdapter.notifyDataSetChanged();
			if (DisplayActivity.MALFORMED_URL_DIALOG == r)
				DisplayActivity.this.showDialog(DisplayActivity.MALFORMED_URL_DIALOG);
			if (DisplayActivity.IO_EXECPTION_DIALOG == r)
				DisplayActivity.this.showDialog(DisplayActivity.IO_EXECPTION_DIALOG);
		}

		@Override
		protected Integer doInBackground(Void... params) {
			return DisplayActivity.this.queryDojo();
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
				if (DisplayActivity.this.doFetchGravatar) {
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
