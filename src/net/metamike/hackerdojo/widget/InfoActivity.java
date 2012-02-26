package net.metamike.hackerdojo.widget;

import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TabHost;

public class InfoActivity extends TabActivity {

	static final int PREFERENCE_ACTIVITY = 1;
	public static final String DOJO_PREFERENCES_UPDATED = "Dojo_Preferences_Updated";
	static final int MESSAGE_START = 1;
	static final int MESSAGE_STOP = 2;
	
	private TabHost tabHost;
	private String events;
	private String staff;

	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.main);

	    events = getString(R.string.events_name);
	    staff = getString(R.string.staff_name);
	    
	    Resources res = getResources(); // Resource object to get Drawables
	    tabHost = getTabHost();  // The activity TabHost
	    TabHost.TabSpec spec;  // Resusable TabSpec for each tab
	    Intent intent;  // Reusable Intent for each tab

	    // Create an Intent to launch an Activity for the tab (to be reused)
	    intent = new Intent().setClass(this, StaffActivity.class);

	    // Initialize a TabSpec for each tab and add it to the TabHost
	    spec = tabHost.newTabSpec(staff).setIndicator(staff,
	                      res.getDrawable(R.drawable.ic_tab_staff))
	                  .setContent(intent);
	    tabHost.addTab(spec);

	    // Do the same for the other tabs
	    intent = new Intent().setClass(this, EventActivity.class);
	    spec = tabHost.newTabSpec(events).setIndicator(events,
	                      res.getDrawable(R.drawable.ic_tab_events))
	                  .setContent(intent);
	    tabHost.addTab(spec);

	    tabHost.setCurrentTabByTag(events);
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
				if (tabHost.getCurrentTabTag().equals(staff)) {
					startService( new Intent(this, StaffFetchService.class));
				} else if (tabHost.getCurrentTabTag().equals(events)) {
					startService( new Intent(this, EventFetchService.class));
				}
				return true;
			case (R.id.menu_exit):
				this.stopService(new Intent(this, EventFetchService.class));
				this.stopService(new Intent(this, StaffFetchService.class));
				this.finish();
				return true;
			case (R.id.menu_preferences):
				startActivityForResult(new Intent(this, PreferencesActivity.class), InfoActivity.PREFERENCE_ACTIVITY);
				return true;
		}
		return false;
	}

	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		//TODO:
		//if (requestCode == InfoActivity.PREFERENCE_ACTIVITY)
			/*Since the back button is used to exit the PA,
			  the resultCode will be Activity.RESULT_CANCELLED */
			//if (resultCode == Activity.RESULT_OK)
			//startService( new Intent(this, QueryService.class));
			//sendBroadcast( new Intent(DOJO_PREFERENCES_UPDATED));
	}

	public enum DojoStatus {
		OPEN,
		CLOSED,
		UNKNOWN,
		FETCHING;
	}
	
}
