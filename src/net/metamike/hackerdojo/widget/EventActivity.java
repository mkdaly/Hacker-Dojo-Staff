package net.metamike.hackerdojo.widget;

import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

import net.metamike.hackerdojo.widget.InfoActivity.DojoStatus;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class EventActivity extends Activity implements Handler.Callback {
	private static final String TAG = "EventActivity";
	static final int MESSAGE_EVENT = 100;
	static final int START_INTENT = 99;

	//Views
	private TextView statusView;
	private ProgressBar throbber;
	private ListView eventView;
	
	//State vars
	private EventCursorAdapter eventAdapter;
	private EventDBAdapter dbApapter;
	private Cursor eventCursor;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.i(TAG, "onCreate(" + savedInstanceState + ")");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.events);
		throbber = (ProgressBar) findViewById(R.id.events_throbber);
		statusView = (TextView)findViewById(R.id.events_status);
		statusView.setTextColor(Color.LTGRAY);
		eventView = (ListView)findViewById(R.id.events_list);
		
		dbApapter = new EventDBAdapter(this);
		dbApapter.open();
		eventCursor = dbApapter.getAllEntries();
		startManagingCursor(eventCursor);
		eventAdapter = new EventCursorAdapter(this,eventCursor);
		eventView.setAdapter(eventAdapter);
		eventAdapter.notifyDataSetChanged();

		Context appContext = getApplicationContext();
		Intent startIntent = new Intent(getApplicationContext(), EventFetchService.class);
		startIntent.setFlags(START_INTENT);
		appContext.bindService( startIntent, queryConnection, Context.BIND_AUTO_CREATE);
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		dbApapter.close();
	}

	private void setStatusLine(DojoStatus status) {
		switch (status) {
			case FETCHING:
				statusView.setVisibility(View.VISIBLE);
				throbber.setVisibility(View.VISIBLE);
				break;
			default:
				statusView.setVisibility(View.INVISIBLE);
				throbber.setVisibility(View.INVISIBLE);
				break;
		}
	}
	
	/* TODO: There should be a better way to update the
	 * view as new events hit the database.
	*/
	@Override
	public boolean handleMessage(Message msg) {
		switch(msg.what) {
			case InfoActivity.MESSAGE_START:
				setStatusLine(DojoStatus.FETCHING);
				return true;
			case InfoActivity.MESSAGE_STOP:
				//Using UNKNOWN because we don't care about the status
				setStatusLine(DojoStatus.UNKNOWN);
				eventCursor = dbApapter.getAllEntries();
				startManagingCursor(eventCursor);
				eventAdapter.changeCursor(eventCursor);
				eventAdapter.notifyDataSetChanged();
				return true;
			case MESSAGE_EVENT:
				this.dbApapter.saveEntry((Event)msg.obj);
				return true;
		}
		return false;
	}

	
	private ServiceConnection queryConnection = new ServiceConnection() {
		private EventFetchService queryService;
		@Override
		public void onServiceDisconnected(ComponentName name) {
			queryService = null;
		}
		
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			queryService = ((EventFetchService.EventBinder)service).getService();
			queryService.setHandler( new Handler(EventActivity.this));
			queryService.go();
		}
	};
	
}
