package net.metamike.hackerdojo.widget;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.metamike.hackerdojo.widget.InfoActivity.DojoStatus;

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
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class StaffActivity extends Activity {
	private static final String TAG = "DisplayActivity";
	static final int MALFORMED_URL_DIALOG = 1;
	static final int IO_EXECPTION_DIALOG = 2;

	//Views
	private TextView statusView;
	private ListView peopleView;
	private ProgressBar throbber;

	//State vars
	private List<Person> people = Collections.synchronizedList(new ArrayList<Person>());
	private PersonArrayAdapter personAdapter;
	private String exceptionText = "";
	
	private ExceptionReceiver exceptionReceiver;
	private PersonReceiver personReceiver;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.i(TAG, "onCreate(" + savedInstanceState + ")");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.staff);
		PreferenceManager.setDefaultValues(getApplicationContext(), R.xml.preferences, true);

		throbber = (ProgressBar) findViewById(R.id.throbber);
		statusView = (TextView)findViewById(R.id.view_dojo_status);
		personAdapter = new PersonArrayAdapter(this, R.layout.person, people);
		peopleView = (ListView)findViewById(R.id.view_people);
		peopleView.setAdapter(personAdapter);

		startService( new Intent(this, QueryService.class));
		//bindService( service, queryConnection, Context.BIND_AUTO_CREATE);
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
		String urlString = getString(R.string.widget_url);
		prefs.edit().putString(getString(R.string.PREF_WIDGET_URL), urlString).commit();		
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
	

	@Override
	protected void onPause() {
		unregisterReceiver(exceptionReceiver);
		unregisterReceiver(personReceiver);
		super.onPause();
	}

	@Override
	protected void onResume() {
		IntentFilter exceptionFilter = new IntentFilter(QueryService.INTENT_EXCEPTION_THROWN);
		IntentFilter finishedFilter = new IntentFilter(QueryService.INTENT_QUERYING_DOJO);
//		IntentFilter startedFilter = new IntentFilter(QueryService.INTENT_STARTED_QUERYING_DOJO);
		exceptionReceiver = new ExceptionReceiver();
		registerReceiver(exceptionReceiver, exceptionFilter);
		personReceiver = new PersonReceiver();
		registerReceiver(personReceiver, finishedFilter);
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
	
	public class PersonReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent == null) {
				return;
			}
			Bundle info = intent.getExtras();
			if (info != null && info.containsKey(QueryService.INTENT_EXTRA_PEOPLE)) {
				people.clear();
				people.addAll((List<? extends Person>) info.getParcelableArrayList(QueryService.INTENT_EXTRA_PEOPLE));
				personAdapter.notifyDataSetChanged();
				setStatusLine((DojoStatus) info.get(QueryService.INTENT_EXTRA_STATUS));
			} else {
				setStatusLine(DojoStatus.FETCHING);
			}
		}
	}
	
/*	
	private ServiceConnection queryConnection = new ServiceConnection() {
		private QueryService queryService;
		@Override
		public void onServiceDisconnected(ComponentName name) {
			queryService = null;
		}
		
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			queryService = ((QueryService.QueryBinder)service).getService();
			queryService.setHandler( new Handler(new Handler.Callback() {
				@Override
				public boolean handleMessage(Message msg) {
					if (msg.obj != null && msg.obj instanceof DojoStatus) {
						setStatusLine((DojoStatus)msg.obj);
					}
					return true;
				}
			}));
		}
	};*/
}
