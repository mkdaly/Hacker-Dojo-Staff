package net.metamike.hackerdojo.widget;

import android.app.TabActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TabHost;

public class InfoActivity extends TabActivity {

	static final int PREFERENCE_ACTIVITY = 1;
	public static final String DOJO_PREFERENCES_UPDATED = "Dojo_Preferences_Updated";

	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.main);

	    Resources res = getResources(); // Resource object to get Drawables
	    TabHost tabHost = getTabHost();  // The activity TabHost
	    TabHost.TabSpec spec;  // Resusable TabSpec for each tab
	    Intent intent;  // Reusable Intent for each tab

	    // Create an Intent to launch an Activity for the tab (to be reused)
	    intent = new Intent().setClass(this, StaffActivity.class);

	    // Initialize a TabSpec for each tab and add it to the TabHost
	    spec = tabHost.newTabSpec("staff").setIndicator("Staff",
	                      res.getDrawable(R.drawable.ic_tab_staff))
	                  .setContent(intent);
	    tabHost.addTab(spec);

	    // Do the same for the other tabs
	    intent = new Intent().setClass(this, StaffActivity.class);
	    spec = tabHost.newTabSpec("events").setIndicator("Events",
	                      res.getDrawable(R.drawable.ic_tab_staff))
	                  .setContent(intent);
	    tabHost.addTab(spec);

	    tabHost.setCurrentTabByTag("staff");
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
				//refresh();
				startService(new Intent(this, QueryService.class));
				return true;
			case (R.id.menu_exit):
				this.stopService(new Intent(this, QueryService.class));
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
		if (requestCode == InfoActivity.PREFERENCE_ACTIVITY)
			/*Since the back button is used to exit the PA,
			  the resultCode will be Activity.RESULT_CANCELLED */
			//if (resultCode == Activity.RESULT_OK)
			startService( new Intent(this, QueryService.class));
			//sendBroadcast( new Intent(DOJO_PREFERENCES_UPDATED));
	}

	public enum DojoStatus {
		OPEN,
		CLOSED,
		UNKNOWN,
		FETCHING;
	}
	
}
