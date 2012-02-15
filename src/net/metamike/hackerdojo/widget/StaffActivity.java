package net.metamike.hackerdojo.widget;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.metamike.hackerdojo.widget.InfoActivity.DojoStatus;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class StaffActivity extends Activity implements Handler.Callback {
	private static final String TAG = "StaffActivity";
	static final int MALFORMED_URL_DIALOG = 1;
	static final int IO_EXECPTION_DIALOG = 2;
	static final int MESSAGE_PERSON = 100;

	//Views
	private TextView statusView;
	private ListView peopleView;
	private ProgressBar throbber;

	//State vars
	private List<Person> people = Collections.synchronizedList(new ArrayList<Person>());
	private PersonArrayAdapter personAdapter;
	private String exceptionText = "";
	
	//private ExceptionReceiver exceptionReceiver;
	//private PersonReceiver personReceiver;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.i(TAG, "onCreate(" + savedInstanceState + ")");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.staff);
		PreferenceManager.setDefaultValues(getApplicationContext(), R.xml.preferences, true);

		throbber = (ProgressBar) findViewById(R.id.staff_throbber);
		statusView = (TextView)findViewById(R.id.staff_status);
		personAdapter = new PersonArrayAdapter(this, R.layout.person, people);
		peopleView = (ListView)findViewById(R.id.staff_list);
		peopleView.setAdapter(personAdapter);

		Intent service = new Intent(this, StaffFetchService.class); 
		getApplicationContext().bindService( service, queryConnection, Context.BIND_AUTO_CREATE);
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		AlertDialog.Builder builder = new AlertDialog.Builder(StaffActivity.this);
		switch (id) {
		case MALFORMED_URL_DIALOG:
			builder.setTitle(R.string.DIALOG_malformed_url_title)
				.setCancelable(false)
				.setMessage(R.string.DIALOG_malformed_url_body)
				.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						StaffActivity.this.resetURLString();
						startService( new Intent(StaffActivity.this, QueryService.class));
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

	private void resetURLString() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		String urlString = getString(R.string.staff_url);
		prefs.edit().putString(getString(R.string.PREF_STAFF_URL), urlString).commit();		
	}
	
	private void setStatusLine(DojoStatus status) {
		switch (status) {
			case UNKNOWN:
				statusView.setText(R.string.dojo_unknown);
				statusView.setTextColor(Color.LTGRAY);
				throbber.setVisibility(View.INVISIBLE);
				break;
			case FETCHING:
				statusView.setText(R.string.fetching_status);
				statusView.setTextColor(Color.LTGRAY);
				throbber.setVisibility(View.VISIBLE);
				break;
			case OPEN:
				statusView.setText(R.string.dojo_open);
				statusView.setTextColor(Color.GREEN);
				throbber.setVisibility(View.INVISIBLE);
				break;
			case CLOSED:
				statusView.setText(R.string.dojo_closed);
				statusView.setTextColor(Color.RED);
				throbber.setVisibility(View.INVISIBLE);
				break;
		}
	}
		
	private void setStatus() {
		if (people.isEmpty())
			setStatusLine(DojoStatus.CLOSED);
		else 
			setStatusLine(DojoStatus.OPEN);
	}

	/*
	@Override
	protected void onPause() {
		unregisterReceiver(exceptionReceiver);
		//unregisterReceiver(personReceiver);
		super.onPause();
	}

	
	@Override
	protected void onResume() {
		IntentFilter exceptionFilter = new IntentFilter(QueryService.INTENT_EXCEPTION_THROWN);
		IntentFilter finishedFilter = new IntentFilter(QueryService.INTENT_QUERYING_DOJO);
//		IntentFilter startedFilter = new IntentFilter(QueryService.INTENT_STARTED_QUERYING_DOJO);
		exceptionReceiver = new ExceptionReceiver();
		registerReceiver(exceptionReceiver, exceptionFilter);
		//personReceiver = new PersonReceiver();
		//registerReceiver(personReceiver, finishedFilter);
//		registerReceiver(personReceiver, startedFilter);
		super.onResume();
	}
	
	public class ExceptionReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent == null) {
				return;
			}
			Bundle info = intent.getExtras();
			if (info != null && info.containsKey("exception")) {
				setStatusLine(DojoStatus.UNKNOWN);
				Exception e = (Exception)info.getSerializable("exception");
				if (e instanceof MalformedURLException)
					showDialog(StaffActivity.MALFORMED_URL_DIALOG);
				if (e instanceof IOException) {
					StaffActivity.this.exceptionText = e.toString();
					showDialog(StaffActivity.IO_EXECPTION_DIALOG);
				}
			}
		}
	}
	*/
	/*
	public class PersonReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent == null) {
				return;
			}
			Bundle info = intent.getExtras();
			if (info != null && info.containsKey(QueryService.INTENT_EXTRA_PEOPLE)) {
				personAdapter.notifyDataSetChanged();
				setStatus();
			} else {
				setStatusLine(DojoStatus.FETCHING);
			}
		}
	}
	*/

	@Override
	public boolean handleMessage(Message msg) {
		switch(msg.what) {
			case InfoActivity.MESSAGE_START:
				this.people.clear();
				this.personAdapter.notifyDataSetChanged();
				setStatusLine(DojoStatus.FETCHING);
				return true;
			case InfoActivity.MESSAGE_STOP:
				setStatus();
				this.personAdapter.notifyDataSetChanged();
				return true;
			case MESSAGE_PERSON:
				this.people.add((Person)msg.obj);
				return true;
		}
		return false;
	}
	
	private ServiceConnection queryConnection = new ServiceConnection() {
		private StaffFetchService queryService;
		@Override
		public void onServiceDisconnected(ComponentName name) {
			queryService = null;
		}
		
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			queryService = ((StaffFetchService.StaffBinder)service).getService();
			queryService.setHandler( new Handler(StaffActivity.this));
			queryService.go();
		}
	};
}
