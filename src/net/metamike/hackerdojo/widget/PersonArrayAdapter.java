package net.metamike.hackerdojo.widget;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

public class PersonArrayAdapter extends ArrayAdapter<Person> {
	int resourceID;

	public PersonArrayAdapter(Context context, int textViewResourceId,
			List<Person> objects) {
		super(context, textViewResourceId, objects);
		
		resourceID = textViewResourceId;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LinearLayout personListView;
		
		Person p = getItem(position);
		
		
		if (convertView == null) {
			personListView = new LinearLayout(getContext());
			LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			inflater.inflate(resourceID, personListView, true);
		} else {
			personListView = (LinearLayout)convertView;
		}
		
		TextView nameView = (TextView)personListView.findViewById(R.id.nameTextView);
		TextView timeView = (TextView)personListView.findViewById(R.id.timeTextView);
		
		nameView.setText(p.getName());
		timeView.setText(p.getTime());
		
		return personListView;
	}

	

}
