package net.metamike.hackerdojo.widget;

import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public class PreferencesActivity extends PreferenceActivity {
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
	}
	
	public void doReset() {
		Editor prefEditor = PreferenceManager.getDefaultSharedPreferences(this).edit();
		prefEditor.clear();
		prefEditor.commit();
		PreferenceManager.setDefaultValues(this, R.xml.preferences, true);
		/* This refresh hack is from a mix of Ludwig's and Guillaume's suggestions.
		 * see http://groups.google.com/group/android-developers/browse_thread/thread/60723247e0eca515
		 */
		finish();
		startActivity(getIntent());
	}
}
