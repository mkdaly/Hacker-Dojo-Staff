package net.metamike.hackerdojo.widget;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class DisplayActivity extends Activity {
	private static final String TAG = "DisplayActivity";
	static final int PREFERENCE_ACTIVITY = 1;
	static final int MALFORMED_URL_DIALOG = 1;
	static final int IO_EXECPTION_DIALOG = 2;

	public static final String DOJO_PREFERENCES_UPDATED = "Dojo_Preferences_Updated";

	//Views
	private TextView statusView;
	private ListView peopleView;
	private ProgressBar throbber;

	//State vars
	//private Boolean isOpen = null; //Use null when status is unknown
	private List<Person> people = Collections.synchronizedList(new ArrayList<Person>());
	private PersonArrayAdapter personAdapter;
	private String exceptionText = "";
	
	private ExceptionReceiver exceptionReceiver;
	private PersonReceiver personReceiver;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.i(TAG, "onCreate(" + savedInstanceState + ")");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		PreferenceManager.setDefaultValues(getApplicationContext(), R.xml.preferences, true);

		throbber = (ProgressBar) findViewById(R.id.throbber);
		statusView = (TextView)findViewById(R.id.view_dojo_status);
		personAdapter = new PersonArrayAdapter(this, R.layout.person, people);
		peopleView = (ListView)findViewById(R.id.view_people);
		peopleView.setAdapter(personAdapter);

		startService( new Intent(this, QueryService.class));
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
				((AlertDialog)dialog).setMessage(exceptionText);
				exceptionText = "";
				break;
		}
	}
	
	void setThrobber(int state) {
		//TODO: This might cause threading issues....  Test!
		throbber.setVisibility(state);
	}
	
	private void resetURLString() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		String urlString = getString(R.string.widget_url);
		prefs.edit().putString(getString(R.string.PREF_WIDGET_URL), urlString).commit();		
	}
	
	private void setStatusLine(Boolean status) {
		if (status == null) {
			statusView.setText(R.string.dojo_unknown);
			statusView.setTextColor(Color.LTGRAY);			
		} else {
			if (status) {
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

	@Override
	protected void onPause() {
		unregisterReceiver(exceptionReceiver);
		unregisterReceiver(personReceiver);
		super.onPause();
	}

	@Override
	protected void onResume() {
		IntentFilter exceptionFilter = new IntentFilter(QueryService.EXCEPTION_THROWN);
		IntentFilter finishedFilter = new IntentFilter(QueryService.FINISHED_QUERYING_DOJO);
		exceptionReceiver = new ExceptionReceiver();
		registerReceiver(exceptionReceiver, exceptionFilter);
		personReceiver = new PersonReceiver();
		registerReceiver(personReceiver, finishedFilter);
		super.onResume();
	}

	private void refresh() {
		this.people.clear();
		personAdapter.notifyDataSetChanged();
		startService(new Intent(this, QueryService.class));
	}
	
	private void updateFromPreferences() {
		//TODO: Implement auto-refresh
		
		//TODO: check for change and refresh IFF the url changed
		//setValuesFromPreferences();
		sendBroadcast( new Intent(DOJO_PREFERENCES_UPDATED));
	}
	
	public class ExceptionReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent == null) {
				return;
			}
			Bundle info = intent.getExtras();
			if (info != null && info.containsKey("exception")) {
				Exception e = (Exception)info.getSerializable("exception");
				if (e instanceof MalformedURLException)
					showDialog(DisplayActivity.MALFORMED_URL_DIALOG);
				if (e instanceof IOException) {
					DisplayActivity.this.exceptionText = e.toString();
					showDialog(DisplayActivity.IO_EXECPTION_DIALOG);
				}
			}
		}
	}
	
	public class PersonReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent == null) {
				return;
			}
			Bundle info = intent.getExtras();
			if (info != null && info.containsKey(QueryService.INTENT_EXTRA_PEOPLE)) {
				people.addAll((List<? extends Person>) info.getParcelableArrayList(QueryService.INTENT_EXTRA_PEOPLE));
				personAdapter.notifyDataSetChanged();
				setStatusLine(info.getBoolean(QueryService.INTENT_EXTRA_STATUS));
			}
		}
	}
}
